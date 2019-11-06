package com.novsky.map.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.BDParameterException;
import android.location.BDRNSSManager.LocationStrategy;
import android.location.BDUnknownException;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.bd.comm.protocal.BDSatellite;
import com.bd.comm.protocal.BDSatelliteListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.utils.ReceiverAction;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.type.NSLonLat;
import com.novsky.map.main.BDAvailableStatelliteManager;
import com.novsky.map.main.LocationStatusManager;
import com.novsky.map.main.VerticalProgressBar;
import com.novsky.map.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 北斗II代卫星状态
 * @author steve
 */
public class BD2StatusFragment_bak extends Fragment {

	/**
	 * 日志标识
	 */
	private String TAG = "BD2StatusFragment";

	/**
	 * 频度
	 **/
	private String freq = "1";

	
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
	private Context mContext;
	private final static int BD_SATELLATE_ITEM=0x1001,BD_SATELLATE_LOCATION_STATUS=0x1002;
	/**
	 * 中心点
	 */
	private int iCenterX = 155;
	private int iCenterY = 90;
	private LocationStatusManager locationStatusManager=null;
	/**
	 * 半径
	 */
	private int dwR = 110;
	private BDCommManager mBDCommManager=null;
	private GpsStatus mGpsStatus = null;
	private List<Integer> statellites=new ArrayList<Integer>();
	private BDAvailableStatelliteManager bdAvailableStatelliteManager=null;
    private SharedPreferences sharePref =null;  
	private   int locationModel=-1;  
	private LinearLayout mBD2LinearLayout=null;
	
	private BDRNSSLocationListener mBDRNSSLocationListener=new BDRNSSLocationListener(){
		@Override
		public void onLocationChanged(BDRNSSLocation arg0) {
			Message message=mHandler.obtainMessage();
			message.what=BD_SATELLATE_LOCATION_STATUS;
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
	
	private BDSatelliteListener mBDSatelliteListener=new BDSatelliteListener(){
		@Override
		public void onBDGpsStatusChanged(List<BDSatellite> arg0) {
			Message message=mHandler.obtainMessage();
			message.obj=arg0;
			message.what=BD_SATELLATE_ITEM;
			mHandler.sendMessage(message);
		}
	};

	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			    case BD_SATELLATE_LOCATION_STATUS:
			    	BDRNSSLocation location=(BDRNSSLocation)msg.obj;
			    	boolean ilocsture=location.isAvailable();
				    if(locationModel==LocationStrategy.BD_ONLY_STRATEGY||locationModel==LocationStrategy.HYBRID_STRATEGY){
				    	   if(ilocsture){
							   locationStatus.setText("已定位");
							   MapAPI.getInstance().setVehicleGPS(3);
							   NSLonLat vehicleLonLat=new NSLonLat((float)location.getLongitude(), (float)location.getLatitude());
							   MapAPI.getInstance().setVehiclePosInfo(vehicleLonLat,0);
						   }else{
							   locationStatus.setText("未定位");
						   }
				    }else{
				    	   locationStatus.setText("未定位");
				    }
				   break;
				case Utils.HANDLER_LOCATION_STATUS:
				   Bundle bundle= msg.getData();
				   boolean isture=bundle.getBoolean("LOCATION_FIX_STATUS");
			       if(locationModel==LocationStrategy.BD_ONLY_STRATEGY||locationModel==LocationStrategy.HYBRID_STRATEGY){
			    	   if(isture){
						   locationStatus.setText("已定位");
						   MapAPI.getInstance().setVehicleGPS(3);
					   }else{
						   locationStatus.setText("未定位");
					   }
			       }else{
			    	   locationStatus.setText("未定位");
			       }
				   break;
				case   BD_SATELLATE_ITEM:
					List<BDSatellite> bdsatellites=(List<BDSatellite>)msg.obj;
						int count = 0;
						int index = 0;
						StringBuffer sb=new StringBuffer();
						for(BDSatellite satellite :bdsatellites){
							float azimuth = satellite.getAzimuth();//航向，方位角
							float elevation = satellite.getElevation();//仰角
							int statelliteID = satellite.getPrn();// ID
							float zaizaobi = satellite.getSnr();// 载噪比
							//boolean isUsed = bdAvailableStatelliteManager.usedInFix(statelliteID);//是否参与定位
							boolean isUsed=satellite.usedInFix();
							sb.append(statelliteID+",");
							if (count <= 11){
								if (statelliteID >= 160) {
									statelliteID = statelliteID -160;
									/*1.在载噪比图表上显示载噪比,载噪比必须大于0*/
									if (zaizaobi > 0){
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
									double r = dwR * (1.0 - (elevation / 90.0)); // 高度角
									double x = iCenterX+ (r * Math.sin(2.0 * M_PI * azimuth/ 360.0)); // 方位角
									double y = iCenterY- (r * Math.cos(2.0 * M_PI * azimuth/ 360.0));
									AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) map.getLayoutParams();
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
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		View contentView;
		if("AOSP on wing".equals(Utils.DEVICE_MODEL)){
			contentView = View.inflate(mContext, R.layout.fragment_bd2_status_car_7, null);
			dwR=90;
			iCenterX=200;
			iCenterY=110;
//			7寸全屏
//			dwR=175;
//			iCenterX=235;
//			iCenterY=180;
		}else{
			contentView = View.inflate(mContext, R.layout.activity_bd2_status, null);
		}
		
		sharePref = mContext.getSharedPreferences("LOCATION_MODEL_ACTIVITY", mContext.MODE_PRIVATE);  
		locationModel=sharePref.getInt("LOCATION_MODEL",-1); 
		listProgress = new ArrayList<VerticalProgressBar>();
		listTextView = new ArrayList<TextView>();
		mapList = new ArrayList<TextView>();
		zaizaobiList = new ArrayList<TextView>();
		locationStatusManager=LocationStatusManager.getInstance();
		bdAvailableStatelliteManager=BDAvailableStatelliteManager.getInstance();
		locationStatusManager.add(mHandler);
		
		initView(contentView);
		addReceiver();

		if(locationModel==LocationStrategy.BD_ONLY_STRATEGY||locationModel==LocationStrategy.HYBRID_STRATEGY){
			    locationStatus.setText(Utils.INIT_LOCATION_STATUS);
		}else{
			 locationStatus.setText("未定位");
		}


		return contentView;
	}

	private void addReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ReceiverAction.ACTION_LOCATION_STRATEGY);
		getActivity().registerReceiver(mReceiver,filter);
	}



	/**
	 * 初始化view
	 * @param contentView
	 */
	private void initView(View contentView) {
		
		progress1 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_1);
		progress2 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_2);
		progress3 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_3);
		progress4 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_4);
		progress5 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_5);
		progress6 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_6);
		progress7 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_7);
		progress8 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_8);
		progress9 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_9);
		progress10 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_10);
		progress11 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_11);
		progress12 = (VerticalProgressBar) contentView.findViewById(R.id.progressBar_12);
		locationStatus=(TextView)contentView.findViewById(R.id.bd_location_status);
		
		satellite1_id = (TextView) contentView.findViewById(R.id.satellite_id_1);
		satellite2_id = (TextView) contentView.findViewById(R.id.satellite_id_2);
		satellite3_id = (TextView) contentView.findViewById(R.id.satellite_id_3);
		satellite4_id = (TextView) contentView.findViewById(R.id.satellite_id_4);
		satellite5_id = (TextView) contentView.findViewById(R.id.satellite_id_5);
		satellite6_id = (TextView) contentView.findViewById(R.id.satellite_id_6);
		satellite7_id = (TextView) contentView.findViewById(R.id.satellite_id_7);
		satellite8_id = (TextView) contentView.findViewById(R.id.satellite_id_8);
		satellite9_id = (TextView) contentView.findViewById(R.id.satellite_id_9);
		satellite10_id = (TextView) contentView.findViewById(R.id.satellite_id_10);
		satellite11_id = (TextView) contentView.findViewById(R.id.satellite_id_11);
		satellite12_id = (TextView) contentView.findViewById(R.id.satellite_id_12);

		satellite_map_1 = (TextView) contentView.findViewById(R.id.satellite_map_1);
		satellite_map_2 = (TextView) contentView.findViewById(R.id.satellite_map_2);
		satellite_map_3 = (TextView) contentView.findViewById(R.id.satellite_map_3);
		satellite_map_4 = (TextView) contentView.findViewById(R.id.satellite_map_4);
		satellite_map_5 = (TextView) contentView.findViewById(R.id.satellite_map_5);
		satellite_map_6 = (TextView) contentView.findViewById(R.id.satellite_map_6);
		satellite_map_7 = (TextView) contentView.findViewById(R.id.satellite_map_7);
		satellite_map_8 = (TextView) contentView.findViewById(R.id.satellite_map_8);
		satellite_map_9 = (TextView) contentView.findViewById(R.id.satellite_map_9);
		satellite_map_10 = (TextView) contentView.findViewById(R.id.satellite_map_10);
		satellite_map_11 = (TextView) contentView.findViewById(R.id.satellite_map_11);
		satellite_map_12 = (TextView) contentView.findViewById(R.id.satellite_map_12);
		
		satellite_zaizaobi_1 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_1);
		satellite_zaizaobi_2 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_2);
		satellite_zaizaobi_3 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_3);
		satellite_zaizaobi_4 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_4);
		satellite_zaizaobi_5 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_5);
		satellite_zaizaobi_6 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_6);
		satellite_zaizaobi_7 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_7);
		satellite_zaizaobi_8 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_8);
		satellite_zaizaobi_9 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_9);
		satellite_zaizaobi_10 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_10);
		satellite_zaizaobi_11 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_11);
		satellite_zaizaobi_12 = (TextView) contentView.findViewById(R.id.satellite_zaizaobi_12);

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
		
	}

	@Override
	public void onStart() {
		super.onStart();
		if("S500".equals(Utils.DEVICE_MODEL)){

		}else{
			mBDCommManager=BDCommManager.getInstance(mContext);
			try {
				mBDCommManager.addBDEventListener(mBDSatelliteListener,mBDRNSSLocationListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		}
	}
	
	/* 调用onStop方法时,关闭监听*/
	@Override
	public void onStop() {
		super.onStop();
		if("S500".equals(Utils.DEVICE_MODEL)){

		}else{
			try {
				mBDCommManager.removeBDEventListener(mBDSatelliteListener,mBDRNSSLocationListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		}
		locationStatusManager.remove(mHandler);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(mReceiver);
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
	
	private void initStatelliteData(){
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

	private void initStatelliteMap(){
		for(int i=0;i<mapList.size();i++){
			TextView map = mapList.get(i);
			map.setVisibility(View.INVISIBLE);
		}
	}




	/**
	 * 接收 定位策略变化广播
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action){
				case ReceiverAction.ACTION_LOCATION_STRATEGY:{

						//清除所有数据
					initStatelliteData();
					initStatelliteMap();

					int mode = intent.getIntExtra(ReceiverAction.KEY_LOCATION_MODEL,-1);

					if (LocationStrategy.GPS_ONLY_STRATEGY==mode){
					}

					break;
				}
			}

		}
	};
}
