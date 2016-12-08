package com.novsky.map.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.bd.comm.protocal.GPSatellite;
import com.bd.comm.protocal.GPSatelliteListener;
import com.mapabc.android.activity.R;
import com.novsky.map.util.CollectionUtils;
import com.novsky.map.view.CustomSatelliateMap;
import com.novsky.map.view.CustomSatelliateSnr;

import java.util.ArrayList;
import java.util.List;

/**
 * GPS卫星状态
 * @author steve
 */
public class GPSStatusFragment extends Fragment implements View.OnClickListener{

	private final static int LOCATION_RESULT = 0x1000,
			GP_SATELLIATE_STATUS = 0x1001, BD_SATELLIATE_STATUS = 0x1002;

	private CustomSatelliateSnr mCustomBDSnr = null;

	private TextView mGPSLocationResult,mGPSLocationStatus,mGPSLocationHeight;

	private CustomSatelliateMap mCustomBDMap=null;

	private Context mContext;

	private LinearLayout ll_gps_bd2;


	private TextView mStatellite;

	private RelativeLayout mRLchangeStatellite;

	private TextView mTvChange;

	private BDCommManager mBDCommManager=null;


	List<GPSatellite> gplist = new ArrayList<>();

	private BDRNSSLocationListener mBDRNSSLocationListener=new BDRNSSLocationListener(){
		@Override
		public void onLocationChanged(BDRNSSLocation arg0) {
			Message message=mHandler.obtainMessage();
			message.what=LOCATION_RESULT;
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

	private GPSatelliteListener mGPSatelliteListener =new GPSatelliteListener(){
		@Override
		public void onGpsStatusChanged(List<GPSatellite> list) {
			Message message=mHandler.obtainMessage();
			message.obj=list;
			//message.what=BD_SATELLATE_ITEM;
			message.what=GP_SATELLIATE_STATUS;
			mHandler.sendMessage(message);
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LOCATION_RESULT:
					BDRNSSLocation mBDRNSSLocation=(BDRNSSLocation)msg.obj;
					if(true){//???
						mGPSLocationStatus.setText("状态:已定位");
						int lon=(int)(mBDRNSSLocation.getLongitude()*100000);
						int lat=(int)(mBDRNSSLocation.getLatitude()*100000);
						int height=(int)(mBDRNSSLocation.getAltitude()*100);
						//mGPSLocationResult.setText("("+lon/100000.0+","+lat/100000.0+","+height/100.0+")");
						mGPSLocationResult.setText((lon/100000.0)+" , "+(lat/100000.0));
						mGPSLocationHeight.setText((height/100.0)+"m");
					}else{
						mGPSLocationStatus.setText("状态:未定位");
						mGPSLocationResult.setText("0,0");
						mGPSLocationHeight.setText(0+"m");
					}
					break;
				case GP_SATELLIATE_STATUS:

					gplist= (List<GPSatellite>) msg.obj;
					//gplist= (List<BDSatellite>) msg.obj;
					//排序
					//Collections.sort(gplist);
					showMap(gplist);
					break;
				case BD_SATELLIATE_STATUS:
					break;
				default:
					break;
			}

		}
	};

	/**
	 * 展示数据
	 * @param gplist
	 */
	private void showMap(List<GPSatellite> gplist) {
		List newList = CollectionUtils.removeDuplicate(gplist);
		if (mCustomBDMap==null || mCustomBDMap == null){return;}
		mCustomBDSnr.showMap(newList);//载噪比
		mCustomBDMap.showMap(newList);//星图
	}




	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		mBDCommManager=BDCommManager.getInstance(mContext);
		View view = View.inflate(mContext,R.layout.activity_statellite_status,null);

			ll_gps_bd2 = (LinearLayout) view.findViewById(R.id.ll_gps_bd2);

			ll_gps_bd2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					//finish();
				}
			});

			mCustomBDSnr = (CustomSatelliateSnr) view.findViewById(R.id.gps_snr_view);
			mCustomBDMap=(CustomSatelliateMap)view.findViewById(R.id.gps_map_view);
			mStatellite=(TextView)view.findViewById(R.id.tv_Statellite);
			mRLchangeStatellite=(RelativeLayout)view.findViewById(R.id.rl_change_statellite);
			mGPSLocationResult=(TextView)view.findViewById(R.id.gps_location_result);
			mGPSLocationStatus=(TextView)view.findViewById(R.id.gps_location_status);
			mGPSLocationHeight=(TextView)view.findViewById(R.id.gps_location_height_value);
			mTvChange=(TextView)view.findViewById(R.id.tv_change);
			mTvChange.setOnClickListener(this);
			mRLchangeStatellite.setOnClickListener(this);
			mStatellite.setText("GPS星图");

		return view;
	}


	@Override
	public void onStart() {
		super.onStart();
		try {
			mBDCommManager.addBDEventListener(mGPSatelliteListener,mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		gplist.clear();
		//通知更新
		showMap(gplist);
		updateView(null);

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			mBDCommManager.removeBDEventListener(mGPSatelliteListener,mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	/**
	 *
	 * Iterable<GpsSatellite> satellites = mGpsStatus.getSatellites();
	 Iterator<GpsSatellite> it = satellites.iterator();
	 List<GpsSatellite> list = new ArrayList<GpsSatellite>();
	 while (it.hasNext()) {
	 GpsSatellite satellite = it.next();
	 // add by llg
	 if (satellite.usedInFix()){
	 //已经定位 卫星

	 }
	 list.add(satellite);
	 }

	 Message msg = Message.obtain();
	 msg.what = GP_SATELLIATE_STATUS;
	 msg.obj = list;
	 mHandler.sendMessage(msg );
	 *
	 *
	 *
     */


	//更新显示内容的方法
	public void updateView(Location location){
		if(location==null) {
			if (mGPSLocationStatus!=null){
				mGPSLocationStatus.setText("未定位");
			}
			if (mGPSLocationResult!=null){
				mGPSLocationResult.setText("0,0");
			}
			if (mGPSLocationHeight!=null){
				mGPSLocationHeight.setText(0+"m");
			}

			//清除  最后数据
			gplist.clear();
			//通知更新
			showMap(gplist);

			return;
		}
		Message msg = Message.obtain();
		msg.what = LOCATION_RESULT;
		msg.obj = location;
		mHandler.sendMessage(msg );
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_change:

				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
				break;

			case R.id.rl_change_statellite:

				Intent intent2 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent2);

			default:
				break;
		}

	}
}
