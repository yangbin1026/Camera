package com.monitor.bus.view;

import java.util.Random;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;

public class ZanBean {

	/** 心的当前坐标 */
	public Point point;
	/** 移动动画 */
	private ValueAnimator moveAnim;
	/** 放大动画 */
	private ValueAnimator zoomAnim;
	/** 透明度 */
	public int alpha = 255;//
	/** 心图 */
	private Bitmap bitmap;
	/** 绘制bitmap的矩阵 用来做缩放和移动的 */
	private Matrix matrix = new Matrix();
	/** 缩放系数 */
	private float sf = 0;
	/** 产生随机数 */
	private Random random;
	public boolean isEnd = false;// 是否结束

	public ZanBean(Context context, int resId, MySurfaceView zanView) {
		random = new Random();
		bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
		init(new Point(zanView.getWidth() / 2, zanView.getHeight()),
				new Point((random.nextInt(zanView.getWidth())), 0));
	}

	public ZanBean(Context context, Bitmap bitmap, MySurfaceView zanView) {
		random = new Random();
		this.bitmap = bitmap;
		init(new Point(zanView.getWidth() / 2, zanView.getHeight()),
				new Point((random.nextInt(zanView.getWidth())), 0));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void init(final Point startPoint, Point endPoint) {
		moveAnim = ValueAnimator.ofObject(
				new BezierEvaluator(
						new Point(random.nextInt(startPoint.x * 2), Math.abs(endPoint.y - startPoint.y) / 2)),
				startPoint, endPoint);
		moveAnim.setDuration(2500);
		moveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				point = (Point) animation.getAnimatedValue();
				alpha = (int) ((float) point.y / (float) startPoint.y * 255);
			}
		});
		moveAnim.start();
		zoomAnim = ValueAnimator.ofFloat(0, 1f).setDuration(700);
		zoomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Float f = (Float) animation.getAnimatedValue();
				sf = f.floatValue();
			}
		});
		zoomAnim.start();
	}

	public void pause() {
		if (moveAnim != null && moveAnim.isRunning()) {
			moveAnim.pause();
		}
		if (zoomAnim != null && zoomAnim.isRunning()) {
			zoomAnim.pause();
		}
	}

	public void resume() {
		if (moveAnim != null && moveAnim.isPaused()) {
			moveAnim.resume();
		}
		if (zoomAnim != null && zoomAnim.isPaused()) {
			zoomAnim.resume();
		}
	}

	/** 主要绘制函数 */
	public void draw(Canvas canvas, Paint p) {
		if (bitmap != null && alpha > 0) {
			p.setAlpha(alpha);
			matrix.setScale(sf, sf, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
			matrix.postTranslate(point.x - bitmap.getWidth() / 2, point.y - bitmap.getHeight() / 2);
			canvas.drawBitmap(bitmap, matrix, p);
		} else {
			isEnd = true;
		}
	}

	/**
	 * 二次贝塞尔曲线
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private class BezierEvaluator implements TypeEvaluator {

		private Point centerPoint;

		public BezierEvaluator(Point centerPoint) {
			this.centerPoint = centerPoint;
		}


		@Override
		public Object evaluate(float t, Object arg1, Object arg2) {
			Point startValue=(Point)arg1;
			Point endValue=(Point)arg2;
			int x = (int) ((1 - t) * (1 - t) * startValue.x + 2 * t * (1 - t) * centerPoint.x + t * t * endValue.x);
			int y = (int) ((1 - t) * (1 - t) * startValue.y + 2 * t * (1 - t) * centerPoint.y + t * t * endValue.y);
			return null;
		}
	}}
//Zanbean

//	用来记录心的轨迹 和 绘制的工作 这里简单介绍一下init方法 @TargetApi(Build.VERSION_CODES.HONEYCOMB)

//	private void init(final Point startPoint, Point endPoint){
//		moveAnim =ValueAnimator.ofObject(new BezierEvaluator(new Point(random.nextInt(startPoint.x*2),Math.abs(endPoint.y-startPoint.y)/2)),startPoint,endPoint);
//		moveAnim.setDuration(2500);
//		moveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//		@Override
//		public void onAnimationUpdate(ValueAnimator animation) {
//		point= (Point) animation.getAnimatedValue();
//		alpha= (int) ((float)point.y/(float)startPoint.y*255);
//		}
//		});
//		moveAnim.start();
//		zoomAnim =ValueAnimator.ofFloat(0,1f).setDuration(700);
//		zoomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//		@Override
//		public void onAnimationUpdate(ValueAnimator animation) {
//		Float f= (Float) animation.getAnimatedValue();
//		sf=f.floatValue();
//		}
//		});
//		zoomAnim.start();
//		}