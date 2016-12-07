package com.novsky.map.main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.location.BDEventListener;
import android.location.BDLocation;
import android.location.BDParameterException;
import android.location.BDRDSSManager.HeightType;
import android.location.BDRDSSManager.ImmediateLocState;
import android.location.BDUnknownException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDLocationTabManager;
import com.novsky.map.util.BDRDSSLocationInfoManager;
import com.novsky.map.util.BDTimeCountManager;
import com.novsky.map.util.BDTimeFreqChangedListener;
import com.novsky.map.util.LocSetDatabaseOperation;
import com.novsky.map.util.LocationSet;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

/**
 * 北斗I代定位
 * @author steve
 */
public class BD1LocActivity extends Activity implements
		 OnClickListener, OnCustomListListener{
	/**
	 * Log日志标识
	 */
	private static final String TAG = "BD1LocActivity";
	/**
	 * 上下文
	 */
	private final Context mContext = this;
	/**
	 * 北斗I代定位数据经度的文本组件
	 */
	private TextView mLongitude = null;
	/**
	 * 北斗I代定位数据纬度的文本组件
	 */
	private TextView mLatitude = null;
	/**
	 * 北斗I代定位数据高程的文本组件
	 */
	private TextView mHeight = null;
	/**
	 * 北斗I代定位数据时间的文本组件
	 */
	private TextView mTime = null;
	/**
	 * 显示“经度”或"X"字样的文本组件
	 */
	private TextView mLongitudeLable = null;
	/**
	 * 显示"维度"或"Y"字样的文本组件
	 */
	private TextView mLatitudeLable = null;
	/**
	 * 显示"高程"或"Z"字样的文本组件
	 */
	private TextView mHeightLable = null;
	/**
	 * 定位设置图标
	 */
	private ImageView locationSettingBtn = null;
	/**
	 * 位置报告图标
	 */
	private ImageView locationReportBtn = null;
	/**
	 * 友邻位置图标
	 */
	private ImageView locationFriendsBtn = null;
	/**
	 * 中间菜单图标
	 */
	private ImageView menuLoc = null;
	/**
	 * 紧急定位Button
	 */
	private Button immediateLocationBtn = null;
	/**
	 * 开始定位Button
	 */
	private Button startLocationBtn = null;
	/**
	 * RDSS管理类
	 */
	private BDCommManager manager = null;
	/**
	 * 坐标类型
	 */
	private CustomListView customeListView = null;
	/**
	 * 坐标类型的标识
	 */
	private static int COODRINATE_FLAG = 0;

	/**
	 * 循环定位的服务频度
	 */
	private int frequency = 0;

	/**
	 * TabHost对象
	 */
	private TabHost tabHost = BDLocationPortActivity.tabHost;

	/**
	 * tab管理类
	 */
	private BDLocationTabManager tabManager = null;
	
	
	private ProgressDialog  progressDialog=null;
	
	/**
	 * 音频播放对象
	 */
	private MediaPlayer mediaPlayer;
	
	/**
	 * 定位设置
	 */
	private LocationSet set=null;
	
	/**
	 * 北斗时间频率管理类对象
	 */
	private BDTimeCountManager timeInstance=null;
	
    
    private static final int BD_LOCATION_ITEM=0x100;
    
    
    private static final int BD_TIME_FREQ_ITEM=0x101;
    
	/**
	 * 反馈信息监听器
	 */
	private BDEventListener fkilistener = null;
	
	
	private BDCardInfoManager cardManager=null;
    
	private LocSetDatabaseOperation operation=null;
    
	private  BDTimeFreqChangedListener timeLocFreqListener=new BDTimeFreqChangedListener(){
		@Override
		public void onTimeChanged(int remainder_time) {
			/* 用消息传递数据 */
			Message msg = new Message();
			msg.arg1=remainder_time;
			msg.what=BD_TIME_FREQ_ITEM;
			mhandler.sendMessage(msg);
		}
    };

    private Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
		  super.handleMessage(msg);
		  switch(msg.what){
			  case BD_LOCATION_ITEM:
				    progressDialog.dismiss();
					playSoundAndVibrate();
					Utils.isProgressDialogShowing=false;
					BDLocation location = (BDLocation) msg.obj;
					mLongitude.setText(Utils.setDoubleNumberDecimalDigit(location.mLongitude, 10));
					mLatitude.setText(Utils.setDoubleNumberDecimalDigit(location.mLatitude,10));
					mHeight.setText(String.valueOf(location.earthHeight));
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time=sdf.format(new Date());
					mTime.setText(time);
					break;
			  case BD_TIME_FREQ_ITEM:
					int remainder_time=msg.arg1;
					if (Utils.isCycleLocation){	 
						startLocationBtn.setText("结束定位("+ remainder_time+"秒)");
					}else{
	                    if (remainder_time == 0) {
	    					startLocationBtn.setText("开始定位");
	    					Utils.isImmediateLocation=false;   					
						}else{
							startLocationBtn.setText("开始定位(" + remainder_time+ "秒)");
							if(Utils.isImmediateLocation){
								immediateLocationBtn.setEnabled(true);
							}
					   }
					}	
				  break;
			  default:
				  break;
		  }
		}
	};
	
	
	/**
	 * 定位监听器
	 */
	private BDEventListener listener = new BDEventListener.BDLocationListener() {
		@Override
		public void onLocationChange(BDLocation location) {
			if(location!=null){
				BDRDSSLocationInfoManager singleton = BDRDSSLocationInfoManager.getInstance();
				singleton.setBDLocation(location);
				BDLocation bdlocation = Utils.translate(singleton.getBDLocatoion(),COODRINATE_FLAG);
				/* 用消息传递数据 */
				Message msg = new Message();
				msg.obj = bdlocation;
				msg.what=BD_LOCATION_ITEM;
				mhandler.sendMessage(msg);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bd1_loc);
		this.setRequestedOrientation(Utils.isLand?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		initUI();
		BDRDSSLocationInfoManager singleton = BDRDSSLocationInfoManager.getInstance();
		BDLocation location=singleton.getBDLocatoion();
		if(location!=null){
			if(location.mLongitude!=0){
				mLongitude.setText(location.mLongitude+"");	
			}
			if(location.mLongitude!=0){
				mLatitude.setText(location.mLatitude+"");	
			}
			if(location.earthHeight!=0){
				mHeight.setText(location.earthHeight+"");	
			}
			if(!location.mLocationTime.equals("")){
				mTime.setText(location.mLocationTime);
			}
		}
		manager = BDCommManager.getInstance(this);
		try {
			manager.addBDEventListener(fkilistener,listener);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		timeInstance.registerBDTimeFreqListener(BD1LocActivity.class.getSimpleName(),timeLocFreqListener);
		boolean isStart=Utils.isServiceRunning(this, "com.novsky.map.main.CycleLocationService");
		if(isStart){
			startLocationBtn.setText(this.getResources().getString(R.string.bdloc_stop_loc_str));
		}
		try {
			manager.sendRMOCmdBDV21("BSI", 2, 1);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * 经纬度显示
	 */
	public void mSwitchCoodriate(BDLocation location) {
		//设置经度
		mLongitude.setText((location != null && location.mLongitude != 0.0) ? String.valueOf(location.mLongitude) 
				: mContext.getResources().getString(R.string.common_lon_value));
		//设置纬度
		mLatitude.setText((location != null && location.mLatitude != 0.0) ? String.valueOf(location.mLatitude)
				: mContext.getResources().getString(R.string.common_lat_value));
		// 设置高程
		mHeight.setText((location != null && location.getEarthHeight() != 0.0) ? String.valueOf(location.getEarthHeight()) 
				: mContext.getResources().getString(R.string.common_dadi_heigh_value));
	}

	/**
	 * 修改Lable
	 */
	public void mSwitchCoodriateLabel(String longitudeLableText,
			String mLatitudeLableText, String mHeightLableText) {
		mLongitudeLable.setText(longitudeLableText);
		mLatitudeLable.setText(mLatitudeLableText);
		mHeightLable.setText(mHeightLableText);
	}


	@Override
	protected void onStop() {
		super.onStop();
		try {
			manager.removeBDEventListener(fkilistener,listener);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(operation!=null){
			operation.close();
		}
	}                                                     

	public void initUI() {
		operation=new LocSetDatabaseOperation(this);
		initSound();
		cardManager = BDCardInfoManager.getInstance();
		fkilistener = new BDResponseListener(mContext);
		timeInstance=BDTimeCountManager.getInstance();
		mLongitude = (TextView) this.findViewById(R.id.bdloc_lon);
		mLatitude = (TextView) this.findViewById(R.id.bdloc_lat);
		mHeight = (TextView) this.findViewById(R.id.bdloc_height);
		mTime = (TextView) this.findViewById(R.id.bdloc_time);
		immediateLocationBtn = (Button)this.findViewById(R.id.bd1ImmediateLocBtn);		
		startLocationBtn = (Button) this.findViewById(R.id.bd1LocationBtn);
		mLongitudeLable = (TextView) this.findViewById(R.id.bdloc_lon_lable);
		mLatitudeLable = (TextView) this.findViewById(R.id.bdloc_lat_lable);
		mHeightLable = (TextView) this.findViewById(R.id.bdloc_height_lable);
		locationSettingBtn = (ImageView) this.findViewById(R.id.bd1_loc_set);
		locationReportBtn = (ImageView) this.findViewById(R.id.bd1_loc_report);
		locationFriendsBtn = (ImageView) this.findViewById(R.id.bd1_loc_friends);
		menuLoc = (ImageView) this.findViewById(R.id.bd1_loc_menu);
		customeListView = (CustomListView) this.findViewById(R.id.bd_coodr_type);
		customeListView.setData(getResources().getStringArray(R.array.bdloc_zuobiao_array));
		progressDialog=new ProgressDialog(this.getParent());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("RDSS定位");
		progressDialog.setMessage("正在定位中...");
		tabManager = new BDLocationTabManager(this, tabHost);
		startLocationBtn.setOnClickListener(this);
		immediateLocationBtn.setOnClickListener(this);
		locationSettingBtn.setOnClickListener(this);
		locationReportBtn.setOnClickListener(this);
		locationFriendsBtn.setOnClickListener(this);
		menuLoc.setOnClickListener(this);
		customeListView.setOnCustomListener(this);	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		   case R.id.bd1LocationBtn: {
			   boolean isStart=Utils.isServiceRunning(this, "com.novsky.map.main.CycleLocationService");
			   if(!isStart){
					/* 判断是否安装北斗卡 */
					if (!Utils.checkBDSimCard(this))return;
					/* 定位设置查询 */
					set =operation.getFirst();
					frequency = Integer.valueOf(set.getLocationFeq());
					/*检查是否是循环定位*/
					if (frequency>0){
						startLocationBtn.setText(this.getResources().getString(R.string.bdloc_stop_loc_str));
						Utils.COUNT_DOWN_TIME=frequency;
						Utils.isCycleLocation=true;
						Intent mIntent=new Intent();
						mIntent.setClass(this,CycleLocationService.class);
						startService(mIntent);
					}else{
						Utils.COUNT_DOWN_TIME=cardManager.getCardInfo().mSericeFeq;
					}
					try {
						 manager.sendLocationInfoReqCmdBDV21(ImmediateLocState.LOC_NORMAL_FLAG, Integer.valueOf(set.getHeightType()), "L",Double.valueOf(set.getHeightValue()).doubleValue(),
								Double.valueOf(set.getTianxianValue()).doubleValue(), 0);
						 progressDialog.show();
						 Utils.isProgressDialogShowing=true;
						 new Handler().postDelayed(new Runnable(){
								@Override
								public void run() {
									if(Utils.isProgressDialogShowing){
										progressDialog.dismiss();
										Toast.makeText(mContext, "未接收到定位信息!", Toast.LENGTH_LONG).show();
										Utils.isProgressDialogShowing=false;
									}
	                         	}
							}, 8000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
					//结束循环定位
					Intent mIntent=new Intent();
					mIntent.setClass(this,CycleLocationService.class);
					this.stopService(mIntent);
					Utils.isCycleLocation=true;
					startLocationBtn.setText(this.getResources().getString(R.string.bdloc_common_loc_str));
				}
				break;
		   }case R.id.bd1ImmediateLocBtn:{
				try {
					/* 判断是否安装北斗卡 */
					if (!Utils.checkBDSimCard(mContext))return;
					Utils.isImmediateLocation=true;
					LocSetDatabaseOperation oper = new LocSetDatabaseOperation(mContext);
					set = oper.getFirst();
				    immediateLocationBtn.setEnabled(false);
				    manager.sendLocationInfoReqCmdBDV21(ImmediateLocState.LOC_IMMEDIATE_FLAG,HeightType.HAVE_HEIGHT_VALUE, "L", Double.valueOf(set.getHeightValue()).doubleValue(),
							Double.valueOf(set.getTianxianValue()).doubleValue(), 0);
					Utils.COUNT_DOWN_TIME=(cardManager.getCardInfo().mSericeFeq)*2;
					Log.i(TAG, "COUNT_DOWN_TIME="+Utils.COUNT_DOWN_TIME+","+((cardManager.getCardInfo().mSericeFeq)*2));
					progressDialog.show();
					Utils.isProgressDialogShowing=true;
					new Handler().postDelayed(new Runnable(){
							@Override
							public void run() {
								if(Utils.isProgressDialogShowing){
									progressDialog.dismiss();
									Toast.makeText(mContext, "未接收到定位信息!", Toast.LENGTH_LONG).show();
									Utils.isProgressDialogShowing=false;
								}
                         	}
					}, 8000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case R.id.bd1_loc_friends:{
				Intent intent = new Intent();
				intent.setClass(BD1LocActivity.this,FriendsLocationActivity.class);
				startActivity(intent);
				break;
			}
			
			case R.id.bd1_loc_report:{
				tabHost.clearAllTabs();
				tabManager.addTab("bd1_1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("report_location", mContext.getResources().getString(R.string.bdloc_loc_report_str),BDLocationReportActivity.class);
				//BDLocationCurrentTab.getInstance().setCurrentIndex(1);
				tabManager.setCurrentPageIndex(1);
				break;
			}
			
			case R.id.bd1_loc_set:{				
				tabHost.clearAllTabs();
				tabManager.addTab("bd1_2",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("locationset",mContext.getResources().getString(R.string.bdloc_1_top_tab_msg),BDRDSSLocationSetActivity.class);
				tabManager.setCurrentPageIndex(1);
				break;
			}
			
			case R.id.bd1_loc_menu:{
				tabHost.clearAllTabs();
				tabManager.addTab("bd1_3",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("bd2_1",mContext.getResources().getString(R.string.bdloc_1_top_tab_msg),BD1LocActivity.class);
				tabManager.setCurrentPageIndex(1);
				break;
            }
			default:
				break;
		}
	}

	@Override
	public void onListIndex(int num){
		COODRINATE_FLAG = num;
		BDLocation param = null;
		BDRDSSLocationInfoManager singleton = BDRDSSLocationInfoManager.getInstance();
		BDLocation bdlocation = singleton.getBDLocatoion();
		if (bdlocation!=null&&bdlocation.getLongitude() != 0.0 && bdlocation.getLatitude() != 0.0){
			param = Utils.translate(bdlocation, num);
			mSwitchCoodriate(param);
		}
		switch(num){
			case 0:
				mSwitchCoodriateLabel(mContext.getResources().getString(R.string.common_lon_str),
						              mContext.getResources().getString(R.string.common_lat_str),
						              mContext.getResources().getString(R.string.common_height_str));
				break;
			case 1:
				mSwitchCoodriateLabel(mContext.getResources().getString(R.string.common_lon_str),
			              mContext.getResources().getString(R.string.common_lat_str),
			              mContext.getResources().getString(R.string.common_height_str));
				break;
			case 2:
				mSwitchCoodriateLabel(mContext.getResources().getString(R.string.common_x_cood),
						              mContext.getResources().getString(R.string.common_y_cood),
						              mContext.getResources().getString(R.string.common_height_str));
				break;
			case 3:
				mSwitchCoodriateLabel(mContext.getResources().getString(R.string.common_x_cood),
						              mContext.getResources().getString(R.string.common_y_cood),
						              mContext.getResources().getString(R.string.common_height_str));
				break;
			case 4:
				mSwitchCoodriateLabel(mContext.getResources().getString(R.string.common_x_cood),
						              mContext.getResources().getString(R.string.common_y_cood),
						              mContext.getResources().getString(R.string.common_z_cood));
				break;
			default:
				break;
		}
	}

	 private void initSound(){  
	        if (mediaPlayer == null) {   
	            setVolumeControlStream(AudioManager.STREAM_MUSIC);  
	            mediaPlayer = new MediaPlayer();  
	            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  
	            mediaPlayer.setOnCompletionListener(beepListener);  
	            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.location);  
	            try {  
	                mediaPlayer.setDataSource(file.getFileDescriptor(),  
	                        file.getStartOffset(), file.getLength());  
	                file.close();  
	                mediaPlayer.setVolume(0.1f, 0.1f);  
	                mediaPlayer.prepare();  
	            } catch (IOException e){  
	                mediaPlayer = null;  
	            }  
	        }  
	    }  
	 
	    private void playSoundAndVibrate(){  
	        if (mediaPlayer != null) {  
	            mediaPlayer.start();  
	        }   
	    }
	    
	    private final OnCompletionListener beepListener = new OnCompletionListener(){  
	        public void onCompletion(MediaPlayer mediaPlayer){  
	            mediaPlayer.seekTo(0);  
	        }  
	    };
}