package com.jniUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.monitor.bus.activity.R;

import android.content.Context; 
import android.content.res.Resources.NotFoundException;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class SaveUtil {   
	private static final String TAG = null;
	private static final int BUFFER_SIZE = 400000;//30000000;
	private static final String DB_PATH = 
			Environment.getExternalStorageDirectory().getAbsolutePath()+ "/BusMonitorClient";  //在手机里存放数据库的位置 

	/**
	 * 获取数据完整路径
	 */
	public static String getDataPath(Context context, String fileName){
		String path = DB_PATH + "/" + fileName;
		Log.i(TAG, "文件路径"+path);
		return path;
	}

	/**
	 * 保存到指定路径，如果需要的话
	 * @throws IOException 
	 */
	public static String saveIfNeed(Context context, String fileName, int id) { 
		String file = getDataPath(context, fileName);//文件路径
		/*if (!(new File(file).exists())) {//判断文件是否存在，若不存在则执行导入，否则直接打开数据库 
			
			try {
				InputStream is = context.getResources().openRawResource(id); 
				FileOutputStream fos;
				fos = new FileOutputStream(file);

				byte[] buffer = new byte[BUFFER_SIZE]; 
				int count = 0; 
				while ((count = is.read(buffer)) > 0) { 
					fos.write(buffer, 0, count); 
				} 
				fos.close(); 
				is.close(); 
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}*/
		File fl = null;
		FileOutputStream fos = null;
		try {
			File dir = new File(DB_PATH);//目录路径
			if(!dir.exists()){//如果不存在，则创建路径名
				Log.i("+++++++++++", "要存储的目录不存在");
				if(dir.mkdirs()){//创建该路径名，返回true则表示创建成功  
					Log.i("++++++++", "已经创建文件存储目录");
				}else{
					Log.i("++++++++", "创建目录失败");
				}
			}
			// 目录存在，则将apk中raw中的需要的文档复制到该目录下  
			fl = new File(file);
			boolean bFileNotExist = false;
			if(!fl.exists()){
				bFileNotExist = true;
			}else{
				if(fl.length() <= 0){
					fl.delete();
					bFileNotExist = true;
				}
			}
			if(bFileNotExist){// 文件不存在  
				Log.i("++++++++", "要打开的文件不存在");
				InputStream ins = context.getResources().openRawResource(id);
				fos = new FileOutputStream(fl);
				byte[] buffer = new byte[1024];
				int count = 0;
				while((count = ins.read(buffer)) > 0){
					fos.write(buffer, 0, count);
				}
				fos.close();
				ins.close();
			}
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
			
			if(fl != null){
				fl.delete();
			}
		}
		return file;
	}
}
