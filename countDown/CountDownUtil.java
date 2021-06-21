package com.longface.common.countDown;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 倒计时工具类
 * <p>
 * 每次倒计时都创建一个带tag的TaskModel
 * CountDownTimer每秒更新一次内存中的TaskModel
 * 同时通过相同tag的callback的Tick方法返回倒计时时间
 * 直到所有内存中的TaskModel倒计时全部结束,Timer停止更新,当开始新的倒计时任务时重新开启.
 */
public class CountDownUtil implements CountDownTimeCallback, Runnable {

    private final TaskRecorder taskRecorder = new TaskRecorder();

    private final HashMap<String, CountDownCallback> callbacks = new HashMap<>();

    private final CountDownTimer mTimer = new CountDownTimer(this);

    private Handler mHandler;

    private CountDownUtil() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    private volatile static CountDownUtil instance;

    public static CountDownUtil getInstance() {
        if (instance != null) {
            return instance;
        } else {
            synchronized (CountDownUtil.class) {
                if (instance == null) {
                    instance = new CountDownUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 是否还在倒计时中
     *
     * @param tag 每个tag都是一个单独的倒计时任务
     * @return
     */
    public TaskModel isCountDown(String tag) {
        return taskRecorder.hasCountDownTask(tag);
    }

    /**
     * 开始
     * 如果这个倒计时已经存在,则改变暂停状态即可
     *
     * @param tag 任务的tag
     * @param sec 倒计时多少秒
     */
    public void startCountDown(String tag, int sec) {
        boolean isAddSuccess = taskRecorder.createTaskRecord(tag, sec);
        if (!isAddSuccess) {
            TaskModel countDownTask = taskRecorder.loadToMemory(tag);
            countDownTask.setPause(false);
            taskRecorder.updateTaskRecord(tag, countDownTask);
        }
        mTimer.start();
    }

    /**
     * 暂停
     *
     * @param tag
     */
    public void pauseCountDown(String tag) {
        TaskModel countDownTask = taskRecorder.loadToMemory(tag);
        if (countDownTask != null) {
            countDownTask.setPause(true);
            taskRecorder.updateTaskRecord(tag, countDownTask);
            TaskRecorder.taskRecordMap.remove(tag);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callbacks.containsKey(tag)) {
                        callbacks.get(tag).onTimerPause();
                    }
                }
            });
        }
    }

    /**
     * 删除
     *
     * @param tag
     */
    public void delCountDow(String tag) {
        taskRecorder.delTaskRecord(tag);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callbacks.get(tag).onTimerStateChange(State.COUNT_DOWN_DEL, -1);
            }
        });
    }


    public void register(String tag, CountDownCallback callback) {
        callbacks.put(tag, callback);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                TaskModel countDown = isCountDown(tag);
                if (countDown != null) {
                    callbacks.get(tag).onTimerStateChange(State.COUNT_DOWN_INIT, countDown.getCountDownSec());
                }
            }
        });
    }

    public void unregister(String tag) {
        callbacks.remove(tag);
        TaskRecorder.taskRecordMap.remove(tag);
    }

    @Override
    public void onTick() {
        mHandler.post(this);
    }

    @Override
    public void run() {
        for (Map.Entry<String, TaskModel> next : TaskRecorder.taskRecordMap.entrySet()) {
            TaskModel value = next.getValue();
            if (value.isPause()) {
                continue;
            }
            value.setCountDownSec(value.getCountDownSec() - 1);
            if (value.getCountDownSec() <= 0) {
                taskRecorder.delTaskRecord(next.getKey());
                if (callbacks.containsKey(next.getKey())) {
                    callbacks.get(next.getKey()).onTimerStateChange(State.COUNT_DOWN_FINISH, -1);
                }
            } else {
                if (callbacks.containsKey(next.getKey())) {
                    Log.i("TimerTick", value.getCountDownSec() + "-" + next.getKey());
                    callbacks.get(next.getKey()).onTimerTick(value.getCountDownSec());
                }
            }
        }
    }
}
