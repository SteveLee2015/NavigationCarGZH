package com.novsky.map.fragment;

import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.R;
import com.novsky.map.main.BDFriendLocationOperation;
import com.novsky.map.main.FriendBDPoint;
import com.novsky.map.util.Config;

/**
 * 友邻位置  深圳海力特 fuck
 * @author llg
 */
public class FriendLocationTaskFragment extends Fragment{
	/**
	 *日志标识
	 */
	private static final String TAG="FriendLocationTaskFragment";
	/**
	 *导航任务的ListView对象
	 */
	private ExpandableListView expandistView=null;
	/**
	 *绑定在ListView组件的所有短信数据适配器对象  
	 */
	private FriendLocationTaskAdapter mFriendLocationTaskAdapter=null;
    private TextView noLinePrompt=null;	
	private BDRNSSLocation mRnssLocation=null;
	
	
	
	private BDFriendLocationOperation operation=null;
	
    private BDRNSSLocationListener mBDRNSSLocationListener=new BDRNSSLocationListener(){
		@Override
		public void onLocationChanged(BDRNSSLocation arg0) {
			mRnssLocation=arg0;
		}
		@Override
		public void onProviderDisabled(String arg0){}

		@Override
		public void onProviderEnabled(String arg0) {}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2){}
		
	};
	
	private BDCommManager mBDCommManager=null;
	
	private List<FriendBDPoint> navs =null; 
	
	
	/**
	 * 广播接收者
	 * SMSReceiver 中 发送该广播
	 */
	private BroadcastReceiver receiver=new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String sendTimeStr=intent.getStringExtra("sendTimeStr");
			List<FriendBDPoint> byReceiveTime = operation.getByReceiveTime(sendTimeStr);
			mFriendLocationTaskAdapter=new FriendLocationTaskAdapter(getActivity(), operation);
			if (expandistView!=null) {
				expandistView.setAdapter(mFriendLocationTaskAdapter);
				//mFriendLocationTaskAdapter.notifyDataSetChanged();
			}
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBDCommManager=BDCommManager.getInstance(getActivity().getApplicationContext());
		operation=new BDFriendLocationOperation(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView=View.inflate(getActivity(),R.layout.activity_nav_friend_location_mutil, null);
		expandistView=(ExpandableListView)rootView.findViewById(R.id.nav_line_expand_listview);
		expandistView.setCacheColorHint(0);
		expandistView.setGroupIndicator(null);
		noLinePrompt=(TextView)rootView.findViewById(R.id.no_nav_line_prompt);
		/*1.从数据库中查询所有的短信数据,如果数据库没有数据则发送指令请求最新插入的数据*/
		navs = operation.getAll();
		mFriendLocationTaskAdapter=new FriendLocationTaskAdapter(getActivity(), operation);
		if(navs.size()>1000){
			AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
			builder.setTitle("提示");
			builder.setMessage("友邻位置数量超过1000条,请删除不必要的友邻位置信息!");
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
		if(navs.size()>0){
			/*2.把数据转换成List*/
			expandistView.setAdapter(mFriendLocationTaskAdapter);
			/*增加选项*/
			//父条目的点击事件   长按时间   子条目的点击事件 长按事件
			
			//子条目点击事件
			expandistView.setOnChildClickListener(new OnChildClickListener() {
				
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					
					
					
					return false;
				}
			});
			
			//父条目点击事件  展开 与 不展开
			expandistView.setOnGroupClickListener(new OnGroupClickListener() {

				@Override
				public boolean onGroupClick(ExpandableListView parent, View v,
						int groupPosition, long id) {
					
					Toast.makeText(getActivity(), groupPosition+"", 0).show();
					return true;
				}
				
			});
		}else{
			noLinePrompt.setText("当前没有友邻位置相关信息！");
			noLinePrompt.setVisibility(View.VISIBLE);
		}
		return rootView;
	}

	public void onResume() {
		super.onResume();
		try {
			mBDCommManager.addBDEventListener(mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		//com.bd.action.FRIEND_LOCATIONMESSAGE_ACTION
		IntentFilter filter=new IntentFilter("com.bd.action.FRIEND_LOCATIONMESSAGE_ACTION");
		getActivity().registerReceiver(receiver, filter);
		//取消线路导航 通知
		NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(Config.FRIENDS_LOC_NOTIFICATION);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(receiver);
		try {
			mBDCommManager.removeBDEventListener(mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(operation!=null){
		   operation.close();
		   operation=null;
		}
	}
	
}
