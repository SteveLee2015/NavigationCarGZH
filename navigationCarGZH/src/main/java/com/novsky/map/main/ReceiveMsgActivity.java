package com.novsky.map.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.main.SendMsgAdapter.OnCheckBoxClickLinstener;
import com.novsky.map.util.DatabaseHelper.CustomColumns;
import com.novsky.map.util.DatabaseOperation;
import com.novsky.map.util.TabSwitchActivityData;
import com.novsky.map.util.Utils;
/**
 * 收件箱
 * @author llg
 */
public class ReceiveMsgActivity extends Activity {
	
	private ListView listView=null;
	private SendMsgAdapter adapter=null;
	private List<Item> items=null;
	List<Item> toRemoveItems = new ArrayList<Item>();
//	private List<Map<String,Object>> list=null;
	private final String TAG="SendedMsgActivity";
	private Context mContext=this;
	private TabSwitchActivityData mInstance=TabSwitchActivityData.getInstance();
	private DatabaseOperation operation=null;
	
	/**
	 * 广播接受者
	 */
	private BroadcastReceiver receiver=new BroadcastReceiver(){
		//接受到新的数据添加到首部
		@Override
		public void onReceive(Context context, Intent intent){
			long rowId=intent.getLongExtra("ROWID", 0);
			DatabaseOperation operation=new DatabaseOperation(mContext);
			//查询
			Cursor cursor=operation.get(rowId);
			List<Map<String,Object>> mlist=build(cursor);
			Map<String,Object> map=mlist.get(0);
//			List<Map<String,Object>> mlist=build(cursor);
//			Map<String,Object> map=mlist.get(0);
//			list.add(0, map);
			//将查询到的数据封装到item对象中
			Item item = new Item();
			toItemObject(map, item);
			items.add(0, item);
			adapter.notifyDataSetChanged();
		}

	};
	private Button btn_delete;
	private Button btn_select_all;
	private Button btn_cancel_all;
	LinearLayout ll_checked_title ;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sended_msg);
		LinearLayout linear=(LinearLayout)this.findViewById(R.id.msg_sended_layout);
		ll_checked_title = (LinearLayout) this.findViewById(R.id.ll_checked_title);
		btn_delete = (Button) this.findViewById(R.id.btn_delete);
		btn_select_all = (Button) this.findViewById(R.id.btn_select_all);
		btn_cancel_all = (Button) this.findViewById(R.id.btn_cancel_all);
        /* 增加背景图片 */
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg);
		mInstance.setTabFlag(1);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		bd.setDither(true);
		linear.setBackgroundDrawable(bd);
		listView=(ListView)this.findViewById(R.id.msg_sended_listview);
		operation=new DatabaseOperation(this);
		
		//TODO  怎么清除notification??
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (items==null) {
			items=new ArrayList<Item>();
		}
		
//		list=new ArrayList<Map<String,Object>>();
		Cursor cursor=operation.getAll();
		showListView(cursor);
		cursor.close();
		//注册一个广播接受者
		// SMSReceiver 中接收该广播
		IntentFilter filter=new IntentFilter("com.bd.action.MESSAGE_ACTION");
		this.registerReceiver(receiver, filter);
		//TODO  怎么清除notification??
		
		if (Utils.destoryNotification!=null) {
			Utils.destoryMessageNotification(mContext);
		}
		
		initListener();
	}
	
	
	private void deleteSelecedMsg() {

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
	/**
	 * 各种点击事件
	 */
	private void initListener() {

		//删除所选中的条目
		btn_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				deleteSelecedMsg();
			}

		});
		
		//全选
		btn_select_all.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int k = items.size();
				for(int i=0; i<k; i++){
					 items.get(i).checked = true;	
				}	
				adapter.notifyDataSetChanged();
			}
		});
		//取消全选
		btn_cancel_all.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int k = items.size();
				for(int i=0; i<k; i++){
					 items.get(i).checked = false;	
				}	
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(receiver);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.delete_message_menu, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.delete_all_messages:
				AlertDialog.Builder builder=new AlertDialog.Builder(ReceiveMsgActivity.this.getParent());
				builder.setTitle("全部删除短信");
				builder.setMessage("是否全部删除短信?");
				builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						deleteAllMsg();				
					}

				});
				
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {}
				});
				builder.show();
				break;
			default:
				break;						
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void deleteAllMsg() {
		boolean istrue=operation.deleteReceiveMessages();
		if(istrue){
			//list.clear();
			items.clear();
			adapter.notifyDataSetChanged();
			Toast.makeText(mContext, "删除所有收件箱内容成功!", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(mContext, "删除所有收件箱内容失败!", Toast.LENGTH_LONG).show();
		}
	}	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(operation!=null){
		   operation.close();
		}
	}
	
	private List<Map<String,Object>> build(Cursor cursor){
		List<Map<String,Object>> templist=new ArrayList<Map<String,Object>>();
		Calendar calendar=Calendar.getInstance();
		int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH)+1;
		int day=calendar.get(Calendar.DAY_OF_MONTH);
		while (cursor.moveToNext()){
			Map<String,Object> map=new HashMap<String, Object>();
			String msg=cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_MSG_CONTENT));
			if(msg.length()>28){
			   msg=msg.substring(0, 26)+"...";	
			}
			map.put("COLUMNN_ID", cursor.getString(cursor.getColumnIndex(CustomColumns._ID)));
			map.put("SEND_CONTENT_SIZE", cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_MSG_LEN)));
			map.put("SEND_CONTENT", msg);
			String date=cursor.getString((cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_TIME)));
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
			String flag=cursor.getString((cursor.getColumnIndex(CustomColumns.COLUMNS_FLAG)));
			if(flag!=null&&"0".equals(flag)){
				String receiverAddress=cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS));
				String receiverAddressName=Utils.getContactNameFromPhoneNum(this,receiverAddress);
				map.put("SEND_ID", "发件人:"+((receiverAddressName!=null&&!receiverAddressName.equals(""))?receiverAddressName:receiverAddress));
				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_received_box));
			}else if(flag!=null&&"1".equals(flag)){
				String senderAddress=cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS));
				if(senderAddress.contains("(")&&senderAddress.contains(")")){
				  String senderAddressNumber=senderAddress.substring(senderAddress.indexOf("(")+1,senderAddress.indexOf(")") );
				  String senderAddressName=Utils.getContactNameFromPhoneNum(this,senderAddressNumber);
				  if(senderAddressName!=null&&!"".equals(senderAddressName)){
					  senderAddress=senderAddressName;
				  }
				}
				map.put("SEND_ID", "收件人:"+senderAddress);
				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_sended_box));
			}else if(flag!=null&&"2".equals(flag)){
				String userid=cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS));
				if(userid!=null&&!"".equals(userid)){
					map.put("SEND_ID", userid);
				}else{
					map.put("SEND_ID", "草稿");
				}
				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_caogao));
			}else if(flag!=null&&"3".equals(flag)){
				map.put("SEND_ID", "发件人: "+cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS)));
				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_not_read));
			}else {
				
			}
			templist.add(map);
		}
		return templist;
	}
	
	private void showListView(Cursor cursor){
		//list=build(cursor);
		final List<Map<String,Object>> list=build(cursor);
		//把数据转为  item对象 放到items集合中
		Item item = null;
		items.clear();
		for (Map<String, Object> map : list) {
			
			item = new Item();
			toItemObject(map, item);
			items.add(item);
			
		}
		if (adapter!=null) {
			adapter.notifyDataSetChanged();
			return;
		}
		
		/*2.把数据转换成List*/
		adapter=new SendMsgAdapter(ReceiveMsgActivity.this, items);
//		adapter=new SendMsgAdapter(ReceiveMsgActivity.this, list);
		//回调
		adapter.setOnCheckBoxClickLinstener(new OnCheckBoxClickLinstener() {
			
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
		
		/*增加选项*/
		//短按
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Log.w("ReceiveMsgActivity", "setOnItemClickListener");
				String rowId=String.valueOf(list.get(position).get("COLUMNN_ID"));
				/*转发功能*/
				Intent intent=new Intent();
				intent.putExtra("MSG_DEL_ID",rowId);
				intent.setClass(mContext,MsgZhuanFaActivity.class);
			    mContext.startActivity(intent);
			    
			}
		});
		//长按
		listView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int index, long arg3) {
				AlertDialog.Builder alert=new AlertDialog.Builder(ReceiveMsgActivity.this.getParent());
				alert.setTitle("删除短信");
				alert.setMessage("是否删除该条短信?");
				alert.setPositiveButton("删除", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int index1) {
						DatabaseOperation operation=new DatabaseOperation(mContext.getApplicationContext());
						int id=Integer.valueOf(String.valueOf(list.get(index).get("COLUMNN_ID")));  
						boolean istrue=operation.delete(id);
						if(istrue){
							list.remove(index);
							adapter.notifyDataSetChanged();
						   Toast.makeText(mContext, mContext.getResources().getString(R.string.bd_msg_del_success), Toast.LENGTH_SHORT).show();  
						}else{
						   Toast.makeText(mContext,mContext.getResources().getString(R.string.bd_msg_del_fail), Toast.LENGTH_SHORT).show();   
						}
					}
				});
				alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
				alert.show();
				return false;
			}
		});
		listView.setChoiceMode(listView.CHOICE_MODE_SINGLE);
		listView.setAdapter(adapter);
	}
	
}
