package com.longface.common.countDown;

public class State {

    /**
     * 倒计时结束
     */
    public static int COUNT_DOWN_FINISH = -1;
    /**
     * 刚注册时候会触发这个状态
     */
    public static int COUNT_DOWN_INIT = -2;
    /**
     * 删除倒计时会触发
     */
    public static int COUNT_DOWN_DEL = -3;

}
