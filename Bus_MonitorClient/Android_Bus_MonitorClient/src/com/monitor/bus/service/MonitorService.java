package com.monitor.bus.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.PullParseXML;
import com.monitor.bus.Constants;
import com.monitor.bus.Constants.CALLBACKFLAG;
import com.monitor.bus.Constants.LGOINFLAG;
import com.monitor.bus.activity.R;
import com.monitor.bus.adapter.NotifycationManager;
import com.monitor.bus.bean.AlarmInfo;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.bean.LoginInfo;
import com.monitor.bus.bean.ServerInfo;
import com.monitor.bus.bean.manager.AlarmManager;
import com.monitor.bus.bean.manager.DeviceManager;
import com.monitor.bus.bean.manager.RecordManager;
import com.monitor.bus.control.LoginEventControl;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;


public class MonitorService extends Service{
	private static String TAG = "MonitorService";
	private Context mContext;
	
	private boolean is_alarm_flag = false;// 记录是否有报警
	private boolean alarm_times = false;// 记录是否已报警过一次
	private NotifycationManager myNotification;// 引用通知
	private static Timer alarmTimer;// 计时器
	private SoundPool soundPool;
	private int nGetServerIndex = 0; // 获取服务器列表次数
	private WeakReference<LoginStatusCallBack> mWRLoginStatuCallback;
	
	static {
		try {
			System.loadLibrary("ffmpeg");
			System.loadLibrary("JNVCommon_jni");
			System.loadLibrary("JNVPlayer_jni");
		} catch (UnsatisfiedLinkError e) {
			LogUtils.getInstance().localLog(TAG, "load库文件异常:" + e.getMessage(), LogUtils.LOG_NAME);
		}
	}
	@Override
	public void onCreate() {
		super.onCreate();
		mContext=this;
		JNVPlayerUtil.JNV_Init(Constants.SCREEN_COUNT);// 初始化so
		myNotification = new NotifycationManager(mContext);
		soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 5);// 一个参数为同时播放数据流的最大个数，二数据流类型，三为声音质量
		soundPool.load(mContext, R.raw.alarm_sound, 1);// 把你的声音素材放到res/raw里，2个参数即为资源文件，3个为音乐的优先级
		alarmTimer = new Timer();
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return new MyBinder();
	}
	
	@Override
	public void onDestroy() {
		JNVPlayerUtil.JNV_UnInit();
		super.onDestroy();
	}
	
	
	public void login(LoginInfo info){
		JNVPlayerUtil.JNV_N_Login(info.getIp(), info.getPort(), info.getUserName(), info.getPassWord(), 30,
				this, "callbackLogin", 0);
	}
	
	public void setLoginStatusListener(LoginStatusCallBack callback) {
		mWRLoginStatuCallback=new WeakReference<LoginStatusCallBack>(callback);
	}
	
	public synchronized int callbackLogin(long iUserParam, String strEvent) throws JSONException {
		Message msg;
		strEvent = strEvent.replace("NaN", "1");
		LogUtils.getInstance().localLog(TAG,
				"-callbackLoginEvent-" + "iUserParam:" + iUserParam + "  strEvent:" + strEvent, LogUtils.LOG_NAME);
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
		LogUtils.getInstance().localLog(TAG, "error Info:" + ret, LogUtils.LOG_NAME);
		return ret;
	}

	private void addServerTable(ArrayList<ServerInfo> list) {
		for (int i = 0; i < Constants.SERVICE_LIST.size(); i++) {
			if (Constants.SERVICE_LIST.get(i).equals(list)) {
				LogUtils.getInstance().localLog(TAG, "Repeat Service List！！!", LogUtils.LOG_NAME);
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
			LogUtils.getInstance().localLog(TAG, "只有3张服务器列表，申请参数错误!!", LogUtils.LOG_NAME);
		}
	}
	
	/**
	 * 上传服务器列表到JNI层
	 */
	private void upDataServerList() {
		int i, j;
		List<ArrayList<ServerInfo>> tmpll = Constants.SERVICE_LIST;
		if (Constants.IS_CASCADE_SERVER) {
			if (tmpll.size() != Constants.SERVER_TYPES)
				LogUtils.getInstance().localLog(TAG, "服务器列表出错！！", LogUtils.LOG_NAME);
			else {
				for (i = 0; i < tmpll.size(); i++) {
					ArrayList<ServerInfo> tmpl = tmpll.get(i);
					for (j = 0; j < tmpl.size(); j++) {
						ServerInfo tmp = tmpl.get(j);
						LogUtils.getInstance().localLog(TAG, "服务器表：" + tmp.getServerID() + "," + tmp.getServerIp() + ","
								+ tmp.getServerPort() + "," + i, LogUtils.LOG_NAME);
						int ret = JNVPlayerUtil.JNV_N_AddServerInfo(tmp.getServerID(), tmp.getServerIp(),
								tmp.getServerPort(), i);
						LogUtils.i(TAG, "add service info return:" + ret);
					}
				}
			}
		}
	}
	
	private Handler myHandler = new Handler() {
		Intent mIntent = new Intent(Constants.ACTION_LOGIN_EVENT);

		@Override
		public void handleMessage(Message msg) {
			LoginStatusCallBack mStatusCallback=mWRLoginStatuCallback.get();
			switch (msg.what) {
			case CALLBACKFLAG.LOGIN_ING:// 登陆中
				if (mStatusCallback != null) {
					mStatusCallback.onStatus(CALLBACKFLAG.LOGIN_ING);
				}
				break;
			case CALLBACKFLAG.LOGIN_SUCCESS:// 登陆成功
				LogUtils.i(TAG, "login succ");
				JNVPlayerUtil.JNV_N_GetDevList(Constants.DEVICELIST_PASTH);// 获取设备列表
				if (mStatusCallback != null) {
					mStatusCallback.onStatus(CALLBACKFLAG.LOGIN_SUCCESS);
				}
				break;
			case CALLBACKFLAG.LONGIN_FAILD:// 登陆失败
				String str = switchFlag(msg.arg1);
				LogUtils.i(TAG, "login faild" + str);
				JNVPlayerUtil.JNV_N_Logout();
				if (mStatusCallback != null) {
					mStatusCallback.onStatus(CALLBACKFLAG.LONGIN_FAILD);
				}
				break;
			case CALLBACKFLAG.GET_EVENT_DEVLIST:// 获取设备列表
				LogUtils.i(TAG, "GET_EVENT_DEVLIST:callback data:" + msg.arg1);
				if (1 == msg.arg1) {// 设备列表文件
					DeviceManager.getInstance().resetInfos();
					// 设备列表解析后，申请其他信息
					if (Constants.IS_CASCADE_SERVER) {
						getFirstServerList();// 获取一张服务器列表
					} else {
						// JNVPlayerUtil.JNV_N_GetAlarmStart("");//获取报警信息
						for (DeviceInfo info: DeviceManager.getInstance().getDeviceList()) {
							String guid = info.getGuId();
							if (!info.issDeviceGroup()&& guid!=null && !guid.isEmpty()) {
								int ret = JNVPlayerUtil.JNV_N_GetAlarmStart(guid);// 获取报警信息
								if (ret != 0) {
									LogUtils.getInstance().localLog(TAG, "get AlarmInof Error" + info,LogUtils.LOG_NAME_ALARM);
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
						LogUtils.getInstance().localLog(TAG, "servicexml文件不存在!!", LogUtils.LOG_NAME);
					} catch (XmlPullParserException e) {
						LogUtils.getInstance().localLog(TAG, "servicexml文件解析异常!!" + e.getMessage(), LogUtils.LOG_NAME);
					} catch (IOException e) {
						LogUtils.getInstance().localLog(TAG, "servicexml文件读取异常!!", LogUtils.LOG_NAME);
					}

					if (nGetServerIndex < 2) {
						getNextServerList();
					} else {
						upDataServerList();
						for (int i = 0; i < DeviceManager.getInstance().getSize(); i++) {
							DeviceInfo info = DeviceManager.getInstance().getAll().get(i);
							if (!info.issDeviceGroup()) {
								// String guid = info.getGuId();
								String guid = info.getNewGuId();
								int ret = JNVPlayerUtil.JNV_N_GetAlarmStart(guid);// 获取报警信息
								if (ret != 0) {
									// if(info.getOnLine()==1){
									// {
									LogUtils.i(TAG, "get Alarminfo ERROR!!:" + info);
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
					RecordManager.getInstance(mContext).resetList();
				}
				mIntent.putExtra("eventType", msg.what);
				// 发送广播
				mContext.sendBroadcast(mIntent);
				break;

			case CALLBACKFLAG.STREAM_OPEN_SUCCESS: // 流打开成功
				LogUtils.i(TAG, "=========>STREAM_OPEN_SUCCESS!");
				break;

			case CALLBACKFLAG.STREAM_OPEN_FAILD: // 流打开失败
				LogUtils.i(TAG, "=========>STREAM_OPEN_FAILD!");
				break;

			case CALLBACKFLAG.STREAM_CLOSED:// 流关闭
				LogUtils.i(TAG, "=========>STREAM_CLOSED!");
				break;
			case CALLBACKFLAG.DEVICE_ALARM_EVENT:// 报警事件
				doAlarmVoice();
				is_alarm_flag = true;
				try {
					JSONObject jo = new JSONObject(msg.obj + "");
					String deviceID = jo.getString("devID");
					int channelId = jo.getInt("alarmChn") + 1;
					int alarmType = jo.getInt("alarmType");

					AlarmInfo alarmInfo = new AlarmInfo();
					alarmInfo.setDeviceId(deviceID);
					alarmInfo.setChannelId(channelId);
					alarmInfo.setAlarmType(alarmType);

					AlarmManager manager = AlarmManager.getInstance(mContext);
					manager.addAlarmInfo(alarmInfo);

					if (MUtils.isBackGround(mContext)) {// 后台运行
						myNotification.showNotification("您有报警信息");
					}

					if (4096 == alarmType || 8192 == alarmType) {
						//上线下线
						mIntent.putExtra("eventType", alarmType);
						mIntent.putExtra("devID", deviceID);
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
						LogUtils.i(TAG, "+++++++++++++alarm thread ID：" + Thread.currentThread().getId()
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
	
	
	public class MyBinder extends Binder{
		public MonitorService getService(){
			return MonitorService.this;
		}
	}
	
	public interface LoginStatusCallBack {
		void onStatus(int statu);
	}

}
