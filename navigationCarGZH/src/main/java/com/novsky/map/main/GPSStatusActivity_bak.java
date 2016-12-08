package com.novsky.map.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.BDParameterException;
import android.location.BDRNSSManager.LocationStrategy;
import android.location.BDUnknownException;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.bd.comm.protocal.GPSatellite;
import com.bd.comm.protocal.GPSatelliteListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.base.NaviStudioApplication;
import com.mapabc.naviapi.MapAPI;
import com.novsky.map.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * GPS卫星状态
 * @author steve
 */
public class GPSStatusActivity_bak extends Activity {

    private String TAG="GPSStatusActivity";

	/* 频度 */
	private String feq = "1";

	private double M_PI = 3.14159265358979323846;
                                                                                                   
	private VerticalProgressBar progress1 = null;

	private VerticalProgressBar progress2 = null;

	private VerticalProgressBar progress3 = null;

	private VerticalProgressBar progress4 = null;

	private VerticalProgressBar progress5 = null;

	private VerticalProgressBar progress6 = null;

	private VerticalProgressBar progress7 = null;

	private VerticalProgressBar progress8 = null;

	private VerticalProgressBar progress9 = null;

	private VerticalProgressBar progress10 = null;

	private VerticalProgressBar progress11 = null;

	private VerticalProgressBar progress12 = null;

	private VerticalProgressBar progress13 = null;

	private VerticalProgressBar progress14 = null;

	private VerticalProgressBar progress15 = null;

	private String cmd = "";

	private TextView satellite1_id = null;

	private TextView satellite2_id = null;

	private TextView satellite3_id = null;

	private TextView satellite4_id = null;

	private TextView satellite5_id = null;

	private TextView satellite6_id = null;

	private TextView satellite7_id = null;

	private TextView satellite8_id = null;

	private TextView satellite9_id = null;

	private TextView satellite10_id = null;

	private TextView satellite11_id = null;

	private TextView satellite12_id = null;

	private TextView satellite_map_1 = null;
	private TextView satellite_map_2 = null;
	private TextView satellite_map_3 = null;
	private TextView satellite_map_4 = null;
	private TextView satellite_map_5 = null;
	private TextView satellite_map_6 = null;
	private TextView satellite_map_7 = null;
	private TextView satellite_map_8 = null;
	private TextView satellite_map_9 = null;
	private TextView satellite_map_10 = null;
	private TextView satellite_map_11 = null;
	private TextView satellite_map_12 = null;

	private TextView satellite_zaizaobi_1 = null;
	private TextView satellite_zaizaobi_2 = null;
	private TextView satellite_zaizaobi_3 = null;
	private TextView satellite_zaizaobi_4 = null;
	private TextView satellite_zaizaobi_5 = null;
	private TextView satellite_zaizaobi_6 = null;
	private TextView satellite_zaizaobi_7 = null;
	private TextView satellite_zaizaobi_8 = null;
	private TextView satellite_zaizaobi_9 = null;
	private TextView satellite_zaizaobi_10 = null;
	private TextView satellite_zaizaobi_11 = null;
	private TextView satellite_zaizaobi_12 = null;
	
	private TextView locationStatus=null;

	private List<VerticalProgressBar> listProgress = null;

	private List<TextView> listTextView = null;

	private List<TextView> mapList = null;

	private List<TextView> zaizaobiList = null;

	private Context mContext = this;

	private int  iCenterX=155;
	
	private int  iCenterY=90;
	
	private int dwR =110;

	private static final int GPS_SATELLATE_ITEM=0x1002,
			GPS_SATELLATE_LOCATION_STATUS=0x1003;
	
	private BDCommManager mBDCommManager=null;
	
	private List<Integer> statellites=new ArrayList<Integer>();
	
	private SharedPreferences sharePref =null;  
	
	private   int locationModel=-1;  
	
	private LinearLayout mGPSStatusLayout=null;
	
	
	private GPSatelliteListener mGPSatelliteListener=new GPSatelliteListener(){
		@Override
		public void onGpsStatusChanged(List<GPSatellite> arg0) {
		    Message message=mHandler.obtainMessage();
		    message.what=GPS_SATELLATE_ITEM;
		    message.obj=arg0;
		    mHandler.sendMessage(message);
		}
	};
	
	private LocationStatusManager locationStatusManager=null;


	
	private Handler mHandler=new Handler(){
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
				case Utils.HANDLER_LOCATION_STATUS:
				   Bundle bundle= msg.getData(); 
				   boolean isture=bundle.getBoolean("LOCATION_FIX_STATUS");
				   if(locationModel==LocationStrategy.GPS_ONLY_STRATEGY||locationModel==LocationStrategy.HYBRID_STRATEGY){
			    	   if(isture){
						   locationStatus.setText("已定位");
						   //application.isLocationed = true;
						   //不合理啊  可以优化
						   MapAPI.getInstance().setVehicleGPS(3);
					   }else{
						   locationStatus.setText("未定位");
						   MapAPI.getInstance().setVehicleGPS(2);
						   //application.isLocationed = false;
					   }
				   }else{
				       locationStatus.setText("未定位");
				   }
				   break;
				case GPS_SATELLATE_LOCATION_STATUS:
					BDRNSSLocation location=(BDRNSSLocation)msg.obj;
					 boolean islocture=location.isAvailable();
					 if(locationModel==LocationStrategy.GPS_ONLY_STRATEGY||locationModel==LocationStrategy.HYBRID_STRATEGY){
				    	   if(islocture){
							   locationStatus.setText("已定位");
						   }else{
							   locationStatus.setText("未定位");
						   }
					}else{
					       locationStatus.setText("未定位");
					}
					break;
				case GPS_SATELLATE_ITEM:
					List<GPSatellite> list=(List<GPSatellite>)msg.obj;
					int count = 0;
					int index=0;
                    for(GPSatellite satellite: list){
						float azimuth=satellite.getAzimuth();//航向，方位角
						float elevation=satellite.getElevation();//仰角
						int statelliteID = satellite.getPrn();//ID
						float zaizaobi = satellite.getSnr();//载噪比
						boolean isUsed=satellite.usedInFix();//是否参与定位
						if (count <= 11) {
							if(statelliteID<=32){
							//1.在载噪比图表上显示载噪比
							if (zaizaobi> 0) {
								int flag=-1;					
								/*如果在当前星图上已显示该卫星的载噪比,则更新载噪比的值*/
								if((flag=checkExists(statelliteID))!=-1){
									listProgress.get(flag).setProgress(Utils.getProgressValue(zaizaobi));
									listTextView.get(flag).setText(statelliteID + "");
								    zaizaobiList.get(flag).setText(String.valueOf((int) zaizaobi));
								}else{
								/*如果在当前星图上没有显示该卫星的载噪比,
								 * 则首先在statellites对象中增加卫星号并进行排序,然后取出该卫星号的index值，
								 * 并根据index值显示载噪比*/
									statellites.add(statelliteID);
									Collections.sort(statellites,new Comparator<Integer>(){
										
										public int compare(Integer arg0,
												Integer arg1) {
											if(arg0>arg1){
												return 1;
											}else if(arg0==arg1){
												return 0;
											}
											return -1;
										}
									});	
									int k=checkExists(statelliteID);
									//然后排序，根据返回的statelliteID的Index
									listProgress.get(k).setProgress(Utils.getProgressValue(zaizaobi));
									listTextView.get(k).setText(statelliteID + "");
							        zaizaobiList.get(k).setText(String.valueOf((int) zaizaobi));
								}
							}else{
									int flag=-1;
									if((flag=checkExists(statelliteID))!=-1){
										listProgress.get(flag).setProgress(0);
										listTextView.get(flag).setText("");
										zaizaobiList.get(flag).setText("");
									    statellites.remove(flag);
									    initStatelliteData();
									}
							}
							// 2.在星图上显示卫星方位
							TextView map = mapList.get(count);
							// 得到坐标
							double r = dwR * (1.0 - ( elevation/ 90.0)); // 高度角
							double x = iCenterX+ (r * Math.sin(2.0 * M_PI * azimuth/ 360.0)); // 方位角
							double y = iCenterY- (r * Math.cos(2.0 * M_PI * azimuth/ 360.0));
							AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) map
									.getLayoutParams();
							param.x = Utils.dip2px(mContext, x);
							param.y = Utils.dip2px(mContext, y);
							map.setLayoutParams(param);
							map.setVisibility(View.VISIBLE);
							if (isUsed&&zaizaobi>5) {
								map.setBackgroundDrawable(mContext.getResources()
										.getDrawable(R.drawable.satellite_strong_signal_bg));
								map.setTextColor(Color.BLACK);
							} else {
								map.setBackgroundDrawable(mContext.getResources()
										.getDrawable(R.drawable.satellite_week_signal_bg));
								map.setTextColor(Color.WHITE);
							}
							map.setText("" + statelliteID);
	                        }
						}
						count++;
                    }	
					break;
				default:
					break;
			}
		}
	};

		private BDRNSSLocationListener mBDRNSSLocationListener=new BDRNSSLocationListener(){
			@Override
			public void onLocationChanged(BDRNSSLocation arg0) {
				Message message=mHandler.obtainMessage();
				message.what=GPS_SATELLATE_LOCATION_STATUS;
				message.obj=arg0;
				mHandler.sendMessage(message);
			}
			@Override
			public void onProviderDisabled(String arg0) {}

			@Override
			public void onProviderEnabled(String arg0) {}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
			
		};

		private NaviStudioApplication application;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(Utils.isLand?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		if("AOSP on wing".equals(Utils.DEVICE_MODEL)){
			setContentView(R.layout.activity_gpsstatus_car_7);	
			dwR=175;
			iCenterX=235;
			iCenterY=180;
		}else{
			setContentView(R.layout.activity_gpsstatus);	
		}
		
		application = (NaviStudioApplication) getApplication();
		sharePref = getSharedPreferences("LOCATION_MODEL_ACTIVITY", mContext.MODE_PRIVATE);  
		locationModel=sharePref.getInt("LOCATION_MODEL",-1); 
		listProgress = new ArrayList<VerticalProgressBar>();
		listTextView = new ArrayList<TextView>();
		mapList = new ArrayList<TextView>();
		zaizaobiList = new ArrayList<TextView>();
		progress1 = (VerticalProgressBar) this.findViewById(R.id.progressBar_1);
		progress2 = (VerticalProgressBar) this.findViewById(R.id.progressBar_2);
		progress3 = (VerticalProgressBar) this.findViewById(R.id.progressBar_3);
		progress4 = (VerticalProgressBar) this.findViewById(R.id.progressBar_4);
		progress5 = (VerticalProgressBar) this.findViewById(R.id.progressBar_5);
		progress6 = (VerticalProgressBar) this.findViewById(R.id.progressBar_6);
		progress7 = (VerticalProgressBar) this.findViewById(R.id.progressBar_7);
		progress8 = (VerticalProgressBar) this.findViewById(R.id.progressBar_8);
		progress9 = (VerticalProgressBar) this.findViewById(R.id.progressBar_9);
		progress10 = (VerticalProgressBar) this.findViewById(R.id.progressBar_10);
		progress11 = (VerticalProgressBar) this.findViewById(R.id.progressBar_11);
		progress12 = (VerticalProgressBar) this.findViewById(R.id.progressBar_12);
		
		locationStatus=(TextView)this.findViewById(R.id.gps_location_status);
		if(locationModel==LocationStrategy.GPS_ONLY_STRATEGY||locationModel==LocationStrategy.HYBRID_STRATEGY){
		    locationStatus.setText(Utils.INIT_LOCATION_STATUS);
		}else{
			 locationStatus.setText("未定位");
		}
		satellite1_id = (TextView) this.findViewById(R.id.satellite_id_1);
		satellite2_id = (TextView) this.findViewById(R.id.satellite_id_2);
		satellite3_id = (TextView) this.findViewById(R.id.satellite_id_3);
		satellite4_id = (TextView) this.findViewById(R.id.satellite_id_4);
		satellite5_id = (TextView) this.findViewById(R.id.satellite_id_5);
		satellite6_id = (TextView) this.findViewById(R.id.satellite_id_6);
		satellite7_id = (TextView) this.findViewById(R.id.satellite_id_7);
		satellite8_id = (TextView) this.findViewById(R.id.satellite_id_8);
		satellite9_id = (TextView) this.findViewById(R.id.satellite_id_9);
		satellite10_id = (TextView) this.findViewById(R.id.satellite_id_10);
		satellite11_id = (TextView) this.findViewById(R.id.satellite_id_11);
		satellite12_id = (TextView) this.findViewById(R.id.satellite_id_12);

		satellite_map_1 = (TextView) this.findViewById(R.id.satellite_map_1);
		satellite_map_2 = (TextView) this.findViewById(R.id.satellite_map_2);
		satellite_map_3 = (TextView) this.findViewById(R.id.satellite_map_3);
		satellite_map_4 = (TextView) this.findViewById(R.id.satellite_map_4);
		satellite_map_5 = (TextView) this.findViewById(R.id.satellite_map_5);
		satellite_map_6 = (TextView) this.findViewById(R.id.satellite_map_6);
		satellite_map_7 = (TextView) this.findViewById(R.id.satellite_map_7);
		satellite_map_8 = (TextView) this.findViewById(R.id.satellite_map_8);
		satellite_map_9 = (TextView) this.findViewById(R.id.satellite_map_9);
		satellite_map_10 = (TextView) this.findViewById(R.id.satellite_map_10);
		satellite_map_11 = (TextView) this.findViewById(R.id.satellite_map_11);
		satellite_map_12 = (TextView) this.findViewById(R.id.satellite_map_12);

		satellite_zaizaobi_1 = (TextView) this.findViewById(R.id.satellite_zaizaobi_1);
		satellite_zaizaobi_2 = (TextView) this.findViewById(R.id.satellite_zaizaobi_2);
		satellite_zaizaobi_3 = (TextView) this.findViewById(R.id.satellite_zaizaobi_3);
		satellite_zaizaobi_4 = (TextView) this.findViewById(R.id.satellite_zaizaobi_4);
		satellite_zaizaobi_5 = (TextView) this.findViewById(R.id.satellite_zaizaobi_5);
		satellite_zaizaobi_6 = (TextView) this.findViewById(R.id.satellite_zaizaobi_6);
		satellite_zaizaobi_7 = (TextView) this.findViewById(R.id.satellite_zaizaobi_7);
		satellite_zaizaobi_8 = (TextView) this.findViewById(R.id.satellite_zaizaobi_8);
		satellite_zaizaobi_9 = (TextView) this.findViewById(R.id.satellite_zaizaobi_9);
		satellite_zaizaobi_10 =(TextView)this.findViewById(R.id.satellite_zaizaobi_10);
		satellite_zaizaobi_11 =(TextView)this.findViewById(R.id.satellite_zaizaobi_11);
		satellite_zaizaobi_12 =(TextView)this.findViewById(R.id.satellite_zaizaobi_12);

		zaizaobiList.add(satellite_zaizaobi_1);
		zaizaobiList.add(satellite_zaizaobi_2);
		zaizaobiList.add(satellite_zaizaobi_3);
		zaizaobiList.add(satellite_zaizaobi_4);
		zaizaobiList.add(satellite_zaizaobi_5);
		zaizaobiList.add(satellite_zaizaobi_6);
		zaizaobiList.add(satellite_zaizaobi_7);
		zaizaobiList.add(satellite_zaizaobi_8);
		zaizaobiList.add(satellite_zaizaobi_9);
		zaizaobiList.add(satellite_zaizaobi_10);
		zaizaobiList.add(satellite_zaizaobi_11);
		zaizaobiList.add(satellite_zaizaobi_12);

		mapList.add(satellite_map_1);
		mapList.add(satellite_map_2);
		mapList.add(satellite_map_3);
		mapList.add(satellite_map_4);
		mapList.add(satellite_map_5);
		mapList.add(satellite_map_6);
		mapList.add(satellite_map_7);
		mapList.add(satellite_map_8);
		mapList.add(satellite_map_9);
		mapList.add(satellite_map_10);
		mapList.add(satellite_map_11);
		mapList.add(satellite_map_12);

		listTextView.add(satellite1_id);
		listTextView.add(satellite2_id);
		listTextView.add(satellite3_id);
		listTextView.add(satellite4_id);
		listTextView.add(satellite5_id);
		listTextView.add(satellite6_id);
		listTextView.add(satellite7_id);
		listTextView.add(satellite8_id);
		listTextView.add(satellite9_id);
		listTextView.add(satellite10_id);
		listTextView.add(satellite11_id);
		listTextView.add(satellite12_id);

		listProgress.add(progress1);
		listProgress.add(progress2);
		listProgress.add(progress3);
		listProgress.add(progress4);
		listProgress.add(progress5);
		listProgress.add(progress6);
		listProgress.add(progress7);
		listProgress.add(progress8);
		listProgress.add(progress9);
		listProgress.add(progress10);
		listProgress.add(progress11);
		listProgress.add(progress12);
		mGPSStatusLayout=(LinearLayout)this.findViewById(R.id.gps_status_linearlayout);
		mGPSStatusLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				GPSStatusActivity_bak.this.finish();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationStatusManager=LocationStatusManager.getInstance();
		locationStatusManager.add(mHandler);
		if("S500".equals(Utils.DEVICE_MODEL)){

		}else{
			mBDCommManager=BDCommManager.getInstance(mContext);
			try {
				mBDCommManager.addBDEventListener(mGPSatelliteListener,mBDRNSSLocationListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		}
		
	}

	/* 调用onStop方法时,关闭GSV */
	@Override
	protected void onStop() {
		super.onStop();
		if("S500".equals(Utils.DEVICE_MODEL)){

		}else{
			try {
				mBDCommManager.removeBDEventListener(mGPSatelliteListener,mBDRNSSLocationListener);
			} catch (BDUnknownException e) {
				e.printStackTrace();
			} catch (BDParameterException e) {
				e.printStackTrace();
			}
		}
		locationStatusManager.remove(mHandler);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	/**
	 * 检查卫星状态信息是否已存在
	 * @param statelliteId
	 */
	public int  checkExists(int statelliteId){
		for(int i=0;i<statellites.size();i++){
			if(statellites.get(i)==statelliteId){
			   return i;	
			}
		}
		return -1;
	}

	
	public void initStatelliteData(){
		for(int i=0;i<listProgress.size();i++){
			VerticalProgressBar bar =listProgress.get(i);
			bar.setProgress(0);
		}
		
		for(int i=0;i<listTextView.size();i++){
			TextView tv=listTextView.get(i);
			tv.setText("");
		}

		for(int i=0;i<zaizaobiList.size();i++){
			TextView tv=zaizaobiList.get(i);
			tv.setText("");
		}
			
	}
}
