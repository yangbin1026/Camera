package com.monitor.bus.activity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TimePicker;

import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.SpinnerBusAdapter;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.DeviceInfo;

/**
 * 录像查询界面
 */
public class RecordQueryActivity extends BaseActivity {
	private RadioButton record_native;//本地
	private RadioButton record_remote;//设备端
	private Button queryDate;
	private Button start_time;
	private Button end_time;
	private Spinner queryDevList;
	private Spinner queryDevChnCount;
	private Spinner queryRecordType;

	private static final int QUERY_DATEPICKER = 1;// 开始日期
	private static final int START_TIMEPICKER = 2;// 开始时间
	private static final int END_TIMEPICKER = 3;// 结束时间
	private Calendar cale;
	private SimpleDateFormat df;
	private List<DeviceInfo> listBus;
	private List<String> mItems;
	private String guId;
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyUtil.initTitleName(this,R.layout.record_query,R.string.query);
		queryDate = (Button) findViewById(R.id.queryDate);
		start_time = (Button) findViewById(R.id.start_time);
		end_time = (Button) findViewById(R.id.end_time);

		cale = Calendar.getInstance();
		df = new SimpleDateFormat( "yyyy-MM-dd" );
		queryDate.setText( df .format( cale .getTime()));
		start_time.setText(R.string.start_text);
		end_time.setText(R.string.end_text);


		record_native = (RadioButton) findViewById(R.id.record_native);
		record_remote = (RadioButton) findViewById(R.id.record_remote);

		getOnlineBusDevices();//查询在线的设备
		if(0 == listBus.size()){
			record_remote.setVisibility(View.GONE);
		}

		record_native.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					TableLayout remoteLayout =  (TableLayout) findViewById(R.id.remoteLayout);//设备端查询布局
					remoteLayout.setVisibility(View.GONE);
				}
			}
		}); 

		record_remote.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					TableLayout remoteLayout =  (TableLayout) findViewById(R.id.remoteLayout);//设备端查询布局
					remoteLayout.setVisibility(View.VISIBLE);

					queryDevList = (Spinner)findViewById(R.id.queryDevList);
					queryDevChnCount = (Spinner)findViewById(R.id.queryDevChnCount);
					queryRecordType = (Spinner)findViewById(R.id.queryRecordType);



					SpinnerBusAdapter queryDevListAdapter = new SpinnerBusAdapter(RecordQueryActivity.this, R.layout.spinner_item, listBus);
					queryDevListAdapter.setDropDownViewResource(R.layout.spinner_checkview);
					queryDevList.setAdapter(queryDevListAdapter);


					queryDevList.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0, View arg1,
								int location, long arg3) {
							int chnCount = listBus.get(location).getEncoderNumber();
							//							guId = listBus.get(location).getGuId();
							guId = listBus.get(location).getNewGuId();
							mItems = new ArrayList<String>();
							for(int i = 1; i <= chnCount ; i++){
								mItems.add(Constants.CHANNEL_PREFIX_NAME + i);
							}

							ArrayAdapter<String> chnCountAdapter = new ArrayAdapter<String>(RecordQueryActivity.this,R.layout.spinner_item, mItems);
							chnCountAdapter.setDropDownViewResource(R.layout.spinner_checkview);
							queryDevChnCount.setAdapter(chnCountAdapter);

						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {

						}
					});
					//					queryDevList.setSelection(1, true);

					ArrayAdapter<String> recordTypeAdapter = new ArrayAdapter<String>(RecordQueryActivity.this,R.layout.spinner_item, getResources().getStringArray(R.array.recordTypeData));
					recordTypeAdapter.setDropDownViewResource(R.layout.spinner_checkview);
					queryRecordType.setAdapter(recordTypeAdapter);
				}
			}
		});
	}

	/**
	 * 获取在线的bus设备
	 * @return
	 */
	public void getOnlineBusDevices(){
		listBus = new ArrayList<DeviceInfo>();
		for (DeviceInfo busInfo : Constants.DEVICE_LIST) {
			if (0 != busInfo.getOnLine()) {
				listBus.add(busInfo);
			}
		}
	}

	DatePickerDialog.OnDateSetListener onStartDateListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			int m = monthOfYear + 1;
			int d =dayOfMonth;
			String month = m > 9 ? m + "" : "0" + m;
			String day = d > 9 ? d + "" : "0" + d;
			queryDate.setText(year+"-"+month+"-"+day);

		}
	};


	TimePickerDialog.OnTimeSetListener onStartTimeListener = new TimePickerDialog.OnTimeSetListener() {//开始时间选择框

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			System.out.println(hourOfDay + "---------" + minute);
			String h = hourOfDay > 9 ? (hourOfDay + "") : ("0" + hourOfDay);
			String m = minute > 9 ? (minute + "") : ("0" + minute);
			start_time.setText(h + ":" + m);
		}
	};


	TimePickerDialog.OnTimeSetListener onEndTimeListener = new TimePickerDialog.OnTimeSetListener() {//结束时间选择框

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			System.out.println(hourOfDay + "---------" + minute);
			String h = hourOfDay > 9 ? (hourOfDay + "") : ("0" + hourOfDay);
			String m = minute > 9 ? (minute + "") : ("0" + minute);
			end_time.setText(h + ":" + m);
		}
	};


	@Override
	protected android.app.Dialog onCreateDialog(int id) {
		switch (id) {
		case QUERY_DATEPICKER:

			return new DatePickerDialog(this, onStartDateListener, cale.get(Calendar.YEAR ), cale.get(Calendar.MONTH), cale.get(Calendar.DAY_OF_MONTH));


		case START_TIMEPICKER:

			return new TimePickerDialog(this, onStartTimeListener, 0, 0, true);

		case END_TIMEPICKER:

			return new TimePickerDialog(this, onEndTimeListener, 23, 59, true);

		}

		return null; 
	};
	/**
	 * 弹出日期选择框
	 * 
	 * @param view
	 */
	public void showStartDatePicker(View view) {
		showDialog(QUERY_DATEPICKER);
	}

	/**
	 * 弹出开始时间选择框
	 * 
	 * @param view
	 */
	public void showStartTimePicker(View view) {
		showDialog(START_TIMEPICKER);
	}

	/**
	 * 弹出结束时间选择框
	 * 
	 * @param view
	 */
	public void showEndTimePicker(View view) {
		showDialog(END_TIMEPICKER);
	}

	/**
	 * 按条件查询相应的录像记录
	 * 
	 * @param view
	 * @throws ParseException 
	 */
	public void queryRecord(View view) throws ParseException {
		String queryStartTime = queryDate.getText() +" " + start_time.getText();// 获取选择的开始时间
		String queryEndTime =  queryDate.getText()  +" " + end_time.getText();// 获取选择的结束时间
		if(!compareTime(queryStartTime,queryEndTime)){
			MyUtil.commonToast(this, R.string.time_validate);
		}else{
			if(record_native.isChecked()){//本地
				Intent intent = new Intent();
				intent.setClass(this, LocalRecordActivity.class);
				intent.putExtra("start_time", queryStartTime);
				intent.putExtra("end_time", queryEndTime);
				startActivity(intent);
			}else{//设备端
				//String guId = queryDevList.getSelectedItem().toString();
				int chnNumber = queryDevChnCount.getSelectedItemPosition()+1;
				int iType = queryRecordType.getSelectedItemPosition()+1;
				Log.e("RecordQueryActivity", "guId:"+guId+"开始时间："+queryStartTime 
						+"结束时间："+queryEndTime+"设备ID:"+guId + "=通道号："+chnNumber+"录像类型："+iType);
				JNVPlayerUtil.JNV_N_RecQuery(guId, 0, iType, queryStartTime, queryEndTime, chnNumber, Constants.DEVRECORD_PASTH);
				Intent intent = new Intent();
				intent.setClass(this, DevRecordListActivity.class);
				intent.putExtra("guid", guId);
				startActivity(intent);
			}
		}
	}

	/**
	 * 取消
	 * */
	public void cancelQuery(View view){
		finish();
	}
	/**
	 * 校验输入的时间是否合法
	 * @param time
	 * @throws ParseException 
	 */
	@SuppressLint("SimpleDateFormat")
	public boolean compareTime(String start_time,String end_time) throws ParseException{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date start_date = df.parse(start_time);
		Date end_date = df.parse(end_time);
		if (start_date.getTime() >= end_date.getTime()) {
			return false;
		}else{
			return true;
		}
	}
}
