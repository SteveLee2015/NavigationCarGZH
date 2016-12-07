package com.mapabc.android.activity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.novsky.map.main.AutoCheckedActivity;
import com.novsky.map.main.BD2StatusActivity;
import com.novsky.map.main.BDLocationPortActivity;
import com.novsky.map.main.BDSendMsgPortActivity;
import com.novsky.map.main.BDSoftwareActivity;
import com.novsky.map.main.BDTimeActivity;
import com.novsky.map.main.BDZuoZhanTimeActivity;
import com.novsky.map.main.GPSStatusActivity;
import com.novsky.map.main.LocalMachineInfoActivity;
import com.novsky.map.main.LocationModelActivity;
import com.novsky.map.main.ManagerInfoActivity;

@SuppressLint("NewApi")
public class BDManagerActivity extends PreferenceActivity {
	/**
	 * 日志标识
	 */
	private final static String TAG = "BDManagerActivity";
	/**
	 * RDSS定位
	 */
	private Preference rdss_location_pref;
	/**
	 * 定位设置
	 */
	private Preference rdss_location_setting_pref;
	/**
	 * RNSS定位
	 */
	private Preference rnss_location_pref;
	/**
	 * 位置报告
	 */
	private Preference location_report_pref;
	
	private Preference friend_location_pref;
	
	/**
	 * 本机信息
	 */
	private Preference local_machine_pref;
	
	/**
	 * RDSS波束检测
	 */
	private Preference auto_check_pref;
	
	/**
	 * RNSS卫星状态
	 */
	private Preference bd2_satlite_status;
	
	/**
	 * GPS卫星状态
	 */
	private Preference gps_satelite_status;
	
	/**
	 * 导航设置
	 */
	private Preference navi_set_pref;
	
	/**
	 * 北斗校时
	 */
	private Preference check_time_pref;
	
	
	private Preference zuo_zhan_time_pref;
	
	/**
	 * 距离计算
	 */
	private Preference distanceCalcPref;

	/**
	 * 管信管理
	 */
	private Preference manager_info_pref;
	
	/**
	 * 定位模式
	 */
	private Preference location_model_pref;
	
	/**
	 *常用短语
	 */
	private Preference usal_message_pref;
	
	/**
	 * 经纬度转换
	 */
	private Preference lon_lat_pref;
	
	private Preference about_pref;
	
	
	private LocationManager locationManager=null;
	
	/**
	 * 设置中继站号码*/
	private Preference set_relay_station_num_pref;
	
	private RelativeLayout
			mapStatBarLayout=null,naviStatBarLayout=null,
			messageStatBarLayout=null,settingStatBarLayout=null;
	
	
	private ImageView  mapStatBarImageView=null,naviStatBarImageView=null,
		    messageStatBarImageView=null,settingStatBarImageView=null;
	
	
	private TextView   mapStatBarTextView=null,naviStatBarTextView=null,
			messageStatBarTextView=null,settingStatBarTextView=null;
	
	 /**
	 * 定位监听器
	 */
	 private final LocationListener locationListener=new LocationListener() {
		 
		 public void onStatusChanged(String provider, int status, Bundle extras) {}
		 
		 public void onProviderEnabled(String provider) {}
		 
		 public void onProviderDisabled(String provider) {}
		 
		 public void onLocationChanged(Location location) {}
	 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(com.novsky.map.util.Utils.isLand?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		this.setContentView(R.layout.activity_bdmanager);
		addPreferencesFromResource(R.xml.preferences);
		Resources mResource = this.getResources();
		locationManager =(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		/*RDSS定位*/
		rdss_location_pref=(Preference) getPreferenceScreen().findPreference(
				mResource.getString(R.string.key_rdss_location_manager));
		rdss_location_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent();
						intent.setClass(BDManagerActivity.this,BDLocationPortActivity.class);
						startActivity(intent);
						return false;
					}
		});
		/*本机信息 */
		local_machine_pref = (Preference) getPreferenceScreen().findPreference(
		mResource.getString(R.string.key_local_mach_pref));
		local_machine_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(BDManagerActivity.this,LocalMachineInfoActivity.class);
				startActivity(intent);
				return false;
			}
		});

		/*波束状态 */
		auto_check_pref = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_auto_check));
		auto_check_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(BDManagerActivity.this,AutoCheckedActivity.class);
				startActivity(intent);
				return false;
			}
		});

		/*BD2状态 */
		bd2_satlite_status =(Preference)getPreferenceScreen().findPreference(mResource.getString(R.string.key_bd2_satelite_status));
		bd2_satlite_status.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(BDManagerActivity.this,BD2StatusActivity.class);
				startActivity(intent);
				return false;
			}
		});

		/*GPS状态 */
		gps_satelite_status = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_gps_status));
		gps_satelite_status.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(BDManagerActivity.this,GPSStatusActivity.class);
				startActivity(intent);
				return false;
			}
		});
		mapStatBarLayout=(RelativeLayout)this.findViewById(R.id.map_status_layout);
		naviStatBarLayout=(RelativeLayout)this.findViewById(R.id.navi_status_layout);
		messageStatBarLayout=(RelativeLayout)this.findViewById(R.id.message_status_layout);
		settingStatBarLayout=(RelativeLayout)this.findViewById(R.id.setting_status_layout);
		mapStatBarImageView=(ImageView)this.findViewById(R.id.map_status_imageview);
		naviStatBarImageView=(ImageView)this.findViewById(R.id.navi_status_imageview);
		messageStatBarImageView=(ImageView)this.findViewById(R.id.message_status_imageview);
		settingStatBarImageView=(ImageView)this.findViewById(R.id.setting_status_imageview);
		mapStatBarTextView=(TextView)this.findViewById(R.id.map_status_textview);
		naviStatBarTextView=(TextView)this.findViewById(R.id.navi_status_textview);
		messageStatBarTextView=(TextView)this.findViewById(R.id.message_status_textview);
		settingStatBarTextView=(TextView)this.findViewById(R.id.setting_status_textview);
		
		mapStatBarLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				BDManagerActivity.this.finish();
				//JsNavi.mUi.mStaBar.doStatus(0);
			}
		});
		naviStatBarLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				BDManagerActivity.this.finish();
				//JsNavi.mUi.mStaBar.doStatus(1);
			}
		});
		
		messageStatBarLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent();
				intent.setClass(BDManagerActivity.this, BDSendMsgPortActivity.class);
				BDManagerActivity.this.startActivity(intent);
				BDManagerActivity.this.finish();
			}
		});
		
		settingStatBarLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
			}
		});
		/* 串口设置 */
		// serial_port_pref=(Preference)getPreferenceScreen().
		// findPreference(mResource.getString(R.string.key_set_serial_port));
		//
		// serial_port_pref.setOnPreferenceClickListener(new
		// OnPreferenceClickListener() {
		// @Override
		// public boolean onPreferenceClick(Preference preference) {
		// Intent intent=new Intent();
		// intent.setClass(BDManagerActivity.this,
		// UpdateSerialPortActivity.class);
		// startActivity(intent);
		// return false;
		// }
		// });

		/* 导航设置 */
		//		navi_set_pref = (Preference) getPreferenceScreen().findPreference(
		//				mResource.getString(R.string.key_navi_set));
		//
		//		navi_set_pref
		//				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		//					@Override
		//					public boolean onPreferenceClick(Preference preference) {
		//						Intent intent = new Intent();
		//						intent.setClass(BDManagerActivity.this,
		//								NaviSetActivity.class);
		//						startActivity(intent);
		//						return false;
		//					}
		//				});
		/*北斗校时*/
		check_time_pref = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_bd_check_time));
		check_time_pref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent();
						intent.setClass(BDManagerActivity.this,BDTimeActivity.class);
						startActivity(intent);
						return false;
					}
				});
		
		zuo_zhan_time_pref = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_zuozhan_time));
		zuo_zhan_time_pref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent();
						intent.setClass(BDManagerActivity.this,BDZuoZhanTimeActivity.class);
						startActivity(intent);
						return false;
					}
		});

		/* 管理信息 */
		manager_info_pref = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_manager_info));
		manager_info_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent();
						intent.setClass(BDManagerActivity.this,ManagerInfoActivity.class);
						startActivity(intent);
						return false;
					}
		});
		/**
		 * 定位模式转换
		 */
		location_model_pref = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_location_model));
		location_model_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent();
					intent.setClass(BDManagerActivity.this,LocationModelActivity.class);
					startActivity(intent);
					return false;
				}
		 });	
		
		usal_message_pref = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_words_manager));
		usal_message_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent();
					intent.setClass(BDManagerActivity.this,MsgUsalWordActivity.class);
					startActivity(intent);
					return true;
				}
		 });	
		
		/*中继站*/
		final SharedPreferences relayStation=this.getSharedPreferences("BD_RELAY_STATION_PREF", MODE_PRIVATE);				
		set_relay_station_num_pref = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_relay_station_manager));
		set_relay_station_num_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					final AlertDialog.Builder builder=new AlertDialog.Builder(BDManagerActivity.this);
					builder.setTitle(BDManagerActivity.this.getResources().getString(R.string.title_activity_relay_station_manager));
					LayoutInflater inflater=LayoutInflater.from(BDManagerActivity.this);
					final View view=inflater.inflate(R.layout.activity_relay_station_manager, null);
					final EditText edit=(EditText)view.findViewById(R.id.relayStationNum);
					String relayStationNum=relayStation.getString("BD_RELAY_STATION_NUM", "");
					//if(!relayStationNum.equals("")){
						edit.setText(relayStationNum);
					//}
					builder.setView(view);
					builder.setCancelable(false);
					builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							//if(!edit.getText().toString().equals("")){
								relayStation.edit().putString("BD_RELAY_STATION_NUM", edit.getText().toString()).commit();
								Toast.makeText(BDManagerActivity.this, "成功设置中继站号码!", Toast.LENGTH_SHORT).show();
							//}
						}
					});
					builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							
						}
					});
					AlertDialog dialog=builder.create();
					dialog.show();
					return true;
				}
		});	
		about_pref = (Preference) getPreferenceScreen().findPreference(mResource.getString(R.string.key_about_software));
		about_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
				    Intent mIntnt =new Intent(BDManagerActivity.this,BDSoftwareActivity.class);
				    startActivity(mIntnt);
					return false;
				}
		});
//		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
//		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
//		this.getWindow().setGravity(Gravity.BOTTOM);
//		this.getWindow().set
//		this.getWindow().addContentView(mView,params);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		/* 查找到服务信息 */
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
		String provider = locationManager.getBestProvider(criteria, false);
		 //获取GPS信息
		locationManager.requestLocationUpdates(provider, 1000, 0,locationListener);
	}
	

	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		locationManager.removeUpdates(locationListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			BDManagerActivity.this.finish();
			//JsNavi.mUi.mStaBar.doStatus(0);
			return true;
		}
		return false;
	}
}
