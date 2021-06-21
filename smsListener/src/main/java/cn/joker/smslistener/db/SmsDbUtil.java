package cn.joker.smslistener.db;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import cn.joker.smslistener.SmsCallback;

public class SmsDbUtil {

    private HashSet<String> mSmsIdSet = new HashSet<>();

    public void getNewSmsFromDb(Context context , SmsCallback callback) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    new String[] { "_id", "address", "read", "body", "date" },
                    null, null, "date desc limit 1"); // datephone想要的短信号码
            if (cursor != null) {
                String _id = "";
                String body = "";
                String address = "";
                String time = "";
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                while (cursor.moveToNext()) {
                    _id = cursor.getString(cursor.getColumnIndex("_id"));// 在这里获取短信信息id
                    boolean add = mSmsIdSet.add(_id);
                    if(add){
                        body = cursor.getString(cursor.getColumnIndex("body"));// 在这里获取短信信息内容
                        address = cursor.getString(cursor.getColumnIndex("address"));// 在这里获取短信地址
                        String date = cursor.getString(cursor.getColumnIndex("date"));
                        time = dateFormat.format(new Date(Long.parseLong(date)));

                        SmsEntity smsEntity = new SmsEntity();
                        smsEntity.setId(_id);
                        smsEntity.setNumber(address);
                        smsEntity.setContent(body);
                        smsEntity.setTime(time);
                        smsEntity.setStatus(0);
                        callback.onHasNewMessage(smsEntity);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
