package com.monitor.bus.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.jniUtil.JNVPlayerUtil;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.bean.LoginInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.control.LoginEventControl;
import com.monitor.bus.control.LoginEventControl.LoginStatusCallBack;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.SPUtils;
import com.monitor.bus.view.MyEditText;
import com.monitor.bus.view.dialog.DateUtil;
import com.monitor.bus.view.dialog.ShapeLoadingDialog.ShapeLoadingDialog;

/**
 * 用户登陆
 * 
 */
public class LoginActivity extends Activity implements android.view.View.OnClickListener, OnCheckedChangeListener {
	private static String TAG = "LoginActivity";

	private MyEditText et_userName;// 用户名
	private MyEditText et_password;// 密码
	private MyEditText et_login_port;// 端口
	private MyEditText et_login_address;// 地址
	private Button btn_login;
	private CheckBox cb_remenber, cb_autoLogin;
	private ShapeLoadingDialog dialog;

	private Handler handler = new Handler();
	private LoginEventControl loginControl;// 登陆回调类
	private Context mContext;
	private LoginInfo info;
	private boolean isResume;

	private int currentCode = 0;// 旧版本号
	private int newVerCode;// 新版本号
	private String newVerName, newAppName;// 新版本名称,新应用程序名称
	private ProgressDialog pbar;// 进度条对话框

	static {
		try {
			System.loadLibrary("ffmpeg");
			System.loadLibrary("JNVCommon_jni");
			System.loadLibrary("JNVPlayer_jni");
		} catch (UnsatisfiedLinkError e) {
			LogUtils.getInstance().localLog(TAG, "load库文件异常:" + e.getMessage(), LogUtils.LOG_NAME);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		mContext = this;
		JNVPlayerUtil.JNV_Init(Constants.SCREEN_COUNT);// 初始化so
		initView();
		checkVersion();
		initData();
	}
	@Override
	protected void onResume() {
		isResume=true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		isResume=false;
		super.onPause();
	}
	private void initView() {
		et_userName = (MyEditText) findViewById(R.id.userName);
		et_password = (MyEditText) findViewById(R.id.login_password);
		et_login_port = (MyEditText) findViewById(R.id.login_port);
		et_login_address = (MyEditText) findViewById(R.id.login_address);

		et_userName.setImage(R.drawable.account);
		et_password.setImage(R.drawable.password);
		et_login_port.setImage(R.drawable.port);
		et_login_address.setImage(R.drawable.ip);

		cb_remenber = (CheckBox) findViewById(R.id.remember);
		cb_autoLogin = (CheckBox) findViewById(R.id.cb_auto_login);

		cb_remenber.setOnCheckedChangeListener(this);
		cb_autoLogin.setOnCheckedChangeListener(this);
		btn_login = (Button) findViewById(R.id.login);
		btn_login.setOnClickListener(this);
	}

	private void initData() {
		loginControl = new LoginEventControl(getApplicationContext());
		loginControl.setLoginStatusListener(new LoginStatusCallBack() {
			@Override
			public void onStatus(int statu) {
				switch (statu) {
				case CALLBACKFLAG.LOGIN_SUCCESS:
					dissmissDialog();
					Intent intent = new Intent();
					intent.setClass(mContext, HomeActivity.class);
					mContext.startActivity(intent);
					((Activity) mContext).finish();
					break;
				case CALLBACKFLAG.LOGIN_ING:
					showLoginDialog();
					break;
				case CALLBACKFLAG.LONGIN_FAILD:
					dissmissDialog();
					MUtils.toast(mContext, mContext.getString(R.string.loginFailed));
					break;

				default:
					break;
				}
			}
		});
		boolean savePwd = SPUtils.getBoolean(mContext, SPUtils.KEY_REMEMBER_USERINFO, true);
		boolean autoLogin = SPUtils.getBoolean(mContext, SPUtils.KEY_AUTO_LOGIN, false);

		if (savePwd) {
			cb_remenber.setChecked(true);
		}
		if (autoLogin) {
			cb_autoLogin.setChecked(true);
		}
		info = SPUtils.getLoginInfo(this);

		et_userName.setEditText(info.getUserName());
		et_password.setEditText(info.getPassWord());
		et_login_address.setEditText(info.getIp());
		et_login_port.setEditText("" + info.getPort());

		et_userName.setEditFocus();
		et_userName.setEditTextType();
		et_password.setEditPasswordType();
		et_login_port.setEditNumberType();
		et_login_address.setIpConfigType();

		if (LogUtils.Debug) {
//			 et_userName.setEditText("hswl");
//			 et_password.setEditText("000000");
//			et_userName.setEditText("123");
//			et_password.setEditText("123");
			et_login_port.setEditText("6008");
			et_login_address.setEditText("183.61.171.28");
		}
		if (autoLogin) {
			login(info);
		}
	}

	private void showLoginDialog() {
		if(!isResume){
			return;
		}
		dialog = new ShapeLoadingDialog.Builder(this).cancelable(false).canceledOnTouchOutside(false)
				.loadText(R.string.logining).build();
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	private void dissmissDialog() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (dialog != null && dialog.isShowing()) {
		} else {
			JNVPlayerUtil.JNV_UnInit();
		}

	}

	/* 登陆按钮 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			String userName = et_userName.getEditText();
			String password = et_password.getEditText();
			String ip = et_login_address.getEditText();
			int port = Integer.parseInt(et_login_port.getEditText());
			info = new LoginInfo(userName, port, password, ip);
			if (cb_remenber.isChecked()) {
				SPUtils.saveLoginInfo(this, info);
			}
			login(info);
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		switch (arg0.getId()) {
		case R.id.remember:
			SPUtils.saveBoolean(this, SPUtils.KEY_REMEMBER_USERINFO, arg1);
			break;
		case R.id.cb_auto_login:
			SPUtils.saveBoolean(this, SPUtils.KEY_AUTO_LOGIN, arg1);
			break;

		default:
			break;
		}

	}

	/**
	 * 进行版本的比较，提示是否更新当前的应用
	 */
	private void checkToUpdate() {
		LogUtils.i(".....................", "checkToUpdate()" + "....");
		if (getServerVersion()) {
			try {
				currentCode = MUtils.getVerCode(this);
				if (newVerCode > currentCode) {
					showUpdateDialog();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 显示更新提示框
	 */
	private void showUpdateDialog() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本：");
		sb.append(MUtils.getVerName(this));
		sb.append("VerCode:");
		sb.append(MUtils.getVerCode(this));
		sb.append("\n");
		sb.append("发现新版本：");
		sb.append(newVerName);
		sb.append("NewVerCode:");
		sb.append(newVerCode);
		sb.append("\n");
		sb.append("是否更新?");
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setCancelable(false);
		dialog.setTitle("软件更新").setMessage(sb.toString())
				.setPositiveButton("更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showProgressBar();// 更新当前版本
					}
				}).setNegativeButton("暂时不更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		dialog.create().show();
	}

	/**
	 * 下载最新版本进度条
	 */
	private void showProgressBar() {
		pbar = new ProgressDialog(this);
		pbar.setTitle("正在下载");
		pbar.setMessage("请稍候...");
		pbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pbar.setCanceledOnTouchOutside(false);
		downAppFile(Constants.SERVER_IP + newAppName);
	}

	/**
	 * 下载更新的APK文件
	 */
	private void downAppFile(final String url) {
		pbar.show();
		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					long length = entity.getContentLength();
					LogUtils.i("------------", "length : " + (int) length);
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is == null) {
						throw new RuntimeException("inputStream is null");
					}
					File file = new File(Environment.getExternalStorageDirectory() + "/", newAppName);
					LogUtils.i("==============", "file : " + file.getAbsolutePath().toString());
					fileOutputStream = new FileOutputStream(file);
					/*
					 * fileOutputStream =
					 * LoginActivity.this.openFileOutput(file.toString(),
					 * Context.MODE_WORLD_READABLE);
					 */
					byte[] buf = new byte[1024];
					int ch = -1;
					do {
						ch = is.read(buf);
						if (ch <= 0) {
							break;
						}
						fileOutputStream.write(buf, 0, ch);
					} while (true);
					is.close();
					fileOutputStream.close();
					installNewApkDialog();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 下载完成时要将进度条对话框取消并进行是否安装新应用的提示
	 */
	private void installNewApkDialog() {

		handler.post(new Runnable() {

			@Override
			public void run() {
				pbar.cancel();
				AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
				dialog.setCancelable(false);
				dialog.setTitle("下载完成").setMessage("是否安装新的应用")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						installNewApk();

						finish();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						/* finish(); */
					}
				}).create().show();
			}
		});
	}

	/**
	 * 安装新的应用程序
	 */
	private void installNewApk() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/", newAppName)),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	/**
	 * 将从服务器version.json获得的字符串解析出我们需要的版本信息
	 */
	private boolean getServerVersion() {
		try {
			LogUtils.i("--------------", "ip:" + Constants.SERVER_IP);
			String newVerJSON = MUtils.getUpdateVerJSON(Constants.SERVER_IP + "version_jilian.json");
			LogUtils.i("--------------", "newVerJSON:" + newVerJSON);
			JSONArray jsonArray = new JSONArray(newVerJSON);
			if (jsonArray.length() > 0) {
				JSONObject obj = jsonArray.getJSONObject(0);
				LogUtils.i("--------------", "obj:" + obj.toString());
				try {
					newVerCode = Integer.parseInt(obj.getString("verCode"));
					newVerName = obj.getString("verName");
					newAppName = obj.getString("apkName");
				} catch (Exception e) {
					newVerCode = -1;
					newVerName = "";
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 登录
	 */
	private void login(LoginInfo info) {
		InetAddress iAdd;
		String ipAdd = null;
		if (MUtils.hasUselessString(info.getUserName(), info.getPassWord(), info.getIp()) || info.getPort() == 0) {
			LogUtils.getInstance().localLog(TAG, "login Info Error:" + info.toString());
			MUtils.toast(this, "请填写完整信息！");
			return;
		}
		if (!MUtils.isConnect(this)) {
			MUtils.toast(this, getString(R.string.network_error));
			return;
		}
		try {
			// 解析域名的IP地址
			iAdd = InetAddress.getByName(info.getIp());
			// 得到字符串形式的IP地址
			ipAdd = iAdd.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		Pattern pattern = Pattern.compile(
				"^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$");
		Matcher matcher = pattern.matcher(ipAdd);
		if (!matcher.matches()) {
			MUtils.commonToast(this, R.string.ipFailed);
			return;
		}
		// 登录
		LogUtils.getInstance().localLog(TAG, "login Info:" + info.toString());
		JNVPlayerUtil.JNV_N_Login(info.getIp(), info.getPort(), info.getUserName(), info.getPassWord(), 30,
				loginControl, "callbackLogin", 0);
	}

	private void checkVersion() {
		String today = DateUtil.getTodayDateString(DateUtil.REPLAY_SHOW_FORMAT);
		if (DateUtil.getTimeMails(today, DateUtil.REPLAY_SHOW_FORMAT) > DateUtil.getTimeMails("2018-04-01",
				DateUtil.REPLAY_SHOW_FORMAT)) {
			MUtils.toast(mContext, "请安装最新的版本");
			finish();
		}
	}

}
