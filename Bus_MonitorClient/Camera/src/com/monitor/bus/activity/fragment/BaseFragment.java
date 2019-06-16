package com.monitor.bus.activity.fragment;

import com.monitor.bus.activity.R;
import com.monitor.bus.activity.listener.FoucusChangeListener;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment {
    FoucusChangeListener foucusListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        return view;
    }

    public void setFoucusListener(FoucusChangeListener listener){
        this.foucusListener=listener;
    }
    public boolean onBackPress() {
        return false;
    }

    protected Context getContext() {
        return (Context) getActivity();
    }

}
