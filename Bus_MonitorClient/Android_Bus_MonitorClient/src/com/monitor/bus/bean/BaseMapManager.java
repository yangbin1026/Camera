package com.monitor.bus.bean;

import android.content.Context;

public abstract class BaseMapManager {
	
	public abstract void setDeviceInfo(DeviceInfo info);

	public abstract void onCreat();

	public abstract void onResum();

	public abstract void onPause();

	public abstract void onDestory();
}
