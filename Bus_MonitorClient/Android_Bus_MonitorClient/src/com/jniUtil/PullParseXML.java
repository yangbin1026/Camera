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

import com.monitor.bus.bean.DevRecordInfo;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.bean.DeviceManager;
import com.monitor.bus.bean.ServerInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.LogUtils;

/**
 * 解析XML文件
 */
public class PullParseXML {
	public static void getBusDevices(InputStream in) throws XmlPullParserException, IOException {
		boolean isCheck = false;
		DeviceInfo deviceInfo = null;
		DeviceManager manager = DeviceManager.getInstance();
		XmlPullParser parser = Xml.newPullParser();
		InputStreamReader streamReader = new InputStreamReader(in, "gb2312");
		parser.setInput(streamReader);

		int event = parser.getEventType();// 产生第一个事件
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
				// Constants.BUSDEVICEDATA.clear();
				LogUtils.getInstance().localLog("PullParseXML", "START_DOCUMENT");
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
				deviceInfo.setIsDeviceGroup(parser.getAttributeValue(null, "IsDeviceGroup"));

				if ("0".equals(parser.getAttributeValue(null, "IsDeviceGroup"))) {// 具有子节点时不需要加载设备信息
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
						LogUtils.getInstance().localLog("PullParseXML", "this Device is Useless");
					} else {
						manager.addDeviceInfo(deviceInfo);
					}
				}
				break;
			}
			event = parser.next();
		}
	}

	private static boolean isDeviceValid(DeviceInfo deviceInfo) {
		if ("".equals(deviceInfo.getCmdServerID()) || "".equals(deviceInfo.getMediaServerID())) {
			return false;
		}
		return true;
	}

	/**
	 * 获取排列后的巴士设备，并设置服务器模式，上传到JNI层
	 * 
	 * @param in
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static void getSortBusDevices(InputStream in) throws XmlPullParserException, IOException {
		getBusDevices(in);
	}

	public static List<DevRecordInfo> getDevRecords(InputStream in) throws XmlPullParserException, IOException {

		List<DevRecordInfo> recordInfoList = null;
		DevRecordInfo recordInfo = null;

		XmlPullParser parser = Xml.newPullParser();
		InputStreamReader streamReader = new InputStreamReader(in, "gb2312");
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
	 * 
	 * @param in
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static ArrayList<ServerInfo> getServerList(InputStream in) throws XmlPullParserException, IOException {

		ArrayList<ServerInfo> serverInfoList = null;
		ServerInfo serverInfo = null;

		XmlPullParser parser = Xml.newPullParser();
		InputStreamReader streamReader = new InputStreamReader(in, "gb2312");
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
					if ("".equals(onLine)) {
						serverInfo.setOnline(0);
					} else {
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
