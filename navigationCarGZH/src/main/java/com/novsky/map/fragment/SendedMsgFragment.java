package com.novsky.map.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.R;
import com.novsky.map.main.Item;
import com.novsky.map.main.MsgZhuanFaActivity;
import com.novsky.map.main.SendMsgAdapter;
import com.novsky.map.util.DatabaseHelper.CustomColumns;
import com.novsky.map.util.DatabaseOperation;
import com.novsky.map.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 北斗短信箱
 * @author steve
 */
public class SendedMsgFragment extends Fragment {
	
	/**
	 *日志标识
	 */
	private static final String TAG="SendedMsgFragment";
	/**
	 *北斗信箱的短信内容的ListView对象
	 */
	private ListView listView=null;
	/**
	 *绑定在ListView组件的所有短信数据适配器对象  
	 */
	private SendMsgAdapter adapter=null;
	private List<Item> items = new ArrayList<Item>();;
	List<Item> toRemoveItems = new ArrayList<Item>();
    private TextView noMessagePrompt=null;	
	private int  pageFirstIndex=0;
	private BDRNSSLocation mRnssLocation=null;
	
	
	private Button btn_delete;
	private Button btn_select_all;
	private Button btn_cancel_all;
	LinearLayout ll_checked_title ;

	private BDCommManager mBDCommManager=null;

	private Button queryBtn=null;

	private EditText queryCondEditText=null;
	private DatabaseOperation operation;

	private TextView tv_msg_sum;
	private TextView tv_unread_msg_sum;
	private TextView tv_sended_msg_sum;


	List<Map<String,Object>> list;

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

	private BroadcastReceiver receiver=new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			// 数据更新
			long rowId=intent.getLongExtra("ROWID", 0);
			DatabaseOperation operation=new DatabaseOperation(getActivity());
			Cursor cursor=operation.get(rowId);
			List<Map<String,Object>> mlist=build(cursor);
			Map<String,Object> map=mlist.get(0);
			
			
			//将查询到的数据封装到item对象中
			Item item = new Item();
			toItemObject(map, item);
			items.add(0, item);
			adapter.notifyDataSetChanged();
			
			if(cursor!=null){
				cursor.close();
			}
			if(operation!=null){
			    operation.close();
			}
		}
	};


	
	/**
	 * 转化为item对象
	 * @param map
	 * @param item
	 */
	private void toItemObject(Map<String, Object> map, Item item) {
		String send_id = String.valueOf(map.get("SEND_ID"));
		String send_content = String.valueOf(map.get("SEND_CONTENT"));
		String send_date = String.valueOf(map.get("SEND_DATE"));
		//Long rowId = (Long) (map.get("KEY_ROWID"));
		Long rowId = Long.valueOf(String.valueOf(map.get("COLUMNN_ID")));  
		Drawable message_flag = (Drawable) (map.get("MESSAGE_FLAG"));
		Boolean checked = false;
		
		item.send_id=send_id;
		item.send_content=send_content;
		item.send_date=send_date;
		item.message_flag=message_flag;
		item.checked=checked;
		item.rowId = rowId;
	}
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBDCommManager=BDCommManager.getInstance(getActivity().getApplicationContext());
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (items==null) {
			items=new ArrayList<Item>();
		}
		if (operation==null) {
			
			operation = new DatabaseOperation(getActivity());
		}
		
		View rootView=inflater.inflate(R.layout.fragment_sended_msg, null);
		
		ll_checked_title = (LinearLayout) rootView.findViewById(R.id.ll_checked_title);
		btn_delete = (Button) rootView.findViewById(R.id.btn_delete);
		btn_select_all = (Button) rootView.findViewById(R.id.btn_select_all);
		btn_cancel_all = (Button) rootView.findViewById(R.id.btn_cancel_all);
		
		listView=(ListView)rootView.findViewById(R.id.msg_sended_listview);
		listView.setCacheColorHint(0);
		View headerView=inflater.inflate(R.layout.activit_bd_message_query, null);
		listView.addHeaderView(headerView);
		noMessagePrompt=(TextView)rootView.findViewById(R.id.no_message_prompt);
		
		tv_msg_sum = (TextView) headerView.findViewById(R.id.tv_msg_sum);
		tv_unread_msg_sum = (TextView) headerView.findViewById(R.id.tv_unread_msg_sum);
		tv_sended_msg_sum = (TextView) headerView.findViewById(R.id.tv_sended_msg_sum);

		//关键字查询
		queryCondEditText=(EditText)headerView.findViewById(R.id.message_condit_query);
		queryBtn=(Button)headerView.findViewById(R.id.query_message_btn);

		initListener();
		
		return rootView;
	}

	/**
	 * 点击事件
	 */
	private void initListener() {

		// 删除所选中的条目
		btn_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				//删除所选信息
				deleteSelecedMsg();
				//更新 状态
				setMsgInfo();
			}

		});

		// 全选
		btn_select_all.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				int k = items.size();
				for (int i = 0; i < k; i++) {
					items.get(i).checked = true;
				}
				adapter.notifyDataSetChanged();
			}
		});
		// 取消全选
		btn_cancel_all.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				int k = items.size();
				for (int i = 0; i < k; i++) {
					items.get(i).checked = false;
				}
				//把三个按钮隐藏掉
				ll_checked_title.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
			}
		});

		// 查询
		queryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String condition = queryCondEditText.getText().toString();
				Toast.makeText(getActivity(), condition, Toast.LENGTH_SHORT)
						.show();
				DatabaseOperation myoperation = new DatabaseOperation(
						getActivity());
				Cursor mcursor = myoperation.getMessageByCond(condition);
				List<Map<String, Object>> tempList = build(mcursor);
				// 转化为item对象
				if (items != null) {
					items.clear();
					Item item = null;
					for (Map<String, Object> map : tempList) {

						item = new Item();
						toItemObject(map, item);
						items.add(item);

					}
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						return;
					}
				}
				adapter.notifyDataSetChanged();
				mcursor.close();
				myoperation.close();
			}
		});

///////////////////////////////////////////////////////////////////////////////////////////////////////

			/*增加选项*/
		//单击
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				int temp=position-1;
				String rowId=String.valueOf(list.get(temp).get("COLUMNN_ID"));
					/*转发功能*/
				Intent intent=new Intent();
				intent.putExtra("MSG_DEL_ID",rowId);
				intent.setClass(getActivity(),MsgZhuanFaActivity.class);
				getActivity().startActivity(intent);
			}
		});
		//长按
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
										   int index, long arg3) {
				final int temp=index-1;
				//final int temp=index;
				AlertDialog.Builder alert=new AlertDialog.Builder(getActivity());
				alert.setTitle("删除短信");
				alert.setMessage("是否删除该条短信?");
				alert.setPositiveButton("删除", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int index1) {
						if (operation==null) {
							operation=new DatabaseOperation(getActivity().getApplicationContext());
						}
						int id=Integer.valueOf(String.valueOf(list.get(temp).get("COLUMNN_ID")));
						boolean istrue=operation.delete(id);
						if(istrue){
							list.remove(temp);
							adapter.notifyDataSetChanged();
							onResume();
							Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.bd_msg_del_success), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.bd_msg_del_fail), Toast.LENGTH_SHORT).show();
						}
					}
				});
				alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
				alert.show();
				return true;
			}
		});




	}


	protected void deleteSelecedMsg() {
		


		//遍历 checked 为true 删除
		int k = items.size();
		
		for(int i=0; i<k; i++){
			if (items.get(i).checked) {//被选中
				//删除 数据库数据
				Long rowId = items.get(i).rowId;
				operation.delete(rowId);
				//更新list
				//items.remove(items.get(i));
				toRemoveItems.add(items.get(i));
				//adapter.notifyDataSetChanged();
			} 
		}	
		
		items.removeAll(toRemoveItems);
		toRemoveItems.clear();
		if (toRemoveItems.size()==0) {
			//隐藏 三个按钮
			ll_checked_title.setVisibility(View.GONE);
		}
		//更新界面
		adapter.notifyDataSetChanged();
		
	}

	@Override
	public void onStart() {
		super.onStart();
		//注册广播
		IntentFilter filter=new IntentFilter("com.bd.action.MESSAGE_ACTION");
		getActivity().registerReceiver(receiver, filter);

		try {
			mBDCommManager.addBDEventListener(mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}

	}

	public void onResume() {
		super.onResume();


		/*1.从数据库中查询所有的短信数据,如果数据库没有数据则发送指令请求最新插入的数据*/
		Cursor cursor=operation.getAll();
		if(cursor.getCount()>1000){
			AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
			builder.setTitle("提示");
			builder.setMessage("短信数量超过1000条,请删除不必要的短信!");
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
		showListView(cursor);
		cursor.close();
		operation.close();

		//设置已读未读短信条数
		
		setMsgInfo();


		
		//销毁 notification
		if (Utils.destoryNotification!=null) {
			Utils.destoryMessageNotification(getActivity());
		}
	}


	private void setMsgInfo() {
		int msg_sum = operation.getReceiveMessages().getCount();
		int unRead_msg_sum = operation.getUnReadMessages().getCount();
		int sended_msg_sum = operation.getSendMessages().getCount();
		tv_msg_sum.setText(msg_sum+"");
		tv_unread_msg_sum.setText(unRead_msg_sum+"");
		tv_sended_msg_sum.setText(sended_msg_sum+"");
	}
	
	
	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(receiver);
		try {
			mBDCommManager.removeBDEventListener(mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}

	private List<Map<String,Object>> build(Cursor mcursor){
		List<Map<String,Object>> templist=new ArrayList<Map<String,Object>>();
		Calendar calendar=Calendar.getInstance();
		if(mRnssLocation!=null){
			calendar.setTimeInMillis(mRnssLocation.getTime());	
		}else{
			calendar.setTimeInMillis(new Date().getTime());	
		}
		int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH)+1;
		int day=calendar.get(Calendar.DAY_OF_MONTH);
		while (mcursor.moveToNext()) {
			Map<String,Object> map=new HashMap<String, Object>();
			String msg=mcursor.getString(mcursor.getColumnIndex(CustomColumns.COLUMNS_MSG_CONTENT));
			if(msg.length()>28){
			   msg=msg.substring(0, 26)+"...";	
			}
			map.put("COLUMNN_ID", mcursor.getString(mcursor.getColumnIndex(CustomColumns._ID)));
			map.put("SEND_CONTENT_SIZE", mcursor.getString(mcursor.getColumnIndex(CustomColumns.COLUMNS_MSG_LEN)));
			map.put("SEND_CONTENT", msg);
			String date=mcursor.getString((mcursor.getColumnIndex(CustomColumns.COLUMNS_SEND_TIME)));
			/**
			 *1.如果短信日期不是当天的信息,则仅仅显示月、日
			 *2.如果短信日期不是当年的信息,则显示年、月、日 
			 */
			String reg="[0-9,-]{2}:[0-9,-]{2}";
			Pattern pattern=Pattern.compile(reg);
			Matcher matcher = pattern.matcher(date); 
			if(matcher.matches()){//如果事件格式00:00
				date=date.replaceAll("-", "0");
			}else{//時間格式yyyy-MM-dd HH:mm:ss
				String[] date1=date.split(" ");
				String [] time=date1[0].split("-");
				if(time[2].equals(Utils.showTwoBitNum(day))&&
						time[1].equals(Utils.showTwoBitNum(month))&&
						time[0].equals(String.valueOf(year))){
					//显示时分秒
					date=date1[1];
				}else if(time[2].equals(Utils.showTwoBitNum(day))&&
						(!time[1].equals(Utils.showTwoBitNum(month))||
								!time[0].equals(String.valueOf(year)))){
					//显示月、日
					date=time[1]+"月"+time[2]+"日";
				}else if(!time[2].equals(Utils.showTwoBitNum(day))&&
						!time[1].equals(Utils.showTwoBitNum(month))&&
						!time[0].equals(String.valueOf(year))){
					//显示年、月、日
					date=time[0]+"年"+time[1]+"月"+time[2]+"日";
				}
			}
			map.put("SEND_DATE", date);
			String flag=mcursor.getString((mcursor.getColumnIndex(CustomColumns.COLUMNS_FLAG)));
			if(flag!=null&&"0".equals(flag)){
				String receiverAddress=mcursor.getString(mcursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS));
				String receiverAddressName=Utils.getContactNameFromPhoneNum(getActivity(),receiverAddress);
				map.put("SEND_ID", "发件人:"+((receiverAddressName!=null&&!receiverAddressName.equals(""))?receiverAddressName:receiverAddress));
				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_received_box));
			}else if(flag!=null&&"1".equals(flag)){
				String senderAddress=mcursor.getString(mcursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS));
				if(senderAddress.contains("(")&&senderAddress.contains(")")){
				  String senderAddressNumber=senderAddress.substring(senderAddress.indexOf("(")+1,senderAddress.indexOf(")") );
				  String senderAddressName=Utils.getContactNameFromPhoneNum(getActivity(),senderAddressNumber);
				  if(senderAddressName!=null&&!"".equals(senderAddressName)){
					  senderAddress=senderAddressName;
				  }
				}
				map.put("SEND_ID", "收件人:"+senderAddress);
				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_sended_box));
			}else if(flag!=null&&"2".equals(flag)){
				String userid=mcursor.getString(mcursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS));
				if(userid!=null&&!"".equals(userid)){
					map.put("SEND_ID", userid);
				}else{
					map.put("SEND_ID", "草稿");
				}
				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_caogao));
			}else if(flag!=null&&"3".equals(flag)){
				map.put("SEND_ID", "发件人: "+mcursor.getString(mcursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS)));
				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_not_read));
			}else {
				
			}
			templist.add(map);
		}
		return templist;
	}
	
	
	private void showListView(Cursor cursor){
		if(cursor!=null&&cursor.getCount()>0){
			list = build(cursor);
			//把数据转为  item对象 放到items集合中
			Item item = null;
			items.clear();
			for (Map<String, Object> map : list) {
				
				item = new Item();
				toItemObject(map, item);
				items.add(item);
				
			}
//
//			if (adapter!=null) {
//				adapter.notifyDataSetChanged();
//				return;
//			}

			/*2.把数据转换成List*/
			adapter=new SendMsgAdapter(getActivity(), items);

			listView.setAdapter(adapter);



			//回调
			adapter.setOnCheckBoxClickLinstener(new SendMsgAdapter.OnCheckBoxClickLinstener() {

				@Override
				public void onCheckBoxClicked(int positon,List<Item> items) {

					for (Item item : items) {

						if (item.checked) {

							ll_checked_title.setVisibility(View.VISIBLE);
							return;
						}
					}

					ll_checked_title.setVisibility(View.GONE);

				}
			});

			

		}else{
			items.clear();
			noMessagePrompt.setText("当前没有北斗短信！");
			noMessagePrompt.setVisibility(View.VISIBLE);
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.delete_all_messages:
				AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
				builder.setTitle("全部删除短信");
				builder.setMessage("是否全部删除短信?");
				builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface arg0, int arg1) {
						final	DatabaseOperation operation=new DatabaseOperation(getActivity());
						boolean istrue=operation.delete();
						operation.close();
						if(istrue){
							adapter.deleteAllMessageData();
							adapter.notifyDataSetChanged();
							noMessagePrompt.setText("当前没有北斗短信!");
							noMessagePrompt.setVisibility(View.VISIBLE);
							Toast.makeText(getActivity(), "删除所有短信成功!", Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(getActivity(), "删除所有短信失败!", Toast.LENGTH_LONG).show();
						}				
					}	
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
				builder.show();
				break;
			default:
				break;						
		}
		return super.onOptionsItemSelected(item);
	}
	
}
