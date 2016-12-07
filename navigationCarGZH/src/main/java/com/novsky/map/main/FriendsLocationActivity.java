package com.novsky.map.main;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.novsky.map.util.FriendsLocationDatabaseOperation;
import com.novsky.map.util.Utils;
/**
 * 友邻位置
 * 用ListView显示所有的数据,并通过
 * @author steve
 */
public class FriendsLocationActivity extends Activity {

	private ListView listView=null;
	private LinearLayout mLinerLayout=null;
	private Context mContext=this;
	private String TAG="FriendsLocationActivity";
	private FriendsLoctionAdapter  adapter=null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friends_location);
		this.setRequestedOrientation(Utils.isLand?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		listView=(ListView)this.findViewById(R.id.friends_loc_listview);
		mLinerLayout=(LinearLayout)this.findViewById(R.id.friend_loc_layout);
		/*增加背景*/
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT , TileMode.REPEAT);
		bd.setDither(true);
		mLinerLayout.setBackgroundDrawable(bd); 
		
		final FriendsLocationDatabaseOperation oper=new FriendsLocationDatabaseOperation(this);
		final List<Map<String,Object>> list=oper.getAllLocationList();
		oper.close();
		adapter=new FriendsLoctionAdapter(this, list);
		listView.setAdapter(adapter);
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final int index=position;
				final String[] arrayFruit = mContext.getResources().getStringArray(R.array.friend_loc_oper);
				Dialog dialog = new AlertDialog.Builder(mContext).setTitle(mContext.getResources().getString(R.string.title_activity_friends_location))
                .setItems(arrayFruit, new DialogInterface.OnClickListener() {                  
                	public void onClick(DialogInterface dialog, int which) {         
                		Toast.makeText(mContext, arrayFruit[which], Toast.LENGTH_SHORT).show();    
                		if(which==0){
                			//显示地图
                			Intent notificationIntent = new Intent(mContext,NaviStudioActivity.class);
                			Map<String, Object> map=list.get(index);
                			String id=String.valueOf(map.get("F_ID"));  
                			//SharedPreferences locationsharePrefs = getActivity().getSharedPreferences("BD_FRIEND_LOCATION_PREF",0); 
                			//locationsharePrefs.edit().putInt("RERPORT_ROW_ID", Integer.valueOf(id)).commit();
                			notificationIntent.putExtra("RERPORT_ROW_ID",Integer.valueOf(id));
                			startActivity(notificationIntent);
                		}else{
                			//从数据库中删除数据
                			Map<String, Object> map=list.get(index);
                			String id=String.valueOf(map.get("F_ID"));                			
                			boolean istrue=oper.delete(Long.valueOf(id));
                			oper.close();
                			if(istrue){
                				Toast.makeText(mContext, mContext.getResources().getString(R.string.friend_loc_del_success), Toast.LENGTH_SHORT).show();
                			}else{
                				Toast.makeText(mContext, mContext.getResources().getString(R.string.friend_loc_del_fail), Toast.LENGTH_SHORT).show();
                			}
                			list.remove(index);
                        	adapter.notifyDataSetChanged();
                		}
                    }      
                 })        
                .setNegativeButton(mContext.getResources().getString(R.string.common_cancle_btn), 
                		new DialogInterface.OnClickListener() {                 
                             public void onClick(DialogInterface dialog, int which) {}        
                 }).create();  dialog.show();
             	return false;
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.location_friends, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.delete_all_data:
				
				AlertDialog.Builder builder=new AlertDialog.Builder(FriendsLocationActivity.this);
				builder.setTitle("全部删除短信");
				builder.setMessage("是否全部删除短信?");
				builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						final FriendsLocationDatabaseOperation oper=new FriendsLocationDatabaseOperation(mContext);
						boolean istrue=oper.delete();
						oper.close();
						if(istrue){
							adapter.deleteAllData();
							adapter.notifyDataSetChanged();
							Toast.makeText(mContext, "删除所有位置报告信息成功!", Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(mContext, "删除所有位置报告信息失败!", Toast.LENGTH_LONG).show();
						}				
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
