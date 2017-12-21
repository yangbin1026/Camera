package com.monitor.bus.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monitor.bus.utils.LogUtils;


public class DeviceManager {
	private static final String TAG= DeviceManager.class.getSimpleName();
	private List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();//设备列表
	//key:parentId
	private Map<String,ArrayList<DeviceInfo>> mDeviceMap=new HashMap<String, ArrayList<DeviceInfo>>();
	private static DeviceManager manager;
	private DeviceManager(){
		
	}
	
	public synchronized static DeviceManager getInstance(){
		if(manager==null){
			manager=new DeviceManager();
		}
		return manager;
	}
	public void init(){
		mDeviceList = new ArrayList<DeviceInfo>();
		mDeviceMap=new HashMap<String, ArrayList<DeviceInfo>>();
	}
	public void addDeviceInfo(DeviceInfo info){
		if(info==null){
			return;
		}
		mDeviceList.add(info);
		if(mDeviceMap.containsKey(info.getParentId())){
			mDeviceMap.get(info.getParentId()).add(info);
			LogUtils.getInstance().localLog(TAG, "addAsDevcieInfo  "+info.toString(),LogUtils.LOG_NAME_DEVICE);
		}else{
			ArrayList<DeviceInfo> list=new ArrayList<DeviceInfo>();
			list.add(info);
			mDeviceMap.put(info.getParentId(), list);
			LogUtils.getInstance().localLog(TAG, "addAsDir  "+info.toString(),LogUtils.LOG_NAME_DEVICE);
		}
	}
//	public void setDeviceList(List<DeviceInfo> list){
//		mDeviceList=list;
//		LogUtils.getInstance().localLog(TAG, "setDeviceList"+list.size(),LogUtils.LOG_NAME_DEVICE);
//	}
//	public boolean removeFirst(){
//		if(mDeviceList.size()>0){
//			mDeviceList.remove(0);
//			return true;
//		}
//		return false;
//	}
	public List<DeviceInfo> getDeviceListAll(){
		return mDeviceList;
	}
	public ArrayList<DeviceInfo> getDeviceList(){
		ArrayList<DeviceInfo> list=new ArrayList<DeviceInfo>();
		for(DeviceInfo info: mDeviceList){
			if(!info.issDeviceGroup()){
				list.add(info);
			}
		}
		return list;
	}
	public DeviceInfo getDeviceInfoById(String id){
		for(DeviceInfo info: mDeviceList){
			if (id.equals(info.getGuId())) {
				return info;
			}
		}
		return null;
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
