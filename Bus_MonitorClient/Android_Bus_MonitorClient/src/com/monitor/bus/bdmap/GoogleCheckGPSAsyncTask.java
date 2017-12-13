package com.monitor.bus.bdmap;

import com.jniUtil.GpsCorrection;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.UserGoogleMapActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
 * gps校正任务
 * @author Administrator
 *
 */
public class GoogleCheckGPSAsyncTask extends AsyncTask<Void,Integer,Integer>{
	protected Context context = null;
	protected String  gpsCorrectionFileName = "";
	protected ProgressDialog  dialog = null;
	protected Handler mHandler = null;
	
	public GoogleCheckGPSAsyncTask(Context context,Handler handler,String strFileName){
		this.context = context;
		mHandler = handler;
		gpsCorrectionFileName = strFileName;
		dialog = new ProgressDialog(this.context);
		dialog.setMessage(context.getResources().getString(R.string.googleGpsCorrectionFileLoad));
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		return GpsCorrection.getInstance().initialize(gpsCorrectionFileName);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
		if(dialog != null){
			dialog.show();
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		if(dialog != null){
			dialog.dismiss();
		}
		
		if(mHandler != null){
			Message msg = new Message();
			msg.what = UserGoogleMapActivity.MSG_WHAT_NEW_LOCATION;
			msg.obj =false;
			mHandler.sendMessage(msg);
		}
	}
	 
 }