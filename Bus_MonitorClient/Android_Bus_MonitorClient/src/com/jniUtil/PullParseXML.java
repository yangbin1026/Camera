package com.jniUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.monitor.bus.bean.DeviceManager;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.DeviceInfo;
import com.monitor.bus.model.DevRecordInfo;
import com.monitor.bus.model.ServerInfo;
import com.monitor.bus.utils.LogUtils;

/**
 * 解析XML文件
 */
public class PullParseXML {

	public static List<DeviceInfo> getBusDevices(InputStream in)
			throws XmlPullParserException, IOException {
		boolean isCheck = false; 
		DeviceInfo busInfo = null;
		List<DeviceInfo> devices = null;
		XmlPullParser parser = Xml.newPullParser();
		InputStreamReader streamReader = new InputStreamReader(in,"gb2312"); 
		parser.setInput(streamReader);

		int event = parser.getEventType();// 产生第一个事件
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
				devices = new ArrayList<DeviceInfo>();
				//Constants.BUSDEVICEDATA.clear();
				break;
			case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
				if ("device".equals(parser.getName())) {// 判断开始标签元素是否是name
					busInfo = new DeviceInfo();
					busInfo.setGroupId(parser.getAttributeValue(null,"GroupID"));
					busInfo.setParentId(parser.getAttributeValue(null,"ParentID"));
					busInfo.setIsDeviceGroup(parser.getAttributeValue(null,"IsDeviceGroup"));
					busInfo.setGroupName(parser.getAttributeValue(null,"GroupName"));

					if ("0".equals(parser.getAttributeValue(null,"IsDeviceGroup"))) {// 具有子节点时不需要加载设备信息
						busInfo.setDeviceId(parser.getAttributeValue(null,"DeviceID"));
						busInfo.setDeviceName(parser.getAttributeValue(null,"DeviceName"));
						busInfo.setGuId(parser.getAttributeValue(null,"GUID"));
						busInfo.setSn(parser.getAttributeValue(null,"SN"));
						String onLine = parser.getAttributeValue(null,"Online");
						if("".equals(onLine)){
							busInfo.setOnLine(0);
						}else{
							busInfo.setOnLine(Integer.parseInt(onLine));
						}
						busInfo.setMaxSpeed(parser.getAttributeValue(null,"MaxSpeed"));
						busInfo.setMinSpeed(parser.getAttributeValue(null,"MinSpeed"));
						String longitude = parser.getAttributeValue(null,"Longitude");
						if(!"".equals(longitude)){
							busInfo.setLongitude(Double.parseDouble(longitude));
						}else{
							busInfo.setLongitude(0d);
						}
						String latitude = parser.getAttributeValue(null,"Latitude");
						if(!"".equals(latitude)){
							busInfo.setLatitude(Double.parseDouble(latitude));
						}else{
							busInfo.setLatitude(0d);
						}
						String temp = parser.getAttributeValue(null,"EncoderNumber");
						if(!"".equals(temp)){
							busInfo.setEncoderNumber(Integer.parseInt(temp));
						}else{
							busInfo.setEncoderNumber(0);
						}
						try {
							
							busInfo.setDeviceType(parser.getAttributeValue(null,"DeviceType"));
							busInfo.setMaxSpeedNation(parser.getAttributeValue(null,"MaxSpeedNation"));
							busInfo.setMinSpeedNation(parser.getAttributeValue(null,"MinSpeedNation"));
							busInfo.setMaxSpeedRapid(parser.getAttributeValue(null,"MaxSpeedRapid"));
							busInfo.setMinSpeedRapid(parser.getAttributeValue(null,"MinSpeedRapid"));
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
						/*
						busInfo.setModel(parser.getAttributeValue(18));
						busInfo.setPlateNum(parser.getAttributeValue(19));
						busInfo.setLineId(parser.getAttributeValue(20));
						busInfo.setDriver(parser.getAttributeValue(21));*/

						//检验获取到的XML判断当前服务器模式，并上传到JNI层
						if(!isCheck){
							boolean bCasCadeServer = false;
							int nAttributeCount = parser.getAttributeCount();
							for(int nn=0;nn<nAttributeCount;nn++)
							{
								if(parser.getAttributeName(nn).equals("CenterServerID"))
								{
									bCasCadeServer = true;
									break;
								}
							}
							if(bCasCadeServer){
								LogUtils.i("PullParseXML", "服务器为级联服务器！！");
								Constants.IS_CASCADE_SERVER = true;
								/*jni*/
								JNVPlayerUtil.JNV_N_SetConnectServerType(1);//上传到JNI层
							}else{
								LogUtils.i("PullParseXML", "服务器为单服务器！！");
								Constants.IS_CASCADE_SERVER = false; 
								/*jni*/
								JNVPlayerUtil.JNV_N_SetConnectServerType(0);
							}
							isCheck = true; 
						}
						//级联模式才需要读取
						if(Constants.IS_CASCADE_SERVER){
							busInfo.setCenterServerID(parser.getAttributeValue(null,"CenterServerID"));
							busInfo.setCmdServerID(parser.getAttributeValue(null,"CmdServerID"));
							busInfo.setMediaServerID(parser.getAttributeValue(null,"MediaServerID"));
						}
					}
				}
				else if ("channel".equals(parser.getName())){
					int jjjj = 0;
				}
				break;
			case XmlPullParser.END_TAG: // 结束元素事件
				if ("device".equals(parser.getName())) {// 判断结束标签元素是否是device
					if(Constants.IS_CASCADE_SERVER){//级联模式才需要验证
						if( isDeviceValid(busInfo) ) 
							devices.add(busInfo);
						else{
							Log.i("PullParseXML", "无效信息："+busInfo);
						}
					}else{
						devices.add(busInfo);
					}
				}
				break;
			}
			event = parser.next();
		}
		return devices;
	}

	private static boolean isDeviceValid(DeviceInfo busInfo) {
		if( "".equals(busInfo.getCmdServerID()) 
				|| "".equals(busInfo.getMediaServerID())){
			return false;
		}
		return true;
	}

	/**
	 * 获取排列后的巴士设备，并设置服务器模式，上传到JNI层
	 * @param in
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static  void getSortBusDevices(InputStream in) throws XmlPullParserException, IOException{
		DeviceManager manager= DeviceManager.getInstance();
		manager.setDeviceList(getBusDevices(in));
		manager.sort();
	}


	public static List<DevRecordInfo> getDevRecords(
			InputStream in) throws XmlPullParserException, IOException {

		List<DevRecordInfo> recordInfoList = null;
		DevRecordInfo recordInfo = null;

		XmlPullParser parser = Xml.newPullParser();
		InputStreamReader streamReader = new InputStreamReader(in,"gb2312"); 
		parser.setInput(streamReader);

		int event = parser.getEventType();// 产生第一个事件

		while (event != XmlPullParser.END_DOCUMENT) {

			switch (event) {

			case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
				recordInfoList = new ArrayList<DevRecordInfo>();
				break;
			case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
				recordInfo = new DevRecordInfo();
				if ("recInfo".equals(parser.getName())) {// 判断开始标签元素是否是name
					recordInfo.setFileName(parser.getAttributeValue(0));
					recordInfo.setbTime(parser.getAttributeValue(1));
					recordInfo.seteTime(parser.getAttributeValue(2));
					recordInfo.setFileSize(parser.getAttributeValue(3));
					recordInfo.setTimeLen(parser.getAttributeValue(4));
					recordInfo.setChnIndex(Integer.parseInt(parser.getAttributeValue(5)));
					recordInfo.setRecType(Integer.parseInt(parser.getAttributeValue(6)));
				}
				break;
			case XmlPullParser.END_TAG: // 结束元素事件
				if ("recInfo".equals(parser.getName())) {// 判断结束标签元素是否是device
					recordInfoList.add(recordInfo);
				}
				break;
			}
			event = parser.next();
		}
		return recordInfoList;
	}

	/**
	 * 解析服务器列表XML
	 * @param in
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static ArrayList<ServerInfo> getServerList(InputStream in)
			throws XmlPullParserException, IOException {


		ArrayList<ServerInfo> serverInfoList = null;
		ServerInfo serverInfo = null;

		XmlPullParser parser = Xml.newPullParser();
		InputStreamReader streamReader = new InputStreamReader(in,"gb2312"); 
		parser.setInput(streamReader);

		int event = parser.getEventType();// 产生第一个事件

		while (event != XmlPullParser.END_DOCUMENT) {

			switch (event) {

			case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
				serverInfoList = new ArrayList<ServerInfo>();
				break;
			case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
				serverInfo = new ServerInfo();
				if ("Node".equals(parser.getName())) {// 判断开始标签元素是否是Node
					serverInfo.setServerID(parser.getAttributeValue(0));
					serverInfo.setServerName(parser.getAttributeValue(1));
					serverInfo.setServerIp(parser.getAttributeValue(2));
					serverInfo.setServerPort(Integer.parseInt(parser.getAttributeValue(3)));
					serverInfo.setDomainName(parser.getAttributeValue(4));
					serverInfo.setParentID(parser.getAttributeValue(5));
					serverInfo.setCurFlag(parser.getAttributeValue(6));

					String onLine = parser.getAttributeValue(7);
					if("".equals(onLine)){
						serverInfo.setOnline(0);
					}else{
						serverInfo.setOnline(Integer.parseInt(onLine));
					}

					serverInfo.setOnlineTime(parser.getAttributeValue(8));
					serverInfo.setMemoryUsed(parser.getAttributeValue(9));
					serverInfo.setVirtualMemoryUsed(parser.getAttributeValue(10));
					serverInfo.setCPUUsedRate(parser.getAttributeValue(11));
					serverInfo.setThreadNumber(parser.getAttributeValue(12));
					serverInfo.setHandleNumber(parser.getAttributeValue(13));
				}
				break;
			case XmlPullParser.END_TAG: // 结束元素事件
				if ("Node".equals(parser.getName())) {// 判断结束标签元素是否是Node
					serverInfoList.add(serverInfo);
				}
				break;
			}
			event = parser.next();
		}
		return serverInfoList;
	}

}
