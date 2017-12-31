package com.monitor.bus.view.dialog;
import com.monitor.bus.activity.R;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by lmt on 16/7/6.
 */
public class DateUtil {

//    public static final String ymdhms = "yyyy-MM-dd HH:mm:ss";
    private static String TAG = "DateUtils";
 	public static final String CHECKVERSION_FORMAT="yyyy-MM-dd";
 	public static final String REPLAY_SHOW_FORMAT="yyyy-MM-dd";
 	
 	public static final String SAVEPATH_FORMAT = "yyyyMMdd";// （字符串）日期格式 年月日
 	public static final String PIC_NAME_FORMAT = "yyyyMMddHHmmssSSS";// 日期格式
	public static final String DB_FORMAT = "yyyy-MM-dd HH:mm:ss";// （正式）日期格式 年月日
 	
    public static int getYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    public static String getToday() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return year + "-" + (month > 9 ? month : ("0" + month)) + "-" + day;
    }

    public static List<Integer> getDateForString(String date) {
        String[] dates = date.split("-");
        List<Integer> list = new ArrayList<Integer>();
        list.add(Integer.parseInt(dates[0]));
        list.add(Integer.parseInt(dates[1]));
        list.add(Integer.parseInt(dates[2]));
        return list;
    }


    public static String formatDate(String date, String format) {
        String resultD = date;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date d = sdf.parse(date);
            resultD = sdf.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultD;
    }

    public static String formatDate(long milliseconds, String format) {
        String resultD = "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date d = new Date(milliseconds);
            resultD = sdf.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultD;
    }

    public static Date formatDateStr(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date1 = null;
        try {
            date1 = sdf.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date1;
    }


    /**
     * 返回当前月份1号位于周几
     *
     * @param year  年份
     * @param month 月份，传入系统获取的，不需要正常的
     * @return 日：1		一：2		二：3		三：4		四：5		五：6		六：7
     */
    public static int getFirstDayWeek(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    
    
    

	public static String getCurrentDateTime(String fomat) {
		Date date = new Date();
		SimpleDateFormat from = new SimpleDateFormat(fomat);
		String times = from.format(date);
		return times;
	}
	
	///////////////////////
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
	public static String getTodayDateString(String fomat) {
		Calendar calendar=Calendar.getInstance();
		Date date = new Date();
		date=calendar.getTime();
		SimpleDateFormat from = new SimpleDateFormat(fomat);
		String times = from.format(date);
		return times;
	}
	public static String getTodayStart(){
		Calendar calendar=Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0,0);
		Date date=calendar.getTime();
		SimpleDateFormat from = new SimpleDateFormat(DB_FORMAT);
		String times = from.format(date);
		return times;
	}
	public static String getTodayEnd(){
		Calendar calendar=Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59,59);
		Date date=calendar.getTime();
		SimpleDateFormat from = new SimpleDateFormat(DB_FORMAT);
		String times = from.format(date);
		return times;
	}

}
