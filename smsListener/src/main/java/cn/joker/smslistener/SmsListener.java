package cn.joker.smslistener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.joker.smslistener.core.SmsDog;
import cn.joker.smslistener.core.SmsDogObserver;
import cn.joker.smslistener.db.SmsDbUtil;
import cn.joker.smslistener.db.SmsEntity;

public class SmsListener extends SmsDogObserver implements SmsCallback ,Runnable{

    private Context mContext;
    private SmsCallback mCallback;
    private ExecutorService mSingleThreadExecutor;
    private SmsDog mSmsDog;
    private Handler mainThreadHandler;

    private SmsListener() {
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    private volatile static SmsListener instance;

    public static SmsListener getInstance() {
        if (instance != null) {
            return instance;
        } else {
            synchronized (SmsListener.class) {
                if (instance == null) {
                    instance = new SmsListener();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mSmsDog = new SmsDog();
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public void getPermission(Activity activity) {
        activity.startActivityForResult(new Intent(activity , PermissionActivity.class) , 100);
    }

    public void startListener(SmsCallback callback) {
        this.mCallback = callback;
        mSmsDog.register(mContext, this);
    }

    public void stopListener() {
        mCallback = null;
        if (mSmsDog != null) {
            mSmsDog.unregister(mContext);
            mSmsDog = null;
        }
    }

    @Override
    public void woof() {
        // 短信数据库发生改变
        mSingleThreadExecutor.submit(this);
    }

    @Override
    public void onHasNewMessage(SmsEntity smsEntity) {
        // 从短信数据库拿到新消息内容
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onHasNewMessage(smsEntity);
                }
            }
        });
    }

    @Override
    public void run() {
        SmsDbUtil smsDbUtil = new SmsDbUtil();
        smsDbUtil.getNewSmsFromDb(mContext, this);
    }
}
