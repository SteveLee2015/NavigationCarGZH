package com.novsky.map.main;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.LocationParam;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BD2LocActivity extends Activity{
	
	private static final String TAG="BD2LocActivity";
	private Context mContext = BD2LocActivity.this;
	private CustomListView  customListView=null;
	private LocationManager locationManager = null;
	private TextView  mLongitude= null;
	private TextView  mLatitude= null;
	private TextView  mHeight = null;
	private TextView  mTime = null;
	private TextView savedata,deletedata;
	private TextView  mLongitudeLable= null;
	private TextView  mLatitudeLable= null;
	private TextView  mHeightLable = null;
	private  int COOD_FLAG = 0;
	private SimpleDateFormat sdf = null;
	private LocationParam param = null;
	private PowerManager pm=null;
	private WakeLock wakeLock=null;
	
	private LocationListener locationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			double lon=location.getLongitude();
			double lat=location.getLatitude();
			
			Utils.LOCATION_REPORT_LON=location.getLongitude();
			Utils.LOCATION_REPORT_LAT=location.getLatitude();
			Utils.LOCATION_REPORT_ALTITUDE=location.getAltitude();
			Utils.LOCATION_REPORT_SPEED=location.getSpeed();
			Utils.LOCATION_REPORT_BEARING=location.getBearing();
			Utils.LOCATION_REPORT_ACCURACY=location.getAccuracy();
			Utils.LOCATION_REPORT_TIME=location.getTime();
			Log.i(TAG,"Utils.LOCATION_REPORT_SPEED="+Utils.LOCATION_REPORT_SPEED+",Utils.LOCATION_REPORT_BEARING="+Utils.LOCATION_REPORT_BEARING);
			
			if (location.getLongitude() != 0.0 && location.getLatitude() != 0.0
					&& location.getAltitude() != 0.0) {
				param = Utils.translate(lon, lat, location.getAltitude(),
						COOD_FLAG);
			}
			mLongitude.setText((param != null && param.getmLon() != null) ? param
					.getmLon() : mContext.getResources().getString(
					R.string.common_lon_value));
			mLatitude.setText((param != null && param.getmLat() != null) ? param
					.getmLat() : mContext.getResources().getString(
					R.string.common_lat_value));
			mHeight.setText((param != null && param.getmHeight() != null) ? param
					.getmHeight() : mContext.getResources().getString(
					R.string.common_dadi_heigh_value));
			String time = sdf.format(new Date(location.getTime()+(16*60*60*1000)));
			mTime.setText(time);
		}
	};
	boolean trun = true;
	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler.postDelayed(this, 60*1000);
			savedata();
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bd2_loc);
		Log.i(TAG, "---------------------------->initLocation()");
		pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);  
	        //保持cpu一直运行，不管屏幕是否黑屏  
	    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPUKeepRunning");  
	    wakeLock.acquire();  
		initUI();
		savedata.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(trun){
					handler.postDelayed(task, 60*1000);
					handler.post(task);
					savedata.setText("结束采集（60秒/次）");
					trun = false;
				}else{
					handler.removeCallbacks(task);
					savedata.setText("开始采集（60秒/次）");
					trun = true;
				}
			}
		});
		deletedata.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				File sd = Environment.getExternalStorageDirectory() ;
				String path = sd.getPath() + "/RNSSDatacollect"+ ".txt";
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				Toast.makeText(getApplicationContext(),"删除成功！", Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(getApplicationContext(),"数据已经清空！", Toast.LENGTH_SHORT).show();
				}
			}
		});
		/* 坐标增加数据 */
		customListView = (CustomListView) this.findViewById(R.id.bd_coodr_type);
		customListView.setData(getResources().getStringArray(
				R.array.bdloc_zuobiao_array));
		customListView.setOnCustomListener(new OnCustomListListener() {
			@Override
			public void onListIndex(int num) {
				COOD_FLAG = num;
				switch (num) {
					case 0:
						mSwitchCGCS2000HeightCoodriate(param);
						break;
					case 1:
						mSwitchGaoSiCoodriate(param);
						break;
					case 2:
						mSwitchMaiKaTuoCoodriate(param);
						break;
					case 3:
						mSwitchKongJianZhiJiaoCoodriate(param);
						break;
					case 4:
						mSwitchBeiJing54Coodriate(param);
						break;
					default:
						break;
				}
			}
		});
	}

	/**
	 * 经纬度转换成beijing54坐标
	 */
	protected void mSwitchBeiJing54Coodriate(LocationParam param) {
		
		mLongitude.setText((param != null && param.getmLon() != null) ? param
				.getmLon() : mContext.getResources().getString(
				R.string.common_x_value));
		mLongitudeLable.setText(mContext.getResources().getString(
				R.string.common_x_cood));
		mLatitude.setText((param != null && param.getmLat() != null) ? param
				.getmLat() : mContext.getResources().getString(
				R.string.common_y_value));
		mLatitudeLable.setText(mContext.getResources().getString(
				R.string.common_y_cood));
		mHeight.setText((param != null && param.getmHeight() != null) ? param
				.getmHeight() : mContext.getResources().getString(
				R.string.common_z_value));
		mHeightLable.setText(mContext.getResources().getString(
				R.string.common_z_cood));
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		/*查找到服务信息*/
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
		String provider = locationManager.getBestProvider(criteria, false); // 获取GPS信息
		locationManager.requestLocationUpdates(provider, 1000, 0,locationListener);
	}

	/**
	 * 经纬度转换成CGCS2000-正常高
	 */
	public void mSwitchCGCS2000HeightCoodriate(LocationParam param) {
		mLongitude.setText((param != null && param.getmLon() != null) ? param
				.getmLon() : mContext.getResources().getString(
				R.string.common_lon_value));
		mLongitudeLable.setText(mContext.getResources().getString(
				R.string.common_lon_str));
		mLatitude.setText((param != null && param.getmLat() != null) ? param
				.getmLat() : mContext.getResources().getString(
				R.string.common_lat_value));
		mLatitudeLable.setText(mContext.getResources().getString(R.string.common_lat_str));
		mHeight.setText((param != null && param.getmHeight() != null) ? param
				.getmHeight() : mContext.getResources().getString(R.string.common_dadi_heigh_value));
		mHeightLable.setText(mContext.getResources().getString(R.string.common_height_str));
	}

	/**
	 * 经纬度转换成高斯平面坐标
	 */
	public void mSwitchGaoSiCoodriate(LocationParam param) {
		mLongitude.setText((param != null && param.getmLon() != null) ? param
				.getmLon() : mContext.getResources().getString(
				R.string.common_x_value));
		mLongitudeLable.setText(mContext.getResources().getString(
				R.string.common_x_cood));
		mLatitude.setText((param != null && param.getmLat() != null) ? param
				.getmLat() : mContext.getResources().getString(
				R.string.common_y_value));
		mLatitudeLable.setText(mContext.getResources().getString(
				R.string.common_y_cood));
		mHeight.setText((param != null && param.getmHeight() != null) ? param
				.getmHeight() : mContext.getResources().getString(
				R.string.common_height_value));
		mHeightLable.setText(mContext.getResources().getString(
				R.string.common_height_str));
	}

	/**
	 * 经纬度转换成麦卡托平面坐标
	 */
	public void mSwitchMaiKaTuoCoodriate(LocationParam param) {
		mLongitude.setText((param != null && param.getmLon() != null) ? param
				.getmLon() : mContext.getResources().getString(
				R.string.common_x_value));
		mLongitudeLable.setText(mContext.getResources().getString(
				R.string.common_x_cood));
		mLatitude.setText((param != null && param.getmLat() != null) ? param
				.getmLat() : mContext.getResources().getString(
				R.string.common_y_value));
		mLatitudeLable.setText(mContext.getResources().getString(
				R.string.common_y_cood));
		mHeight.setText((param != null && param.getmHeight() != null) ? param
				.getmHeight() : mContext.getResources().getString(
				R.string.common_height_value));
		mHeightLable.setText(mContext.getResources().getString(R.string.common_height_str));
	}

	/**
	 * 经纬度转换成空间直角坐标
	 */
	public void mSwitchKongJianZhiJiaoCoodriate(LocationParam param) {
		mLongitude.setText((param != null && param.getmLon() != null) ? param
				.getmLon() : mContext.getResources().getString(
				R.string.common_x_value));
		mLongitudeLable.setText(mContext.getResources().getString(
				R.string.common_x_cood));
		mLatitude.setText((param != null && param.getmLat() != null) ? param
				.getmLat() : mContext.getResources().getString(
				R.string.common_y_value));
		mLatitudeLable.setText(mContext.getResources().getString(
				R.string.common_y_cood));
		mHeight.setText((param != null && param.getmHeight() != null) ? param
				.getmHeight() : mContext.getResources().getString(
				R.string.common_z_value));
		mHeightLable.setText(mContext.getResources().getString(
				R.string.common_z_cood));
	}

	@Override
	protected void onStop(){
		super.onStop();
		if(wakeLock!=null){
			Log.i(TAG, "---------------------------->removeLocation()");
			wakeLock.release();
			wakeLock=null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* 取消定位注册 */
		locationManager.removeUpdates(locationListener);
	}
	
	/**
	 * 初始化UI
	 */
    public void initUI(){
		mLongitude = (TextView) this.findViewById(R.id.bdloc_lon);
		mLatitude = (TextView) this.findViewById(R.id.bdloc_lat);
		mHeight = (TextView) this.findViewById(R.id.bdloc_height);
		mTime= (TextView) this.findViewById(R.id.bdloc_time);
		savedata = (TextView) this.findViewById(R.id.save_data);
		deletedata = (TextView)this.findViewById(R.id.delete_data);
		mLongitudeLable = (TextView) this.findViewById(R.id.bdloc_lon_lable);
		mLatitudeLable = (TextView) this.findViewById(R.id.bdloc_lat_lable);
		mHeightLable = (TextView) this.findViewById(R.id.bdloc_height_lable);
	    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    
    }
    
	private  void savedata(){
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String filename = "RNSSDatacollect.txt";
			File sd = Environment.getExternalStorageDirectory() ;
			String path = sd.getPath() + "/RNSSDatacollect"+ ".txt";
			File file = new File(path);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				String aa = mLongitude.getText().toString() + mLatitude.getText().toString();
				if(!aa.equals("")){
					String bb = aa+ "\r\n";
					File f = new File(Environment.getExternalStorageDirectory(), filename);
					FileOutputStream out = null;
					try {
						out = new FileOutputStream(f, true);
						out.write(bb.getBytes("UTF-8"));
						Toast.makeText(getApplicationContext(),"存储成功", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					Toast.makeText(getApplicationContext(),"没有可以存储的内容", Toast.LENGTH_SHORT).show();
				}
//			   String aa = 666666666 + "\r\n";
			
			
		} else {
			Toast.makeText(getApplicationContext(),
					"没有SD卡，请插入SD卡尝试！！！", Toast.LENGTH_SHORT).show();
		}
	}
}
