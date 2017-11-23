package com.monitor.bus.utils;

import java.math.BigDecimal;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Vibrator;
import android.widget.Toast;

public class MyUtils {
	/**
	 * 获取旋转后的位图
	 */
	public static Bitmap getRotatedBmp(Bitmap bmp, int angle) {
		Matrix myMatrix = new Matrix();
		myMatrix.reset();
		myMatrix.postRotate(angle);
		Bitmap dstBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
				bmp.getHeight(), myMatrix, true);
		return dstBitmap;
	}
	
	public static void showToast(Context context,String msg){
		Toast.makeText(context, ""+msg, Toast.LENGTH_LONG).show();
	}
	
	// double类型保留6位小数
		public static double convertDoubleType6(double d) {
			BigDecimal b = new BigDecimal(d);
			return b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		


		
		public static void Vibrate(final Activity activity, long milliseconds){
			Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
			vib.vibrate(milliseconds);
		}
		public static void Vibrate(final Activity activity,long[] pattern,boolean isRepeat){
			Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
			vib.vibrate(pattern, isRepeat? 1: -1);
		}

}
