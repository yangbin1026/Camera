package com.monitor.bus.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.monitor.bus.bean.RecordDBInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.LogUtils;

/**
 * 数据库Helper类
 */
@SuppressLint("SimpleDateFormat")
public class DatabaseHelper extends SQLiteOpenHelper {
	private static String TAG = "DatabaseHelper";
	
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public DatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public DatabaseHelper(Context context, String name) {
		this(context, name, Constants.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		LogUtils.i(TAG, "++++++++++++ onCreate");
		createTable(db);
	}

	/**
	 * 创建数据库表
	 * 
	 * @param db
	 */
	private void createTable(SQLiteDatabase db) {
		// 回放表
		db.execSQL("create table andr_playback ("
				+ "ID             INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "REC_DATE             DATE,"
				+ "REC_PATH             VARCHAR(50),"
				+ "REC_FILENAME         VARCHAR(20)" + ");");
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LogUtils.i(TAG, "++++++++++++++++++onUpgrade");
		db.execSQL("drop table if exists andr_playback;");
		onCreate(db);
	}
	
	  /**
	   * 新增录像记录
	   * @param currentDate 的当前时间
	   * @param fileName 文件名称
	   * @param filePath 文件路径
	   * @return
	 * @throws ParseException 
	   */

	  public void insertRecordInfo(String currentDate,String fileName,String filePath) throws ParseException{
	    SQLiteDatabase db = this.getWritableDatabase();
	    ContentValues cv = new ContentValues();
	    SimpleDateFormat formater = new SimpleDateFormat(Constants.YMD_HMS_FORMAT);
	    Date datetime = formater.parse(currentDate);
	    SimpleDateFormat formater2 = new SimpleDateFormat(Constants.FORMAT);
	    String date =formater2.format(datetime);
	    cv.put("REC_DATE",date);
	    cv.put("REC_PATH",filePath);
	    cv.put("REC_FILENAME",fileName);
	    db.insert("andr_playback", null, cv);
	    db.close();
	  }

	  
	  /**
	   * 根据ID删除录像
	   * @param id
	   */
	  public long deleteRecordInfo(String id){
	    SQLiteDatabase db = this.getWritableDatabase();
	    String where = "ID = ?";
	    String[] whereValue = { id };
	    long row = db.delete("andr_playback",  where, whereValue);
	    db.close();
	    return row;
	  }
	  
	  
		
		/**
		 * 查询某个时间段的所有录像记录
		 * @param start
		 * @param end
		 * @return
		 */
		public ArrayList<HashMap<String, String>> queryRecordInfoList(String start,String end){
			LogUtils.i(TAG, "++++++++++queryRecordInfoList");
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query("andr_playback",
					new String[] { "ID,REC_FILENAME,REC_PATH,REC_FILENAME" },
					"REC_DATE >= datetime('" + start+ "') AND REC_DATE <= datetime('" + end + "')",null, "", "", "ID");
			LogUtils.i(TAG, "++++++++++count:"+ cursor.getCount());
			
			
			ArrayList<HashMap<String, String>> recoderList = new ArrayList<HashMap<String, String>>();
			while (cursor.moveToNext()) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("file_name",	cursor.getString(cursor.getColumnIndex("REC_FILENAME")));
				map.put("file_path",	cursor.getString(cursor.getColumnIndex("REC_PATH")));
				map.put("id",	cursor.getString(cursor.getColumnIndex("ID")));
				recoderList.add(map);
			}
			cursor.close();
			db.close();
			
			return recoderList;
		}
		
		/**
		 * 查询某个时间段的所有录像记录
		 * @param start
		 * @param end
		 * @return
		 */
		public ArrayList<RecordDBInfo> queryAllDBList(String start,String end){
			LogUtils.i(TAG, "queryAllDBList()");
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query("andr_playback",
					new String[] { "ID,REC_FILENAME,REC_PATH,REC_FILENAME" },
					"REC_DATE >= datetime('" + start+ "') AND REC_DATE <= datetime('" + end + "')",null, "", "", "ID");
			
			ArrayList<RecordDBInfo> recoderList = new ArrayList<RecordDBInfo>();
			while (cursor.moveToNext()) {
				RecordDBInfo info=new RecordDBInfo();
				info.setFileName(cursor.getString(cursor.getColumnIndex("REC_FILENAME")));
				info.setFilePath(cursor.getString(cursor.getColumnIndex("REC_PATH")));
				info.setId(cursor.getString(cursor.getColumnIndex("ID")));
				recoderList.add(info);
			}
			cursor.close();
			db.close();
			return recoderList;
		}
		
}
