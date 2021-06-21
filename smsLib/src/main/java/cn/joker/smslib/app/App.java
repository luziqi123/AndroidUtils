package cn.joker.smslib.app;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import cn.joker.smslib.util.LogUtil;


public class App{

    private static Application mApplication;
    protected static Context mContext;
    protected static Handler sHandler;
    protected static int sMainThreadId;


    public static void init(Application application) {
        mApplication = application;
        mContext = application.getApplicationContext();
        sHandler = new Handler();
        sMainThreadId = android.os.Process.myTid();
        LogUtil.init(true);
    }

    public static Context getContext(){
        return mContext;
    }

    /**
     * 是否运行在UI主线程
     *
     * @return
     */
    public static boolean isRunOnUIThread() {
        int myTid = android.os.Process.myTid();
        if (myTid == sMainThreadId) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 运行在UI主线程
     *
     * @param runnable
     */
    public static void runOnUIThread(Runnable runnable) {
        if (isRunOnUIThread()) {
            // 已经是主线程, 直接运行
            runnable.run();
        } else {
            // 如果是子线程, 借助handler让其运行在主线程
            sHandler.post(runnable);
        }
    }

}
