package com.monitor.bus.activity;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class PhotoActivity extends Activity {
    public static final String KEY_FILE_PATH = "file_path";
    private static final String TAG = PhotoActivity.class.getSimpleName();

    private ImageView iv_photo;
    private String path;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_photo);
        path = getIntent().getStringExtra(KEY_FILE_PATH);
        initView();
    }

    private void initView() {
        Log.d(TAG, "path:" + path);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        iv_photo.setImageBitmap(bitmap);
    }
}