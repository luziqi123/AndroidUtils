package cn.joker.smslib.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.joker.smslib.R;
import cn.joker.smslib.adapter.SMSAdapter;
import cn.joker.smslib.base.BaseActivity;
import cn.joker.smslib.db.DBManager;
import cn.joker.smslib.entity.SMSEntity;
import cn.joker.smslib.event.Events;
import cn.joker.smslib.receiver.SMSObserver;
import cn.joker.smslib.util.LogUtil;
import cn.joker.smslib.util.ToastUtil;
import pub.devrel.easypermissions.EasyPermissions;

public class SmsListActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    RecyclerView rv_sms;

    private SMSAdapter smsAdapter;
    private List<SMSEntity> smsDataList;

    private int PERMISSION_STORAGE_CODE = 10001;

    String[] PERMS = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS};

    private SMSObserver smsObserver;
    protected static final int MSG_INBOX = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INBOX:
                    handleSMS();
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sms_list;
    }

    @Override
    protected boolean isRegisterEventBus() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("sms", Context.MODE_PRIVATE);
        String msg = sharedPreferences.getString("newMSG", "null");
        Log.i("HasSMS" , msg);
    }

    @Override
    protected void initView() {
        super.initView();
        rv_sms = findViewById(R.id.rv_sms);
        rv_sms.setLayoutManager(new LinearLayoutManager(this));
        smsDataList = new ArrayList<>();
        smsAdapter = new SMSAdapter(this,R.layout.item_sms,smsDataList);
        rv_sms.setAdapter(smsAdapter);
    }


    @Override
    protected void initData() {
        super.initData();
        requestPermission();
        smsObserver = new SMSObserver(this, mHandler);
        getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, smsObserver);// 注册监听短信数据库的变化

        List<SMSEntity> smsEntityAll = DBManager.newInstance().playSongDao().getSMSByStatus(0);
        if(smsEntityAll.size() > 0){
            smsDataList.addAll(smsEntityAll);
            smsAdapter.notifyDataSetChanged();
        }
    }

    private void requestPermission(){
        if (EasyPermissions.hasPermissions(this, PERMS)) {
            // 已经申请过权限，做想做的事

        } else {
            // 没有申请过权限，现在去申请
            /**
             *@param host Context对象
             *@param rationale  权限弹窗上的提示语。
             *@param requestCode 请求权限的唯一标识码
             *@param perms 一系列权限
             */
            EasyPermissions.requestPermissions(this, "权限申请失败", PERMISSION_STORAGE_CODE, PERMS);
        }
    }

//    /**
//     * 接收短信
//     * @param smsEvent
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onCodeEvent(Events.SMSEvent smsEvent){
////        smsDataList.add(smsEvent.getSmsEntity());
////        smsAdapter.notifyDataSetChanged();
//    }

    /**
     * 处理短信
     */
    private void handleSMS() {
        LogUtil.d("SMSObserver handleSMS:---------");
        Cursor cursor = null;
        // 添加异常捕捉
        try {
            cursor = getContentResolver().query(
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

                    SMSEntity smsEntityById = DBManager.newInstance().playSongDao().getSMSById(_id);
                    if(null == smsEntityById){
                        body = cursor.getString(cursor.getColumnIndex("body"));// 在这里获取短信信息内容
                        address = cursor.getString(cursor.getColumnIndex("address"));// 在这里获取短信地址
                        String date = cursor.getString(cursor.getColumnIndex("date"));
                        time = dateFormat.format(new Date(Long.parseLong(date)));
                        LogUtil.d("SMSObserver handleSMS:---------"+_id + "****" + time);
                        Log.i("HasSMS_handler" , address + "****" + time + "****" +body);
                        SharedPreferences sharedPreferences = getSharedPreferences("sms", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("newMSG", _id + "****" + time + "****" + body).apply();
                        SMSEntity smsEntity = new SMSEntity();
                        smsEntity.setId(_id);
                        smsEntity.setNumber(address);
                        smsEntity.setContent(body);
                        smsEntity.setTime(time);
                        smsEntity.setStatus(0);
                        DBManager.newInstance().playSongDao().insert(smsEntity);

                        EventBus.getDefault().post(new Events.SMSEvent(smsEntity));
                        smsDataList.add(smsEntity);
                        smsAdapter.notifyDataSetChanged();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将结果转发给EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        new Handler().postDelayed(new Runnable(){
            public void run() {

            }
        }, 2000);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        ToastUtil.showToast("权限申请失败");
        finish();
    }

}