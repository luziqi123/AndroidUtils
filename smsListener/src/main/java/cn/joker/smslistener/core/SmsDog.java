package cn.joker.smslistener.core;

import android.content.Context;
import android.net.Uri;

public class SmsDog {

    /**
     * content://sms/inbox       收件箱
     * content://sms/sent       已发送
     * content://sms/draft       草稿
     * content://sms/outbox       发件箱
     * content://sms/failed       发送失败
     * content://sms/queued       待发送列表
     */
    private SmsDogObserver mSmsObserver;

    public void register(Context context , SmsDogObserver smsObserver) {
        this.mSmsObserver = smsObserver;
        context.getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, mSmsObserver);// 注册监听短信数据库的变化
    }

    public void unregister(Context context) {
        if (mSmsObserver == null) {
            return;
        }
        context.getContentResolver().unregisterContentObserver(mSmsObserver);// 注册监听短信数据库的变化
    }

    interface HasNewMessageCallback{
        void woof();
    }

}
