package com.monitor.bus.activity.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.monitor.bus.utils.MUtils;
import com.monitor.bus.activity.R;
import com.monitor.bus.adapter.FileListAdapter;
import com.monitor.bus.consts.Constants;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class PhotoFragment extends BaseFragment{
	View view;
	private List<String> items = null; // items：存放显示的名称
	private List<String> paths = null; // paths：存放文件路径
	private List<String> sizes = null; // sizes：文件大小
	private String rootPath = Constants.IMAGE_PATH; // rootPath：起始文件夹
	private String currentPath = Constants.IMAGE_PATH;
	private TextView back_title;
	private ListView fileList;
	private int isOpen = 0; 
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_photo, container, false);
		setTitle();
		initView();
		getFilePathList(rootPath);
		return view;
	}

	private void setTitle() {
		TextView title= (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.pic_list));
	}
	@Override
	public boolean onBackPress() {
		File file = new File(currentPath);
		if (rootPath.equals(file.getParent())) {
			back_title.setVisibility(View.INVISIBLE);
		}
		if (rootPath.equals(currentPath)) {
		} else {
			getFilePathList(file.getParent());
			return true;
		}
		return false;
	}

	public void initView() {
		fileList = (ListView) view.findViewById(R.id.fileList);
		fileList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				File file = new File(paths.get(position));
				fileOrDirHandle(file, "short");
				back_title.setVisibility(View.VISIBLE);
				
			}
		});
		fileList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				File file = new File(paths.get(arg2));
				fileOrDirHandle(file, "long");
				return true;
			}
		});
		back_title = (TextView) view.findViewById(R.id.back_title);
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
				getFilePathList(file.getPath());
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
	private void getFilePathList(String filePath) {
		currentPath = filePath;
		/* 设置目前所在路径 */
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
						sizes.add(MUtils.fileSizeMsg(files[i]));
					}
				}
			} else {
				MUtils.commonToast(getContext(), R.string.not_imagefile);
			}
		} else {
			MUtils.commonToast(getContext(), R.string.not_imagefile);
		}
		/* 使用自定义的MyAdapter来将数据传入ListActivity */
		fileList.setAdapter(new FileListAdapter(getContext(), items, paths, sizes));
	}

	/**
	 * 删除文件或者文件夹
	 * 
	 * @param f
	 */
	private void delFileOrDir(File f) {
		final File f_del = f;
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.alert)
				.setIcon(R.drawable.alert)
				.setMessage(
						R.string.delMsg)
				.setPositiveButton(R.string.delDev, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						/* 删除文件或者文件夹 */
						if (f_del.isDirectory()) {
							if (delDir(f_del)) {
								MUtils.commonToast(getContext(),
										R.string.already_del);
								getFilePathList(f_del.getParent());
							} else {
								MUtils.commonToast(getContext(),
										R.string.wrong);
							}
						} else {
							if (delFile(f_del)) {
								MUtils.commonToast(getContext(),
										R.string.already_del);
								getFilePathList(f_del.getParent());
							} else {
								MUtils.commonToast(getContext(),
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
			type = MUtils.getMIMEType(f, true);
		}
		// 设置intent的file与MimeType
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
//		isCompleteExit = true;//启动系统自带相册浏览器时，强制修改程序运行状态为前台
	}

	class TextViewListener implements android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			File file = new File(currentPath);
			System.out.println(file.getParent());
			if (rootPath.equals(file.getParent())) {
				back_title.setVisibility(View.INVISIBLE);
			}
			if (!rootPath.equals(currentPath)) {
				getFilePathList(file.getParent());
			}

		}
	}


}
