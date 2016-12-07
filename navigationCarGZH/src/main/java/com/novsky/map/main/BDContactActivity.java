package com.novsky.map.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.Utils;

/**
 * 北斗通讯录页面
 * @author steve
 */
public class BDContactActivity extends Activity {

	/**
	 * 日志标识
	 */
    private  static final String TAG="BDContactActivity";
	
    /**
     * 列表对象
     */
    private  ListView  mListView=null;
    
    
    private EditText mSearchContact=null;
    
    
    /**
     * 增加联系人图标
     */
    private  ImageButton  addContact=null;
    
    
    private  Context mContext=this;
    
    
    private  SimpleAdapter adapter=null;
    
    
    private  long rowId=0;
    
    private  List<Map<String,Object>> list=null;
    
    
    private  Uri mUri=null;
    
    
    private Cursor mCursor=null;
    
    
    private LinearLayout mAddContactLinearLayout=null;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_bdcontact);
		//this.setRequestedOrientation(JsNavi.horizon?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		addContact=(ImageButton)this.findViewById(R.id.addContact);
		mListView=(ListView)this.findViewById(R.id.contact_list);
		mSearchContact=(EditText)this.findViewById(R.id.search_contact);
		mAddContactLinearLayout=(LinearLayout)this.findViewById(R.id.add_contact_linearlayout);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mUri=getIntent().getData();
        //mCursor=this.managedQuery(BDContactColumn.CONTENT_URI, BDContactColumn.COLUMNS, null, null, null);
		mCursor=this.getContentResolver().query(BDContactColumn.CONTENT_URI, BDContactColumn.COLUMNS, null, null, null);
        list=cursorToList(mCursor);
        adapter=new SimpleAdapter(mContext,list,R.layout.bd_contact_list_item
				,new String[]{BDContactColumn.USER_NAME,BDContactColumn.CARD_NUM},
				new int[]{R.id.item_user_name,R.id.item_card_num});
        
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				 Intent intent=new Intent();
				 long mid=0;
				 if(mCursor.moveToPosition(position)){
					 mid=mCursor.getLong(mCursor.getColumnIndexOrThrow(BDContactColumn._ID));
				 }
				 if(mUri!=null){
					 Uri data=ContentUris.withAppendedId(BDContactColumn.CONTENT_URI, mid);
				     intent.setData(data);
				     setResult(RESULT_OK,intent);
				     BDContactActivity.this.finish();
				 }else{
					intent.setClass(mContext, GetContactActivity.class);
					intent.putExtra("UPDATE_BD_CONTACT_ID", mid);
		    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				    String name="";
					if(mCursor.moveToPosition(position)){
						name=mCursor.getString(mCursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME));
						rowId=mCursor.getLong(mCursor.getColumnIndexOrThrow(BDContactColumn._ID));
					}
					final String[] items={"修改","删除"};
					AlertDialog.Builder builder=new AlertDialog.Builder(mContext)
					.setTitle(name)
					.setItems(items,new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface arg0, int position) {
						    switch(position){
						    	case 0: //修改
						    		Intent intent=new Intent();
						    		intent.putExtra("UPDATE_BD_CONTACT_ID", rowId);
						    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						    		intent.setClass(mContext, UpdateContactActivity.class);
						    		startActivity(intent);
						    		break;
						    	case 1://删除
						    		AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
						    		builder.setTitle("删除联系人");
						    		builder.setMessage("是否删除该联系人?");
						    		builder.setCancelable(false);
						    		builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											Uri deleteUri=ContentUris.withAppendedId(BDContactColumn.CONTENT_URI, rowId);
								    		int num=mContext.getContentResolver().delete(deleteUri,null, null);
								    		if(num>0){
								    			Toast.makeText(mContext, "北斗联系人删除成功!", Toast.LENGTH_SHORT).show();
								    		}else{
								    			Toast.makeText(mContext, "北斗联系人删除失败!", Toast.LENGTH_SHORT).show();
								    		}
								    		for(int i=0;i<list.size();i++){
								    			Map<String,Object> map=list.get(i);
								    			if(String.valueOf(map.get(BDContactColumn._ID)).equals(""+rowId)){
								    				list.remove(map);
								    				break;
								    			}
								    		}
								    		adapter.notifyDataSetChanged();
										}
						    		});
						    		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
										
											
										}
									});
						    		final AlertDialog dialog=builder.create();
						    		dialog.show();
						    		break;
						    	default:
						    		break;
						    }
							
						}
					});
					builder.create().show();
					
				return false;
			}
		});
		
		mSearchContact.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable editable) {
				if(editable!=null&&!"".equals(editable.toString())){
					String content=editable.toString();
					Cursor cursor=null;
					if(Utils.isNumber(content)){
						cursor=mContext.getContentResolver().query(BDContactColumn.CONTENT_URI, BDContactColumn.COLUMNS, BDContactColumn.CARD_NUM+" like '%"+content+"%'",null, null);
					}else{
						cursor=mContext.getContentResolver().query(BDContactColumn.CONTENT_URI, BDContactColumn.COLUMNS, BDContactColumn.USER_NAME+" like '%"+content+"%'",null, null);
					}
					list.clear();
					List<Map<String,Object>> mlist=cursorToList(cursor);
					for(int i=0;i<mlist.size();i++){
						list.add(mlist.get(i));
					}
					cursor.close();
					adapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {}
			
		});
		
		mAddContactLinearLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
			   Intent intent=new Intent();
			   intent.setClass(mContext, AddContactActivity.class);
			   startActivity(intent);
			}
		});
	}
     
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.bdcontact, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.action_del_all:
				AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
	    		builder.setTitle("删除联系人");
	    		builder.setMessage("是否全部删除该联系人?");
	    		builder.setCancelable(false);
	    		builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
			    		int num=mContext.getContentResolver().delete(BDContactColumn.CONTENT_URI,null, null);
			    		if(num>0){
			    			Toast.makeText(mContext, "全部删除北斗联系人成功!", Toast.LENGTH_SHORT).show();
			    		}else{
			    			Toast.makeText(mContext, "全部删除北斗联系人失败!", Toast.LENGTH_SHORT).show();
			    		}
			    		list.clear();
			    		adapter.notifyDataSetChanged();
					}
	    		});
	    		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
	    		final AlertDialog dialog=builder.create();
	    		dialog.show();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private List<Map<String,Object>> cursorToList(Cursor cursor){
		List<Map<String,Object>> mlist=new ArrayList<Map<String,Object>>();
		while (cursor.moveToNext()) {
			  Map<String,Object> map=new HashMap<String, Object>();
			  map.put(BDContactColumn._ID, cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn._ID)));
			  map.put(BDContactColumn.USER_NAME,cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME)));
			  map.put(BDContactColumn.CARD_NUM,cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_NUM)));
			  map.put(BDContactColumn.CARD_LEVEL,cursor.getInt(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_LEVEL)));
			  map.put(BDContactColumn.CARD_SERIAL_NUM,cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_SERIAL_NUM)));
			  map.put(BDContactColumn.CARD_FREQUENCY,cursor.getInt(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_FREQUENCY)));
			  map.put(BDContactColumn.USER_ADDRESS,cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_ADDRESS)));
			  map.put(BDContactColumn.PHONE_NUMBER,cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.PHONE_NUMBER)));
			  map.put(BDContactColumn.USER_EMAIL,cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_EMAIL)));
			  map.put(BDContactColumn.CHECK_CURRENT_NUM,cursor.getInt(cursor.getColumnIndexOrThrow(BDContactColumn.CHECK_CURRENT_NUM)));
			  map.put(BDContactColumn.FIRST_LETTER_INDEX,cursor.getInt(cursor.getColumnIndexOrThrow(BDContactColumn.FIRST_LETTER_INDEX)));
			  map.put(BDContactColumn.REMARK,cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.REMARK)));
			  mlist.add(map);
		}
		return mlist;
	}
	
	
	
	@Override
	protected void onStop() {
		super.onStop();
		if(mCursor!=null){
			mCursor.close();
			mCursor=null;
		}
		if(adapter!=null){
			adapter=null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		
	}
	
	
	
}
