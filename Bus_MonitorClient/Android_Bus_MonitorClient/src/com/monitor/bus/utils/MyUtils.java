package com.monitor.bus.utils;

import java.math.BigDecimal;

import android.graphics.Bitmap;
import android.graphics.Matrix;

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
	
	// double类型保留6位小数
		public static double convertDoubleType6(double d) {
			BigDecimal b = new BigDecimal(d);
			return b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		}

}
