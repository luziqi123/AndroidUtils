package cn.joker.smslib.event;


import cn.joker.smslib.entity.SMSEntity;

public class Events {

    /**
     * 接收sms事件
     */
    public static class SMSEvent{
        private SMSEntity smsEntity;

        public SMSEvent(SMSEntity smsEntity) {
            this.smsEntity = smsEntity;
        }

        public SMSEntity getSmsEntity() {
            return smsEntity;
        }

        public void setSmsEntity(SMSEntity smsEntity) {
            this.smsEntity = smsEntity;
        }
    }


}
