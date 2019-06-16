package com.monitor.bus.activity;

import java.util.ArrayList;
import java.util.List;

import com.monitor.bus.activity.fragment.SettingFragment;
import com.monitor.bus.activity.listener.FoucusChangeListener;
import com.monitor.bus.adapter.MyFragmentPageAdapter;
import com.monitor.bus.bean.manager.DeviceManager;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.activity.fragment.AlarmFragment;
import com.monitor.bus.activity.fragment.BaseFragment;
import com.monitor.bus.activity.fragment.PhotoFragment;
import com.monitor.bus.activity.fragment.RePlayFragment;
import com.monitor.bus.activity.fragment.DeviceListFragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class HomeActivity extends FragmentActivity implements OnClickListener, FoucusChangeListener {

    private ViewPager mViewPager;
    private FragmentPagerAdapter mPageAdapter;
    private List<BaseFragment> mFragments = new ArrayList<BaseFragment>();
    private BaseFragment currentFragment;
    private LinearLayout ll_tab;

    /**
     * 底部四个按钮
     */
    private ImageButton ib_0;
    private ImageButton ib_1;
    private ImageButton ib_2;
    private ImageButton ib_3;
    private ImageButton ib_4;


    long lastBackPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        DeviceManager.getInstance().init();
        initData();
        initView();

    }

    private void initData() {
        SettingFragment fragment4 = new SettingFragment();
        AlarmFragment fragment3 = new AlarmFragment();
        PhotoFragment fragment2 = new PhotoFragment();
        RePlayFragment fragment1 = new RePlayFragment();
        DeviceListFragment fragment0 = new DeviceListFragment();
        fragment0.setFoucusListener(this);
        fragment1.setFoucusListener(this);
        fragment2.setFoucusListener(this);
        fragment3.setFoucusListener(this);
        fragment4.setFoucusListener(this);

        mFragments.add(fragment0);
        mFragments.add(fragment1);
        mFragments.add(fragment2);
        mFragments.add(fragment3);
        mFragments.add(fragment4);
        currentFragment = mFragments.get(0);
    }

    protected void resetTabBtn(int position) {
        ((ImageButton) ib_0).setBackgroundColor(position == 0 ? getResources().getColor(R.color.home_tab_blue_sheng) : getResources().getColor(R.color.transparent));
        ((ImageButton) ib_1).setBackgroundColor(position == 1 ? getResources().getColor(R.color.home_tab_blue_sheng) : getResources().getColor(R.color.transparent));
        ((ImageButton) ib_2).setBackgroundColor(position == 2 ? getResources().getColor(R.color.home_tab_blue_sheng) : getResources().getColor(R.color.transparent));
        ((ImageButton) ib_3).setBackgroundColor(position == 3 ? getResources().getColor(R.color.home_tab_blue_sheng) : getResources().getColor(R.color.transparent));
        ((ImageButton) ib_4).setBackgroundColor(position == 4 ? getResources().getColor(R.color.home_tab_blue_sheng) : getResources().getColor(R.color.transparent));
        currentFragment = mFragments.get(position);
    }

    private void initView() {
        ll_tab = (LinearLayout) findViewById(R.id.ll_tab);
        ib_0 = (ImageButton) findViewById(R.id.ib_1);
        ib_1 = (ImageButton) findViewById(R.id.ib_2);
        ib_2 = (ImageButton) findViewById(R.id.ib_3);
        ib_3 = (ImageButton) findViewById(R.id.ib_4);
        ib_4 = (ImageButton) findViewById(R.id.ib_5);
        mViewPager = (ViewPager) findViewById(R.id.vp_content);

        ll_tab.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                LogUtils.d("yangbintest", "ll_tab has Focus");
                if (hasFocus) {
                    ib_0.requestFocus();
                }
            }
        });

        ib_0.setOnClickListener(this);
        ib_1.setOnClickListener(this);
        ib_2.setOnClickListener(this);
        ib_3.setOnClickListener(this);
        ib_4.setOnClickListener(this);
        mPageAdapter = new MyFragmentPageAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mPageAdapter);
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

    @Override
    public void onBackPressed() {
        if (currentFragment.onBackPress()) {
            return;
        }
        if (System.currentTimeMillis() - lastBackPress >= 2000) {
            MUtils.toast(this, "再点一次退出应用！");
            lastBackPress = System.currentTimeMillis();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.ib_1:
                mViewPager.setCurrentItem(0);
                resetTabBtn(0);
                break;
            case R.id.ib_2:
                mViewPager.setCurrentItem(1);
                resetTabBtn(1);
                break;
            case R.id.ib_3:
                mViewPager.setCurrentItem(2);
                resetTabBtn(2);
                break;
            case R.id.ib_4:
                mViewPager.setCurrentItem(3);
                resetTabBtn(3);
                break;
            case R.id.ib_5:
                mViewPager.setCurrentItem(4);
                resetTabBtn(4);
                break;

            default:
                break;
        }

    }

    @Override
    public void onFoucusChanged(View v, boolean hasFoucus) {
        LogUtils.d("yangbintest", "requestFocus");
        ib_0.setVisibility(View.VISIBLE);
        ib_0.setFocusable(true);
        ib_0.requestFocus();
    }
}