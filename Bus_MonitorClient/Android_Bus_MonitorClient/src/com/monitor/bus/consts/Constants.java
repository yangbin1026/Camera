package com.monitor.bus.consts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Environment;

import com.monitor.bus.model.AlarmInfo;
import com.monitor.bus.model.BusDeviceInfo;
import com.monitor.bus.model.DevRecordInfo;
import com.monitor.bus.model.ServerInfo;

/**
 * 全局变量
 */
public class Constants {

	public static final String DATABASE_NAME = "androidClient"; // 数据库名称
	public static final int DATABASE_VERSION = 6; // 数据库版本

	public static final int SCREEN_COUNT = 4; // 分屏数(so初始化)
	//public static final int RECORD_STREAM_TYPE = 1; // 录像回放
	public static int STREAM_PLAY_TYPE = 1; //播放视频类型 1：	实时流 	2：	录像回放
	public static final int VIDEO_TIMEOUT = 5; // 超时时间(秒)

	public static final String SPLIT_FLAG = " | "; // 设备列表group名称 由设备名称 + “ |
													// ”+IP地址组成
	public static final String CHANNEL_PREFIX_NAME = "channel_";// 播放通道的名称前缀
	public static final String DEVLIST_GROUP_KEY = "DevName";// 设备列表group的key名称
	public static final String DEVLIST_CHILD_KEY = "child";
	// 传输协议 0:JPF 1:JXJ
	public static final int JPF_PROTOCOL = 0;
	public static final int JXJ_PROTOCOL = 1;

	public static final String SDCardRoot = Environment
			.getExternalStorageDirectory().getAbsolutePath() + File.separator;
	public static final String RECORD_FILE_PATH = SDCardRoot + "busRecord/";
	public static final String IMAGE_PATH = SDCardRoot + "busPic";
	public static final String DEVICELIST_PASTH = SDCardRoot + "DevList.xml";
	
	//服务器列表保存地址（0 中心 1信令 2媒体）
	public static final String SERVERLIST_PASTH[] = {
		SDCardRoot + "ServerList0.xml",
		SDCardRoot + "ServerList1.xml",
		SDCardRoot + "ServerList2.xml",
	};
	
	public static final String DEVRECORD_PASTH = SDCardRoot + "DevRecordList.xml";
	
	//public static final int FLAG_VIDEO_PLAY = 1;// 操作标识--------表示先进行实时视频操作
	public static int FLAG_EXIST_FILE = 0;// 操作表示----------表示是否存在文件

	public static final int SPEED = 90;// 云台速度

	// 云台控制---JPF
	public static final class PTZ_DERECTION {
		public static final int PTZ_STOP = 255;// 停止
		public static final int PTZ_UP = 1;// 上
		public static final int PTZ_DOWN = 2;// 下
		public static final int PTZ_LEFT = 3;// 左
		public static final int PTZ_RIGHT = 4;// 右
	}


	public static int FLAG_FULLSCREEN = 0;// 全屏操作---------是否长按全屏 1全屏，0还原
	public static final String YMD_FORMAT = "yyyyMMdd";// （字符串）日期格式 年月日
	public static final String YMD_HMS_FORMAT = "yyyyMMddHHmmss";// 日期格式 年月日时分秒
	public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";// （正式）日期格式 年月日
	public static final String YMD_HMSS_FORMAT = "yyyyMMddHHmmssSSS";//日期格式 精确到毫秒
	public static final String RECORD_FILE_FORMAT = ".jnv";// 录像文件格式
	public static final String IMAGE_FILE_FORMAT = ".jpg";// 照片文件格式

	// 视频播放的回调标志
	public static final class CALLBACKFLAG {
		public static final int LOGIN_ING = 0; // 正在登录
		public static final int LOGIN_SUCCESS = 1; // 登录成功
		public static final int LONGIN_FAILD = 2; // 登录失败
		public static final int LOGIN_OUT = 3; // 退出登录
		public static final int GET_EVENT_DEVLIST = 301;//获取设备列表
		public static final int GET_EVENT_RECLIST = 302;//获取录像列表
		public static final int DEVICE_ALARM_EVENT = 201;//报警事件
		public static final int DEVICE_EVENT_GPS_INFO = 400;//下发GPS信息 
		public static final int STREAM_OPENING = 4; // 流正在打开
		public static final int STREAM_OPEN_SUCCESS = 5; // 流打开成功
		public static final int STREAM_OPEN_FAILD = 6; // 流打开失败
		public static final int STREAM_CLOSED = 7; // 流关闭
		public static final int IMAGE_CHANGE = 100;// 图像大小改变
		public static final int NATIVE_FILEPLAY_STOP = 101;// 文件播放完毕
		public static final int STREAM_OPEN_EXCEPTION = 103;//码流异常
		public static final int JNET_EET_TALK_OPENING = 12; // 对讲正在打开
		public static final int JNET_EET_TALK_OK = 13; // 对讲打开成功
		public static final int JNET_EET_TALK_ERROR = 14; // 对讲打开失败
		public static final int JNET_EET_TALK_CLOSE = 15; // 对讲关闭
		
		//级联服务器
		public static final int JNET_EET_EVENT_SERVER_LIST = 305; // 获取服务器列表,iDataType=eJNVFileType
	}
	
	/**
	typedef enum e_NV_UserLogin_Return
	{
		E_NV_uSerLogin_Login_Success		= 0,	// 登录成功
		E_NV_UserLogin_Analysis				= 1,	// 解析错误
		E_NV_UserLogin_Information_Error	= 2,	// 用户名或用户密码错误
		E_NV_UserLogin_ReLogin_Error		= 3,	// 重复登录
		E_NV_UserLogin_Blacklist_Error		= 4,	// 黑名单
		E_NV_UserLogin_Version_Error		= 5,	// 版本太老
		E_NV_UserLogin_OverMaxUserNumber	= 6,	// 超过最大用户数
	}e_NV_UserLogin_Return;
	 */
	//登陆回调
	public static final class LGOINFLAG {
		public static final int LOGIN_SUCCESS			= 0;
		public static final int ANALYSIS_ERROR			= 1;
		public static final int INFOMATION_ERROR		= 2;
		public static final int RELOGIN_ERROR			= 3;
		public static final int BLACKLIST_ERROR			= 4;
		public static final int VERSION_ERROR			= 5;
		public static final int OVER_MAX_USER_NUM		= 6;
	}
 
	public static final int AUDIO_RATE = 8000;// 采样频率，一般为8000hz/s
	public static final int FRAME_SIZE = 320;//
	public static final int RECORD_FRAME_SIZE=160;

	public static boolean ISOPEN_TALK=false;//打开对讲是否成功 True 成功 false失败
	public static boolean ISOPEN_AUDIO = false;//是否打开监听 True 打开 false 关闭
	//------镜像-------- 0=正常;1=反转;2 =获取当前正反状态----------
	public static final int MIRROE_NORMAL = 0;
	public static final int MIRROE_REVERSE = 1;
	public static final int MIRROE_STATUS = 2; 
	
	public static boolean DERECTION_STATE = true;// 当前方向状态标志 true 正 false 反
	
	public static boolean SCREEN_CHANGE_STATUS = false;//是否切已换屏幕方向		false：否	 true是
	public static boolean IS_ACTIVE = true;//该应用程序是否处于前台运行状态 true：前台 false 后台
	
	public static Activity INSTANCE; 
	
	
	public static List<BusDeviceInfo> BUSDEVICEDATA = new ArrayList<BusDeviceInfo>();//设备信息
	public static List<ArrayList<ServerInfo>> SERVERDATA = new ArrayList<ArrayList<ServerInfo>>();//服务器信息
	public static List<DevRecordInfo> DEVRECORDINFOS = new ArrayList<DevRecordInfo>();//设备端录像
	public static List<AlarmInfo> ALARMINFOS = new ArrayList<AlarmInfo>();//报警信息
	
	public static boolean IS_CASCADE_SERVER = false; //是否是级联服务器
	public static final int SERVER_TYPES 	= 3;	//服务器类型数
	
	public static boolean IS_TEST_VERSION = false;//是否是测试版
	public static final String EFFECTIVE_DATE = "2017-12-31";
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final boolean HAS_LOGO = true;
	
	public static final String SHARED_PREFERENCES_NAME = "setting";
	
	public static final String SERVICE_APK_NAME	=	"com.google.android.gms";	//服务
	public static final String SERVICE_URL_KEY	=	"service";	//服务
	public static final String SERVICE_URL		=	"http://183.61.171.28:8002/download/googleplayservice_8.4.89.apk";	//服务
	
	public static final String STORE_APK_NAME	=	"com.android.vending";		//商店
	public static final String STORE_URL_KEY	=	"store";		//商店
	public static final String STORE_URL		=	"http://183.61.171.28:8002/download/googleplaystore_6.0.5.apk";		//商店
	
	public static final boolean IS_DEFAULT_BAIDU_MAP 		= 	true;
	public static final String 	DEFAULT_MAP_KEY		= 	"is_def_baidu";
	
	public static final boolean IS_GOOGLE_GPS_CORRRECTION 		= 	false;	//是否google gps校正
	public static final String 	GOOGLE_GPS_CORRRECTION		= 	"GoogleGpsCorrection";
	
	public static final String SERVER_IP = "http://183.61.171.28:8002/";//更新APK的服务器地址
	public static final String SDCARD = Environment.getExternalStorageDirectory() + "/";//sdcard目录
}
