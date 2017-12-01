package com.monitor.bus.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.monitor.bus.model.DeviceInfo;
import com.monitor.bus.utils.LogUtils;


public class DeviceManager {
	private static final String TAG= DeviceManager.class.getSimpleName();
	private List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();//设备列表
	static DeviceManager manager;
	private DeviceManager(){
		
	}
	
	public synchronized static DeviceManager getInstance(){
		if(manager==null){
			manager=new DeviceManager();
		}
		return manager;
	}
	
	public void addDeviceInfo(DeviceInfo info){
		if(info==null){
			return;
		}
		mDeviceList.add(info);
		LogUtils.getInstance().localLog(TAG, ""+info.toString());
	}
	public void setDeviceList(List<DeviceInfo> list){
		mDeviceList=list;
	}
	public void sort(){
		Collections.sort(mDeviceList, new Comparator<DeviceInfo>() {

			@Override
			public int compare(DeviceInfo lhs, DeviceInfo rhs) {

				return rhs.getOnLine() - lhs.getOnLine();
			}
		});
	}
	public boolean removeFirst(){
		if(mDeviceList.size()>0){
			mDeviceList.remove(0);
			return true;
		}
		return false;
	}
	public List<DeviceInfo> getDeviceList(){
		return mDeviceList;
	}
	public int getSize(){
		return mDeviceList.size();
	}
	
	/**
	 * 获取在线的bus设备
	 * 
	 * @return
	 */
	public ArrayList<DeviceInfo> getOnlineDevice() {
		ArrayList<DeviceInfo> list = new ArrayList<DeviceInfo>();
		for (DeviceInfo busInfo : mDeviceList ) {
			if (0 != busInfo.getOnLine()) {
				list.add(busInfo);
			}
		}
		return list;
	}
	public ArrayList<DeviceInfo> getDeviceInfoByParentId(String parentId){
		ArrayList<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
		for (DeviceInfo info :mDeviceList) {
			if (parentId.equals(info.getParentId())) {
				deviceList.add(info);
			}
		}
		return deviceList;
	}
	

}
