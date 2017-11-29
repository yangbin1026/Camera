package com.monitor.bus.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.monitor.bus.model.DeviceInfo;


public class DeviceManager {
	private List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();//报警信息
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
		mDeviceList.add(info);
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
	

}
