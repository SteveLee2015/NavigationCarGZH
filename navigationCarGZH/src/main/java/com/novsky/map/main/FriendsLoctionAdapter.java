package com.novsky.map.main;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapabc.android.activity.R;

/**
 * 友邻位置的Adapter
 * @author steve
 */
public class FriendsLoctionAdapter extends BaseAdapter {
	
	private ViewHolder viewHolder=null;
	private Context mContext=null;
	private List<Map<String,Object>> list=null;
	private LayoutInflater mInflater=null; 
	
	/**
	 * 构造方法
	 * @param mContext
	 * @param list
	 */
	public FriendsLoctionAdapter(Context mContext,List<Map<String,Object>> list){
		this.mContext=mContext;
		this.list=list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View contentView, ViewGroup parent) {
		if(contentView==null){
			viewHolder=new ViewHolder();
			mInflater=LayoutInflater.from(mContext);
			contentView=mInflater.inflate(R.layout.item_bdloc_friends, null);
			viewHolder.userId=(TextView)contentView.findViewById(R.id.friends_loc_user_address_tx);
			viewHolder.time=(TextView)contentView.findViewById(R.id.friends_loc_time);
			viewHolder.lon=(TextView)contentView.findViewById(R.id.friends_loc_lon);
			viewHolder.lat=(TextView)contentView.findViewById(R.id.friends_loc_lat);
			viewHolder.height=(TextView)contentView.findViewById(R.id.friends_loc_height);
			contentView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)contentView.getTag();
		}
		if(list!=null){
			viewHolder.userId.setText(String.valueOf(list.get(position).get("FRIEND_ID")));
			viewHolder.time.setText(String.valueOf(list.get(position).get("FRIEND_REPORT_TIME")));
			viewHolder.lon.setText(String.valueOf(list.get(position).get("FRIEND_LON")));
			viewHolder.lat.setText(String.valueOf(list.get(position).get("FRIEND_LAT")));
			viewHolder.height.setText(String.valueOf(list.get(position).get("FRIEND_HEIGHT")));
		}
		return contentView;
	}
	
	public void deleteAllData(){
		if(list!=null){
		    list.clear();
		}
	}
	public static class ViewHolder{
		TextView userId;
		TextView time;
		TextView lon;
		TextView lat;
		TextView height;
	}

}
