package com.monitor.bus.bean.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import com.monitor.bus.bean.RecordInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.LogUtils;

import android.content.Context;
import android.util.Xml;

public class RecordManager {
	private static final String TAG=RecordManager.class.getSimpleName();
	private List<RecordInfo> recordInfoList = new ArrayList<RecordInfo>();// 报警信息
	static RecordManager manager;
	private Context mContext;

	private RecordManager(Context context) {
		if(mContext!=null){
			mContext=context;
		}
	}

	public synchronized static RecordManager getInstance(Context context) {
		if(context==null){
			LogUtils.getInstance().localLog(TAG, "Context IS NULL!!!", LogUtils.LOG_NAME);
		}
		if (manager == null) {
			manager = new RecordManager(context);
		}
		return manager;
	}

	public synchronized void resetList() {
		try {
			File recordFile = new File(Constants.DEVRECORD_PASTH);
			InputStream in = new FileInputStream(recordFile);
			RecordInfo recordInfo = null;
			XmlPullParser parser = Xml.newPullParser();
			InputStreamReader streamReader = new InputStreamReader(in, "gb2312");
			parser.setInput(streamReader);
			int event = parser.getEventType();// 产生第一个事件
			while (event != XmlPullParser.END_DOCUMENT) {
				switch (event) {
				case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
					recordInfoList.clear();
					break;
				case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
					recordInfo = new RecordInfo();
					if ("recInfo".equals(parser.getName())) {// 判断开始标签元素是否是name
						recordInfo.setFileName(parser.getAttributeValue(0));
						recordInfo.setStartTime(parser.getAttributeValue(1));
						recordInfo.setEndTime(parser.getAttributeValue(2));
						recordInfo.setFileSize(parser.getAttributeValue(3));
						recordInfo.setTimeLen(parser.getAttributeValue(4));
						recordInfo.setChanneId(Integer.parseInt(parser.getAttributeValue(5)));
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
			in.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void clean(){
		recordInfoList.clear();
	}
	
	public List<RecordInfo> getAll(){
		return recordInfoList;
	}
	public int getAllSize(){
		return recordInfoList.size();
	}
}
