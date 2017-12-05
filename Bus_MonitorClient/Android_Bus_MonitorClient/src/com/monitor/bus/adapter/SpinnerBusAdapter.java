package com.monitor.bus.adapter;

import java.util.List;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.monitor.bus.activity.R;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.utils.LogUtils;

/**
 * 显示spinner下拉列表的适配器
 */
public class SpinnerBusAdapter extends ArrayAdapter<Object> {

	private LayoutInflater mInflater;
	private List<DeviceInfo> items;
	private TextView busItem;
	private int SpinnerItemLayout;
	public SpinnerBusAdapter(Context context, int textViewResourceId,
			List<DeviceInfo> objects) {
		super(context, textViewResourceId);
		mInflater = LayoutInflater.from(context);
		items = objects;
		SpinnerItemLayout = textViewResourceId;
	}




	/* 因继承BaseAdapter，需重写以下方法 */
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position).getDeviceName();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
 
	@Override
	public View getView(int position, View convertView, ViewGroup par) {
		if (convertView == null) {
			/* 使用自定义的list_items作为Layout */
			convertView = mInflater.inflate(SpinnerItemLayout, null);
		}
		busItem = 	(TextView) convertView.findViewById(R.id.busItem);
		busItem.setText(items.get(position).getDeviceName());
		LogUtils.d("yangbin", "mapActDevice: "+items.get(position).getDeviceName()+" l :"+items.get(position).getLatitude()+" w :"+items.get(position).getLongitude());
		return convertView;
	}



}