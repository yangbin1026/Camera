package com.monitor.bus.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.LogUtils;

/**
 * 自定义视频回放窗口类
 * 
 * 
 */
public class MyVideoView extends ImageView {
	private static String TAG = "MyVideoView";
	private Paint mPaint;// 画笔对象
	public ByteBuffer buffer; // 内存数组包
	
	public Bitmap VideoBit; // 图像
	private Bitmap bitmap;// 转换后的图像
	
	public int posWidth;// 当前视频显示的位置----距view的宽
	public int posHeight;// 当前视频显示的位置---距view的高

	public boolean isPlaying = false; // 播放显示标志
	public boolean is_drawblack = false;// 是否将画布画黑 true :画黑
	public boolean shoudDrawCircle = false;// 是否画实心圆标志

	public int videoHeight; // 视频原高度
	public int videoWidth;// 视频原宽度
	public int displayWidth; // 要显示的宽度
	public int displayHeight; // 要显示的高度

	public MyVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
	}

	/* 调节摄像头远近功能 , 放大 */
	public void videoFang(View view) {
		displayHeight += 90;
		displayWidth += 90;
	}

	/* 调节摄像头远近功能 , 缩小 */
	public void videoSuo(View view) throws IOException {
		if (displayHeight > 0 && displayWidth > 0) {
			displayHeight -= 90;
			displayWidth -= 90;
		}
	}
	

	/**
	 * 判断是否正常播放
	 * 
	 */
	public boolean isNormalPlay() {
		if (is_drawblack || !isPlaying) {
			return false;
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		LogUtils.i(TAG,"onSizeChanged()W:" + w + " h:" + h + " oldw:" + oldw + " oldh:" + oldh);
		getScaleSize(w, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * 显示视频流
	 * 
	 * @param canvas
	 * @throws IOException
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		try {
			drawStream(canvas);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void drawStream(Canvas canvas) throws IOException {
		if (isPlaying) {// 显示图像
			VideoBit.copyPixelsFromBuffer(buffer.position(0));
			LogUtils.d(TAG, "drawStream()VideoBit.Height:"+VideoBit.getHeight()+"Width:"+VideoBit.getWidth());
			bitmap = Bitmap.createScaledBitmap(VideoBit, displayWidth, displayHeight, true);
			LogUtils.d(TAG, "drawStream()bitmap.Height:"+bitmap.getHeight()+"Width:"+bitmap.getWidth());
			canvas.drawBitmap(bitmap, posWidth, posHeight, mPaint);
			if (shoudDrawCircle == true) {// 画红圆
				mPaint.setColor(Color.RED);
				canvas.drawCircle(getWidth() - 40, 50, 30, mPaint);
			}

			if (is_drawblack == true) {
				canvas.drawColor(Color.BLACK);// 将屏幕画黑
			}
		}

	}

	// ----------------------------抓拍-------------------------

	/**
	 * 保存当前抓拍图片
	 * 
	 * @param currentFile
	 *            保存的路径
	 */
	public boolean saveBitmap(File currentFile) {
		FileOutputStream out;
		boolean temp = false;
		try {
			out = new FileOutputStream(currentFile);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)) {// 抓拍成功
				temp = true;
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return temp;
	}

	/**
	 * 获取当前视频显示的高度和宽度
	 * 
	 * @param viewWidth
	 *            view宽度
	 * @param viewHeight
	 *            view高度
	 */
	public void getScaleSize(int viewWidth, int viewHeight) {
		// 横屏时候用一下算法;
		int scaleWidth = videoWidth;
		int scaleHeight = videoHeight;
		double ImgRation = scaleWidth / (scaleHeight * 1.0);
		scaleWidth = viewWidth;
		scaleHeight = (int) (scaleWidth / ImgRation);
		if (scaleHeight > viewHeight) {
			scaleHeight = viewHeight;
			scaleWidth = (int) (scaleHeight * ImgRation);
		}
		LogUtils.i(TAG, "getScaleSize() scaleWidth:" + scaleWidth + " scaleHeight:" + scaleHeight);
		displayWidth = scaleWidth;
		displayHeight = scaleHeight;

		posWidth = (viewWidth - displayWidth) / 2;
		posHeight = (viewHeight - displayHeight) / 2;
	}

}
