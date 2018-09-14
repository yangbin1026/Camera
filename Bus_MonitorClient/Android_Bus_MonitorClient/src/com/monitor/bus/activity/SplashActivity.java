package com.monitor.bus.activity;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.monitor.bus.Constants;
import com.monitor.bus.utils.MUtils;

//SplashActivity
public class SplashActivity extends Activity {

	private static final int END = 0;
	private static final int DOING = 1;
	private boolean flag = true;
	private int state;
	private int alpha = 0;
	ImageView imageView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// getWindow().getAttributes().windowAnimations =
		// android.R.anim.fade_in;
		this.overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);

		setContentView(R.layout.logo);
		if (Constants.HAS_LOGO) {
			imageView = (ImageView) findViewById(R.id.logo);
			// 语言环境不是中国
			/*if (!MUtils.isChina(this.getBaseContext())) {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.logo4e));
			}*/
			state = DOING;
			alpha = 0;
			imageView.setAlpha(alpha);
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(state);
				}

			}, 5);
		} else {
			StartIntent();
		}
	}
	@Override
	public void onBackPressed() {
		System.exit(0);
		super.onBackPressed();
	}

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (flag) {
				if (msg.what == END) {
					flag = false;
					StartIntent();
				} else if (msg.what == DOING) {

					alpha += 10;
					if (alpha >= 255) {
						alpha = 255;
						state = END;
						new Timer().schedule(new TimerTask() {

							@Override
							public void run() {
								handler.sendEmptyMessage(state);
							}

						}, 1000);
					} else {
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								handler.sendEmptyMessage(state);
							}
						}, 2);
					}
					imageView.setAlpha(alpha);
				}
			}

		}
	};

	/**
	 * 启动下一个界面
	 */
	public void StartIntent() {
		Intent intent = new Intent();
		intent.setClass(SplashActivity.this, LoginActivity.class);
		startActivity(intent);
		this.overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		finish();
	}

}