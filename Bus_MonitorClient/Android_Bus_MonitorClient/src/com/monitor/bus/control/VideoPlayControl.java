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
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.RealTimeVideoActivity;
import com.monitor.bus.activity.ReplayActivity;
import com.monitor.bus.bean.RecordInfo;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.database.DatabaseHelper;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.view.MyVideoView;
import com.monitor.bus.view.dialog.DateUtil;

/**
 * 视频播放控制类
 */
@SuppressLint("HandlerLeak")
public class VideoPlayControl {
	private static String TAG = "VideoPlayControl";
	private static String CALLBACK_STREAM_INFO="callbackSetStreamInfo";
	private Activity mContext;


	public byte[] encodeData;
	public int startStreamId = -1; // 播放返回的id，JNV_OpenStream返回
	public int recStreamId = -1;// 录像回放返回的id,JNV_RecOpenFile返回
	public int replayStreamId = -1;// 设备端录像返回的id，JNV_ReplayStart返回
	public int talkId = -1;// 打开对讲返回的id
	int maxjitter;
	public String deviceId;
	public int deviceChn;
	
	private DatabaseHelper db;// 数据库操作对象
	private DeviceInfo mDeviceInfo;// 当前设备信息
	// private int start_stream_flag = 0;// 打开流的标志
	public AudioTrack track;// 音频操作对象
	// public byte[] audioData ;
	public AudioRecord audioRecord;// 对讲操作对象
	
	
	private MyVideoView videoView;
	ProgressDialog loadingDialog;
	private Message msg;
	// FileOutputStream fos;


	public VideoPlayControl(Activity currentActivity, MyVideoView videoView) {
		this.mContext = currentActivity;
		this.videoView = videoView;
		db = new DatabaseHelper(currentActivity, Constants.DATABASE_NAME);
		loadingDialog = new ProgressDialog(mContext);// 进度条
	}

	/**
	 * 視頻流回調
	 * 
	 * @param errorCode
	 *            JNV_OpenStream或JNV_ReplayStart或JNV_RecOpenFile 函数的返回值.
	 * @param lpBuf
	 *            解码后数据;
	 * @param lSize
	 *            解码后数据长;
	 * @param lWidth
	 *            图像的宽;
	 * @param lHeight
	 *            图像的高;
	 * @param lStamp
	 *            帧的时间戳;
	 * @param lType
	 *            帧的类型,见 AVPDecCBType 说明;
	 * @param lUserParam
	 *            JNV_OpenStream或JNV_ReplayStart或JNV_RecOpenFile 函数中指定的用户参数;
	 * @return
	 */
	public int callbackSetStreamInfo(int lStream, byte[] lpBuf, int lSize, int lWidth, int lHeight, long lStamp,
			int lType, int lUserParam) {
//				errorCode:
//				JNETErrSuccess				= 0,			// 成功
//				JNETErrUnInit				= -1,			// 未初始化
//				JNETErrHandle				= -2,			// 句柄不存在
//				JNETErrParam				= -3,			// 参数错误
//				JNETErrBuffSize				= -4,			// 缓存满
//				JNETErrNoMem				= -5,			// 内存不足
//				JNETErrRecv					= -6,			// 接收错误
//				JNETErrSend					= -7,			// 发送错误
		Log.e(TAG, "callbackSetStreamInfo()lWidth:" + lWidth + 
				   " lHeight:" + lHeight + 
				   " lStream:" + lStream+ 
				   " lSize:" + lSize+ 
			   	   " type:" + lType + 
				   " userparam:" + lUserParam);
		//视频type=8
		if (lType == 8) {// 视频
			if (videoView.videoWidth != lWidth || videoView.videoHeight != lHeight) {
				videoView.is_drawblack = false;
				videoView.videoWidth = lWidth;
				videoView.videoHeight = lHeight;
//				videoView.picBytes=lpBuf;
				videoView.buffer = ByteBuffer.wrap(lpBuf);
				videoView.VideoBit = Bitmap.createBitmap(videoView.videoWidth, videoView.videoHeight, Config.RGB_565);
				videoView.getScaleSize(videoView.getMeasuredWidth(), videoView.getMeasuredHeight());
				videoView.isPlaying = true;
			}
			videoView.postInvalidate();
		} else if (lType == 1) {// 音频
			if (Constants.ISOPEN_AUDIO) {
				LogUtils.i(TAG, "callbackSetStreamInfo() size:" + lSize + " length:" + lpBuf.length);
				writeAudioTrack(lpBuf, lSize);
			}
		} else {// 无

		}
		return 0;
	}

	/**
	 * 打开远程录像回放回調，JNV_RecOpenFile 函数的返回值
	 * 
	 * @param iType
	 * @param lParam1
	 * @param lParam2
	 * @param userParam
	 * @return
	 */
	public int callbackPlayInfo(int iType, long lParam1, long lParam2, int userParam) {
		// iType = 6;
		Log.e(TAG, "callbackSetStreamInfo() type:"+ iType + 
				" lParam1" + lParam1 + 
				" lParam2" + lParam2 +
				" userParam"+userParam);
		msg = myHandler.obtainMessage();
		msg.what = iType;
		msg.sendToTarget();
		return 0;
	}

	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:// 未知
				break;
			case 1:// 错误
				break;
			case 5:// 结束播放
				System.out.println("============结束播放");
				if (Constants.ISOPEN_AUDIO) {// 停止监听
					Constants.ISOPEN_AUDIO = false;
					track.stop();// 停止音频
				}
				mContext.finish();
				break;
			case 6:// 暂停播放
				break;
			case 7:// 继续播放
				break;
			default:
				break;
			}

			super.handleMessage(msg);
		}
	};

	public void initRealPlay(DeviceInfo deviceInfo) {
		Log.e(TAG, "initRealPlay()");
		initAudio();
		initSendTalk();// 初始化对讲
		if (deviceInfo == null) {
			MUtils.commonToast(mContext, R.string.not_playdata);
			return;
		}
		mDeviceInfo = deviceInfo;
		Constants.DERECTION_STATE = AVP_GetMirror();// 获取当前设备的镜像状态
		startStream(deviceInfo.getNewGuId(), deviceInfo.getCurrentChn());// 打开实时流
	}

	/**
	 * 初始化
	 */
	public void initLocalPlay(Intent intent) {
		Log.e(TAG, "initLocalPlay()");
		initAudio();// 初始化音频
		RecordInfo devRecordInfo = (RecordInfo) intent.getSerializableExtra(ReplayActivity.EXTRA_RECORDINFO);// 回放的文件名称
		if (null != devRecordInfo) {// 设备端播放

			replayStreamId = JNVPlayerUtil.JNV_ReplayStart(devRecordInfo.getDeviceId(), 0, devRecordInfo.getChanneId(),
					devRecordInfo.getStartTime(), devRecordInfo.getEndTime(), 0, devRecordInfo.getRecType(), 0, this,
					CALLBACK_STREAM_INFO, 0);

		} else {// 本地回放 deviceInfo==null
			String filePath = intent.getStringExtra(ReplayActivity.EXTRA_FIELPATH);// 回放的文件名称
			// String filePath = Constants.SDCardRoot+"20130608_111847.jnv";//
			// 回放的文件名称
			String record_id = intent.getStringExtra(ReplayActivity.EXTRA_ID);// 回放文件的id
			Log.e(TAG, " filepath:" + filePath + "recordid:" + record_id);
			File mFile = new File(filePath);
			if (!mFile.exists()) {// 文件不存在，删除数据库的信息
				MUtils.commonToast(mContext, R.string.not_findfile);
				db.deleteRecordInfo(record_id);
			}
			recStreamId = JNVPlayerUtil.JNV_RecOpenFile(filePath, this, CALLBACK_STREAM_INFO, "callbackPlayInfo", 0);
		}
	}

	/*-------------------------------------实时视频--------------------------------------*/

	/**
	 * 开始实时视频
	 */
	private void startStream(String deviceId, int deviceChn) {
		LogUtils.getInstance().localLog(TAG, "startStream():" + deviceId + "," + deviceChn);
		Log.e(TAG, "Open Strean:" + deviceId + "," + deviceChn);
		startStreamId = JNVPlayerUtil.JNV_OpenStream(deviceId, deviceChn, 0, 0, this, CALLBACK_STREAM_INFO, 0);
		if (startStreamId < 0) {
			LogUtils.getInstance().localLog(TAG, "++++++++++++startStream FAIL code:" + startStreamId);
		}

		// start_stream_flag = 1;
	}

	/**
	 * 停止实时视频
	 */
	public void stopStream() {
		if (0 > startStreamId) {
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
		JNVPlayerUtil.JNV_N_PtzCtrl(mDeviceInfo.getNewGuId(), mDeviceInfo.getCurrentChn(), iDirection, 0, 0);
	}

	// ----------------------------------------------录像-----------------------------
	/**
	 * 开始录像
	 * 
	 * @param path
	 *            录像的存储路径及文件名
	 */
	public int recordStart(String path) {

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
	public int recordStop() {
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
	 * 本地文件继续播放
	 * 
	 * @param handleId
	 * @param status
	 */
	public void resumeVideo() {
		if (0 > recStreamId) {
			return;
		}
		JNVPlayerUtil.JNV_RecPlayStart(recStreamId);
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
	 * 本地文件播放停止
	 * 
	 * @param handleId
	 * @param status
	 */
	public void stopVideo() {
		Log.e(TAG, "++++++++++++AVP_Native_Stop:" + recStreamId);
		if (0 > recStreamId) {
			return;
		}
		JNVPlayerUtil.JNV_RecCloseFile(recStreamId);
	}

	/**
	 * 设备端录像文件播放停止
	 */
	public void AVP_Native_RecordStop() {
		JNVPlayerUtil.JNV_ReplayStop(replayStreamId);// 停止录像回放
	}

	/**
	 * 设备端录像回放控制
	 * 
	 * @param status
	 */
	public void AVP_Native_RecordCtrl(int status) {
		JNVPlayerUtil.JNV_ReplayCtrl(replayStreamId, status, 1);
	}

	/**
	 * 打开对讲
	 * 
	 * @return
	 */
	public void openTalk() {
		// File file = new File(Constants.SDCardRoot+"test.pcm");
		// 删除录音文件
		// if (file.exists())
		// file.delete();
		// 创建录音文件
		// try {
		// file.createNewFile();
		// } catch (IOException e) {
		// throw new IllegalStateException("Failed to create " +
		// file.toString());
		// }
		// try {
		// fos = new FileOutputStream(file);
		// } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }// 建立一个可存取字节的文件

		// talkId =
		// JNVPlayerUtil.JNV_N_TalkStart(currentDeviceInfo.getGuId(),currentDeviceInfo.getCurrentChn());
		talkId = JNVPlayerUtil.JNV_N_TalkStart(mDeviceInfo.getNewGuId(), mDeviceInfo.getCurrentChn());
		if (0 > talkId) {
			return;
		} else {
			new Thread(talkRunnable).start();
		}
	}

	/**
	 * 关闭对讲
	 * 
	 * @return
	 */
	public boolean closeTalk() {
		if (0 > talkId) {
			return false;
		}
		// try {
		// fos.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		if (-1 < JNVPlayerUtil.JNV_N_TalkStop(talkId)) {
			return true;
		} else {
			return false;
		}
	}

	// ----------------------------镜像管理-------------------------
	/**
	 * 调用接口，进行镜像翻转
	 */
	public int AVP_SetMirror(int MirrorParam) {
		// return
		// JAVNativeUtil.JAV_Native_SetMirror(handleId,currentDeviceInfo.getCurrentChn(),
		// MirrorParam);
		return 1;
	}

	/**
	 * 抓拍，将当前的文件存储在本地
	 * 
	 * @param bitmap
	 * @throws IOException
	 */
	public Bitmap CapturePicture() {
		String imageFilePath = MUtils.getCurrentFilePath(Constants.IMAGE_PATH + "/", mDeviceInfo);// 文件目录
		File devFile = new File(imageFilePath);
		if (!devFile.exists()) {// 目录不存在
			devFile.mkdirs();// 创建相应的文件夹
		}
		String times = MUtils.getCurrentDateTime(DateUtil.PIC_NAME_FORMAT);// 当前时间
		File f = new File(imageFilePath + times + ".jpg");// 文件路径
		Bitmap bitmap=videoView.saveBitmap(f);
		if (bitmap!=null) {// 抓拍成功
			MUtils.toast(mContext, mContext.getString(R.string.capture_success));
		}
		return bitmap;
	}

	// ----------------------------音频管理-------------------------
	/**
	 * 初始化音频
	 */
	private void initAudio() {
		maxjitter = AudioTrack.getMinBufferSize(Constants.AUDIO_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		LogUtils.getInstance().localLog(TAG, "+++initAudio++++++++++初始化音频:" + maxjitter);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.AUDIO_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, maxjitter * 6, AudioTrack.MODE_STREAM);
		// audioData = new byte[Constants.FRAME_SIZE];// 接收音频数据.
	}

	/**
	 * 写入音频
	 */
	private void writeAudioTrack(byte[] trackData, int dataLenght) {
		track.write(trackData, 0, dataLenght);
	}

	// ----------------------------对讲管理-------------------------
	/**
	 * 初始化录音
	 */
	private void initSendTalk() {
		int min = AudioRecord.getMinBufferSize(Constants.AUDIO_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		Log.e(TAG, "++++initSendTalk++++++++" + min);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, // the
																		// recording
																		// source
				Constants.AUDIO_RATE, // 采样频率，一般为8000hz/s
				AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, min);
		encodeData = new byte[Constants.FRAME_SIZE];// 存储音频数据
	}

	/**
	 * 读取本地音频
	 */
	private int readAudioRecord() {
		return audioRecord.read(encodeData, 0, Constants.FRAME_SIZE);
	}

	/**
	 * 调用接口，发送对讲数据
	 * 
	 * @param isOpenAudio
	 */
	private void AVP_SendTalk_Ctrl(byte[] pBuff, int size) {
		JNVPlayerUtil.JNV_N_TalkSend(talkId, pBuff, size);

	}

	/**
	 * 对讲线程
	 */
	public Runnable talkRunnable = new Runnable() {// 开启对讲

		@Override
		public void run() {

			LogUtils.i(TAG, "+++++++++++++talkThreadID：" + Thread.currentThread().getId() + "+++++++++name："
					+ Thread.currentThread().getName());
			while (Constants.ISOPEN_TALK) {// 打开对讲成功
				int size = readAudioRecord();
				// LogUtils.i(TAG, "=========>size:"+size);
				if (size <= 0)
					continue;

				// try {
				// fos.write(encodeData);
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				AVP_SendTalk_Ctrl(encodeData, size);
				size = 0;
			}
		}
	};

	/**
	 * 调用接口，获取镜像翻转
	 * 
	 * @param MirrorParam
	 * @return
	 */
	private boolean AVP_GetMirror() {
		/*
		 * if (0 ==
		 * JAVNativeUtil.JAV_Native_SetMirror(handleId,currentDeviceInfo.
		 * getCurrentChn(), Constants.MIRROE_STATUS)) {// 正常 return true; } else
		 * {// 反转
		 * videoActivity.mirrorButton.setBackgroundDrawable(currentContext.
		 * getResources().getDrawable(R.drawable.close)); return false; }
		 */
		return true;
	}

}
