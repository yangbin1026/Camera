package com.monitor.bus.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable.Callback;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;

import com.jniUtil.JNVPlayerUtil;
import com.monitor.bus.utils.MUtils;
import com.jniUtil.PullParseXML;
import com.monitor.bus.activity.HomeActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.adapter.NotifycationManager;

import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.consts.Constants.LGOINFLAG;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.bean.AlarmInfo;
import com.monitor.bus.bean.AlarmManager;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.bean.DeviceManager;
import com.monitor.bus.bean.ServerInfo;

/**
 * 登陆回调管理类
 */
@SuppressLint("HandlerLeak")
public class LoginEventControl {
	private Context mContext;
	private static String TAG = "LoginEventControl";
	private boolean is_alarm_flag = false;// 记录是否有报警
	private boolean alarm_times = false;// 记录是否已报警过一次
	private Message msg;
	private NotifycationManager myNotification;// 引用通知
	private static Timer alarmTimer;// 计时器
	private SoundPool soundPool;
	private int nGetServerIndex = 0; // 获取服务器列表次数
	private LoginStatusCallBack mStatusCallback;

	public interface LoginStatusCallBack{
		void onStatus(int statu);
	}
	public LoginEventControl(Context context) {
		if(context==null){
			LogUtils.getInstance().localLog(TAG, "ERROR: LoginEventControl Context is NULL!!!",LogUtils.LOG_NAME);
		}
		this.mContext = context;
		myNotification = new NotifycationManager(context);
		// 创建声音播放
		soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 5);// 一个参数为同时播放数据流的最大个数，二数据流类型，三为声音质量
		soundPool.load(mContext, R.raw.alarm_sound, 1);// 把你的声音素材放到res/raw里，2个参数即为资源文件，3个为音乐的优先级

		// 创建定时器
		alarmTimer = new Timer();
	}
	public void setLoginStatusListener(LoginStatusCallBack callback){
		mStatusCallback=callback;
	}

	/***
	 * 简单测试用
	 */
	public int simpleCallBack(long iUserParam, String strEvent) {
		JSONObject jo;
		try {
			jo = new JSONObject(strEvent);
			int eventType = jo.getInt("eventType");
			if (eventType == 1) {
				LogUtils.i(TAG, "登陆成功！！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 登陆之后的回调函数
	 * 
	 * @param strEvent
	 *            0:正在登录 1:登录成功 2: 登录失败 3:退出登录 4: 流正在打开 5:流打开成功 6:流打开失败 7:流关闭
	 * 
	 */
	public synchronized int callbackLogin(long iUserParam, String strEvent) throws JSONException {

		strEvent = strEvent.replace("NaN", "1");
		LogUtils.getInstance().localLog(TAG,"-callbackLoginEvent-"+
				"iUserParam:" + iUserParam + "  strEvent:" + strEvent,LogUtils.LOG_NAME);
		JSONObject jo = new JSONObject(strEvent);
		int eventType = jo.getInt("eventType");
		msg = myHandler.obtainMessage();
		if (eventType == CALLBACKFLAG.DEVICE_ALARM_EVENT || eventType == CALLBACKFLAG.DEVICE_EVENT_GPS_INFO) {
			msg.obj = strEvent;
		}
		if (eventType == CALLBACKFLAG.GET_EVENT_RECLIST || eventType == CALLBACKFLAG.GET_EVENT_DEVLIST
				|| eventType == CALLBACKFLAG.JNET_EET_EVENT_SERVER_LIST || eventType == CALLBACKFLAG.LONGIN_FAILD) {
			int dataType = jo.getInt("dataType");
			msg.arg1 = dataType;
		}
		msg.what = eventType;
		msg.sendToTarget();

		return 0;
	}

	private Handler myHandler = new Handler() {
		Intent mIntent = new Intent("ACTION_NAME");

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CALLBACKFLAG.LOGIN_ING:// 登陆中
				if(mStatusCallback!=null){
					mStatusCallback.onStatus(CALLBACKFLAG.LOGIN_ING);
				}
				break;
			case CALLBACKFLAG.LOGIN_SUCCESS:// 登陆成功
				LogUtils.i(TAG, "登陆成功！！");
				JNVPlayerUtil.JNV_N_GetDevList(Constants.DEVICELIST_PASTH);// 获取设备列表
				if(mStatusCallback!=null){
					mStatusCallback.onStatus(CALLBACKFLAG.LOGIN_SUCCESS);
				}
				break;
			case CALLBACKFLAG.LONGIN_FAILD:// 登陆失败
				String str = switchFlag(msg.arg1);
				LogUtils.i(TAG, "登陆失败!" + str);
				JNVPlayerUtil.JNV_N_Logout();
				if(mStatusCallback!=null){
					mStatusCallback.onStatus(CALLBACKFLAG.LONGIN_FAILD);
				}
				break;
			case CALLBACKFLAG.GET_EVENT_DEVLIST:// 获取设备列表
				LogUtils.i(TAG, "获取设备列表，回调数据为：" + msg.arg1);

				if (1 == msg.arg1) {// 设备列表文件
					File myFile = new File(Constants.DEVICELIST_PASTH);
					try {
						InputStream in = new FileInputStream(myFile);
						PullParseXML.getSortBusDevices(in);
					} catch (FileNotFoundException e) {
						LogUtils.getInstance().localLog(TAG, "xml文件不存在!!",LogUtils.LOG_NAME);
					} catch (XmlPullParserException e) {
						LogUtils.getInstance().localLog(TAG, "xml文件解析异常!!",LogUtils.LOG_NAME);
					} catch (IOException e) {
						LogUtils.getInstance().localLog(TAG, "xml文件读取异常!!",LogUtils.LOG_NAME);
					}
					// 设备列表解析后，申请其他信息
					if (Constants.IS_CASCADE_SERVER) {
						getFirstServerList();// 获取一张服务器列表
					} else {
						// JNVPlayerUtil.JNV_N_GetAlarmStart("");//获取报警信息
						for (int i = 0; i < DeviceManager.getInstance().getSize(); i++) {
							DeviceInfo info = DeviceManager.getInstance().getDeviceList().get(i);
							if ("0".equals(info.getIsDeviceGroup())) {
								String guid = info.getGuId();
								int ret = JNVPlayerUtil.JNV_N_GetAlarmStart(guid);// 获取报警信息
								if (ret != 0) {
									LogUtils.i(TAG, "申请获取报警信息出错" + info);
								}
							}
						}
					}
				}
				mIntent.putExtra("eventType", msg.what);
				// 发送广播
				mContext.sendBroadcast(mIntent);
				break;

			case CALLBACKFLAG.JNET_EET_EVENT_SERVER_LIST:// 获取服务器列表
				if (4 == msg.arg1) {// 设备列表文件
					File myFile = new File(Constants.SERVERLIST_PASTH[nGetServerIndex]);

					try {
						InputStream in = new FileInputStream(myFile);
						addServerTable(PullParseXML.getServerList(in));
					} catch (FileNotFoundException e) {
						LogUtils.getInstance().localLog(TAG, "servicexml文件不存在!!",LogUtils.LOG_NAME);
					} catch (XmlPullParserException e) {
						LogUtils.getInstance().localLog(TAG, "servicexml文件解析异常!!" + e.getMessage(),LogUtils.LOG_NAME);
					} catch (IOException e) {
						LogUtils.getInstance().localLog(TAG, "servicexml文件读取异常!!",LogUtils.LOG_NAME);
					}

					if (nGetServerIndex < 2) {
						getNextServerList();
					} else {
						upDataServerList();
						for (int i = 0; i < DeviceManager.getInstance().getSize(); i++) {
							DeviceInfo info = DeviceManager.getInstance().getDeviceList().get(i);
							if ("0".equals(info.getIsDeviceGroup())) {
								// String guid = info.getGuId();
								String guid = info.getNewGuId();
								int ret = JNVPlayerUtil.JNV_N_GetAlarmStart(guid);// 获取报警信息
								if (ret != 0) {
									// if(info.getOnLine()==1){
									// {
									LogUtils.i(TAG, "申请获取报警信息出错" + info);
								}
							}
						}
						mIntent.putExtra("eventType", msg.what);
						// 发送广播
						mContext.sendBroadcast(mIntent);
					}
				}

				break;

			case CALLBACKFLAG.GET_EVENT_RECLIST:// 获取录像列表
				if (2 == msg.arg1) {// 录像查询文件
					File recordFile = new File(Constants.DEVRECORD_PASTH);
					try {
						InputStream in = new FileInputStream(recordFile);
						Constants.RECORD_LIST = PullParseXML.getDevRecords(in);
						in.close();
					} catch (FileNotFoundException e) {
						LogUtils.getInstance().localLog(TAG, "xml文件不存在!!",LogUtils.LOG_NAME);
					} catch (IOException e) {
						LogUtils.getInstance().localLog(TAG, "xml文件读取异常!!",LogUtils.LOG_NAME);
					} catch (XmlPullParserException e) {
						LogUtils.getInstance().localLog(TAG, "xml文件解析异常!!",LogUtils.LOG_NAME);
					}
				}
				mIntent.putExtra("eventType", msg.what);
				// 发送广播
				mContext.sendBroadcast(mIntent);
				break;

			case CALLBACKFLAG.STREAM_OPEN_SUCCESS: // 流打开成功
				LogUtils.i(TAG, "=========>流打开成功!");
				break;

			case CALLBACKFLAG.STREAM_OPEN_FAILD: // 流打开失败
				LogUtils.i(TAG, "=========>流打开失败!");
				break;

			case CALLBACKFLAG.STREAM_CLOSED:// 流关闭
				LogUtils.i(TAG, "=========>流关闭!");
				break;
			case CALLBACKFLAG.DEVICE_ALARM_EVENT:// 报警事件
				doAlarmVoice();
				is_alarm_flag = true;
				try {
					JSONObject jo = new JSONObject(msg.obj + "");
					String devID = jo.getString("devID");
					int channelId = jo.getInt("alarmChn") + 1;
					int alarmType = jo.getInt("alarmType");
					AlarmInfo alarmInfo = new AlarmInfo();
					alarmInfo.setDeviceId(devID);
					alarmInfo.setChannelId(channelId);
					alarmInfo.setAlarmType(alarmType);

					AlarmManager manager = AlarmManager.getInstance(mContext);
					LogUtils.i(TAG, "alarmChn" + channelId + "devID" + devID);
					if (manager.getAlarmList().size() >= 100) {// 获取的报警信息超过100条
						manager.removeFirst();
					}
					manager.addAlarmInfo(alarmInfo);

					if (MUtils.isBackGround(mContext)) {// 后台运行
						myNotification.showNotification("您有报警信息");
					}

					if (4096 == alarmType || 8192 == alarmType) {
						mIntent.putExtra("eventType", alarmType);
						mIntent.putExtra("devID", devID);
						// 发送广播
						mContext.sendBroadcast(mIntent);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;

			case CALLBACKFLAG.DEVICE_EVENT_GPS_INFO:// 下发GPS信息
				// Log.e(TAG, "+++callbackLonginEvent："+msg.obj);
				try {
					JSONObject jo = new JSONObject(msg.obj + "");
					double longitude = jo.getDouble("gpsBaseLongitude");
					double latitude = jo.getDouble("gpsBaseLatitude");
					// LogUtils.i(TAG, "接收到的：lon=" + longitude + ",lat=" +
					// latitude);

					int baseDirect = jo.getInt("gpsBaseDirect");
					String guId = jo.getString("gpsDevID");
					if (0d == longitude || 0d == latitude) {
						return;
					}
					mIntent.putExtra("eventType", msg.what);
					mIntent.putExtra("gpsBaseLongitude", longitude);
					mIntent.putExtra("gpsBaseLatitude", latitude);
					mIntent.putExtra("gpsBaseDirect", baseDirect);
					mIntent.putExtra("gpsDevID", guId);
					// Log.e(TAG, "context输出："+currentContext);
					mContext.sendBroadcast(mIntent);
					// 发送广播
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}

			super.handleMessage(msg);
		}

		private void doAlarmVoice() {
			if (!alarm_times) {// 一次报警时播放声音
				soundPool.play(1, 1, 1, 0, 0, 1);
				// 创建定时器，60秒执行一次
				alarmTimer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						LogUtils.i(TAG, "+++++++++++++报警线程ID：" + Thread.currentThread().getId()
								+ "+++++++++is_alarm_flag：" + is_alarm_flag);
						if (is_alarm_flag) {
							soundPool.play(1, 1, 1, 0, 0, 1);
							is_alarm_flag = false;
						}
					}
				}, 0, 60000);
				alarm_times = true;
			}
		}
	};

	/**
	 * 上传服务器列表到JNI层
	 */
	private void upDataServerList() {
		int i, j;
		List<ArrayList<ServerInfo>> tmpll = Constants.SERVICE_LIST;
		if (Constants.IS_CASCADE_SERVER) {
			if (tmpll.size() != Constants.SERVER_TYPES)
				LogUtils.getInstance().localLog(TAG, "服务器列表出错！！",LogUtils.LOG_NAME);
			else {
				for (i = 0; i < tmpll.size(); i++) {
					ArrayList<ServerInfo> tmpl = tmpll.get(i);
					for (j = 0; j < tmpl.size(); j++) {
						ServerInfo tmp = tmpl.get(j);
						LogUtils.getInstance().localLog(TAG, "服务器表：" + tmp.getServerID() + "," + tmp.getServerIp() + ","
								+ tmp.getServerPort() + "," + i,LogUtils.LOG_NAME);
						int ret = JNVPlayerUtil.JNV_N_AddServerInfo(tmp.getServerID(), tmp.getServerIp(),
								tmp.getServerPort(), i);
						LogUtils.i(TAG, "加入服务器信息，返回：" + ret);
					}
				}
			}
		}
	}

	/**
	 * 对登陆的返回值进行区分和反馈
	 */
	private String switchFlag(int arg) {
		String ret = "";
		LogUtils.i(TAG, "switch:" + arg);
		switch (arg) {
		case LGOINFLAG.ANALYSIS_ERROR:
			ret = mContext.getString(R.string.analysis_error);
			break;

		case LGOINFLAG.INFOMATION_ERROR:
			ret = mContext.getString(R.string.infomation_error);
			break;

		case LGOINFLAG.RELOGIN_ERROR:
			ret = mContext.getString(R.string.relogin_error);
			break;

		case LGOINFLAG.BLACKLIST_ERROR:
			ret = mContext.getString(R.string.blacklist_error);
			break;

		case LGOINFLAG.VERSION_ERROR:
			ret = mContext.getString(R.string.version_error);
			break;

		case LGOINFLAG.OVER_MAX_USER_NUM:
			ret = mContext.getString(R.string.over_max_user_num);
			break;

		default:
			if (MUtils.isConnect(mContext)) {
				ret = mContext.getString(R.string.other_error);
			} else {
				ret = mContext.getString(R.string.network_error);
			}
			break;
		}
		LogUtils.getInstance().localLog(TAG, "错误标志信息：" + ret,LogUtils.LOG_NAME);
		return ret;
	}

	private void addServerTable(ArrayList<ServerInfo> list) {
		for (int i = 0; i < Constants.SERVICE_LIST.size(); i++) {
			if (Constants.SERVICE_LIST.get(i).equals(list)) {
				LogUtils.getInstance().localLog(TAG, "出现重复服务器列表！！",LogUtils.LOG_NAME);
				return;
			}
		}
		Constants.SERVICE_LIST.add(list);
	}

	/**
	 * 获取一张服务列表
	 */
	private void getFirstServerList() {
		if (Constants.IS_CASCADE_SERVER) {
			nGetServerIndex = 0;
			Constants.SERVICE_LIST.clear();
			JNVPlayerUtil.JNV_N_GetServerList(Constants.SERVERLIST_PASTH[nGetServerIndex], nGetServerIndex);// 获取服务器列表（0
																											// 中心
																											// 1信令
																											// 2媒体）
		}
	}

	/**
	 * 获取下一张服务列表
	 */
	private void getNextServerList() {
		if (Constants.IS_CASCADE_SERVER && nGetServerIndex < 2) {
			nGetServerIndex++;
			JNVPlayerUtil.JNV_N_GetServerList(Constants.SERVERLIST_PASTH[nGetServerIndex], nGetServerIndex);// 获取服务器列表（0
		} else {
			LogUtils.getInstance().localLog(TAG, "只有3张服务器列表，申请参数错误!!",LogUtils.LOG_NAME);
		}
	}
	
}

