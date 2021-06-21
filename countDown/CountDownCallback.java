package com.longface.common.countDown;

/**
 * 倒计时回调
 */
public interface CountDownCallback {

    /**
     * 注册回调后 , 每秒调用一次
     * @param sec 当前倒计时剩余秒数
     */
    void onTimerTick(int sec);

    /**
     * 当暂停时候调用
     */
    void onTimerPause();

    /**
     * 重要
     * 注册回调时会调用该方法
     * 倒计时完成时会调用该方法
     * @param state {@State.COUNT_DOWN_FINISH}...
     * @param sec sec == -1 -> 倒计时闲置状态;   sec >= 1 -> 倒计时任务仍在进行中 sec为剩余秒数
     */
    void onTimerStateChange(int state ,int sec);
}
