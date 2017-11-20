package com.monitor.bus.activity.fragment;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyFragmentPageAdapter extends FragmentPagerAdapter {
	private List<Fragment> mFragments;

	public MyFragmentPageAdapter(FragmentManager fm) {
		super(fm);
	}

	public MyFragmentPageAdapter(FragmentManager fm, List<Fragment> list) {
		this(fm);
		mFragments = list;
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	@Override
	public Fragment getItem(int arg0) {
		return mFragments.get(arg0);
	}

}
