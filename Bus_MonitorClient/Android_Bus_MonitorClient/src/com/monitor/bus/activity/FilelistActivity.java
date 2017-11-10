package com.monitor.bus.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.FileListAdapter;
import com.monitor.bus.consts.Constants;

/**
 * 浏览抓拍文件
 */
public class FilelistActivity extends BaseActivity implements
		OnItemLongClickListener {

	private List<String> items = null; // items：存放显示的名称
	private List<String> paths = null; // paths：存放文件路径
	private List<String> sizes = null; // sizes：文件大小
	private String rootPath = Constants.IMAGE_PATH; // rootPath：起始文件夹
	private TextView path_view;
	private TextView back_title;
	private ListView fileList;
	private int isOpen = 0; 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	/**
	 * 重写返回键功能:返回上一级文件夹
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 是否触发按键为back键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			path_view = (TextView) findViewById(R.id.path_view);
			File file = new File(path_view.getText().toString());
			if (rootPath.equals(file.getParent())) {
				back_title.setVisibility(View.INVISIBLE);
			}
			if (rootPath.equals(path_view.getText().toString())) {
				return super.onKeyDown(keyCode, event);
			} else {
				getFileDir(file.getParent());
				return true;
			}

			// 如果不是back键正常响应
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		MyUtil.initTitleName(this,R.layout.filelist,R.string.pic_list);
		path_view = (TextView) findViewById(R.id.path_view);
		fileList = (ListView) findViewById(R.id.fileList);
		fileList.setOnItemLongClickListener(this);
		fileList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				File file = new File(paths.get(position));
				fileOrDirHandle(file, "short");
				back_title.setVisibility(View.VISIBLE);
				
			}
		});
		getFileDir(rootPath);

		back_title = (TextView) findViewById(R.id.back_title);
		back_title.setOnClickListener(new TextViewListener());
	}

	/**
	 * 设置ListItem被点击时要做的动作
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(paths.get(position));
		fileOrDirHandle(file, "short");
		back_title.setVisibility(View.VISIBLE);
	}
	 */

	/**
	 * 设置ListItem被长按时要做的动作
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		File file = new File(paths.get(arg2));
		fileOrDirHandle(file, "long");
		return true;
	}

	/**
	 * 处理文件或者目录的方法
	 * 
	 * @param file
	 * @param flag
	 */
	private void fileOrDirHandle(final File file, String flag) {
		/* 点击文件时的OnClickListener 
		OnClickListener listener_list = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					delFileOrDir(file);
				}
			}
		};*/

		if (flag.equals("long")) {
			delFileOrDir(file);
			//String[] list_file = { getString(R.string.delDev) }; // file操作
			/* 选择一个文件或者目录时，跳出要如何处理文件的ListDialog 
			new AlertDialog.Builder(FilelistActivity.this)
					.setTitle(file.getName())
					.setIcon(R.drawable.list)
					.setItems(list_file, listener_list)
					.setPositiveButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();*/
		} else {
			if (file.isDirectory()) {
				getFileDir(file.getPath());
			} else {
				openFile(file);
			}
		}
	}

	/**
	 * 取得文件结构的方法
	 * 
	 * @param filePath
	 */
	private void getFileDir(String filePath) {
		/* 设置目前所在路径 */
		path_view.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		sizes = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();
		if (files != null) {

			if (files.length > 0) {
				/* 将所有文件添加ArrayList中 */
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						items.add(files[i].getName());
						paths.add(files[i].getPath());
						sizes.add("");
					}
				}
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
						items.add(files[i].getName());
						paths.add(files[i].getPath());
						sizes.add(MyUtil.fileSizeMsg(files[i]));
					}
				}
			} else {
				MyUtil.commonToast(this, R.string.not_imagefile);
			}
		} else {
			MyUtil.commonToast(this, R.string.not_imagefile);
		}
		/* 使用自定义的MyAdapter来将数据传入ListActivity */
		fileList.setAdapter(new FileListAdapter(this, items, paths, sizes));
	}

	/**
	 * 删除文件或者文件夹
	 * 
	 * @param f
	 */
	private void delFileOrDir(File f) {
		final File f_del = f;
		new AlertDialog.Builder(FilelistActivity.this)
				.setTitle(R.string.alert)
				.setIcon(R.drawable.alert)
				.setMessage(
						R.string.delMsg)
				.setPositiveButton(R.string.delDev, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						/* 删除文件或者文件夹 */
						if (f_del.isDirectory()) {
							if (delDir(f_del)) {
								MyUtil.commonToast(FilelistActivity.this,
										R.string.already_del);
								getFileDir(f_del.getParent());
							} else {
								MyUtil.commonToast(FilelistActivity.this,
										R.string.wrong);
							}
						} else {
							if (delFile(f_del)) {
								MyUtil.commonToast(FilelistActivity.this,
										R.string.already_del);
								getFileDir(f_del.getParent());
							} else {
								MyUtil.commonToast(FilelistActivity.this,
										R.string.wrong);
							}
						}
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	/**
	 * 新建文件
	 * 
	 * @param file
	 * @return
	 */
	public boolean newFile(File f) {
		try {
			f.createNewFile();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 删除单个文件
	 * 
	 * @param file
	 * @return
	 */
	public boolean delFile(File f) {
		boolean ret = false;
		try {
			if (f.exists()) {
				f.delete();
				ret = true;
			}
		} catch (Exception e) {
			return false;
		}
		return ret;
	}

	/**
	 * 删除文件夹
	 * 
	 * @param file
	 * @return
	 */
	public boolean delDir(File f) {
		boolean ret = false;
		try {
			if (f.exists()) {
				File[] files = f.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						if (!delDir(files[i])) {
							return false;
						}
					} else {
						files[i].delete();
					}
				}
				f.delete(); // 删除空文件夹
				ret = true;
			}
		} catch (Exception e) {
			return false;
		}
		return ret;
	}

	/**
	 * 打开文件
	 * 
	 * @param f
	 */
	private void openFile(File f) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		// 跳出列表供选择
		String type = "*/*";
		if (isOpen == 0) {
			type = MyUtil.getMIMEType(f, true);
		}
		// 设置intent的file与MimeType
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
		isCompleteExit = true;//启动系统自带相册浏览器时，强制修改程序运行状态为前台
	}

	class TextViewListener implements android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			path_view = (TextView) findViewById(R.id.path_view);
			File file = new File(path_view.getText().toString());
			System.out.println(file.getParent());
			if (rootPath.equals(file.getParent())) {
				back_title.setVisibility(View.INVISIBLE);
			}
			if (!rootPath.equals(path_view.getText().toString())) {
				getFileDir(file.getParent());
			}

		}
	}

	@Override
	protected void onResume() {
		getFileDir(path_view.getText().toString());
		super.onResume();
	}

}
