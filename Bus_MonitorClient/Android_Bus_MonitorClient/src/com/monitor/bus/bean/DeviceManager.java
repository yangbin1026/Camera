package com.monitor.bus.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monitor.bus.model.DeviceInfo;
import com.monitor.bus.utils.LogUtils;


public class DeviceManager {
	private static final String TAG= DeviceManager.class.getSimpleName();
	private List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();//设备列表
	//key:parentId
	private Map<String,ArrayList<DeviceInfo>> mDeviceMap=new HashMap<String, ArrayList<DeviceInfo>>();
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
		if(mDeviceMap.containsKey(info.getParentId())){
			mDeviceMap.get(info.getParentId()).add(info);
			LogUtils.getInstance().localLog(TAG, "addDevcieInfo"+info.toString());
		}else{
			ArrayList<DeviceInfo> list=new ArrayList<DeviceInfo>();
			list.add(info);
			mDeviceMap.put(info.getParentId(), list);
			LogUtils.getInstance().localLog(TAG, "addDevcieList"+info.toString());
		}
	}
	public void setDeviceList(List<DeviceInfo> list){
		mDeviceList=list;
		LogUtils.getInstance().localLog(TAG, "setDeviceList"+list.size());
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
	public ArrayList<DeviceInfo> getListByPId(String parentId){
		if(!mDeviceMap.containsKey(parentId)){
			return new ArrayList<DeviceInfo>();
		}
		return mDeviceMap.get(parentId);
	}
	

}
