package com.monitor.bus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.android.gms.maps.model.LatLng;
import com.jniUtil.GpsCorrection;
import com.jniUtil.GpsCorrection.LongitudeLatitude;
import com.monitor.bus.activity.R;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.consts.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class MyDateUtils {
	private static String TAG = "DateUtils";
	public static final String FORMAT_1="yyyy-MM-dd";
	public static final String FORMAT_RECORD="yyyy-MM-dd hh:mm";

	public static String getCurrentDateTime(String fomat) {
		Date date = new Date();
		SimpleDateFormat from = new SimpleDateFormat(fomat);
		String times = from.format(date);
		return times;
	}
	
	public static String getTodayDateString(String fomat) {
		Calendar calendar=Calendar.getInstance();
		Date date = new Date();
		date=calendar.getTime();
		SimpleDateFormat from = new SimpleDateFormat(fomat);
		String times = from.format(date);
		return times;
	}
	public static long getTimeMails(String dateString,String formatString){
		Date date=new Date();
		SimpleDateFormat format=new SimpleDateFormat(formatString);
		try {
			date=format.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date.getTime();
	}
}
