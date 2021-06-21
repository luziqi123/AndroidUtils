package com.longface.common.countDown;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 倒计时管理类
 */
class CountDownTimer {

    /**
     * 间隔 , 现在只支持1秒一次
     */
    private long period = 1000;

    private Timer mTimer = new Timer();

    private TimerTask mTimerTask;

    private boolean isRun;

    private final CountDownTimeCallback callback;

    public CountDownTimer(CountDownTimeCallback callback) {
        this.callback = callback;
    }

    /**
     * 开始Timer计时器
     */
    public void start() {
        if (isRun && mTimer != null) {
            return;
        }
        isRun = true;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (TaskRecorder.taskRecordMap.size() == 0) {
                    cancelTimer();
                } else {
                    callback.onTick();
                }
                Log.i("TimerTick", "tick");
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, period);
    }

    /**
     * 任务全部执行完毕会自动调用
     */
    private void cancelTimer() {
        isRun = false;
        mTimerTask.cancel();
        mTimerTask = null;
        mTimer.cancel();
        mTimer.purge();
        mTimer = null;
    }

}
