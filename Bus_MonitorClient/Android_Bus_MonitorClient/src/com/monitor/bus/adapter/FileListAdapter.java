package com.monitor.bus.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.monitor.bus.utils.MUtils;
import com.monitor.bus.activity.R;
/**
 * 抓拍文件列表的适配器
 */
/* 自定义的Adapter，继承android.widget.BaseAdapter */
public class FileListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private Bitmap mIcon_folder;
	private Bitmap mIcon_file;
	private Bitmap mIcon_image;
	private List<String> items;
	private List<String> paths;
	private List<String> sizes;

	/* MyAdapter的构造器 */
	public FileListAdapter(Context context, List<String> it, List<String> pa,
			List<String> si) {

		mInflater = LayoutInflater.from(context);
		items = it;
		paths = pa;
		sizes = si;
		mIcon_folder = BitmapFactory.decodeResource(context.getResources(),R.drawable.folder); // 文件夹的图文件
		mIcon_file = BitmapFactory.decodeResource(context.getResources(), R.drawable.file); // 文件的图文件
		mIcon_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.image); // 图片的图文件
	}

	/* 因继承BaseAdapter，需重写以下方法 */
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup par) {
		Bitmap bitMap = null;
		ViewHolder holder = null;
		if (convertView == null) {
			/* 使用自定义的list_items作为Layout */
			convertView = mInflater.inflate(R.layout.filelist_items, null);
			/* 初始化holder的text与icon */
			holder = new ViewHolder();
			holder.f_title = ((TextView) convertView.findViewById(R.id.f_title));
			holder.f_text = ((TextView) convertView.findViewById(R.id.f_text));
			holder.f_icon = ((ImageView) convertView.findViewById(R.id.f_icon));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		File f = new File(paths.get(position).toString());
		/* 设置文件或文件夹的文字与icon */
		holder.f_title.setText(f.getName());
		String f_type = MUtils.getMIMEType(f, false);
		
		if (f.isDirectory()) {
			holder.f_icon.setImageBitmap(mIcon_folder);
			holder.f_text.setText("");
		} else {
			holder.f_text.setText(sizes.get(position));
			if ("image".equals(f_type)) {
				bitMap = MUtils.fitSizePic(f);
				if (bitMap != null) {
					holder.f_icon.setImageBitmap(bitMap);
				} else {
					holder.f_icon.setImageBitmap(mIcon_image);
				}
				bitMap = null;
			} else {
				holder.f_icon.setImageBitmap(mIcon_file);
			}
		}
		return convertView;
	}


	/**
	 * 不单独写get set可以提高效率 class ViewHolder
	 * */
	private class ViewHolder {
		TextView f_title;
		TextView f_text;
		ImageView f_icon;
	}
}