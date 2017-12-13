package com.jniUtil;

/**
 * JNI类
 */
public class JNVPlayerUtil {

	//获取CPU效率
	public static native int JNV_UpdateCpuInfo();
	public static native int JNV_GetCPUUsage(int index);
	// 系统操作
	public static native int JNV_Init(int size);
	public static native int JNV_UnInit();

	// 登录相关操作参数
	/**
	 * @param ip			服务器ip
	 * @param port			服务器端口
	 * @param userName		用户名
	 * @param psw			密码
	 * @param timeout		登陆超时时间
	 * @param target		回调对象类
	 * @param callbackEvent	回调方法
	 * @param lUserParam	
	 * @return
	 */
	public static native int JNV_N_Login(String ip, int port, String userName, String psw, int timeout,Object target, String callbackEvent,int lUserParam);
	public static native int JNV_N_Logout();

	// 请求设备列表
	/**
	 * @param filePath		获取设备列表文件路径
	 */
	public static native int JNV_N_GetDevList(String filePath);   

	// 请求服务器列表，级联服务器
	/**
	 * @param filePath		获取服务器列表文件路径
	 * @param nServerType   服务器类型（0 中心 1信令 2媒体）
	 */
	public static native int JNV_N_GetServerList(String filePath, int nServerType);

	/**
	 * 设置连接服务器类型:0,表示成功;其他,表示失败
	 * @param：nConnectServerType  连接服务器类型  0 连单服务器版   1 连级联版本
	 */
	public static native int JNV_N_SetConnectServerType(int nConnectServerType);  
  
	/**
	 * 增加服务器信息:0,表示成功;其他,表示失败
	 * @param lpszServerId 服务器id  
	 * @param lpszServerIp 服务器ip
	 * @param nServerPort  服务器port
	 * @param nServerType  服务器类型（0 中心 1信令 2媒体）
	 */
	public static native int JNV_N_AddServerInfo(String lpszServerId, String lpszServerIp, int nServerPort, int nServerType);


	// GPS信息相关
	public static native int JNV_N_GetGPSStart(String deviceId); 
	public static native int JNV_N_GetGPSStop(String deviceId); 

	// 录像下载相关
	/**
	 * 录像查询
	 * @param deviceId		设备ID
	 * @param iCenter		1，中心录像；0，设备录像
	 * @param iType			1,普通录像；2,报警录像
	 * @param startTime		开始时间
	 * @param endTime		结束时间
	 * @param iChnFlag		通道号
	 * @param filePath		设备端文件路径
	 * @return
	 */
	public static native int JNV_N_RecQuery(String deviceId,int iCenter,int iType,String startTime,String endTime,int iChnFlag,String filePath);
	/**
	 * 录像下载
	 * @param deviceId		设备GUID
	 * @param devPath		设备端录像文件路径
	 * @param iStartPos		开始位置
	 * @param filePath		下载到本地路径
	 * @return
	 */
	public static native int JNV_N_RecDownload(String deviceId,String devPath,int iStartPos,String filePath);

	// 云台
	/**
	 * 云台控制
	 * @param deviceId		设备GUID
	 * @param iChn			设备通道号
	 * @param iAction		云台控制类型
	 * @param iValue1
	 * @param iValue2
	 * @return
	 */
	public static native int JNV_N_PtzCtrl(String deviceId,int iChn,int iAction,int iValue1,int iValue2);

	// 实时视频相关
	/**
	 * 开启视频流
	 * @param deviceID			设备GUID
	 * @param iChn				设备通道号
	 * @param iStream			码流编号
	 * @param iType				码流类型
	 * @param target
	 * @param callbackDecFrame	回调
	 * @param lUserParam
	 * @return
	 */
	public static native int JNV_OpenStream(String deviceID, int iChn, int iStream, int iType, Object target, String callbackDecFrame,int lUserParam);
	/**
	 * 关闭视频流
	 * @param streamId
	 * @return
	 */
	public static native int JNV_StopStream(int streamId);

	// 远程录像回放相关
	/**
	 * 设备端录像回放
	 * @param deviceID			设备GUID
	 * @param iCenter			中心或者设备录像，1，中心录像；0，设备录像
	 * @param iChn				设备通道号
	 * @param startTime			开始时间
	 * @param endTime			结束时间
	 * @param iOnlyIFrame		是否播放一帧，1：是，0：否
	 * @param iRecType			录像类型，1,普通录像；2,报警录像
	 * @param iStartPos			开始位置
	 * @param target			
	 * @param callbackDecFrame	回调
	 * @param lUserParam
	 * @return
	 */
	public static native int JNV_ReplayStart(String deviceID,int iCenter,int iChn,String startTime,String endTime,int iOnlyIFrame,int iRecType,int iStartPos, Object target, String callbackDecFrame,int lUserParam);
	/**
	 * 录像回放控制
	 * @param streamId
	 * @param ctrlType			录像回放控制类型
	 * @param ctrlValue			控制值
	 * @return
	 */
	public static native int JNV_ReplayCtrl(int streamId,int ctrlType,int ctrlValue);
	public static native int JNV_ReplayStop(int streamId);//停止录像回放

	// 解码操作 begin
	/**
	 * 抓拍
	 * @param lStream
	 * @param filePath
	 * @return
	 */
	public static native int JNV_Capture(int lStream,String filePath);
	/**
	 * 开始本地录像
	 * @param lStream
	 * @param filePath		本地录像文件存储路径
	 * @return
	 */
	public static native int JNV_RecStart(int lStream,String filePath);
	public static native int JNV_RecStop(int lStream);//停止本地录像
	// 解码操作 end

	// 本地录像回放相关 begin
	/**
	 * @param filePath			本地录像文件路径
	 * @param Target
	 * @param callbackDecFrame	回调，用于显示视频
	 * @param callbackPlayInfo	回调，用于获取回放信息
	 * @param lUserParam
	 * @return
	 */
	public static native int JNV_RecOpenFile(String filePath,Object Target ,String callbackDecFrame,String callbackPlayInfo,int lUserParam);
	public static native int JNV_RecCloseFile(int lRecFile);				//关闭本地录像回放
	public static native int JNV_RecPlayStart(int lRecFile);
	public static native int JNV_RecPlayPause(int lRecFile);				//暂停本地录像
	public static native int JNV_RecPlaySetSpeed(int lRecFile,int iSpeed);	//设置本地录像播放速度
	public static native int JNV_RecPlayGetSpeed(int lRecFile);				//获取本地录像播放速度
	public static native int JNV_RecPlayNextFrame(int lRecFile);			//下一帧播放
	public static native int JNV_RecPlaySeek(int lRecFile,int lTime);		//拖动播放
	// 本地录像回放相关 end 

	public static native byte[] JNV_TEST(int streamId);


	// 报警相关
	public static native int JNV_N_GetAlarmStart(String deviceId);
	public static native int JNV_N_GetAlarmStop(String deviceId);


	//对讲
	public static native int JNV_N_TalkStart(String deviceID,int iChn);
	public static native int JNV_N_TalkStop(int iTalk);
	public static native int JNV_N_TalkSend(int iTalk, byte[] pjBuff,int iLen); 


}
