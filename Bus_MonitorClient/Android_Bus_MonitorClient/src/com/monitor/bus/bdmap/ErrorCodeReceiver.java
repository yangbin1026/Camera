package com.monitor.bus.bdmap;

import com.baidu.mapapi.SDKInitializer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

	/**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
public class ErrorCodeReceiver extends BroadcastReceiver {
	private static final String TAG=ErrorCodeReceiver.class.getSimpleName();
	Context mContext;
	public ErrorCodeReceiver(Context context) {
		mContext=context;
	}

        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            Log.d(TAG, "action: " + s);
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
               Toast.makeText(mContext,"key 验证出错! 错误码 :" + intent.getIntExtra
                        (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        +  " ; 请在 AndroidManifest.xml 文件中检查 key 设置",Toast.LENGTH_LONG).show();
            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
            	Toast.makeText(mContext,"key 验证成功! 功能可以正常使用",Toast.LENGTH_LONG).show();
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
            	Toast.makeText(mContext,"网络出错",Toast.LENGTH_LONG).show();
            }
        }
    }