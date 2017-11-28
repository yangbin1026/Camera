package com.monitor.bus.activity.fragment;

import com.monitor.bus.activity.LoginActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.LoginInfo;
import com.monitor.bus.service.CurrentVersionInfo;
import com.monitor.bus.utils.SPUtils;
import com.monitor.bus.view.SwitchButton;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class SettingFragment extends BaseFragment implements View.OnClickListener,OnCheckedChangeListener{
	private static final String TAG = "SettingActivity";
	View view;
	Button bt_change, bt_exit, bt_saveurl;
	
	
	Button bt_logout;
	SwitchButton sb_autologin,sb_local,sb_gps;
	TextView tv_version,tv_showmode,tv_username;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_setting, container, false);
		setTitle();
		initView();
		initData();
		return view;
	}

	private void setTitle() {
		TextView title= (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.setting));
	}
	private void initData() {
		try {
			String version=CurrentVersionInfo.getVerName(getContext());
			tv_version.setText(version);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoginInfo loginInfo=SPUtils.getLoginInfo(getContext());
		tv_username.setText(loginInfo.getUserName());
		
		boolean autoLogin=SPUtils.getBoolean(getContext(), SPUtils.KEY_AUTO_LOGIN, false);
		boolean gps=SPUtils.getBoolean(getContext(), SPUtils.KEY_GSP, false);
		boolean local=SPUtils.getBoolean(getContext(), SPUtils.KEY_LOCAL, false);
		sb_autologin.setChecked(autoLogin);
		sb_gps.setChecked(gps);
		sb_local.setChecked(local);
	}

	private void initView() {
		bt_logout=(Button) view.findViewById(R.id.bt_logout);
		bt_logout.setOnClickListener(this);
		tv_version=(TextView) view.findViewById(R.id.tv_version);
		tv_username=(TextView) view.findViewById(R.id.tv_username);
		tv_showmode=(TextView) view.findViewById(R.id.tv_showmode);
		sb_autologin=(SwitchButton) view.findViewById(R.id.sb_autologin);
		sb_gps=(SwitchButton) view.findViewById(R.id.sb_gps);
		sb_local=(SwitchButton) view.findViewById(R.id.sb_localpaser);
		sb_autologin.setOnCheckedChangeListener(this);
		sb_gps.setOnCheckedChangeListener(this);
		sb_local.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.bt_logout:
			//登出
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_AUTO_LOGIN, false);
			Intent intent=new Intent(getContext(),LoginActivity.class);
			getContext().startActivity(intent);
			getActivity().finish();
			break;
		default:
			break;
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		switch (arg0.getId()) {
		case R.id.sb_autologin:
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_AUTO_LOGIN, arg1);
			break;
		case R.id.sb_gps:
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_GSP, arg1);
			break;
		case R.id.sb_localpaser:
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_LOCAL, arg1);
			break;

		default:
			break;
		}
	}

}
