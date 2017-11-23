package com.monitor.bus.activity;

import java.util.ArrayList;
import java.util.List;

import com.monitor.bus.activity.fragment.AboutFragment;
import com.monitor.bus.activity.fragment.AlarmFragment;
import com.monitor.bus.activity.fragment.BaseFragment;
import com.monitor.bus.activity.fragment.MyFragmentPageAdapter;
import com.monitor.bus.activity.fragment.PhotoFragment;
import com.monitor.bus.activity.fragment.RePlayFragment;
import com.monitor.bus.activity.fragment.VideoFragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class HomeActivity extends FragmentActivity {

	private ViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	private List<BaseFragment> mFragments = new ArrayList<BaseFragment>();
	private BaseFragment currentFragment;

	/**
	 * 底部四个按钮
	 */
	private LinearLayout mTabBtn1;
	private LinearLayout mTabBtn2;
	private LinearLayout mTabBtn3;
	private LinearLayout mTabBtn4;
	private LinearLayout mTabBtn5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		mViewPager = (ViewPager) findViewById(R.id.vp_content);
		initView();
		initData();

	}

	private void initData() {
		AboutFragment fragment4 = new AboutFragment();
		AlarmFragment fragment3 = new AlarmFragment();
		PhotoFragment fragment2 = new PhotoFragment();
		RePlayFragment fragment1 = new RePlayFragment();
		VideoFragment fragment0 = new VideoFragment();
		mFragments.add(fragment0);
		mFragments.add(fragment1);
		mFragments.add(fragment2);
		mFragments.add(fragment3);
		mFragments.add(fragment4);
		mAdapter = new MyFragmentPageAdapter(getSupportFragmentManager(), mFragments);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				resetTabBtn(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}

	protected void resetTabBtn(int position) {
		((ImageButton) mTabBtn1.findViewById(R.id.ib_1)).setBackgroundColor(position==0? getResources().getColor(R.color.black):getResources().getColor(R.color.blue));
		((ImageButton) mTabBtn2.findViewById(R.id.ib_2)).setImageResource(position==1? getResources().getColor(R.color.black):getResources().getColor(R.color.blue));
		((ImageButton) mTabBtn3.findViewById(R.id.ib_3)).setImageResource(position==2? getResources().getColor(R.color.black):getResources().getColor(R.color.blue));
		((ImageButton) mTabBtn4.findViewById(R.id.ib_4)).setImageResource(position==3? getResources().getColor(R.color.black):getResources().getColor(R.color.blue));
		((ImageButton) mTabBtn5.findViewById(R.id.ib_5)).setImageResource(position==4? getResources().getColor(R.color.black):getResources().getColor(R.color.blue));
		currentFragment = mFragments.get(position);
	}

	private void initView() {
		mTabBtn1 = (LinearLayout) findViewById(R.id.ll_1);
		mTabBtn2 = (LinearLayout) findViewById(R.id.ll_2);
		mTabBtn3 = (LinearLayout) findViewById(R.id.ll_3);
		mTabBtn4 = (LinearLayout) findViewById(R.id.ll_4);
		mTabBtn5 = (LinearLayout) findViewById(R.id.ll_5);
	}

	@Override
	public void onBackPressed() {
		if (currentFragment.onBackPress()) {
			return;
		}
		super.onBackPressed();
	}
}