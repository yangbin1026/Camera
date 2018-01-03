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

import com.monitor.bus.utils.MUtils;
import com.monitor.bus.consts.Constants;

//SplashActivity
public class SplashActivity extends Activity {

	private static final int END = 0;
	private static final int DOING = 1;
	private boolean flag = true;
	private int state;
	private int alpha = 0;
	ImageView imageView;
	private SimpleDateFormat format;// 格式日期
	private java.util.Date curDate;// 当前时间
	private String deadline_time = "2019-12-31";// 截止日期
	private String tip_time = "2019-12-24";// 提醒日期
	private int result;//日期比较结果
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		result = -1;
		try { 
		format = new SimpleDateFormat("yyyy-MM-dd");
		curDate = new java.util.Date(System.currentTimeMillis());
		java.util.Date d = format.parse(deadline_time);// 截止日期
		result = d.compareTo(curDate);// 截止日期	> 当前日期 
		} 
		catch (ParseException e) { 
			// TODO
			e.printStackTrace();
		}
		if (result < 0) {
			String strMsg = getResources().getString(R.string.software_expired);;
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setCancelable(false);
			dialog.setTitle(R.string.warm_tip)
					.setMessage(strMsg)
					.setPositiveButton(R.string.btnSure,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									finish();
								}
							}).create().show();
			return;
		}
		else{
			//日期提醒
			int nTipResult = -1;
			try { 
			java.util.Date dd = format.parse(tip_time);	// 提醒日期
			nTipResult = dd.compareTo(curDate);				// 提醒日期 	> 当前日期
			}
			catch (ParseException e) { 
				// TODO
				e.printStackTrace();
			}
			if (nTipResult < 0) {
				//软件%s过期,请升级最新版本!
				String strTip = String.format("%s %s %s",getResources().getString(R.string.software), deadline_time,getResources().getString(R.string.software_expired_tip));
				Toast.makeText(this, strTip, Toast.LENGTH_LONG).show();
			}
		}
		
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
