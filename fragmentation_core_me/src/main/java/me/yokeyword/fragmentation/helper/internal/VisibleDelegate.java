package me.yokeyword.fragmentation.helper.internal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

import me.yokeyword.fragmentation.ISupportFragment;

/**
 * Created by YoKey on 17/4/4.
 * Modify by JantHsuesh on 20/06/02
 */

public class VisibleDelegate {
    private static final String FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE = "fragmentation_invisible_when_leave";
    private static final String FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE = "fragmentation_compat_replace";

    // SupportVisible相关
    private boolean mCurrentVisible;
    private boolean mNeedDispatch = true;
    private boolean mVisibleWhenLeave = true;

    //true = 曾经可见，也就是onLazyInitView 执行过一次
    private boolean mIsOnceVisible = false;
    private boolean mFirstCreateViewCompatReplace = true;
    private boolean mAbortInitVisible = false;

    private MessageQueue.IdleHandler mIdleDispatchSupportVisible;

    private Handler mHandler;
    private Bundle mSaveInstanceState;

    private ISupportFragment mSupportF;
    private Fragment mFragment;

    public VisibleDelegate(ISupportFragment fragment) {
        this.mSupportF = fragment;
        this.mFragment = (Fragment) fragment;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSaveInstanceState = savedInstanceState;
            // setUserVisibleHint() may be called before onCreate()
            mVisibleWhenLeave = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE);
            mFirstCreateViewCompatReplace = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE, mVisibleWhenLeave);
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE, mFirstCreateViewCompatReplace);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (!mFirstCreateViewCompatReplace && mFragment.getTag() != null && mFragment.getTag().startsWith("android:switcher:")) {
            return;
        }

        if (mFirstCreateViewCompatReplace) {
            mFirstCreateViewCompatReplace = false;
        }

        initVisible();
    }

    private void initVisible() {
        if (mVisibleWhenLeave && isFragmentVisible(mFragment)) {
            if (mFragment.getParentFragment() == null || isFragmentVisible(mFragment.getParentFragment())) {
                mNeedDispatch = false;
                enqueueDispatchVisible();
            }
        }
    }

    public void onResume() {
        if (mIsOnceVisible) {
            if (!mCurrentVisible && mVisibleWhenLeave && isFragmentVisible(mFragment)) {
                mNeedDispatch = false;
                enqueueDispatchVisible();
            }
        } else {
            if (mAbortInitVisible) {
                mAbortInitVisible = false;
                initVisible();
            }
        }
    }

    public void onPause() {
        //界面还没有执行到initVisible 发出的任务taskDispatchSupportVisible，界面就已经pause。
        //为了让下次resume 时候，能正常的执行需要设置mAbortInitVisible ，来确保在resume的时候，可以执行完整initVisible
        if (mIdleDispatchSupportVisible != null) {
            Looper.myQueue().removeIdleHandler(mIdleDispatchSupportVisible);
            mAbortInitVisible = true;
            return;
        }

        if (mCurrentVisible && isFragmentVisible(mFragment)) {
            mNeedDispatch = false;
            mVisibleWhenLeave = true;
            dispatchSupportVisible(false);
        } else {
            mVisibleWhenLeave = false;
        }
    }

    public void onHiddenChanged(boolean hidden) {
        if (!hidden && !mFragment.isResumed()) {
            //Activity 不是resumed 状态，不用显示其下的fragment，只需设置标志位，待OnResume时 显示出来
            //if fragment is shown but not resumed, ignore...
            onFragmentShownWhenNotResumed();
            return;
        }
        if (hidden) {
            dispatchSupportVisible(false);
        } else {
            safeDispatchUserVisibleHint(true);

        }
    }

    private void onFragmentShownWhenNotResumed() {
        //fragment 需要显示，但是Activity状态不是resumed，下次resumed的时候 fragment 需要显示， 所以可以认为离开的时候可见
        mVisibleWhenLeave = true;

        mAbortInitVisible = true;
        // TODO tempCodeTest 这里注释掉之后使用popTo方法不会闪退
//        dispatchChildOnFragmentShownWhenNotResumed();
    }

    private void dispatchChildOnFragmentShownWhenNotResumed() {
        FragmentManager fragmentManager = mFragment.getChildFragmentManager();
        List<Fragment> childFragments = fragmentManager.getFragments();
        if (childFragments != null) {
            for (Fragment child : childFragments) {
                if (child instanceof ISupportFragment && !child.isHidden() && child.getUserVisibleHint()) {
                    ((ISupportFragment) child).getSupportDelegate().getVisibleDelegate().onFragmentShownWhenNotResumed();
                }
            }
        }
    }

    public void onDestroyView() {
        mIsOnceVisible = false;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (mFragment.isResumed() || (!mFragment.isAdded() && isVisibleToUser)) {
            if (!mCurrentVisible && isVisibleToUser) {
                safeDispatchUserVisibleHint(true);
            } else if (mCurrentVisible && !isVisibleToUser) {
                dispatchSupportVisible(false);
            }
        }
    }

    private void safeDispatchUserVisibleHint(boolean visible) {

        if (visible) {
            enqueueDispatchVisible();
        }else {
            if (mIsOnceVisible) {
                dispatchSupportVisible(false);
            }
        }
    }

    private void enqueueDispatchVisible() {

        mIdleDispatchSupportVisible = new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                dispatchSupportVisible(true);
                mIdleDispatchSupportVisible = null;
                return false;
            }
        };

        Looper.myQueue().addIdleHandler(mIdleDispatchSupportVisible);

    }

    private void dispatchSupportVisible(boolean visible) {
        if (visible && isParentInvisible()) return;

        if (mCurrentVisible == visible) {
            mNeedDispatch = true;
            return;
        }

        mCurrentVisible = visible;

        if (visible) {
            if (checkAddState()) return;
            mSupportF.onSupportVisible();

            if (!mIsOnceVisible) {
                mIsOnceVisible = true;
                mSupportF.onLazyInitView(mSaveInstanceState);

            }
            dispatchChild(true);
        } else {
            dispatchChild(false);
            mSupportF.onSupportInvisible();
        }
    }

    private void dispatchChild(boolean visible) {
        if (mNeedDispatch) {
            if (checkAddState()) return;
            FragmentManager fragmentManager = mFragment.getChildFragmentManager();
            List<Fragment> childFragments = fragmentManager.getFragments();
            if (childFragments != null) {
                for (Fragment child : childFragments) {
                    if (child instanceof ISupportFragment && !child.isHidden() && child.getUserVisibleHint()) {
                        ((ISupportFragment) child).getSupportDelegate().getVisibleDelegate().dispatchSupportVisible(visible);
                    }
                }
            }
        } else {
            mNeedDispatch = true;
        }
    }

    private boolean isParentInvisible() {
        Fragment parentFragment = mFragment.getParentFragment();

        if (parentFragment instanceof ISupportFragment) {
            return !((ISupportFragment) parentFragment).isSupportVisible();
        }

        return parentFragment != null && !parentFragment.isVisible();
    }

    private boolean checkAddState() {
        if (!mFragment.isAdded()) {
            mCurrentVisible = !mCurrentVisible;
            return true;
        }
        return false;
    }

    private boolean isFragmentVisible(Fragment fragment) {
        return !fragment.isHidden() && fragment.getUserVisibleHint();
    }

    public boolean isSupportVisible() {
        return mCurrentVisible;
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }
}
