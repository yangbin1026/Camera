package com.monitor.bus.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.monitor.bus.activity.R;
import com.monitor.bus.model.AlarmInfo;
import com.monitor.bus.model.DeviceInfo;
import com.monitor.bus.utils.MUtils;

/**
 * 显示报警信息的列表适配器
 */
/* 自定义的Adapter，继承android.widget.BaseAdapter */
public class DeviceListAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private ArrayList<DeviceInfo> mList =new ArrayList<DeviceInfo>();
	
	private ImageView iv_icon;
	private TextView tv_content;
	
	/* 构造器 */
	public DeviceListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}
	
	public void setData(ArrayList<DeviceInfo> it){
		mList= it;
	}
	
	public ArrayList<DeviceInfo> getData(){
		return mList;
	}
	
	public DeviceInfo getDataByPosition(int position){
		if(position>=mList.size()){
			return null;
		}
		return mList.get(position);
	}
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_devicelist, null);
		}
		iv_icon=(ImageView) convertView.findViewById(R.id.iv_icon);
		tv_content = (TextView) convertView.findViewById(R.id.tv_content);
		DeviceInfo info= mList.get(position);
		if("1".equals(info.getIsDeviceGroup())){
			//是分组
			tv_content.setText(info.getGroupName());
		}else{
			tv_content.setText(info.getDeviceName());
		}
		return convertView;
	} 
  
	
	@Override
	public void notifyDataSetChanged() {
		Log.e("AlarmListAdapter", "报警信息列表已改变！！！");
		super.notifyDataSetChanged();
	}
	
}
