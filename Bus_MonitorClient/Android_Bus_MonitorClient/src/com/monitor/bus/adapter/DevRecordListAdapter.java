package com.monitor.bus.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.monitor.bus.activity.R;
import com.monitor.bus.model.DevRecordInfo;
/**
 * 显示设备端录像列表的适配器
 */
/* 自定义的Adapter，继承android.widget.BaseAdapter */
public class DevRecordListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<DevRecordInfo> items;
	private TextView recordText;

	/* MyAdapter的构造器 */
	public DevRecordListAdapter(Context context, List<DevRecordInfo> it) {

		mInflater = LayoutInflater.from(context);
		items = it;
	}

	/* 因继承BaseAdapter，需重写以下方法 */
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
	public View getView(int position, View convertView, ViewGroup par) {
		if (convertView == null) {
			/* 使用自定义的list_items作为Layout */
			convertView = mInflater.inflate(R.layout.local_item, null);
		}
		recordText = 	(TextView) convertView.findViewById(R.id.localTest);
		recordText.setText(items.get(position).getFileName());
		return convertView;
	}


}