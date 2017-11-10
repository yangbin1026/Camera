package com.monitor.bus.activity;

import java.util.List;

import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.FilterAlarmInfoAdapter;
import com.monitor.bus.adapter.FilterAlarmInfoAdapter.ViewHolder;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;

public class FilterOptionActivity extends BaseActivity{
	private GridView gridview_filter;
	private TextView tv_title;
	private CheckBox checkBox;
	private SharedPreferences prefs;
	private Editor editor;
	private boolean isCheck;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		MyUtil.initTitleName(this,R.layout.gridview_filter,R.string.alarm_list);
		findViews();
		gridview_filter.setAdapter(new FilterAlarmInfoAdapter(this));
		gridview_filter.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getBaseContext(), "位置:" + position, 1000).show();
				prefs = getSharedPreferences("prefs", MODE_WORLD_READABLE);
				editor = prefs.edit();
			}
		});
		
	}
	/*初始化UI控件*/
	private void findViews(){
		gridview_filter = (GridView) findViewById(R.id.gridview_filter); 
		View view = LayoutInflater.from(this).inflate(R.layout.filter_alarm_info, null);
		checkBox = (CheckBox) view.findViewById(R.id.checkbox_filter);
		tv_title = (TextView) findViewById(R.id.tilte_name);
		tv_title.setText(R.string.alarm_action_config);
		
	}
}
