package com.monitor.bus.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

class TabInfo {  
    private Fragment fragment;// 根据clazz和args实例化出来的Fragment对象  
    private Class<?> clazz;// Fragment类的class对象  
    private Bundle args;// 往Fragment传递参数的Bundle  

    TabInfo(Class<? extends Fragment> clazz, Bundle args) {  
        this.clazz = clazz;  
        this.args = args;  
    }  
}