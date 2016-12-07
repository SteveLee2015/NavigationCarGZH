package com.novsky.map.main;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapabc.android.activity.BottomBaseActivity;
import com.mapabc.android.activity.R;
import com.novsky.map.fragment.AutoCheckedFragment;
import com.novsky.map.fragment.BD1LocFragment;
import com.novsky.map.fragment.BD2LocFragment;
import com.novsky.map.fragment.BD2StatusFragment;
import com.novsky.map.fragment.BDLocationReportFragment;
import com.novsky.map.fragment.BDRDSSLocationSetFragment;
import com.novsky.map.fragment.BDSoftwareFragment;
import com.novsky.map.fragment.BDTimeFragment;
import com.novsky.map.fragment.BDZuoZhanTimeFragment;
import com.novsky.map.fragment.FriendsLocationFragment;
import com.novsky.map.fragment.GPSStatusFragment;
import com.novsky.map.fragment.LocalMachineInfoFragment;
import com.novsky.map.fragment.LocationModelFragment;
import com.novsky.map.fragment.ManagerInfoFragment;
import com.novsky.map.fragment.OverspeedFragment;
import com.novsky.map.fragment.RelayStationManagerFragment;

import java.util.HashMap;

/**
 * 横屏 的更多功能
 * @author Administrator
 *
 */
public class BDManagerHorizontalActivity extends BottomBaseActivity implements BDMoreListFragment.Callbacks {
	
	private static final String TAG="BDManagerHorizontalActivity";
    
    //private ImageView mReturnLayout=null;
    
    private final int REQUEST_CONTACT = 1;
    
	private HashMap<Integer, Fragment> fragments=new HashMap<Integer, Fragment>();
			
	@Override
	protected int getContentView() {
		return R.layout.activity_bdmanager_horizontal;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_bdmanager_horizontal);
		title_name.setText("更多");
		//返回键
		//mReturnLayout=(ImageView)this.findViewById(R.id.home_title_flag_img);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BDManagerHorizontalActivity.this.finish();
			}
		});
		fragments.put(1, new BD1LocFragment());//0
		fragments.put(2, new BDRDSSLocationSetFragment());//1
		fragments.put(3, new BD2LocFragment());//2
		fragments.put(4, new BDLocationReportFragment());//3
		fragments.put(5, new FriendsLocationFragment());//4友邻位置(友邻信息)
		fragments.put(6, new LocalMachineInfoFragment());//5
		fragments.put(7, new AutoCheckedFragment());//6北斗1信号
		fragments.put(8, new BD2StatusFragment());//北斗2信号
		fragments.put(9, new GPSStatusFragment());//GPS信号
		fragments.put(10, new BDTimeFragment());//北斗校时
		fragments.put(11, new BDZuoZhanTimeFragment());//作战时间
		fragments.put(12, new ManagerInfoFragment());//11 管理信息
		fragments.put(13, new LocationModelFragment());//定位模式
		fragments.put(14, new RelayStationManagerFragment());//中继站管理
		fragments.put(15, new BDSoftwareFragment());//关于
		fragments.put(16, new OverspeedFragment());//超速发送短报文设置
	}

	int lastIndex=1;
	
	
	@Override
	public void onItemSelected(Integer id) {
		FragmentTransaction trx = getFragmentManager().beginTransaction();
	   switch(id){
		   case 1:
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(1).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(1));
	           }
			   trx.show(fragments.get(1)).commit();
			   lastIndex=1;
			   break;
		   case 2:
//			   getFragmentManager().beginTransaction().remove(fragment);
//			   fragment=new BDRDSSLocationSetFragment();
//			   //Replace an existing fragment that was added to a container. 
//			   //替代已经存在被增加在容器中的fragment
//			   //This is essentially the same as calling remove(Fragment) for
//			   //all currently added fragments that were added with the same containerViewId and 
//			   //then add(int, Fragment, String) with the same arguments given here.
//			   getFragmentManager().beginTransaction()
//			   .replace(R.id.message_detail_container, fragment)
//			   .commit();
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(2).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(2));
	           }
			   trx.show(fragments.get(2)).commit();
			   lastIndex=2;
			   break;
		   case 3:
//			   getFragmentManager().beginTransaction().remove(fragment);
//			   fragment=new BD2LocFragment();
//			   getFragmentManager().beginTransaction()
//					.replace(R.id.message_detail_container, fragment)
//					.commit();
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(3).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(3));
	           }
			   trx.show(fragments.get(3)).commit();
			   lastIndex=3;
			   break;
		   case 4:
			   //位置报告
//			   getFragmentManager().beginTransaction().remove(fragment);
//			   fragment=new BDLocationReportFragment();
//			   getFragmentManager().beginTransaction()
//				.replace(R.id.message_detail_container, fragment)
//				.commit();


			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(4).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(4));
	           }
			   trx.show(fragments.get(4)).commit();
			   lastIndex=4;
			   break;
		   case 5:
			   //友邻位置
//			   getFragmentManager().beginTransaction().remove(fragment);
//			   fragment=new FriendsLocationFragment();
//			   getFragmentManager().beginTransaction()
//			   .replace(R.id.message_detail_container, fragment)
//			   .commit();

				trx.hide(fragments.get(lastIndex));
				if (!fragments.get(5).isAdded()){
				trx.add(R.id.message_detail_container, fragments.get(5));
				}
				trx.show(fragments.get(5)).commit();
				lastIndex=5;

			   break;
		   case 6:
//			   getFragmentManager().beginTransaction().remove(fragment);
//			   fragment=new LocalMachineInfoFragment();
//			   getFragmentManager().beginTransaction()
//			   .replace(R.id.message_detail_container, fragment)
//			   .commit();
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(6).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(6));
	           }
			   trx.show(fragments.get(6)).commit();
			   lastIndex=6;
			   break;
		   case 7:
//			   Intent mIntent=new Intent();
//			   mIntent.setClass(BDManagerHorizontalActivity.this, AutoCheckedActivity.class);
//			   startActivity(mIntent);
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(7).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(7));
	           }
			   trx.show(fragments.get(7)).commit();
			   lastIndex=7;
			   break;
		   case 8:
//			   Intent bdIntent=new Intent();
//			   bdIntent.setClass(BDManagerHorizontalActivity.this, BD2StatusActivity.class);
//			   startActivity(bdIntent);
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(8).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(8));
	           }
			   trx.show(fragments.get(8)).commit();
			   lastIndex=8;
			   break;
		   case 9:
//			   Intent gpsIntent=new Intent();
//			   gpsIntent.setClass(BDManagerHorizontalActivity.this, GPSStatusActivity.class);
//			   startActivity(gpsIntent);
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(9).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(9));
	           }
			   trx.show(fragments.get(9)).commit();
			   lastIndex=9;
			   break;
		   case 10:
//			   Intent timeIntent=new Intent();
//			   timeIntent.setClass(BDManagerHorizontalActivity.this, BDTimeActivity.class);
//			   startActivity(timeIntent);
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(10).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(10));
	           }
			   trx.show(fragments.get(10)).commit();
			   lastIndex=10;
			   break;
		   case 11:
//			   Intent zuoZhanTimeIntent=new Intent();
//			   zuoZhanTimeIntent.setClass(BDManagerHorizontalActivity.this, BDZuoZhanTimeActivity.class);
//			   startActivity(zuoZhanTimeIntent);
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(11).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(11));
	           }
			   trx.show(fragments.get(11)).commit();
			   lastIndex=11;
			   break;
		   case 12:
//			   getFragmentManager().beginTransaction().remove(fragment);
//			   fragment=new ManagerInfoFragment();
//			   getFragmentManager().beginTransaction()
//			   .replace(R.id.message_detail_container, fragment)
//			   .commit();
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(12).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(12));
	           }
			   trx.show(fragments.get(12)).commit();
			   lastIndex=12;
			   break;
		   case 13:
//			   Intent locationModelIntent=new Intent();
//			   locationModelIntent.setClass(BDManagerHorizontalActivity.this, LocationModelActivity.class);
//			   startActivity(locationModelIntent);
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(13).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(13));
	           }
			   trx.show(fragments.get(13)).commit();
			   lastIndex=13;
			   break;
		   case 14:
			   
//			    final SharedPreferences relayStation=this.getSharedPreferences("BD_RELAY_STATION_PREF", MODE_PRIVATE);	
//			    final AlertDialog.Builder builder=new AlertDialog.Builder(BDManagerHorizontalActivity.this);
//				builder.setTitle(BDManagerHorizontalActivity.this.getResources().getString(R.string.title_activity_relay_station_manager));
//				LayoutInflater inflater=LayoutInflater.from(BDManagerHorizontalActivity.this);
//				final View view=inflater.inflate(R.layout.activity_relay_station_manager, null);
//				final EditText edit=(EditText)view.findViewById(R.id.relayStationNum);
//				String relayStationNum=relayStation.getString("BD_RELAY_STATION_NUM", "");
//				//if(!relayStationNum.equals("")){
//					edit.setText(relayStationNum);
//				//}
//				builder.setView(view);
//				builder.setCancelable(false);
//				builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface arg0, int arg1) {
//						//if(!edit.getText().toString().equals("")){
//						 relayStation.edit().putString("BD_RELAY_STATION_NUM", edit.getText().toString()).commit();
//							Toast.makeText(BDManagerHorizontalActivity.this, "成功设置中继站号码!", Toast.LENGTH_SHORT).show();
//						//}
//					}
//				});
//				builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface arg0, int arg1){
//						
//					}
//				});
//				AlertDialog dialog=builder.create();
//				dialog.show();
				
				trx.hide(fragments.get(lastIndex));
				   if (!fragments.get(14).isAdded()){
		                trx.add(R.id.message_detail_container, fragments.get(14));
		           }
				   trx.show(fragments.get(14)).commit();
				   lastIndex=14;
			   break;
		   case 15:
//			   getFragmentManager().beginTransaction().remove(fragment);
//			   fragment=new BDSoftwareFragment();
//			   getFragmentManager().beginTransaction()
//			   .replace(R.id.message_detail_container, fragment)
//			   .commit();
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(15).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(15));
	           }
			   trx.show(fragments.get(15)).commit();
			   lastIndex=15;
			   break;
		   case 16:
			   trx.hide(fragments.get(lastIndex));
			   if (!fragments.get(16).isAdded()){
	                trx.add(R.id.message_detail_container, fragments.get(16));
	           }
			   trx.show(fragments.get(16)).commit();
			   lastIndex=16;
			   break;
		   default:
			   break;
	   }
	}


	
}
