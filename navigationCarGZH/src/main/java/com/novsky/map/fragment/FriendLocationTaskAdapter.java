package com.novsky.map.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.base.Constants;
import com.novsky.map.main.BDFriendLocationOperation;
import com.novsky.map.main.FriendBDPoint;

public class FriendLocationTaskAdapter extends BaseExpandableListAdapter {

	private ViewHolder viewHolder = null;
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<FriendBDPoint> list = null;
	private BDFriendLocationOperation operation;
	private List<String> receiveTimeList= new ArrayList<String>();
	private String newNavId="";

	/**
	 * 构造方法
	 * 
	 * @param mContext
	 * @param navs
	 */
	public FriendLocationTaskAdapter(Context mContext,
			BDFriendLocationOperation operation) {
		this.mContext = mContext;
		this.operation = operation;
		this.mInflater = LayoutInflater.from(mContext);
		List<String> receiveTime = operation.getReceiveTime();
		Set<String> hasSet = new HashSet<String>(receiveTime);
		receiveTimeList.addAll(hasSet);
		Collections.sort(receiveTimeList);
		Collections.reverse(receiveTimeList);
		
	}

	/**
	 * 父标签总数 时间
	 */
	@Override
	public int getGroupCount() {

		return receiveTimeList.size();
	}

	/**
	 * 对应时间节点下的子条目
	 */
	@Override
	public int getChildrenCount(int groupPosition) {
		String receiveTime = receiveTimeList.get(groupPosition);
		List<FriendBDPoint> byReceiveTime = operation
				.getByReceiveTime(receiveTime);
		return byReceiveTime.size();
	}

	/**
	 * 父标签
	 */
	@Override
	public Object getGroup(int groupPosition) {
		String receiveTime = receiveTimeList.get(groupPosition);
		return receiveTime;
	}

	/**
	 * 子标签
	 */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		String receiveTime = receiveTimeList.get(groupPosition);
		List<FriendBDPoint> byReceiveTime = operation
				.getByReceiveTime(receiveTime);
		FriendBDPoint friendBDPoint = byReceiveTime.get(childPosition);
		return friendBDPoint;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	/**
	 * 子标签条目id
	 */
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	/**
	 * 
	 */
	@Override
	public boolean hasStableIds() {
		return true;
	}

	/**
	 * 父布局
	 * 设置tag
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = View.inflate(mContext,
					R.layout.activity_friend_location_parent, null);
		}
		TextView parentView = (TextView) convertView.findViewById(R.id.parent);
		Button parentView_btn = (Button) convertView.findViewById(R.id.btn_parent);
		Button btn_parent_del = (Button) convertView.findViewById(R.id.btn_parent_del);
		Button btn_parent_show_info = (Button) convertView.findViewById(R.id.btn_parent_show_info);
		final String receiveTime = receiveTimeList.get(groupPosition);
		parentView.setText(receiveTime);
		parentView_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//地图展示逻辑
				//从数据库中获取  友邻位置集合
				ArrayList<FriendBDPoint> friendLocationList = (ArrayList<FriendBDPoint>) operation.getByReceiveTime(receiveTime);
				//跳转到地图界面  地图界面添加友邻位置
//				Intent notificationIntent = new Intent(mContext,
//						NaviStudioActivity.class);
//				Bundle bundle = new Bundle();
//				bundle.putSerializable("friendLocationList", friendLocationList);
//				bundle.putInt(Constants.INTENT_ACTION, Constants.INTENT_TYPE_FRIEND_LOCATION_LIST);
//				notificationIntent.putExtra("bundle",bundle);
				Intent notificationIntent = new Intent(mContext,
						NaviStudioActivity.class);
				notificationIntent.putExtra("friendLocationList", friendLocationList);
				notificationIntent.putExtra(Constants.INTENT_ACTION, Constants.INTENT_TYPE_FRIEND_LOCATION_LIST);
				mContext.startActivity(notificationIntent);
			}
		});
		
		//删除该条友邻
		btn_parent_del.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder alert=new AlertDialog.Builder(mContext);
				alert.setTitle("删除友邻位置");
				alert.setMessage("是否删除该条友邻位置?");
				alert.setPositiveButton("删除", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int index){
						boolean delete = operation.delete(receiveTime);
						if(delete){
							receiveTimeList.remove(receiveTime);
							notifyDataSetChanged();
						   Toast.makeText(mContext, "删除友邻位置成功!", Toast.LENGTH_SHORT).show();  
						}else{
						   Toast.makeText(mContext,"删除友邻位置失败!", Toast.LENGTH_SHORT).show();   
						}
					}
				});
				alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});
				alert.setNeutralButton("全部删除", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						boolean istrue=operation.delete();
						if(istrue){
							receiveTimeList.clear();
							notifyDataSetChanged();
						   Toast.makeText(mContext, "删除路线导航成功!", Toast.LENGTH_SHORT).show();  
						}else{
						   Toast.makeText(mContext,"删除路线导航失败!", Toast.LENGTH_SHORT).show();   
						}
					}
				});
				alert.create().show();
				
			}
		});
		
		//详情展示
		btn_parent_show_info.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final List<FriendBDPoint> detalInfoList = operation.getByReceiveTime(receiveTime);
				
				View layout = View.inflate(mContext,R.layout.dialog_friend_location_hailite,null);
				
				AlertDialog.Builder alert=new AlertDialog.Builder(mContext);
				alert.setTitle("友邻位置");
				//alert.setMessage("该条友邻位置详情");
				alert.setView(layout);
				alert.setCancelable(true);
				
				ListView lv_detal = (ListView) layout.findViewById(R.id.lv_detal);
				
				//设置数据适配器
				lv_detal.setAdapter(new DialogAdapter(mContext, detalInfoList));
				
				lv_detal.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 条目点击事件
						
						FriendBDPoint friendBDPoint = detalInfoList.get(position);
						
						Intent notificationIntent = new Intent(mContext,
								NaviStudioActivity.class);
						notificationIntent.putExtra("friendBDPoint", friendBDPoint);
						notificationIntent.putExtra(Constants.INTENT_ACTION, Constants.INTENT_TYPE_FRIEND_LOCATION_ITEM);
						mContext.startActivity(notificationIntent);
						
					}
				});
				
				alert.create().show();
				
			}
		});

		return convertView;
	}

	/**
	 * 子布局
	 */
	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_friend_location_task_msg, null);
			viewHolder.sendId = (TextView) convertView
					.findViewById(R.id.item_nav_line_id);
			viewHolder.content = (TextView) convertView
					.findViewById(R.id.item_nav_line_content);
			viewHolder.date = (TextView) convertView
					.findViewById(R.id.item_nav_line_date);
			viewHolder.naviBtn = (Button) convertView
					.findViewById(R.id.item_navi_line_btn);
			viewHolder.navLineImage = (ImageView) convertView
					.findViewById(R.id.nav_line_imageview);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		List<String> receiveTime = operation.getReceiveTime();
		String receiveTime2 = receiveTime.get(groupPosition);
		final List<FriendBDPoint> listChild = operation.getByReceiveTime(receiveTime2);
		final FriendBDPoint nav = listChild.get(childPosition);
		viewHolder.sendId.setText("友邻ID:" + String.valueOf(nav.getFriendID()));
		viewHolder.content.setText("位置信息:" + nav.getLon() + " " + nav.getLat());
		if (newNavId.equals(nav.getFriendID())) {
			Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.message_not_read);
			viewHolder.navLineImage.setImageBitmap(bm);
		} else {
			Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.message_received_box);
			viewHolder.navLineImage.setImageBitmap(bm);
		}
		viewHolder.naviBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				//地图显示  显示的是单个友邻
//				Intent notificationIntent = new Intent(mContext,
//						NaviStudioActivity.class);
//				String lineId = nav.getFriendID();
//				notificationIntent.putExtra("LINE_ID",
//						(!"".equals(lineId)) ? Integer.valueOf(lineId) : 0);
//				mContext.startActivity(notificationIntent);
				
				Intent notificationIntent = new Intent(mContext,
						NaviStudioActivity.class);
				FriendBDPoint friendBDPoint = listChild.get(childPosition);
				long rowId = friendBDPoint.getRowId();
				notificationIntent.putExtra("RERPORT_ROW_ID",rowId);
				mContext.startActivity(notificationIntent);
			}
		});
		return convertView;

	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public static class ViewHolder {
		/**
		 * 发送ID
		 */
		TextView sendId;
		/**
		 * 信息大小
		 */
		TextView size;
		/**
		 * 内容
		 */
		TextView content;
		/**
		 * 日期
		 */
		TextView date;
		/**
		 * 导航按钮
		 */
		Button naviBtn;
		/**
		 * 图片
		 */
		ImageView navLineImage;
	}

}