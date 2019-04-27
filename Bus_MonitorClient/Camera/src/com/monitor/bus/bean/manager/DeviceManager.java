package com.monitor.bus.bean.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import com.jniUtil.JNVPlayerUtil;
import com.monitor.bus.Constants;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.utils.LogUtils;

import android.util.Log;
import android.util.Xml;


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
	public synchronized void resetInfos(){
		try {
			File myFile = new File(Constants.DEVICELIST_PASTH);
			InputStream in = new FileInputStream(myFile);
			boolean isCheck = false;
			DeviceInfo deviceInfo = null;
			XmlPullParser parser = Xml.newPullParser();
			InputStreamReader streamReader = new InputStreamReader(in, "gb2312");
			parser.setInput(streamReader);
			int event = parser.getEventType();// 产生第一个事件
			while (event != XmlPullParser.END_DOCUMENT) {
				switch (event) {
				case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
					mDeviceList.clear();
					mDeviceMap.clear();
					LogUtils.getInstance().localLog("PullParseXML", "START_DOCUMENT",LogUtils.LOG_NAME_DEVICE);
					break;
				case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
					if (!"device".equals(parser.getName())) {
						// 判断开始标签元素是否是name
						break;
					}
					deviceInfo = new DeviceInfo();
					deviceInfo.setGroupId(parser.getAttributeValue(null, "GroupID"));
					deviceInfo.setParentId(parser.getAttributeValue(null, "ParentID"));
					deviceInfo.setGroupName(parser.getAttributeValue(null, "GroupName"));
					deviceInfo.setIsDeviceGroup("1".equals(parser.getAttributeValue(null, "IsDeviceGroup")));
					if ("0".equals(parser.getAttributeValue(null, "IsDeviceGroup"))) {// 具有子节点时不需要加载设备信息
						//设备
						deviceInfo.setDeviceId(parser.getAttributeValue(null, "DeviceID"));
						deviceInfo.setDeviceName(parser.getAttributeValue(null, "DeviceName"));
						deviceInfo.setGuId(parser.getAttributeValue(null, "GUID"));
						deviceInfo.setSn(parser.getAttributeValue(null, "SN"));
						String onLine = parser.getAttributeValue(null, "Online");
						if ("".equals(onLine)) {
							deviceInfo.setOnLine(0);
						} else {
							deviceInfo.setOnLine(Integer.parseInt(onLine));
						}
						deviceInfo.setMaxSpeed(parser.getAttributeValue(null, "MaxSpeed"));
						deviceInfo.setMinSpeed(parser.getAttributeValue(null, "MinSpeed"));
						String longitude = parser.getAttributeValue(null, "Longitude");
						if (!"".equals(longitude)) {
							deviceInfo.setLongitude(Double.parseDouble(longitude));
						} else {
							deviceInfo.setLongitude(0d);
						}
						String latitude = parser.getAttributeValue(null, "Latitude");
						if (!"".equals(latitude)) {
							deviceInfo.setLatitude(Double.parseDouble(latitude));
						} else {
							deviceInfo.setLatitude(0d);
						}
						String temp = parser.getAttributeValue(null, "EncoderNumber");
						if (!"".equals(temp)) {
							deviceInfo.setEncoderNumber(Integer.parseInt(temp));
						} else {
							deviceInfo.setEncoderNumber(0);
						}
						try {
		
							deviceInfo.setDeviceType(parser.getAttributeValue(null, "DeviceType"));
							deviceInfo.setMaxSpeedNation(parser.getAttributeValue(null, "MaxSpeedNation"));
							deviceInfo.setMinSpeedNation(parser.getAttributeValue(null, "MinSpeedNation"));
							deviceInfo.setMaxSpeedRapid(parser.getAttributeValue(null, "MaxSpeedRapid"));
							deviceInfo.setMinSpeedRapid(parser.getAttributeValue(null, "MinSpeedRapid"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						/*
						 * busInfo.setModel(parser.getAttributeValue(18));
						 * busInfo.setPlateNum(parser.getAttributeValue(19));
						 * busInfo.setLineId(parser.getAttributeValue(20));
						 * busInfo.setDriver(parser.getAttributeValue(21));
						 */
		
						// 检验获取到的XML判断当前服务器模式，并上传到JNI层
						if (!isCheck) {
							boolean bCasCadeServer = false;
							int nAttributeCount = parser.getAttributeCount();
							for (int nn = 0; nn < nAttributeCount; nn++) {
								if (parser.getAttributeName(nn).equals("CenterServerID")) {
									bCasCadeServer = true;
									break;
								}
							}
							if (bCasCadeServer) {
								LogUtils.i("PullParseXML", "服务器为级联服务器！！");
								Constants.IS_CASCADE_SERVER = true;
								/* jni */
								JNVPlayerUtil.JNV_N_SetConnectServerType(1);// 上传到JNI层
							} else {
								LogUtils.i("PullParseXML", "服务器为单服务器！！");
								Constants.IS_CASCADE_SERVER = false;
								/* jni */
								Log.d(TAG, "JNV_N_SetConnectServerType:0");
								JNVPlayerUtil.JNV_N_SetConnectServerType(0);
							}
							isCheck = true;
						}
						// 级联模式才需要读取
						if (Constants.IS_CASCADE_SERVER) {
							deviceInfo.setCenterServerID(parser.getAttributeValue(null, "CenterServerID"));
							deviceInfo.setCmdServerID(parser.getAttributeValue(null, "CmdServerID"));
							deviceInfo.setMediaServerID(parser.getAttributeValue(null, "MediaServerID"));
						}
					}
					break;
				case XmlPullParser.END_TAG: // 结束元素事件
					if ("device".equals(parser.getName())) {// 判断结束标签元素是否是device
						if (Constants.IS_CASCADE_SERVER && !isDeviceValid(deviceInfo)) {// 级联模式才需要验证
							LogUtils.getInstance().localLog("PullParseXML", "this Device is Useless",LogUtils.LOG_NAME_DEVICE);
						} else {
							addDeviceInfo(deviceInfo);
						}
					}
					break;
				}
				event = parser.next();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
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
	public List<DeviceInfo> getAll(){
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
	
	private void addDeviceInfo(DeviceInfo info){
		if(info==null){
			return;
		}
		mDeviceList.add(info);
		if(mDeviceMap.containsKey(info.getParentId())){
			mDeviceMap.get(info.getParentId()).add(info);
			LogUtils.getInstance().localLog(TAG, "add-AsDevcieInfo  "+info.toString(),LogUtils.LOG_NAME_DEVICE);
		}else{
			ArrayList<DeviceInfo> list=new ArrayList<DeviceInfo>();
			list.add(info);
			mDeviceMap.put(info.getParentId(), list);
			LogUtils.getInstance().localLog(TAG, "add-NewDir  "+info.toString(),LogUtils.LOG_NAME_DEVICE);
		}
	}

	private static boolean isDeviceValid(DeviceInfo deviceInfo) {
		if ("".equals(deviceInfo.getCmdServerID()) || "".equals(deviceInfo.getMediaServerID())) {
			return false;
		}
		return true;
	}
	
	
	

}
