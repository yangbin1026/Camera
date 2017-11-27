package com.monitor.bus.adapter;

import java.util.List;

import com.monitor.bus.activity.fragment.BaseFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyFragmentPageAdapter extends FragmentPagerAdapter {
	private List<BaseFragment> mFragments;

	public MyFragmentPageAdapter(FragmentManager fm) {
		super(fm);
	}

	public MyFragmentPageAdapter(FragmentManager fm, List<BaseFragment> list) {
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
