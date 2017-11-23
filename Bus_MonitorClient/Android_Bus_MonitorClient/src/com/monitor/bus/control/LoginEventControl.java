package com.monitor.bus.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.internal.cu;
import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.MyUtil;
import com.jniUtil.PullParseXML;
import com.monitor.bus.activity.BusDeviceList;
import com.monitor.bus.activity.HomeActivity;
import com.monitor.bus.activity.MainListActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.adapter.MyNotification;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.consts.Constants.LGOINFLAG;
import com.monitor.bus.model.AlarmInfo;
import com.monitor.bus.model.BusDeviceInfo;
import com.monitor.bus.model.ServerInfo;

/**
 * 登陆回调管理类
 */
@SuppressLint("HandlerLeak")
public class LoginEventControl extends Object {
	private Activity currentContext;
	private static String TAG = "LoginEventControl";
	private boolean loginsuccess_flag = false; // 记录是否已登陆过一次
	private boolean is_alarm_flag = false;// 记录是否有报警
	private boolean alarm_times = false;// 记录是否已报警过一次
	private Message msg;
	private MyNotification myNotification;// 引用通知
	public static ProgressDialog myProgress;
	public static Timer alarmTimer;// 计时器
	private SoundPool soundPool;
	private int nGetServerIndex = 0; // 获取服务器列表次数
	private BusDeviceInfo currentDeviceInfo;
	public LoginEventControl(Activity currentActivity) {
		this.currentContext = currentActivity;
		myNotification = new MyNotification(currentActivity);
		myProgress = new ProgressDialog(currentActivity);

		// 创建声音播放
		soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 5);// 一个参数为同时播放数据流的最大个数，二数据流类型，三为声音质量
		soundPool.load(currentContext, R.raw.alarm_sound, 1);// 把你的声音素材放到res/raw里，2个参数即为资源文件，3个为音乐的优先级

		// 创建定时器
		alarmTimer = new Timer();
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
				Log.i(TAG, "登陆成功！！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 登陆之后的回调函数
	 * 
	 * @param flag
	 *            0:正在登录 1:登录成功 2: 登录失败 3:退出登录 4: 流正在打开 5:流打开成功 6:流打开失败 7:流关闭
	 * 
	 */
	public synchronized int callbackLonginEvent(long iUserParam, String strEvent)
			throws JSONException {

		strEvent = strEvent.replace("NaN", "1");
		Log.i(TAG, "callbackEvent:"+iUserParam+","+strEvent);
		JSONObject jo = new JSONObject(strEvent);
		int eventType = jo.getInt("eventType");
		msg = myHandler.obtainMessage();
		if (eventType == CALLBACKFLAG.DEVICE_ALARM_EVENT
				|| eventType == CALLBACKFLAG.DEVICE_EVENT_GPS_INFO) {
			msg.obj = strEvent;
		}
		if (eventType == CALLBACKFLAG.GET_EVENT_RECLIST
				|| eventType == CALLBACKFLAG.GET_EVENT_DEVLIST
				|| eventType == CALLBACKFLAG.JNET_EET_EVENT_SERVER_LIST
				|| eventType == CALLBACKFLAG.LONGIN_FAILD) {
			Log.e(TAG, "callbackLonginEvent：" + strEvent);
			int dataType = jo.getInt("dataType");
			msg.arg1 = dataType;
		}
		msg.what = eventType;
		msg.sendToTarget();

		return 0;
	}

	/*
	 * // 事件通知里的文件类型 typedef enum { eJNVFileErr = -1, // 文件失败 eJNVFileUndef = 0,
	 * // 未知类型 eJNVFileDevList = 1, // 设备列表文件 eJNVFileRecList = 2, // 录像查询文件
	 * eJNVFileRecDown = 3, // 录像下载 eJNVFileServerList = 4, // 服务器列表文件
	 * }eJNVFileType;
	 * 
	 * 失败返回值，在DATATYPE中 typedef enum e_NV_UserLogin_Return {
	 * E_NV_uSerLogin_Login_Success = 0, // 登录成功 E_NV_UserLogin_Analysis = 1, //
	 * 解析错误 E_NV_UserLogin_Information_Error = 2, // 用户名或用户密码错误
	 * E_NV_UserLogin_ReLogin_Error = 3, // 重复登录 E_NV_UserLogin_Blacklist_Error
	 * = 4, // 黑名单 E_NV_UserLogin_Version_Error = 5, // 版本太老
	 * E_NV_UserLogin_OverMaxUserNumber = 6, // 超过最大用户数 }e_NV_UserLogin_Return;
	 */

	private Handler myHandler = new Handler() {
		Intent mIntent = new Intent("ACTION_NAME");

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CALLBACKFLAG.LOGIN_ING:// 登陆中
				if (!loginsuccess_flag) {
					/* myProgress = new ProgressDialog(currentContext); */
					myProgress.setTitle(R.string.login_loading_title);
					myProgress.setMessage(currentContext
							.getString(R.string.waiting));
					myProgress.setCanceledOnTouchOutside(false);
					myProgress.show();
				}
				break;

			case CALLBACKFLAG.LOGIN_SUCCESS:// 登陆成功
				Log.e(TAG, "登陆成功！！");
				JNVPlayerUtil.JNV_N_GetDevList(Constants.DEVICELIST_PASTH);// 获取设备列表
				if (!loginsuccess_flag) {
					myProgress.dismiss();
					Intent intent = new Intent();
					intent.setClass(currentContext, MainListActivity.class);
					currentContext.startActivity(intent);
					currentContext.finish();
				}
				loginsuccess_flag = true;
				break;

			case CALLBACKFLAG.LONGIN_FAILD:// 登陆失败
				myProgress.dismiss();
				mIntent.putExtra("eventType", msg.what);
				// 发送广播
				currentContext.sendBroadcast(mIntent);
				String str = switchFlag(msg.arg1);
				Log.e(TAG, "登陆失败!" + str);
				//str = currentContext.getString(R.string.loginFailed) + str;
				JNVPlayerUtil.JNV_N_Logout();
				 Toast.makeText(currentContext, currentContext.getString(R.string.loginFailed),
				 Toast.LENGTH_SHORT).show();
				break;
			case CALLBACKFLAG.GET_EVENT_DEVLIST:// 获取设备列表
				Log.e(TAG, "获取设备列表，回调数据为：" + msg.arg1);

				if (1 == msg.arg1) {// 设备列表文件
					File myFile = new File(Constants.DEVICELIST_PASTH);
					try {
						InputStream in = new FileInputStream(myFile);
						// InputStream in =
						// this.getClass().getClassLoader().getResourceAsStream("DevList.xml");
						PullParseXML.getSortBusDevices(in);
					} catch (FileNotFoundException e) {
						Log.e(TAG, "xml文件不存在!!");
					} catch (XmlPullParserException e) {
						Log.e(TAG, "xml文件解析异常!!");
					} catch (IOException e) {
						Log.e(TAG, "xml文件读取异常!!");
					}
					// 设备列表解析后，申请其他信息
					if (Constants.IS_CASCADE_SERVER) {
						getFirstServerList();// 获取一张服务器列表
					} else {
						// JNVPlayerUtil.JNV_N_GetAlarmStart("");//获取报警信息
						for (int i = 0; i < Constants.BUSDEVICEDATA.size(); i++) {
							BusDeviceInfo info = Constants.BUSDEVICEDATA.get(i);
							if ("0".equals(info.getIsDeviceGroup())) {
								String guid = info.getGuId();
								int ret = JNVPlayerUtil
										.JNV_N_GetAlarmStart(guid);// 获取报警信息
								if (ret != 0) {
									Log.i(TAG, "申请获取报警信息出错" + info);
								}
							}
						}
					}
				}
				mIntent.putExtra("eventType", msg.what);
				// 发送广播
				currentContext.sendBroadcast(mIntent);
				break;

			case CALLBACKFLAG.JNET_EET_EVENT_SERVER_LIST:// 获取服务器列表
				Log.e(TAG, "获取服务器列表，回调数据为：" + msg.arg1);
				if (4 == msg.arg1) {// 设备列表文件
					File myFile = new File(
							Constants.SERVERLIST_PASTH[nGetServerIndex]);

					try {
						InputStream in = new FileInputStream(myFile);
						// InputStream in =
						// this.getClass().getClassLoader().getResourceAsStream("DevList.xml");
						addServerTable(PullParseXML.getServerList(in));
					} catch (FileNotFoundException e) {
						Log.e(TAG, "xml文件不存在!!");
					} catch (XmlPullParserException e) {
						Log.e(TAG, "xml文件解析异常!!" + e.getMessage());
					} catch (IOException e) {
						Log.e(TAG, "xml文件读取异常!!");
					}

					if (nGetServerIndex < 2) {
						getNextServerList();
					} else {
						upDataServerList();
						for (int i = 0; i < Constants.BUSDEVICEDATA.size(); i++) {
							BusDeviceInfo info = Constants.BUSDEVICEDATA.get(i);
							if ("0".equals(info.getIsDeviceGroup())) {
								// String guid = info.getGuId();
								String guid = info.getNewGuId();
								int ret = JNVPlayerUtil
										.JNV_N_GetAlarmStart(guid);// 获取报警信息
								if (ret != 0) {
									// if(info.getOnLine()==1){
									// {
									Log.i(TAG, "申请获取报警信息出错" + info);
								}
							}
						}
						mIntent.putExtra("eventType", msg.what);
						// 发送广播
						currentContext.sendBroadcast(mIntent);
					}
				}

				break;

			case CALLBACKFLAG.GET_EVENT_RECLIST:// 获取录像列表
				Log.e(TAG, "获取录像列表，回调数据为：" + msg.arg1);
				if (2 == msg.arg1) {// 录像查询文件
					File recordFile = new File(Constants.DEVRECORD_PASTH);
					try {
						InputStream in = new FileInputStream(recordFile);
						Constants.DEVRECORDINFOS = PullParseXML
								.getDevRecords(in);
						in.close();
					} catch (FileNotFoundException e) {
						Log.e(TAG, "xml文件不存在!!");
					} catch (IOException e) {
						Log.e(TAG, "xml文件读取异常!!");
					} catch (XmlPullParserException e) {
						Log.e(TAG, "xml文件解析异常!!");
					}
				}
				mIntent.putExtra("eventType", msg.what);
				// 发送广播
				currentContext.sendBroadcast(mIntent);
				break;

			case CALLBACKFLAG.STREAM_OPEN_SUCCESS: // 流打开成功
				Log.i(TAG, "=========>流打开成功!");
				break;

			case CALLBACKFLAG.STREAM_OPEN_FAILD: // 流打开失败
				Log.i(TAG, "=========>流打开失败!");
				break;

			case CALLBACKFLAG.STREAM_CLOSED:// 流关闭
				Log.i(TAG, "=========>流关闭!");
				break;
			case CALLBACKFLAG.DEVICE_ALARM_EVENT:// 报警事件
				if (!alarm_times) {// 一次报警时播放声音
					soundPool.play(1, 1, 1, 0, 0, 1);
					// 创建定时器，60秒执行一次
					alarmTimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							Log.e(TAG, "+++++++++++++报警线程ID："
									+ Thread.currentThread().getId()
									+ "+++++++++is_alarm_flag：" + is_alarm_flag);
							if (is_alarm_flag) {
								soundPool.play(1, 1, 1, 0, 0, 1);
								is_alarm_flag = false;
							}
						}
					}, 0, 60000);
					alarm_times = true;
				}
				is_alarm_flag = true;
				try {
					JSONObject jo = new JSONObject(msg.obj + "");
					String devID = jo.getString("devID");
					int alarmChn = jo.getInt("alarmChn") + 1;
					int alarmType = jo.getInt("alarmType");
					String alarInfo = getAlarmInfo(devID, alarmChn, alarmType);
					Log.i(TAG, "alarmChn" + alarmChn + "devID" + devID);
					if (Constants.ALARMINFOS.size() >= 100) {// 获取的报警信息超过100条
						Constants.ALARMINFOS.remove(0);
					}

					AlarmInfo busInfo = new AlarmInfo();
					busInfo.setGuId(devID);
					busInfo.setCurrentChn(alarmChn);
					busInfo.setExpresion(alarInfo);
					Constants.ALARMINFOS.add(busInfo);
					if (!Constants.IS_ACTIVE) {// 后台运行
						myNotification.showNotification(alarInfo);
					}

					if (4096 == alarmType || 8192 == alarmType) {
						mIntent.putExtra("eventType", alarmType);
						mIntent.putExtra("devID", devID);
						// 发送广播
						currentContext.sendBroadcast(mIntent);
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
					//Log.i(TAG, "接收到的：lon=" + longitude + ",lat=" + latitude);

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
					currentContext.sendBroadcast(mIntent);
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
	};

	/**
	 * 上传服务器列表到JNI层
	 */
	protected void upDataServerList() {
		int i, j;
		List<ArrayList<ServerInfo>> tmpll = Constants.SERVERDATA;
		if (Constants.IS_CASCADE_SERVER) {
			if (tmpll.size() != Constants.SERVER_TYPES)
				Log.e(TAG, "服务器列表出错！！");
			else {
				for (i = 0; i < tmpll.size(); i++) {
					ArrayList<ServerInfo> tmpl = tmpll.get(i);
					for (j = 0; j < tmpl.size(); j++) {
						ServerInfo tmp = tmpl.get(j);
						Log.e(TAG,
								"服务器表：" + tmp.getServerID() + ","
										+ tmp.getServerIp() + ","
										+ tmp.getServerPort() + "," + i);
						int ret = JNVPlayerUtil.JNV_N_AddServerInfo(
								tmp.getServerID(), tmp.getServerIp(),
								tmp.getServerPort(), i);
						Log.i(TAG, "加入服务器信息，返回：" + ret);
					}
				}
			}
		}
	}

	/**
	 * 对登陆的返回值进行区分和反馈
	 */
	protected String switchFlag(int arg) {
		String ret = "";
		Log.i(TAG, "switch:" + arg);
		switch (arg) {
		// case LGOINFLAG.LOGIN_SUCCESS:
		//
		// break;
		case LGOINFLAG.ANALYSIS_ERROR:
			ret = currentContext.getString(R.string.analysis_error);
			break;

		case LGOINFLAG.INFOMATION_ERROR:
			ret = currentContext.getString(R.string.infomation_error);
			break;

		case LGOINFLAG.RELOGIN_ERROR:
			ret = currentContext.getString(R.string.relogin_error);
			break;

		case LGOINFLAG.BLACKLIST_ERROR:
			ret = currentContext.getString(R.string.blacklist_error);
			break;

		case LGOINFLAG.VERSION_ERROR:
			ret = currentContext.getString(R.string.version_error);
			break;

		case LGOINFLAG.OVER_MAX_USER_NUM:
			ret = currentContext.getString(R.string.over_max_user_num);
			break;

		default:
			if (MyUtil.isConnect(currentContext)) {
				ret = currentContext.getString(R.string.other_error);
			} else {
				ret = currentContext.getString(R.string.network_error);
			}
			break;
		}
		Log.i(TAG, "错误标志信息：" + ret);
		return ret;
	}

	protected void addServerTable(ArrayList<ServerInfo> list) {
		for (int i = 0; i < Constants.SERVERDATA.size(); i++) {
			if (Constants.SERVERDATA.get(i).equals(list)) {
				Log.e("LoginEventControl", "出现重复服务器列表！！");
				return;
			}
		}
		Constants.SERVERDATA.add(list);
	}

	/**
	 * 获取一张服务列表
	 */
	protected void getFirstServerList() {
		if (Constants.IS_CASCADE_SERVER) {
			nGetServerIndex = 0;
			Constants.SERVERDATA.clear();
			JNVPlayerUtil.JNV_N_GetServerList(
					Constants.SERVERLIST_PASTH[nGetServerIndex],
					nGetServerIndex);// 获取服务器列表（0 中心 1信令 2媒体）
		}
	}

	/**
	 * 获取下一张服务列表
	 */
	protected void getNextServerList() {
		if (Constants.IS_CASCADE_SERVER && nGetServerIndex < 2) {
			nGetServerIndex++;
			JNVPlayerUtil.JNV_N_GetServerList(
					Constants.SERVERLIST_PASTH[nGetServerIndex],
					nGetServerIndex);// 获取服务器列表（0 中心 1信令 2媒体）
		} else {
			Log.e(TAG, "只有3张服务器列表，申请参数错误!!");
		}
	}

	/**
	 * 根据id获取设备名称
	 * 
	 * @param guId
	 * @return
	 */
	public BusDeviceInfo getBusInfo(String guId) {
		Iterator<BusDeviceInfo> itr = Constants.BUSDEVICEDATA.iterator();
		BusDeviceInfo busInfo = null;
		while (itr.hasNext()) {
			busInfo = itr.next();
			if (guId.equals(busInfo.getGuId())) {
				return busInfo;
			}
		}
		return null;
	}
	
	
	
	/**
	 * 返回报警信息
	 * 
	 * @param devId
	 * @param chn
	 * @param alarmType
	 * @return
	 */
	public String getAlarmInfo(String devId, int chn, int alarmType) {
		String alarmInfo = null;
		String devList = currentContext.getString(R.string.devList);
		String exists = currentContext.getString(R.string.exists);
		String overspeed = currentContext.getString(R.string.overspeed);
		String overspeed_cancel_alarm = currentContext.getString(R.string.overspeed_cancel_alarm);
		/*AlarmInfo devRecordInfo = alarms.get(0);*/
		/*AlarmInfo devRecordInfo = null;*/
		currentDeviceInfo = getBusInfo(devId);
		/*Log.i("+++++++++++", devRecordInfo.getGuId()+ "+" + devId);*/
		String dev_name = currentDeviceInfo.getDeviceName();
		if (1 == alarmType) {// 超速
			alarmInfo = dev_name + devList + exists
					+ overspeed;
		} else if (2 == alarmType) {// 超速消警
			alarmInfo = dev_name + devList + exists
					+ overspeed_cancel_alarm;
		} else if (4 == alarmType) {// 输入触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_trigger);
		} else if (8 == alarmType) {// 移动侦测
			alarmInfo = dev_name + devList + (chn - 1) + exists
					+ currentContext.getString(R.string.motion_detection);
		} else if (16 == alarmType) {// 视频丢失
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.video_lose);
		} else if (17 == alarmType) {// 低速报警
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.low_speed_alarm);
		} else if (18 == alarmType) {// 低速消音
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.low_speed_cancel_alarm);
		} else if (19 == alarmType) {// 违规停车报警
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.violation_parking_alarm);
		} else if (20 == alarmType) {// 违规停车消警
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.violation_parking_cancel_alarm);
		} else if (21 == alarmType) {// GPS存储磁盘空间不足
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.insufficient_GPS_disk_space);
		} else if (22 == alarmType) {// GPS存储停止存储
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.gps_save_ceased);
		} else if (23 == alarmType) {// 高速超速
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.overspeed_freeway);
		} else if (24 == alarmType) {// 高速超速消警
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.overspeed_freeway_cancel_alarm);
		} else if (25 == alarmType) {// 高速低速报警
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.lowspeed_freeway_alarm);
		} else if (26 == alarmType) {// 高速低速消警
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.lowspeed_freeway_cancel_alarm);
		} else if (27 == alarmType) {// 国道超速
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.overspeed_national_road);
		} else if (28 == alarmType) {// 国道超速消警
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.overspeed_nation_road_alarm_canceled);
		} else if (29 == alarmType) {// 国道低速
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.lowspeed_national_road);
		} else if (30 == alarmType) {// 国道低速消警
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.lowspeed_nation_road_alarm_canceled);
		} else if (32 == alarmType) {// GPS模块异常
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.gps_module_abnormal);
		} else if (64 == alarmType) {// GSensor报警
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.gsensor_alarm);
		} else if (128 == alarmType) {// 存储介质1异常
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.storage_medium_one_abnormity);
		} else if (256 == alarmType) {// 存储介质2异常
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.storage_medium_two_abnormity);
		} else if (512 == alarmType) {// 系统异常
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.system_abnormity);
		} else if (4096 == alarmType) {// 设备上线
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.device_online);
			// BusDeviceInfo busInfo = new BusDeviceInfo();

			for (BusDeviceInfo busInfo : Constants.BUSDEVICEDATA) {
				if (devId.equals(busInfo.getGuId())) {
					busInfo.setOnLine(1);
				}
			}

		} else if (8192 == alarmType) {// 设备离线

			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.device_offline);
			for (BusDeviceInfo busInfo : Constants.BUSDEVICEDATA) {
				if (devId.equals(busInfo.getGuId())) {
					busInfo.setOnLine(0);
				}
			}
		} else if (16384 == alarmType) {// 设备进入WIFI区域
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.enter_into_wifi_region);
		} else if (32768 == alarmType) {// 设备离开WIFI区域
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.leave_wifi_region);
		} else if (65536 == alarmType) {// 点火
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.ignition);
		} else if (131072 == alarmType) {// 熄火
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.flameout);
		} else if (131074 == alarmType) {// 前门打开
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.front_door_opens);
		} else if (131076 == alarmType) {// 前门关闭
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.front_door_closed);
		} else if (131080 == alarmType) {// 中门打开
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.middle_door_opens);
		} else if (131088 == alarmType) {// 中门关闭
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.middle_door_closed);
		} else if (131104 == alarmType) {// 后门打开
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.postern_opens);
		} else if (131136 == alarmType) {// 后门关闭
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.postern_closed);
		} else if (131200 == alarmType) {// 到站未停车
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.arrive_at_station_nostop);
		} else if (131328 == alarmType) {// 开门行车
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.drive_with_door_open);
		} else if (131584 == alarmType) {// 到站未开车门
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.arrive_at_station_noopen);
		} else if (132096 == alarmType) {// 车厢温度高
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.high_temperature_on_carriage);
		} else if (133120 == alarmType) {// 车厢温度低
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.low_temperature_on_carriage);
		} else if (135168 == alarmType) {// 滞站告警
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.alarm_when_delay_at_station);
		} else if (139264 == alarmType) {// 紧急加速
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.exigency_accelerate);
		} else if (147456 == alarmType) {// 紧急刹车
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.exigency_brake);
		} else if (163840 == alarmType) {// 票箱门报警
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.ballot_box_door_alarm);
		} else if (196609 == alarmType) {// 紧急报警
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.exigency_alarm);
		} else if (196610 == alarmType) {// 偏离路线
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.deviate_route);
		} else if (196612 == alarmType) {// 电子锁打开
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.electronic_unlock);
		} else if (196616 == alarmType) {// 电子锁关闭
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.electronic_lock_closed);
		} else if (196624 == alarmType) {// 电子锁异常
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.electronic_lock_abnormity);
		} else if (196640 == alarmType) {// 输入1触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_one_trigger);
		} else if (196672 == alarmType) {// 输入2触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_two_trigger);
		} else if (196736 == alarmType) {// 输入3触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_three_trigger);
		} else if (196864 == alarmType) {// 输入4触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_four_trigger);
		} else if (197120 == alarmType) {// 输入5触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_five_trigger);
		} else if (197632 == alarmType) {// 输入6触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_six_trigger);
		} else if (198656 == alarmType) {// 输入7触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_seven_trigger);
		} else if (200704 == alarmType) {// 输入8触发
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.input_eight_trigger);
		} else if (204800 == alarmType) {// 非法开关门
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.illegality_close);
		} else if (212992 == alarmType) {// 车辆载客超载
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.overload);
		} else if (262144 == alarmType) {// 越界
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.beyond_the_mark);
		} else if (524288 == alarmType) {// 越界消除
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.remove_slop_over);
		} else if (1048576 == alarmType) {// 超时停车
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.overtime_parking);
		} else if (2097152 == alarmType) {// 超时停车消除
			alarmInfo = dev_name
					+ devList
					+ exists
					+ currentContext
							.getString(R.string.remove_overtime_parking);
		} else if (4194304 == alarmType) {// 疲劳驾驶
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.fatigue_driving);
		} else if (8388608 == alarmType) {// 疲劳驾驶消除
			alarmInfo = dev_name + devList + exists
					+ currentContext.getString(R.string.remove_fatigue_driving);
		}
		return alarmInfo;
	}

}
