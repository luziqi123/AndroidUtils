package cn.joker.smslistener.core;

import android.database.ContentObserver;


public abstract class SmsDogObserver extends ContentObserver implements SmsDog.HasNewMessageCallback {

    public SmsDogObserver() {
        super(null);
    }

    /**
     * 当观察到的Uri发生变化时，回调该方法去处理。所有ContentObserver的派生类都需要重载该方法去处理逻辑
     * selfChange:回调后，其值一般为false，该参数意义不大
     */
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        woof();
    }

}
