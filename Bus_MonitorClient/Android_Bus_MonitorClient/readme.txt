ndk 用r80

2.录像后，搜索到的文件不能回放。
6.视频默认只能看1通道，不能选择通道。


5.在登录加载的时候，取消登录回到登录界面，再次登录后，设备列表等信息会重复。
4.报警信息里面会出现空白的报警信息，点击会出现程序异常。


3.设置里面有个bug，显示模式和地图选择，点击其中一个后，再点击另一个，显示的内容会变得一样。并且显示模式设置无效。地图选择选谷歌地图后，打开视频程序会异常退出。
8.设置里面的自动登录选择后，重新打开软件无法登录，要重新安装软件才行。//登陆成功的回调没传到LoginActivity
1.地图那个缩小按钮被挡住了。
9.登录用户不能输入英文。
7.视频界面的监听和对讲按钮，点击后功能生效了，但是按钮状态没有变化。建议按钮加上状态变化，增加用户体验。


jni解码回调在 jniUtils.cpp中CallIntMethod    
	DecBuff：解码
	PrepareFrameBuff：解码					
	
	1051-1064yuv  不同deep设置不同格式
	
	AVPDecCBTypeRGB32		= 2,			// 视频.每像素4字节,"BGR0"
	
	JNVStreamTcp.cpp:
	正常：01-09 11:03:57.355 32018 32177 D JNVStreamTcp.cpp: eNVStreamPayloadHeaderFrame:352x288,rate=6,type=0x00000000
	不正常：01-09 15:01:32.671 26217 26547 D JNVStreamTcp.cpp: eNVStreamPayloadHeaderFrame:1280x720,rate=2097152,type=0x00000002
解码后的数据：JNVPlayer.cpp:830

解码代码：Decoder.cpp


// 流类型
typedef enum
{
	eJNVStreamUnknown			= 0,		// 未知流
	eJNVStreamReal				= 1,		// 实时流
	eJNVStreamReplay			= 2,		// 录像流
}eJNVStreamType;

// 事件通知里的文件类型
typedef enum 
{
	eJNVFileErr			= -1,				// 文件失败
	eJNVFileUndef		= 0,				// 未知类型
	eJNVFileDevList		= 1,				// 设备列表文件
	eJNVFileRecList		= 2,				// 录像查询文件
	eJNVFileRecDown		= 3,				// 录像下载
	eJNVFileServerList	= 4,				// 服务器列表文件
}eJNVFileType;

// 数据帧类型
typedef enum
{
	JNET_EMT_I_FRAME			= 0x00002001,
	JNET_EMT_P_FRAME			= 0x00002002,
	JNET_EMT_A_FRAME			= 0x00002003,
	JNET_EMT_PICTURE			= 0x00002004,
	JNET_EMT_MIC_DATA			= 0x00002005,
	JNET_EMT_FILE_DATA			= 0x00002006,
	JNET_EMT_TRANSPARENT		= 0x00002007,
	JNET_EMT_GPS_DATA			= 0x00002008,
	JNET_EMT_HEAD_FRAME			= 0x00002009,
}eJNetMediaType; 

// 流负载类型
typedef enum
{
	eNVStreamPayloadInvalidFrame	= 0,			// 无效帧
	eNVStreamPayloadHeaderFrame		= 0x68643030,	// 头帧('00hd')
	eNVStreamPayloadVideoIFrame		= 0x62643030,	// 视频主帧('00db')
	eNVStreamPayloadVideoVFrame		= 0x63643030,	// 视频虚帧('00dc')
	eNVStreamPayloadAudioFrame		= 0x62773030,	// 音频帧('00wb')
	eNVStreamPayloadGPSFrame		= 0x73706730,	// GPS帧('0gps')
	eNVStreamPayloadAlarmFrame		= 0x6d613030,	// 报警帧('00am')
}eNVStreamPayload;

// 回放控制命令
typedef enum
{
	eNVReplayNULL					= 0x00,			// 无效
	eNVReplayFastForward			= 0x01,			// 快进(1~4，其他无效)
	eNVReplayFastBack				= 0x02,			// 快退(1~4，其他无效)
	eNVReplayPosition				= 0x03,			// 定位
	eNVReplayNormal					= 0x04,			// 正常
	eNVReplayPause					= 0x05,			// 暂停
}eNVReplayType;
// 定义回调类型 begin
typedef enum
{
	AVPDecCBTypeNone	= 0,			// 无
	AVPDecCBTypeAudio16	= 1,			// 音频.采样率16khz,单声道,每个采样点16位表示
	AVPDecCBTypeRGB32	= 2,			// 视频.每像素4字节,排列方式与位图相似,"BGR0",第一个像素位于图像左下角
	AVPDecCBTypeYV12	= 3,			// 视频,yv12格式.排列顺序"Y0Y1...","V0V1...","U0U1..."
	AVPDecCBTypeUYVY	= 4,			// 视频,uyvy格式.排列顺序"U0Y0V0Y1U2Y2V2Y3......",第一个像素位于图像左上角
	AVPDecCBTypeYUV420	= 5,			// 视频,YUV420格式.排列顺序"Y0Y1...","U0U1...","V0V1..."
	AVPDecCBTypeYUYV	= 6,			// 视频,yuy2或yuyv格式.排列顺序"Y0 U0 Y1 V0 Y2 U2 Y3 V2 ... ... ",第一个像素位于图像左上角.
	AVPDecCBTypeRGB24	= 7,			// 视频.每像素3字节,排列方式与位图相似,"BGR",第一个像素位于图像左下角
	AVPDecCBTypeRGB565	= 8,			// 视频.每像素3字节,排列方式与位图相似,"BGR",第一个像素位于图像左下角
}AVPDecCBType;

// 定义错误码 begin
typedef enum
{
	JNETErrSuccess				= 0,		// 成功
	JNETErrUnInit				= -1,		// 未初始化
	JNETErrHandle				= -2,		// 句柄不存在
	JNETErrParam				= -3,		// 参数错误
	JNETErrBuffSize				= -4,		// 缓存满
	JNETErrNoMem				= -5,		// 内存不足
	JNETErrRecv					= -6,		// 接收错误
	JNETErrSend					= -7,		// 发送错误
	JNETErrOperate				= -8,		// 操作错误
	JNETErrCreateFile			= -9,		// 创建文件错误
	JNETErrNoFreePort			= -100,		// 没有空闲通道
	JNETErrProtocol				= -101,		// 协议错误
	JNETErrXMLFormat			= -102,		// 错误的XML数据
	JNETErrNotSupport			= -103,		// 不支持的操作
	JNETErrGetParam				= -104,		// 获取参数错误
	JNETErrSetParam				= -105,		// 设置参数错误
	JNETErrOpenFile				= -106,		// 打开文件出错
	JNETErrUpgOpen				= -107,		// 升级出错
}JNETErr;
// 定义错误码 end

// 定义流类型 begin
typedef enum
{
	AVPStreamUndef		= 0,			// 未定义
	AVPStreamFile		= 1,			// 文件流
	AVPStreamReal		= 2,			// 实时流
	AVPStreamPlayback	= 3,			// 回放流
}AVPStreamType;
// 定义流类型 end

// 定义播放状态 begin
typedef enum
{
	AVPPlayStatusStop	= 0,			// 停止
	AVPPlayStatusRun	= 1,			// 运行
	AVPPlayStatusIdle	= 2,			// 闲置
}AVPPlayStatus;