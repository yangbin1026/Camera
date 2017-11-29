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
import com.monitor.bus.utils.MyUtils;

/**
 * 显示报警信息的列表适配器
 */
/* 自定义的Adapter，继承android.widget.BaseAdapter */
public class AlarmSelectorAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	String[] typeStirngs;
	private TextView tv_alramSelect;
	
	/* 构造器 */
	public AlarmSelectorAdapter(Context context,String[] typeStirngs) {
		mInflater = LayoutInflater.from(context);
		this.typeStirngs=typeStirngs;
	}
	
	@Override
	public int getCount() {
		return typeStirngs.length;
	}

	@Override
	public Object getItem(int position) {
		return typeStirngs[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			/* 使用自定义的list_items作为Layout */
			convertView = mInflater.inflate(R.layout.item_alarmtype, null);
		}
		tv_alramSelect = (TextView) convertView.findViewById(R.id.tv_alarm_select);
		tv_alramSelect.setText(typeStirngs[position]);
		return convertView;
	} 
  
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
}
