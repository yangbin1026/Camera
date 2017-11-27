package com.monitor.bus.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.monitor.bus.activity.R;
import com.monitor.bus.consts.Constants;
/**
 * 公交设备信息列表适配器
 */
public class MyExpandableListAdapter extends SimpleExpandableListAdapter {
	List<HashMap<String, String>> groups = new ArrayList<HashMap<String, String>>();
	
	private LayoutInflater inflater;
	
	private ExpandableListView expandListView;
	private View groupView;
	private ImageView expand_image;
	private ImageView dev_image;
	private TextView textView;

	public MyExpandableListAdapter(Context context,
			List<HashMap<String, String>> groups, int viewGroups,
			String[] group_strings, int[] group_id,
			List<List<Map<String, String>>> childs, int viewChilds,
			String[] child_strings, int[] child_id,
			ExpandableListView expandView) {
		super(context, groups, viewGroups, group_strings, group_id, childs,
				viewChilds, child_strings, child_id);
		inflater = LayoutInflater.from(context);
		this.expandListView = expandView;
		this.groups = groups;

	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if( null == groupView ){
			groupView = inflater.inflate(R.layout.dev_listview_groups, null);
		}
		expand_image = (ImageView) groupView.findViewById(R.id.expanable_image);
		dev_image = (ImageView) groupView.findViewById(R.id.dev_image);
		if ("0".equals(groups.get(groupPosition).get("login_status"))) {
			expand_image.setImageResource(R.drawable.list_line);
			dev_image.setImageResource(R.drawable.dev_alarm);
			expandListView.collapseGroup(groupPosition);
		} else if("1".equals(groups.get(groupPosition).get("login_status"))){
			//dev_image.setImageResource(R.drawable.groups);
			dev_image.setImageResource(R.drawable.g3);
			// 设置展开时的图标
			if (!isExpanded) {
				expand_image.setImageResource(R.drawable.sq_plus);
			} else {
				expand_image.setImageResource(R.drawable.sq_minus);
			}
		} else if("2".equals(groups.get(groupPosition).get("login_status"))){
			//dev_image.setImageResource(R.drawable.groups);
			dev_image.setImageResource(R.drawable.wifi);
			// 设置展开时的图标
			if (!isExpanded) {
				expand_image.setImageResource(R.drawable.sq_plus);
			} else {
				expand_image.setImageResource(R.drawable.sq_minus);
			}
		} else if("3".equals(groups.get(groupPosition).get("login_status"))){
			//有线
			dev_image.setImageResource(R.drawable.g3);
			// 设置展开时的图标
			if (!isExpanded) {
				expand_image.setImageResource(R.drawable.sq_plus);
			} else {
				expand_image.setImageResource(R.drawable.sq_minus);
			}
		}
		else{
			dev_image.setImageResource(R.drawable.list_line);
			expand_image.setImageResource(R.drawable.list_line);
			expandListView.collapseGroup(groupPosition);
		}
		textView = (TextView) groupView.findViewById(R.id.textGroup);
		textView.setText((groups.get(groupPosition)).get(Constants.DEVLIST_GROUP_KEY));
		return groupView;
	}

	// 更新指定item的数据
	public void updateView(int index, boolean success) {
		if (groupView != null) {
			if(success){
				groups.get(index).put("login_status", "1");
			}else{
				groups.get(index).put("login_status", "0");
			}
			notifyDataSetChanged();
		}
	}
}
