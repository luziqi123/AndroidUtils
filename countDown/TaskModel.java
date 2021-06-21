package com.longface.common.countDown;

class TaskModel {

    /**
     * 需要倒计时多少秒
     */
    private int countDownSec;
    /**
     * 是否暂停
     */
    private boolean isPause;
    /**
     * 开始的时间戳
     */
    private long startTimeMillis;

    public int getCountDownSec() {
        return countDownSec;
    }

    public TaskModel setCountDownSec(int countDownSec) {
        this.countDownSec = countDownSec;
        return this;
    }

    public boolean isPause() {
        return isPause;
    }

    public TaskModel setPause(boolean pause) {
        isPause = pause;
        return this;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public TaskModel setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        return this;
    }
}
