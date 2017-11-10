package com.monitor.bus.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.monitor.bus.activity.R;
import com.monitor.bus.model.AlarmInfo;

/**
 * 显示报警信息的列表适配器
 */
/* 自定义的Adapter，继承android.widget.BaseAdapter */
public class AlarmListAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private List<AlarmInfo> items;
	private TextView alarmText;
	private TextView numText;
	
	/* 构造器 */
	public AlarmListAdapter(Context context, List<AlarmInfo> it) {
		mInflater = LayoutInflater.from(context);
		items = it;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			/* 使用自定义的list_items作为Layout */
			convertView = mInflater.inflate(R.layout.alarm_item, null);
		}
		numText = (TextView) convertView.findViewById(R.id.numText);
		alarmText = 	(TextView) convertView.findViewById(R.id.alarmText);
		numText.setText( (position +1) +"");
		alarmText.setText(items.get(position).getExpresion());
		return convertView;
	} 
  
	
	@Override
	public void notifyDataSetChanged() {
		Log.e("AlarmListAdapter", "报警信息列表已改变！！！");
		super.notifyDataSetChanged();
	}
	
}
