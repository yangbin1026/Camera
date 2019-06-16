package com.monitor.bus.utils;

import java.io.BufferedOutputStream;
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
import com.monitor.bus.Constants;
import com.monitor.bus.activity.R;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.view.dialog.DateUtil;

import android.R.integer;
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

public class MUtils {
    private static String TAG = "MUtils";

    private static final int BUFFER_SIZE = 400000;// 30000000;
    private static final String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/BusMonitorClient"; // 在手机里存放数据库的位置

    /**
     * 获取旋转后的位图
     */
    public static Bitmap getRotatedBmp(Bitmap bmp, int angle) {
        Matrix myMatrix = new Matrix();
        myMatrix.reset();
        myMatrix.postRotate(angle);
        Bitmap dstBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), myMatrix, true);
        return dstBitmap;
    }

    public static boolean hasUselessString(String... strings) {
        for (String s : strings) {
            if (s == null || s.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void debugToast(Context context, String msg) {
        if (LogUtils.Debug) {
            return;
        }
        Toast.makeText(context, "" + msg, Toast.LENGTH_LONG).show();
    }

    public static void toast(Context context, String msg) {
        Toast.makeText(context, "" + msg, Toast.LENGTH_LONG).show();
    }

    public static void toast(Context context, int res) {
        Toast.makeText(context, "" + context.getString(res), Toast.LENGTH_LONG).show();
    }

    /**
     * 保存到指定路径，如果需要的话
     *
     * @throws IOException
     */
    public static String saveIfNeed(Context context, String fileName, int id) {
        String file = getDataPath(context, fileName);// 文件路径
        // if (!(new File(file).exists())) {//判断文件是否存在，若不存在则执行导入，否则直接打开数据库
        //
        // try {
        // InputStream is = context.getResources().openRawResource(id);
        // FileOutputStream fos;
        // fos = new FileOutputStream(file);
        //
        // byte[] buffer = new byte[BUFFER_SIZE];
        // int count = 0;
        // while ((count = is.read(buffer)) > 0) {
        // fos.write(buffer, 0, count);
        // }
        // fos.close();
        // is.close();
        //
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        File fl = null;
        FileOutputStream fos = null;
        try {
            File dir = new File(DB_PATH);// 目录路径
            if (!dir.exists()) {// 如果不存在，则创建路径名
                Log.i("+++++++++++", "要存储的目录不存在");
                if (dir.mkdirs()) {// 创建该路径名，返回true则表示创建成功
                    Log.i("++++++++", "已经创建文件存储目录");
                } else {
                    Log.i("++++++++", "创建目录失败");
                }
            }
            // 目录存在，则将apk中raw中的需要的文档复制到该目录下
            fl = new File(file);
            boolean bFileNotExist = false;
            if (!fl.exists()) {
                bFileNotExist = true;
            } else {
                if (fl.length() <= 0) {
                    fl.delete();
                    bFileNotExist = true;
                }
            }
            if (bFileNotExist) {// 文件不存在
                Log.i("++++++++", "要打开的文件不存在");
                InputStream ins = context.getResources().openRawResource(id);
                fos = new FileOutputStream(fl);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = ins.read(buffer)) > 0) {
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
        } catch (IOException e) {
            e.printStackTrace();

            if (fl != null) {
                fl.delete();
            }
        }
        return file;
    }

    /**
     * 获取数据完整路径
     */
    private static String getDataPath(Context context, String fileName) {
        String path = DB_PATH + "/" + fileName;
        return path;
    }

    public static void Vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    public static void Vibrate(final Activity activity, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

    public static String getUpdateVerJSON(String serverPath) throws Exception {
        StringBuilder newVerJSON = new StringBuilder();
        HttpClient client = new DefaultHttpClient();// 新建http客户端
        HttpParams httpParams = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);// 设置连接超时的范围
        // serverPath是服务器端version.json文件的路径
        HttpResponse response = client.execute(new HttpGet(serverPath));
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "utf-8"), 8192);
            String line = null;
            while ((line = reader.readLine()) != null) {
                newVerJSON.append(line + "\n");// 按行读取放入StringBuilder中
            }
            reader.close();
        }
        return newVerJSON.toString();
    }

    public static int getVerCode(Context context) throws Exception {
        int verCode = -1;
        verCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        return verCode;
    }

    // 获取旧应用程序名称版本
    public static String getVerName(Context context) throws Exception {
        String verName = "";
        verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        return verName;
    }

    // 获取旧应用程序的名字
    public static String getAppName(Context context) {
        String appName = context.getResources().getText(R.string.app_name).toString();
        return appName;
    }

    /**
     * 判断文件MimeType的方法
     *
     * @param f
     * @param isOpen 目的打开方式为true
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String getMIMEType(File f, boolean isOpen) {
        String type = "";
        String fName = f.getName();
        /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
        if (isOpen) {
            if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg")
                    || end.equals("bmp")) {
                type = "image";
            } else {
                /* 如果无法直接打开，就跳出软件列表给用户选择 */
                type = "*";
            }
            type += "/*";
        } else {
            if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg")
                    || end.equals("bmp")) {
                type = "image";
            }
        }
        return type;
    }

    /**
     * 缩放图片的方法
     *
     * @return
     */
    public static Bitmap fitSizePic(File f) {
        Bitmap resizeBmp = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 数字越大读出的图片占用的heap越小 不然总是溢出
        if (f.length() < 20480) { // 0-20k
            opts.inSampleSize = 1;
        } else if (f.length() < 51200) { // 20-50k
            opts.inSampleSize = 2;
        } else if (f.length() < 307200) { // 50-300k
            opts.inSampleSize = 4;
        } else if (f.length() < 819200) { // 300-800k
            opts.inSampleSize = 6;
        } else if (f.length() < 1048576) { // 800-1024k
            opts.inSampleSize = 8;
        } else {
            opts.inSampleSize = 10;
        }
        resizeBmp = BitmapFactory.decodeFile(f.getPath(), opts);
        return resizeBmp;
    }

    /**
     * 文件大小描述
     *
     * @param f
     * @return
     */
    public static String fileSizeMsg(File f) {
        int sub_index = 0;
        String show = "";
        if (f.isFile()) {
            long length = f.length();
            if (length >= 1073741824) {
                sub_index = (String.valueOf((float) length / 1073741824)).indexOf(".");
                show = ((float) length / 1073741824 + "000").substring(0, sub_index + 3) + "GB";
            } else if (length >= 1048576) {
                sub_index = (String.valueOf((float) length / 1048576)).indexOf(".");
                show = ((float) length / 1048576 + "000").substring(0, sub_index + 3) + "MB";
            } else if (length >= 1024) {
                sub_index = (String.valueOf((float) length / 1024)).indexOf(".");
                show = ((float) length / 1024 + "000").substring(0, sub_index + 3) + "KB";
            } else if (length < 1024) {
                show = String.valueOf(length) + "B";
            }
        }
        return show;
    }

    /**
     * 获取存储录像及照片的路径 由当前日期+设备IP+当前通道号
     *
     * @param path
     * @param currentDeviceInfo
     * @return
     */
    public static String getCurrentFilePath(String path, DeviceInfo currentDeviceInfo) {
        String currentDate = getCurrentDateTime(DateUtil.SAVEPATH_FORMAT);
        String currentPath = path + currentDate + "/" + currentDeviceInfo.getDeviceName() + "/"
                + currentDeviceInfo.getCurrentChn() + "/";
        return currentPath;
    }

    /**
     * 获取当前时间,并制定返回格式 如 yyyyMMddHHmmss , yyyyMMdd
     *
     * @param fomat
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getCurrentDateTime(String fomat) {
        Date date = new Date();
        // 指定的时间格式
        SimpleDateFormat from = new SimpleDateFormat(fomat);
        // 格式化时间
        String times = from.format(date);
        return times;
    }

    /**
     * 给出相应的提示信息
     *
     * @param currentContext 当前的上下文
     * @param message        提示信息
     */
    public static void commonToast(Context currentContext, int stringValue) {
        Toast.makeText(currentContext, stringValue, Toast.LENGTH_SHORT).show();
    }

    /**
     * 字符串转换到时间格式
     *
     * @param dateStr   需要转换的字符串
     * @param formatStr 需要格式的目标字符串 举例 yyyy-MM-dd
     * @return Date 返回转换后的时间
     * @throws ParseException 转换异常
     */
    @SuppressLint("SimpleDateFormat")
    public static Date stringToDate(String dateStr, String formatStr) {
        DateFormat sdf = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 检测当前网络是否连接
     */
    public static boolean isConnect(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * double类型保留6位小数
     */
    public static double convertDoubleType6(double d) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * GPS位置转google地图位置
     *
     * @return
     */
    public static LatLng fromWgs84ToGoogle(double latitude, double longitude) {

        Log.i(TAG, "换算前的经纬：lat=" + latitude + ",lon=" + longitude);
        LatLng ret = new LatLng(latitude, longitude);
        // 对位置进行运算
        if (GpsCorrection.getInstance().IsInitialize()) {
            LongitudeLatitude dstLongitudeLatitude = GpsCorrection.getInstance().fixGPS((float) longitude,
                    (float) latitude);
            LatLng tmp = new LatLng(dstLongitudeLatitude.mLatitude, dstLongitudeLatitude.mLongitude);

            if (tmp != null) {
                ret = tmp;
            }
        }

        return ret;
    }

    private static final String CHINA = "中国";

    /**
     * 验证是否是中文语言环境
     */
    public static boolean isChina(Context context) {
        String country = context.getString(R.string.country);
        return CHINA.equals(country);
    }

    /**
     * 验证谷歌地图模块完整性
     */
    public static boolean checkGoogleMapModule(Activity activity) {
        boolean ret = true;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (!apkExists(activity, Constants.SERVICE_APK_NAME)) {
            Log.e(TAG, "未安装google服务");
            intent.setData(getGoogleServiceUri(activity));
            activity.startActivity(intent);
            ret = false;
        }
        if (!apkExists(activity, Constants.STORE_APK_NAME)) {
            Log.e(TAG, "未安装google市场");
            intent.setData(getGoogleStoreUri(activity));
            activity.startActivity(intent);
            ret = false;
        }
        if (!ret) {
            Toast.makeText(activity, R.string.googleMapSupport, Toast.LENGTH_LONG).show();
        }
        return ret;
    }

    /**
     * 根据包名判断该应用是否已经安装
     */
    public static boolean apkExists(Context context, String packageName) {
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = pManager.getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pkg = packageInfoList.get(i);
            // Log.d(TAG, "pkg name " + pkg.packageName);
            if (pkg.packageName.equals(packageName))
                return true;
        }
        return false;
    }

    /**
     * 获取谷歌市场的下载地址
     */
    public static Uri getGoogleStoreUri(Activity activity) {
        SharedPreferences spf = activity.getSharedPreferences(SPUtils.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String storeUrl = spf.getString(Constants.STORE_URL_KEY, Constants.STORE_URL);
        // Log.i(TAG, "谷歌市场下载地址："+storeUrl);
        return Uri.parse(storeUrl);
    }

    /**
     * 获取谷歌服务的下载地址
     */
    public static Uri getGoogleServiceUri(Activity activity) {
        SharedPreferences spf = activity.getSharedPreferences(SPUtils.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String serviceUrl = spf.getString(Constants.SERVICE_URL_KEY, Constants.SERVICE_URL);
        // Log.i(TAG, "谷歌服务下载地址："+serviceUrl);
        return Uri.parse(serviceUrl);
    }

    public static boolean isBackGround(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            LogUtils.e(TAG, "appProcesses is NULL");
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName != null && appProcess.processName.equals(context.getPackageName())) {
                return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
            }
        }
        return false;
    }

    /**
     * 创建File对象，其中包含文件所在的目录以及文件的命名
     */
    public static void createFileWithByte(byte[] bytes) {
        String fileName = "saveBitmapBytes" + System.currentTimeMillis();
        File file = new File(Environment.getExternalStorageDirectory() + "/ALog", fileName);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public static void saveBitmap(Bitmap bitmap) {
        File f = new File(Environment.getExternalStorageDirectory() + "/ALog", "" + System.currentTimeMillis() + ".png");
        if (f.exists()) {
            f.delete();

        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
