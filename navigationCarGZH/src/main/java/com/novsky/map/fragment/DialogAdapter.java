package com.novsky.map.fragment;

import java.util.List;

import com.mapabc.android.activity.R;
import com.novsky.map.main.FriendBDPoint;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
/**
 * dialog用到的adapter
 * @author Administrator
 *
 */
public class DialogAdapter extends BaseAdapter {

	private Context mContext;
	private List<FriendBDPoint>  list;
	public DialogAdapter(Context mContext,List<FriendBDPoint>  list){
		this.mContext = mContext;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		FriendBDPoint friendBDPoint = list.get(position);
		if (convertView==null) {
			convertView = View.inflate(mContext, R.layout.dialog_friend_location_hailite_item, null);
			TextView tv_item_info = (TextView) convertView.findViewById(R.id.tv_item_info);
			String friendDetal ="友邻id:"+ friendBDPoint.getFriendID()+"经度:"+friendBDPoint.getLon()+"纬度:"+friendBDPoint.getLat()+"\r\n";
			tv_item_info.setText(friendDetal);
		}
		return convertView;
	}

}
