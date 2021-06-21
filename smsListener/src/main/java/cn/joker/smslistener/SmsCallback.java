package cn.joker.smslistener;

import cn.joker.smslistener.db.SmsEntity;

public interface SmsCallback {

    void onHasNewMessage(SmsEntity smsEntity);

}
