package com.monitor.bus.control;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.MyUtil;
import com.monitor.bus.activity.R;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.database.DatabaseHelper;
import com.monitor.bus.model.BusDeviceInfo;
import com.monitor.bus.model.DevRecordInfo;
import com.monitor.bus.view.MyVideoView;

/**
 * 视频播放控制类
 */
@SuppressLint("HandlerLeak")
public  class VideoPlayControl {
	private static String TAG = "VideoPlayControl";
	//private VideoActivity videoActivity;// 实时视频
	//private int playType; // 播放类型 1:录像回放 2:实时流
	public AudioTrack track;// 音频操作对象
	//public byte[] audioData ;
	public AudioRecord audioRecord;// 对讲操作对象
	public byte[] encodeData;
	public int startStreamId = -1; // 播放返回的id，JNV_OpenStream返回
	public int recStreamId = -1;//录像回放返回的id,JNV_RecOpenFile返回
	public int replayStreamId = -1;//设备端录像返回的id，JNV_ReplayStart返回
	public int talkId = -1;//打开对讲返回的id
	private DatabaseHelper db;// 数据库操作对象
	private BusDeviceInfo currentDeviceInfo;// 当前设备信息
	private Toast myToast;
	//private int start_stream_flag = 0;// 打开流的标志
	int maxjitter;
	private Activity currentContext;
	private MyVideoView videoView;
	ProgressDialog loadingDialog;
	private Message msg;
	//FileOutputStream fos;
	
	public String deviceId;
	public int deviceChn;
	
	public VideoPlayControl(Activity currentActivity , MyVideoView videoView){
		this.currentContext = currentActivity;
		this.videoView = videoView;
		db = new DatabaseHelper(currentActivity,Constants.DATABASE_NAME);
		loadingDialog = new ProgressDialog(currentContext);// 进度条	
	}
	 
	
	public int callbackSetStreamInfo(int lStream,byte[] lpBuf,int lSize,int lWidth,int lHeight,long lStamp,int lType,int lUserParam)
	{
		if(lType==8){//视频
			if(videoView.videoWidth!=lWidth || videoView.videoHeight!=lHeight){
				Log.e(TAG, "视频信息。。。。。。。。。。。。。。。lWidth："+lWidth+".......lHeight:"+lHeight);
				videoView.is_drawblack = false;
				videoView.videoWidth=lWidth;
				videoView.videoHeight=lHeight;
				videoView.buffer = ByteBuffer.wrap(lpBuf);
				videoView.VideoBit = Bitmap.createBitmap(videoView.videoWidth, videoView.videoHeight, Config.RGB_565);
				videoView.getScaleSize(videoView.getMeasuredWidth(),videoView.getMeasuredHeight());
				videoView.flag = 0;
			}
			videoView.postInvalidate();
		}else if(lType==1){//音频 
			if(Constants.ISOPEN_AUDIO){
				Log.i(TAG, "音频信息。。。。。。。。。。。。。。。数据大小："+lSize+"大小："+lpBuf.length);
				writeAudioTrack(lpBuf,lSize);
			}
		}else{//无
			
		}
		return 0;
	} 
	

	public int callbackPlayInfo(int iType,long lParam1,long lParam2,int userParam){
		//iType = 6;
		System.out.println("callbackPlayInfo:"+iType+" "+lParam1+" "+lParam2);

		msg = myHandler.obtainMessage();
		msg.what = iType;
		msg.sendToTarget();
		return 0;
	} 
	private Handler myHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0://未知
				break;
			case 1://错误
				break;
			case 5://结束播放 
				System.out.println("============结束播放");
				if (Constants.ISOPEN_AUDIO) {// 停止监听
				Constants.ISOPEN_AUDIO = false;
				track.stop();// 停止音频
				}
				currentContext.finish();
				break;
			case 6://暂停播放
				break;
			case 7://继续播放
				break;
			default:
				break;
			}
			
			
			super.handleMessage(msg);
		}
	};
	/**
	 * 初始化
	 */
	public void initVideoPlay(Intent intent) {
		initAudio();// 初始化音频 
		if (2 == Constants.STREAM_PLAY_TYPE) {// 录像回放
			DevRecordInfo devRecordInfo = (DevRecordInfo) intent.getSerializableExtra("devRecordInfo");// 回放的文件名称
				if( null != devRecordInfo ){//设备端播放
					
					replayStreamId =	JNVPlayerUtil.JNV_ReplayStart(devRecordInfo.getGuId(), 0, devRecordInfo.getChnIndex(), 
							devRecordInfo.getbTime(), devRecordInfo.geteTime(), 0, devRecordInfo.getRecType(), 
							0, this, "callbackSetStreamInfo", 0);
					 
					 
				}else{//本地回放
					
					String filePath = intent.getStringExtra("playFileName");// 回放的文件名称
					//String filePath = Constants.SDCardRoot+"20130608_111847.jnv";// 回放的文件名称
					String record_id = intent.getStringExtra("id");// 回放文件的id
					Log.e(TAG, "++++++本地回放+++++文件路径:"+ filePath+"++++++++录像ID:"+record_id);
					File mFile = new File(filePath);
					if (!mFile.exists()) {// 文件不存在，删除数据库的信息
						MyUtil.commonToast(currentContext, R.string.not_findfile);
						db.deleteRecordInfo(record_id);
					}
					recStreamId = JNVPlayerUtil.JNV_RecOpenFile(filePath, this, "callbackSetStreamInfo", "callbackPlayInfo", 0);
			}
		} else if (1 == Constants.STREAM_PLAY_TYPE) {// 实时视频流
			currentDeviceInfo = (BusDeviceInfo) intent.getSerializableExtra("videoData");
			initSendTalk();// 初始化对讲
			if (currentDeviceInfo == null) {
				MyUtil.commonToast(currentContext, R.string.not_playdata);
				return;
			}
			Constants.DERECTION_STATE = AVP_GetMirror();// 获取当前设备的镜像状态
//			startStream(currentDeviceInfo.getGuId(), currentDeviceInfo.getCurrentChn());// 打开实时流
			Log.e(TAG, "打开流参数："+currentDeviceInfo.getNewGuId()
					+","+currentDeviceInfo.getCurrentChn());
			startStream(currentDeviceInfo.getNewGuId(), currentDeviceInfo.getCurrentChn());// 打开实时流
		}
	}
	
	/*-------------------------------------实时视频--------------------------------------*/
 
	/**
	 * 开始实时视频
	 */
	public void startStream(String deviceId,int deviceChn) {
		Log.e(TAG, "Open Strean:" + deviceId+","+deviceChn);
		startStreamId=JNVPlayerUtil.JNV_OpenStream(deviceId, deviceChn, 0, 0, this, "callbackSetStreamInfo", 0);
		System.out.println("openStreamId = " + startStreamId);
		if(startStreamId<0){
			Log.e(TAG, "++++++++++++打开流失败,失败返回值:"+ startStreamId);   
		}
		
		//start_stream_flag = 1;
	}
 
	/**
	 * 停止实时视频
	 */
	public void stopStream() {
		if(0 > startStreamId){
			return;
		}
		JNVPlayerUtil.JNV_StopStream(startStreamId);
	}

	


	// --------------------------云台控制----------------------------
	/**
	 * 
	 * @param iDirection
	 *            方向
	 * @param lSpeed
	 *            速度
	 */
	public void PTZCtrl(int iDirection) {
//		JNVPlayerUtil.JNV_N_PtzCtrl(currentDeviceInfo.getGuId(), currentDeviceInfo.getCurrentChn(), iDirection, 0, 0);
		JNVPlayerUtil.JNV_N_PtzCtrl(currentDeviceInfo.getNewGuId(), currentDeviceInfo.getCurrentChn(), iDirection, 0, 0);
	}

	// ----------------------------------------------录像-----------------------------
	/**
	 * 开始录像
	 * 
	 * @param path
	 *            录像的存储路径及文件名
	 */
	public int RecordStart(String path) {
		
		if (startStreamId < 0) {
			return 0;
		}
		if (0 > JNVPlayerUtil.JNV_RecStart(startStreamId, path)) {
			return 0;
		}
		return 1;
	}

	/**
	 * 停止录像
	 */
	public int RecordStop() {
		if (startStreamId < 0) {
			return 0;
		}

		if (0 > JNVPlayerUtil.JNV_RecStop(startStreamId)) {
			return 0;
		}
		return 1;
	}

	// ----------------------------本地回放-------------------------
	/**
	 * 本地文件播放
	 * 
	 * @param handleId
	 * @param status
	 */
	public  void AVP_Native_Start() {
		//JAVNativeUtil.JAV_Native_StartPlayFile(handleId, 0);

	}

	/**
	 * 本地文件暂停
	 * 
	 * @param handleId
	 * @param status
	 */
	public void pauseVideo() {
		if (0 > recStreamId) {
			return; 
		}
		JNVPlayerUtil.JNV_RecPlayPause(recStreamId);
	}

	/**
	 * 本地文件继续播放 
	 * 
	 * @param handleId
	 * @param status
	 */
	public void resumePlayFileVideo() {
		if (0 > recStreamId) {
			return; 
		}
		JNVPlayerUtil.JNV_RecPlayStart(recStreamId);
	}

	/**
	 * 本地文件播放停止
	 * 
	 * @param handleId
	 * @param status
	 */
	public void AVP_Native_Stop() {
		Log.e(TAG, "++++++++++++AVP_Native_Stop:"+ recStreamId);
		if (0 > recStreamId) {
			return; 
		}
		JNVPlayerUtil.JNV_RecCloseFile(recStreamId);
	}
	 
	/**
	 * 设备端录像文件播放停止
	 */
	public void AVP_Native_RecordStop(){
		JNVPlayerUtil.JNV_ReplayStop(replayStreamId);//停止录像回放
	}
	/**
	 * 设备端录像回放控制
	 * @param status
	 */
	public void AVP_Native_RecordCtrl(int status){
		JNVPlayerUtil.JNV_ReplayCtrl(replayStreamId, status, 1);
	}
	
	// ----------------------------音频管理-------------------------
	/**
	 * 初始化音频
	 */
	public void initAudio() {

		maxjitter = AudioTrack.getMinBufferSize(Constants.AUDIO_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		Log.e(TAG, "+++initAudio++++++++++初始化音频:"+maxjitter);
		track = new AudioTrack(AudioManager.STREAM_MUSIC,
				Constants.AUDIO_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, maxjitter*6,
				AudioTrack.MODE_STREAM);
		//audioData = new byte[Constants.FRAME_SIZE];// 接收音频数据.
	} 

	/**
	 * 写入音频
	 */
	public void writeAudioTrack(byte[] trackData,int dataLenght) {
			track.write(trackData, 0, dataLenght);
	}



	// ----------------------------对讲管理-------------------------
	/**
	 * 初始化录音
	 */
	public void initSendTalk() {
		int min = AudioRecord.getMinBufferSize(Constants.AUDIO_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		Log.e(TAG, "++++initSendTalk++++++++初始化录音"+min);
		audioRecord = new AudioRecord(
				MediaRecorder.AudioSource.MIC,// the recording source
				Constants.AUDIO_RATE, // 采样频率，一般为8000hz/s
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, min);
		encodeData = new byte[Constants.FRAME_SIZE];// 存储音频数据
	}

	/**
	 * 读取本地音频
	 */
	public int readAudioRecord() {
		return audioRecord.read(encodeData, 0, Constants.FRAME_SIZE);
	}

	/**
	 * 打开对讲
	 * 
	 * @return
	 */
	public void openTalk() {
		//File file = new File(Constants.SDCardRoot+"test.pcm");
        // 删除录音文件
        //if (file.exists())
           // file.delete();
        // 创建录音文件
       // try {
        //    file.createNewFile();
       // } catch (IOException e) {
       //     throw new IllegalStateException("Failed to create " + file.toString());
      //  }
       // try {
		//	fos = new FileOutputStream(file);
	//	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}// 建立一个可存取字节的文件
        
//		talkId = JNVPlayerUtil.JNV_N_TalkStart(currentDeviceInfo.getGuId(),currentDeviceInfo.getCurrentChn());
		talkId = JNVPlayerUtil.JNV_N_TalkStart(currentDeviceInfo.getNewGuId(),currentDeviceInfo.getCurrentChn());
		if(0 > talkId){
			return;
		}else{
			new Thread(talkRunnable).start();
		}
	}

	/**
	 * 关闭对讲
	 * 
	 * @return
	 */
	public boolean closeTalk() {
		if(0 > talkId){
			return false;
		}
//		try {
//			fos.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if (-1 < JNVPlayerUtil.JNV_N_TalkStop(talkId)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 调用接口，发送对讲数据
	 * 
	 * @param isOpenAudio
	 */
	public void AVP_SendTalk_Ctrl(byte[] pBuff, int size) {
		JNVPlayerUtil.JNV_N_TalkSend(talkId, pBuff, size);
		
	}
 
	/**
	 * 对讲线程
	 */
	public Runnable talkRunnable = new Runnable() {// 开启对讲

		@Override
		public void run() {
			
			Log.i(TAG, "+++++++++++++对讲线程ID："+ Thread.currentThread().getId()+"+++++++++name："+Thread.currentThread().getName());
			while (Constants.ISOPEN_TALK) {// 打开对讲成功
				int size = readAudioRecord();
				//Log.i(TAG, "=========>size:"+size);
				if (size <= 0) continue; 
				
//				try {
//					fos.write(encodeData);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				AVP_SendTalk_Ctrl(encodeData, size);
				size = 0;
			}
		} 
	};

	
	// ----------------------------镜像管理-------------------------
		/**
		 * 调用接口，进行镜像翻转
		 */
		public int AVP_SetMirror(int MirrorParam) {
			//return JAVNativeUtil.JAV_Native_SetMirror(handleId,currentDeviceInfo.getCurrentChn(), MirrorParam);
			return 1;
		}

		/**
		 * 调用接口，获取镜像翻转
		 * 
		 * @param MirrorParam
		 * @return
		 */
		public boolean AVP_GetMirror() {
			/*
			if (0 == JAVNativeUtil.JAV_Native_SetMirror(handleId,currentDeviceInfo.getCurrentChn(), Constants.MIRROE_STATUS)) {// 正常
				return true;
			} else {// 反转
				videoActivity.mirrorButton.setBackgroundDrawable(currentContext.getResources().getDrawable(R.drawable.close));
				return false;
			}*/
			return true;
		}
		
		
		/**
		 * 抓拍，将当前的文件存储在本地
		 * 
		 * @param bitmap
		 * @throws IOException
		 */
		public void CapturePicture() {
			String imageFilePath = MyUtil.getCurrentFilePath(Constants.IMAGE_PATH + "/", currentDeviceInfo);// 文件目录
			File devFile = new File(imageFilePath);
			if (!devFile.exists()) {// 目录不存在
				devFile.mkdirs();// 创建相应的文件夹
			}
			String times = MyUtil.getCurrentDateTime(Constants.YMD_HMSS_FORMAT);// 当前时间 yyyyMMddHHmmssSSS格式

			File f = new File(imageFilePath + times + Constants.IMAGE_FILE_FORMAT);// 文件路径
			if(videoView.saveBitmap(f)){//抓拍成功
				if(myToast == null){
					myToast = Toast.makeText(currentContext, R.string.capture_success, Toast.LENGTH_SHORT);
				}else{
					//myToast.cancel();
					myToast.setText(R.string.capture_success);
				}
				myToast.show();
			  
			}
		}
		
 

		

}
