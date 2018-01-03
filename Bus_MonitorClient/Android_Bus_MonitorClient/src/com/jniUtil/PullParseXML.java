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

import com.monitor.bus.bean.RecordInfo;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.bean.ServerInfo;
import com.monitor.bus.bean.manager.DeviceManager;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.LogUtils;

/**
 * 解析XML文件
 */
public class PullParseXML {
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
