package com.monitor.bus.activity.fragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.MyUtil;
import com.monitor.bus.activity.DevRecordListActivity;
import com.monitor.bus.activity.LocalRecordActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.RecordQueryActivity;
import com.monitor.bus.adapter.SpinnerBusAdapter;
import com.monitor.bus.bean.DeviceManager;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.DeviceInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RePlayFragment extends BaseFragment implements View.OnClickListener{
	private RadioButton rb_native;// 本地
	private RadioButton rb_remote;// 设备端
	private Button bt_queryDate;
	private Button bt_start_time;
	private Button bt_end_time;
	private Spinner sp_queryDevList;
	private Spinner sp_queryDevChnCount;
	private Spinner sp_queryRecordType;

	private Calendar calendar;
	private SimpleDateFormat formater;
	private List<DeviceInfo> deviceList;
	private List<String> listItems;
	private String guId;
	View view;
	
	TextView tv_select_device,tv_file_local,tv_channel,tv_type,tv_select_time,tv_start_time,tv_end_time;
	RelativeLayout rl_1,rl_2,rl_3,rl_4,rl_5,rl_6,rl_7;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_replay, container, false);
		deviceList=DeviceManager.getInstance().getOnlineDevice();
		
		setTitle();
		initView();
		return view;
	}

	private void setTitle() {
		TextView title = (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.alarm_list));
	}

	void initView() {
		tv_channel=(TextView) view.findViewById(R.id.tv_channel);
		tv_end_time=(TextView) view.findViewById(R.id.tv_end_time);
		tv_file_local=(TextView) view.findViewById(R.id.tv_find_local);
		tv_select_device=(TextView) view.findViewById(R.id.tv_select_device);
		tv_select_time=(TextView) view.findViewById(R.id.tv_select_time);
		tv_start_time=(TextView) view.findViewById(R.id.tv_start_time);
		tv_type=(TextView) view.findViewById(R.id.tv_type);
		rl_1=(RelativeLayout) view.findViewById(R.id.rl_1);
		rl_2=(RelativeLayout) view.findViewById(R.id.rl_2);
		rl_3=(RelativeLayout) view.findViewById(R.id.rl_3);
		rl_4=(RelativeLayout) view.findViewById(R.id.rl_4);
		rl_5=(RelativeLayout) view.findViewById(R.id.rl_5);
		rl_6=(RelativeLayout) view.findViewById(R.id.rl_6);
		rl_7=(RelativeLayout) view.findViewById(R.id.rl_7);
		rl_1.setOnClickListener(this);
		rl_2.setOnClickListener(this);
		rl_3.setOnClickListener(this);
		rl_4.setOnClickListener(this);
		rl_5.setOnClickListener(this);
		rl_6.setOnClickListener(this);
		rl_7.setOnClickListener(this);
		
		
		
		calendar = Calendar.getInstance();
		formater = new SimpleDateFormat("yyyy-MM-dd");


		//远程录像
		rb_remote.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					sp_queryDevList = (Spinner) view.findViewById(R.id.queryDevList);

					SpinnerBusAdapter queryDevListAdapter = new SpinnerBusAdapter(getContext(), R.layout.spinner_item,
							deviceList);
					queryDevListAdapter.setDropDownViewResource(R.layout.spinner_checkview);
					sp_queryDevList.setAdapter(queryDevListAdapter);

					sp_queryDevList.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0, View arg1, int location, long arg3) {
							int chnCount = deviceList.get(location).getEncoderNumber();
							// guId = listBus.get(location).getGuId();
							guId = deviceList.get(location).getNewGuId();
							listItems = new ArrayList<String>();
							for (int i = 1; i <= chnCount; i++) {
								listItems.add(Constants.CHANNEL_PREFIX_NAME + i);
							}

							ArrayAdapter<String> chnCountAdapter = new ArrayAdapter<String>(getContext(),
									R.layout.spinner_item, listItems);
							chnCountAdapter.setDropDownViewResource(R.layout.spinner_checkview);
							sp_queryDevChnCount.setAdapter(chnCountAdapter);

						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {

						}
					});
					// queryDevList.setSelection(1, true);

					ArrayAdapter<String> recordTypeAdapter = new ArrayAdapter<String>(getContext(),
							R.layout.spinner_item, getResources().getStringArray(R.array.recordTypeData));
					recordTypeAdapter.setDropDownViewResource(R.layout.spinner_checkview);
					sp_queryRecordType.setAdapter(recordTypeAdapter);
				}
			}
		});
	}


	DatePickerDialog.OnDateSetListener onStartDateListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			int m = monthOfYear + 1;
			int d = dayOfMonth;
			String month = m > 9 ? m + "" : "0" + m;
			String day = d > 9 ? d + "" : "0" + d;
			bt_queryDate.setText(year + "-" + month + "-" + day);

		}
	};

	TimePickerDialog.OnTimeSetListener onStartTimeListener = new TimePickerDialog.OnTimeSetListener() {// 开始时间选择框

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			System.out.println(hourOfDay + "---------" + minute);
			String h = hourOfDay > 9 ? (hourOfDay + "") : ("0" + hourOfDay);
			String m = minute > 9 ? (minute + "") : ("0" + minute);
			bt_start_time.setText(h + ":" + m);
		}
	};

	TimePickerDialog.OnTimeSetListener onEndTimeListener = new TimePickerDialog.OnTimeSetListener() {// 结束时间选择框

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			System.out.println(hourOfDay + "---------" + minute);
			String h = hourOfDay > 9 ? (hourOfDay + "") : ("0" + hourOfDay);
			String m = minute > 9 ? (minute + "") : ("0" + minute);
			bt_end_time.setText(h + ":" + m);
		}
	};

	// @Override
	// protected android.app.Dialog onCreateDialog(int id) {
	// switch (id) {
	// case QUERY_DATEPICKER:
	//
	// return new DatePickerDialog(getContext(), onStartDateListener,
	// cale.get(Calendar.YEAR ), cale.get(Calendar.MONTH),
	// cale.get(Calendar.DAY_OF_MONTH));
	//
	//
	// case START_TIMEPICKER:
	//
	// return new TimePickerDialog(getContext(), onStartTimeListener, 0, 0,
	// true);
	//
	// case END_TIMEPICKER:
	//
	// return new TimePickerDialog(getContext(), onEndTimeListener, 23, 59,
	// true);
	//
	// }
	//
	// return null;
	// };
	/**
	 * 弹出日期选择框
	 * 
	 * @param view
	 */
	public void showStartDatePicker(View view) {
		new DatePickerDialog(getContext(), onStartDateListener, calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
	}

	/**
	 * 弹出开始时间选择框
	 * 
	 * @param view
	 */
	public void showStartTimePicker(View view) {
		new TimePickerDialog(getContext(), onStartTimeListener, 0, 0, true).show();
		;

	}

	/**
	 * 弹出结束时间选择框
	 * 
	 * @param view
	 */
	public void showEndTimePicker(View view) {
		new TimePickerDialog(getContext(), onEndTimeListener, 23, 59, true).show();
	}

	/**
	 * 按条件查询相应的录像记录 点击事件
	 * 
	 * @param view
	 * @throws ParseException
	 */
	public void queryRecord() throws ParseException {
		String queryStartTime = bt_queryDate.getText() + " " + bt_start_time.getText();// 获取选择的开始时间
		String queryEndTime = bt_queryDate.getText() + " " + bt_end_time.getText();// 获取选择的结束时间
		if (!compareTime(queryStartTime, queryEndTime)) {
			MyUtil.commonToast(getContext(), R.string.time_validate);
		} else {
			if (rb_native.isChecked()) {// 本地
				Intent intent = new Intent();
				intent.setClass(getContext(), LocalRecordActivity.class);
				intent.putExtra("start_time", queryStartTime);
				intent.putExtra("end_time", queryEndTime);
				startActivity(intent);
			} else {// 设备端
					// String guId = queryDevList.getSelectedItem().toString();
				int chnNumber = sp_queryDevChnCount.getSelectedItemPosition() + 1;
				int iType = sp_queryRecordType.getSelectedItemPosition() + 1;
				Log.e("RecordQueryActivity", "guId:" + guId + "开始时间：" + queryStartTime + "结束时间：" + queryEndTime
						+ "设备ID:" + guId + "=通道号：" + chnNumber + "录像类型：" + iType);
				JNVPlayerUtil.JNV_N_RecQuery(guId, 0, iType, queryStartTime, queryEndTime, chnNumber,
						Constants.DEVRECORD_PASTH);
				Intent intent = new Intent();
				intent.setClass(getContext(), DevRecordListActivity.class);
				intent.putExtra("guid", guId);
				startActivity(intent);
			}
		}
	}

	/**
	 * 校验输入的时间是否合法
	 * 
	 * @param time
	 * @throws ParseException
	 */
	@SuppressLint("SimpleDateFormat")
	public boolean compareTime(String start_time, String end_time) throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date start_date = df.parse(start_time);
		Date end_date = df.parse(end_time);
		if (start_date.getTime() >= end_date.getTime()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
