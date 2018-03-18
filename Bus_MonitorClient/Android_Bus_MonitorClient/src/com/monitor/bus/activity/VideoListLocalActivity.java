package com.monitor.bus.activity;

import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.monitor.bus.Constants;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.database.DatabaseHelper;

/**
 * 本地視頻文件列表
 * 
 */
public class VideoListLocalActivity extends BaseActivity {

	private String start_time = null;
	private String end_time = null;
	
	List<HashMap<String, String>> testLocalListView;
	DatabaseHelper dbHelper;
	
	private ListView lv_recoder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lv_recoder = (ListView) findViewById(R.id.lv_recoder);
		Intent intent = getIntent();
		start_time = intent.getStringExtra("start_time");
		end_time = intent.getStringExtra("end_time");
		dbHelper = new DatabaseHelper(this, Constants.DATABASE_NAME);

		lv_recoder.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Intent intent = new Intent();
				String path = testLocalListView.get(position).get("file_path")+ testLocalListView.get(position).get("file_name");
				intent.putExtra("playFileName", path);
				intent.putExtra("id", testLocalListView.get(position).get("id"));
				intent.setClass(VideoListLocalActivity.this, ReplayActivity.class);
				startActivity(intent);

			}
		});

		testLocalListView = dbHelper.queryRecordInfoList(start_time, end_time);
		SimpleAdapter simpleAdapter = new SimpleAdapter(this,
				testLocalListView, R.layout.local_item,
				new String[] { "file_name" }, new int[] { R.id.localTest });
		lv_recoder.setAdapter(simpleAdapter);
		if (testLocalListView.size() == 0) {
			MUtils.commonToast(this, R.string.not_recordfile);
		}
	}


}
