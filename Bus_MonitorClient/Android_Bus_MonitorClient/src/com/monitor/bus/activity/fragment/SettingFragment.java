package com.monitor.bus.activity.fragment;

import com.monitor.bus.activity.R;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.SPUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingFragment extends BaseFragment implements View.OnClickListener {

	private boolean isCurDefBaiduMap;
	private String newVerName, newAppName;// 新版本名称,新应用程序名称
	private int newVerCode;// 新版本号
	private int currentCode = 0;// 旧版本号
	private ProgressDialog pbar;// 进度条对话框
	private Handler handler = new Handler();

	private static final String TAG = "SettingActivity";
	private EditText storeEditText, serviceEditText;
	private TextView defMapTextView;
	private CheckBox cbIsGpsCorrection = null;
	private boolean IsGpsCorrection = false;
	View view;

	Button bt_change, bt_exit, bt_saveurl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_setting, container, false);
		findView();
		initPreferences();
		return view;
	}

	private void initPreferences() {
		String serviceUrl = SPUtils.getString((Context) getActivity(), Constants.SERVICE_URL_KEY,
				Constants.SERVICE_URL);

		String storeUrl = SPUtils.getString((Context) getActivity(), Constants.STORE_URL_KEY, Constants.STORE_URL);

		serviceEditText.setText(serviceUrl);
		storeEditText.setText(storeUrl);

		isCurDefBaiduMap = SPUtils.getBoolean((Context) getActivity(), Constants.DEFAULT_MAP_KEY,
				Constants.IS_DEFAULT_BAIDU_MAP);

		defMapTextView.setText(isCurDefBaiduMap ? R.string.baiduMap : R.string.googleMap);

		IsGpsCorrection = SPUtils.getBoolean((Context) getActivity(), Constants.GOOGLE_GPS_CORRRECTION,
				Constants.IS_GOOGLE_GPS_CORRRECTION);
		if (cbIsGpsCorrection != null) {
			cbIsGpsCorrection.setChecked(IsGpsCorrection);
			cbIsGpsCorrection.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					IsGpsCorrection = isChecked;
					SPUtils.saveBooleanPreferences((Context) getActivity(), Constants.GOOGLE_GPS_CORRRECTION,
							isChecked);
				}
			});
		}
	}

	private void findView() {
		storeEditText = (EditText) view.findViewById(R.id.storeUrl);
		serviceEditText = (EditText) view.findViewById(R.id.serviceUrl);
		defMapTextView = (TextView) view.findViewById(R.id.defMapText);
		cbIsGpsCorrection = (CheckBox) view.findViewById(R.id.checkBox_googlegpsCorrection);
		bt_change = (Button) view.findViewById(R.id.changeButton);
		bt_change.setOnClickListener(this);
		bt_exit = (Button) view.findViewById(R.id.exitButton);
		bt_exit.setOnClickListener(this);
		bt_saveurl = (Button) view.findViewById(R.id.storeButton);
		bt_saveurl.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.changeButton:
			Log.i(TAG, "改变"+view);
			isCurDefBaiduMap = !isCurDefBaiduMap;
			SPUtils.setBoolean((Context)getActivity(),Constants.DEFAULT_MAP_KEY, isCurDefBaiduMap);
			if(isCurDefBaiduMap){
				defMapTextView.setText(R.string.baiduMap);
			}else{
				defMapTextView.setText(R.string.googleMap);
			}
			break;
		case R.id.exitButton:
			break;
		case R.id.storeButton:

			break;

		default:
			break;
		}

	}

}
