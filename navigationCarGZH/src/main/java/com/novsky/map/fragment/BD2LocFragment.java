package com.novsky.map.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.BDLocation;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.R;
import com.novsky.map.main.CustomListView;
import com.novsky.map.util.BDLocationManager;
import com.novsky.map.util.BDRNSSLocationInfoManager;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

/**
 * 北斗II代定位  RNSS
 * @author steve
 */
public class BD2LocFragment extends Fragment implements OnCustomListListener{

	/**
	 *自定义ListView 
	 */
	private CustomListView coodrView = null;
	
	/**
	 * 经度
	 */
	private TextView mLongitude = null;
	
	/**
	 * 纬度
	 */
	private TextView mLatitude = null;
	
	/**
	 * 高度
	 */
	private TextView mHeight = null;
	
	/**
	 * 定位时间
	 */
	private TextView mTime = null;
	
	/**
	 * 显示"经度"字样
	 */
	private TextView mLongitudeLable = null;
	
	/**
	 * 显示“纬度”字样
	 */
	private TextView mLatitudeLable = null;
	/**
	 * 显示“高度”字样
	 */
	private TextView mHeightLable = null;
	/**
	 * 采集/删除数据
	 */
	private TextView savedata,deletedata;
	/**
	 * 采集的数据
	 */
	Context context;
	private String logdata = null;
	private int COOD_FLAG = 0;
	
	private SimpleDateFormat sdf = null;
	
	private BDCommManager mBDCommManager=null;
	
	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			BDRNSSLocation location=(BDRNSSLocation)msg.obj;
			BDRNSSLocationInfoManager singleton=BDRNSSLocationInfoManager.getInstance();
            BDLocation bdloc=new BDLocation();
            bdloc.setLongitude(location.getLongitude());
            bdloc.setLatitude(location.getLatitude());
            bdloc.setEarthHeight(location.getAltitude());
            singleton.setBDLocation(bdloc);
			BDLocation bdlocation = Utils.translate(bdloc,COOD_FLAG);
			mSwitchCoodriate(bdlocation);
			mTime.setText(sdf.format(new Date(location.getTime()+8*60*60*1000)));
			logdata ="\r\n"+"时间:"+mTime.getText().toString()
					+"\r\n"+"经度:"+location.getLongitude()
					+"\r\n"+"纬度:"+location.getLatitude()
					+"\r\n"+"高程:"+location.getAltitude()
					+"\r\n"+"速度:"+location.getSpeed();
		}
	};

	private BDRNSSLocationListener mBDRNSSLocationListener=new BDRNSSLocationListener(){
		
		@Override
		public void onLocationChanged(BDRNSSLocation arg0) {
			Message message=mHandler.obtainMessage();
			message.obj=arg0;
			mHandler.sendMessage(message);
		}

		@Override
		public void onProviderDisabled(String arg0){}

		@Override
		public void onProviderEnabled(String arg0) {}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2){}
		
	};

	/**
	 * 定位监听
	 */
	private LocationListener locationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}

		@Override
		public void onProviderEnabled(String provider){}

		@Override
		public void onProviderDisabled(String provider) {
			Utils.checkBDLocationPort(getActivity() ,false,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int whichButton) {
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
						}
					}, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int whichButton) {
							//Intent countTimeIntent = new Intent(getActivity(), CountDownTimeService.class);
							//getActivity().stopService(countTimeIntent);
							Intent intent = new Intent(Intent.ACTION_MAIN);
							intent.addCategory(Intent.CATEGORY_HOME);
							startActivity(intent);
					}
			});	
		}

		@Override
		public void onLocationChanged(Location location) {
			if(location!=null){
	            BDRNSSLocationInfoManager singleton=BDRNSSLocationInfoManager.getInstance();
	            BDLocation bdloc=new BDLocation();
	            bdloc.setLongitude(location.getLongitude());
	            bdloc.setLatitude(location.getLatitude());
	            bdloc.setEarthHeight(location.getAltitude());
	            singleton.setBDLocation(bdloc);
				BDLocation bdlocation = Utils.translate(bdloc,COOD_FLAG);
				mSwitchCoodriate(bdlocation);
				mTime.setText(sdf.format(new Date(location.getTime()+8*60*60*1000)));
			}
		}
	};
	//Button切换开关
	boolean trun = true;
	private Handler handler = new Handler();
	//执行计时 60秒执行一次保存的方法
	private Runnable task = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			savedata();
			handler.postDelayed(this, 60*1000);
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rooter=inflater.inflate(R.layout.activity_bd2_loc, null);
		initUI(rooter);
	    coodrView.setOnCustomListener(this);
	    
		if (Utils.isSaveData) {
			savedata.setText("结束采集数据");
			trun = false;
		}
		//保存数据Button
		savedata.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(trun){
					handler.postDelayed(task, 60*1000);
//					handler.post(task);
//					savedata();
					savedata.setText("结束采集数据");
					Toast.makeText(getActivity().getApplicationContext(),"为保证实时采集请尽量保证停留在此界面！", Toast.LENGTH_SHORT).show();
					trun = false;
					Utils.isSaveData = true;
				}else{
					handler.removeCallbacks(task);
					savedata.setText("开始采集数据");
					Toast.makeText(getActivity().getApplicationContext(),"已经结束数据采集！", Toast.LENGTH_SHORT).show();
					trun = true;
					Utils.isSaveData = false;
				}
			}
		});
		//清楚数据Button
		deletedata.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				File sd = Environment.getExternalStorageDirectory() ;
				String path = sd.getPath() + "/RNSSDatacollect"+ ".txt";
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				Toast.makeText(getActivity().getApplicationContext(),"删除成功！", Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(getActivity().getApplicationContext(),"数据已经清空！", Toast.LENGTH_SHORT).show();
				}
			}
		});
	    
	    if("S500".equals(Utils.DEVICE_MODEL)){
			BDLocationManager.getInstance(getActivity()).requestLocationUpdate(locationListener);
		}else{
			try {
				mBDCommManager.addBDEventListener(mBDRNSSLocationListener);
			} catch(BDParameterException e) {
				e.printStackTrace();
			} catch(BDUnknownException e) {
				e.printStackTrace();
			}
		}
	    
		return rooter;
	}




	@Override
	public void onStart() {
		super.onStart();
	}
	
	
	public void initUI(View view){
		mBDCommManager=BDCommManager.getInstance(getActivity());
		mLongitude = (TextView) view.findViewById(R.id.bdloc_lon);
		mLatitude = (TextView) view.findViewById(R.id.bdloc_lat);
		mHeight = (TextView) view.findViewById(R.id.bdloc_height);
		mTime = (TextView) view.findViewById(R.id.bdloc_time);
		savedata = (TextView) view.findViewById(R.id.save_data);
		deletedata = (TextView) view.findViewById(R.id.delete_data);
		mLongitudeLable = (TextView) view.findViewById(R.id.bdloc_lon_lable);
		mLatitudeLable = (TextView) view.findViewById(R.id.bdloc_lat_lable);
		mHeightLable = (TextView) view.findViewById(R.id.bdloc_height_lable);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		coodrView = (CustomListView) view.findViewById(R.id.bd_coodr_type);
		coodrView.setData(getResources().getStringArray(R.array.bdloc_zuobiao_array));
	}
	
	
	/**
	 * 经纬度显示
	 */
	public void mSwitchCoodriate(BDLocation bdlocation) {
		// 设置经度
		mLongitude.setText((bdlocation != null && bdlocation.mLongitude != 0.0) ? Utils.setDoubleNumberDecimalDigit(bdlocation.mLongitude, 10): getActivity()
				 .getResources().getString(R.string.common_lon_value));
		// 设置纬度
		mLatitude.setText((bdlocation != null && bdlocation.mLatitude != 0.0) ? Utils.setDoubleNumberDecimalDigit(bdlocation.mLatitude, 10): getActivity()
				.getResources().getString(R.string.common_lat_value));
		// 设置高程
		mHeight.setText((bdlocation != null && bdlocation.getEarthHeight() != 0.0) ? String
				.valueOf(bdlocation.getEarthHeight()) : getActivity().getResources()
				.getString(R.string.common_dadi_heigh_value));
		
	}
	
	/**
	 * 经纬度显示
	 */
	public void mSwitchCoodriate(BDRNSSLocation bdlocation) {
		// 设置经度
		mLongitude.setText((bdlocation != null && bdlocation.getLongitude() != 0.0) ? Utils.setDoubleNumberDecimalDigit(bdlocation.getLongitude(), 10): getActivity()
				 .getResources().getString(R.string.common_lon_value));
		// 设置纬度
		mLatitude.setText((bdlocation != null && bdlocation.getLatitude() != 0.0) ? Utils.setDoubleNumberDecimalDigit(bdlocation.getLatitude(), 10): getActivity()
				.getResources().getString(R.string.common_lat_value));
		// 设置高程
		mHeight.setText((bdlocation != null && bdlocation.getAltitude() != 0.0) ? String
				.valueOf(bdlocation.getAltitude()) : getActivity().getResources()
				.getString(R.string.common_dadi_heigh_value));
	}

	/**
	 * 修改Lable
	 */
	public void mSwitchCoodriateLabel(String longitudeLableText,String mLatitudeLableText, String mHeightLableText) {
		mLongitudeLable.setText(longitudeLableText);
		mLatitudeLable.setText(mLatitudeLableText);
		mHeightLable.setText(mHeightLableText);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(!"S500".equals(Utils.DEVICE_MODEL)){
			if(mBDCommManager!=null){
				try {
					mBDCommManager.removeBDEventListener(mBDRNSSLocationListener);
				} catch (BDUnknownException e) {
					e.printStackTrace();
				} catch (BDParameterException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void onListIndex(int num) {
		COOD_FLAG = num;
		BDRNSSLocationInfoManager singleton=BDRNSSLocationInfoManager.getInstance();
		BDLocation location=singleton.getLocatoion();
		if (location!=null&&location.getLongitude() != 0.0 && location.getLatitude() != 0.0) {
		  mSwitchCoodriate(location);
		}
		switch (num) {
		case 0://2000大地
			mSwitchCoodriateLabel(
					getActivity().getResources().getString(R.string.common_lon_str),
					getActivity().getResources().getString(R.string.common_lat_str),
					getActivity().getResources().getString(R.string.common_height_str));
			break;
		case 1://高斯
			mSwitchCoodriateLabel(
					getActivity().getResources().getString(R.string.common_x_cood),
					getActivity().getResources().getString(R.string.common_y_cood),
					getActivity().getResources().getString(R.string.common_z_cood));
			break;
		case 2://麦卡托
			mSwitchCoodriateLabel(
					getActivity().getResources().getString(R.string.common_x_cood),
					getActivity().getResources().getString(R.string.common_y_cood),
					getActivity().getResources().getString(R.string.common_z_cood));
			break;
		case 3://空间直角
			mSwitchCoodriateLabel(
					getActivity().getResources().getString(R.string.common_x_cood),
					getActivity().getResources().getString(R.string.common_y_cood),
					getActivity().getResources().getString(R.string.common_z_cood));
			break;
		case 4://北京54
			mSwitchCoodriateLabel(
					getActivity().getResources().getString(R.string.common_x_cood),
					getActivity().getResources().getString(R.string.common_y_cood),
					getActivity().getResources().getString(R.string.common_height_str));
			break;
		default:
			break;
		}
    }
	//GP 采集并存储RN数据
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
//				String aa ="时间："+mTime.getText().toString()+ "经度："+mLongitude.getText().toString() +" 纬度："+ mLatitude.getText().toString();
			String aa = logdata;
			if(!aa.equals("")){
					String bb = aa+ "\r\n";
					File f = new File(Environment.getExternalStorageDirectory(), filename);
					FileOutputStream out = null;
					try {
						out = new FileOutputStream(f, true);
						out.write(bb.getBytes("UTF-8"));
						Toast.makeText(getActivity().getApplicationContext(),"采集数据成功！", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(getActivity().getApplicationContext(),"没有可以存储的内容", Toast.LENGTH_SHORT).show();
				}
//			   String aa = 666666666 + "\r\n";
			
			
		} else {
			Toast.makeText(getActivity().getApplicationContext(),
					"没有SD卡，请插入SD卡尝试！！！", Toast.LENGTH_SHORT).show();
		}
	}
	
}
