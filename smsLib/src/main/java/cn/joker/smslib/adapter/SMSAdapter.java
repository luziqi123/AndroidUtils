package cn.joker.smslib.adapter;

import android.content.Context;

import java.util.List;

import cn.joker.smslib.R;
import cn.joker.smslib.base.BaseQuickAdapter;
import cn.joker.smslib.base.BaseViewHolder;
import cn.joker.smslib.entity.SMSEntity;

public class SMSAdapter extends BaseQuickAdapter<SMSEntity> {

    public SMSAdapter(Context context, int layoutResID, List<SMSEntity> data) {
        super(context, layoutResID, data);
    }

    @Override
    public void convert(BaseViewHolder holder, SMSEntity item) {
        holder.setText(R.id.tv_content,"内容: " + item.getContent());
        holder.setText(R.id.tv_number,"发送者: " + item.getNumber());
        holder.setText(R.id.tv_time,"时间: " + item.getTime());
    }

}
