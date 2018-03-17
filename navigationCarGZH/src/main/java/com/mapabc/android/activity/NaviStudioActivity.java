package com.mapabc.android.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.BDEventListener;
import android.location.BDLocationReport;
import android.location.BDParameterException;
import android.location.BDRNSSManager.LocationStrategy;
import android.location.BDUnknownException;
import android.location.CardInfo;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.bd.comm.protocal.BDSatellite;
import com.bd.comm.protocal.BDSatelliteListener;
import com.bd.comm.protocal.GPSatellite;
import com.bd.comm.protocal.GPSatelliteListener;
import com.mapabc.android.activity.base.AutoNaviMap;
import com.mapabc.android.activity.base.BDControl;
import com.mapabc.android.activity.base.BaseActivity;
import com.mapabc.android.activity.base.CarBackEvent;
import com.mapabc.android.activity.base.Constants;
import com.mapabc.android.activity.base.CurrentPointListener;
import com.mapabc.android.activity.base.ExitDialog;
import com.mapabc.android.activity.base.GpsControl;
import com.mapabc.android.activity.base.MapModelControl;
import com.mapabc.android.activity.base.NaviControl;
import com.mapabc.android.activity.base.ReportLayer;
import com.mapabc.android.activity.base.RouteLayer;
import com.mapabc.android.activity.base.TMCControl;
import com.mapabc.android.activity.base.VolumeControl;
import com.mapabc.android.activity.base.ZoomControl;
import com.mapabc.android.activity.listener.BackListener;
import com.mapabc.android.activity.listener.DisPatchInfo;
import com.mapabc.android.activity.listener.MyMapListener;
import com.mapabc.android.activity.listener.NaviMapTouchListener;
import com.mapabc.android.activity.route.RoutUtils;
import com.mapabc.android.activity.utils.ActivityStack;
import com.mapabc.android.activity.utils.SettingForLikeTools;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.mapabc.android.activity.utils.UIResourceUtil;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.MapView;
import com.mapabc.naviapi.RouteAPI;
import com.mapabc.naviapi.listener.DayOrNightListener;
import com.mapabc.naviapi.listener.MapListener;
import com.mapabc.naviapi.map.DayOrNightControl;
import com.mapabc.naviapi.search.SearchResultInfo;
import com.mapabc.naviapi.type.FloatValue;
import com.mapabc.naviapi.type.NSLonLat;
import com.novsky.map.main.BDAvailableStatelliteManager;
import com.novsky.map.main.BDInstructionNav;
import com.novsky.map.main.BDInstructionNavOperation;
import com.novsky.map.main.BDLineNav;
import com.novsky.map.main.BDLineNavOperation;
import com.novsky.map.main.BDManagerHorizontalActivity;
import com.novsky.map.main.BDPoint;
import com.novsky.map.main.BDSendMsgLandScapeActivity;
import com.novsky.map.main.BDSendMsgPortActivity;
import com.novsky.map.main.CustomLocationManager;
import com.novsky.map.main.FriendBDPoint;
import com.novsky.map.main.FriendLocation;
import com.novsky.map.main.ReportPosListener;
import com.novsky.map.main.ReportPosManager;
import com.novsky.map.main.TimeService;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.CollectionUtils;
import com.novsky.map.util.FriendsLocationDatabaseOperation;

import static com.novsky.map.util.Utils.LOCATION_LATDIR;
import static com.novsky.map.util.Utils.LOCATION_LONDIR;

@SuppressLint("NewApi")
public class NaviStudioActivity extends BaseActivity {

	private static final String TAG = "NaviStudioActivity";
	public static ExecutorService executorService = Executors
			.newFixedThreadPool(10);
	public MapView mapView = null;// 地图实例
	public ImageButton zoomin, zoomout, volume, ibMapModel, gpsstate, back_car;
	public ImageView bdstate;
	public ImageButton currentPointBtn;
	public ImageButton plantRouteBtn;
	private Context mContext = this;
	public RelativeLayout mapbarLayout;
	public LinearLayout zoomLayout;
	public Animation mShowAction = null; // 动态效果（显示）
	public Animation mHiddenAction = null; // 动态效果（隐藏）
	public ZoomControl zoomControl;
	public VolumeControl volumeControl;
	public static AudioManager audioManager;
	public MapModelControl mapModelControl;
	public GpsControl gpsControl;
	public TextView scaleTextView;
	private NaviMapTouchListener mapTouchListener = null;
	public CarBackEvent carBackEvent;
	private MyMapListener mapListener = new MyMapListener(this);
	public CurrentPointListener currentPointListener;
	public NaviControl naviControl;
	public ProgressDialog pdg;
	public AlertDialog dialog;
	public SearchResultInfo poiInfo;
	private Animation left_in, right_in;
	private BDCommManager mananger = null;
	private ReportLayer reportLayer = null;
	private CustomLocationManager manager = null;
	private boolean isExit;
	boolean isTMCOpen = false;
	public static final int REALTIMETRAFFIC = 201;// 加载实时交通
	private final static int RECEIVE_CARD_INFO = 0xD2;
	private final static int ACCESS_CARD_INFO = 0xD0;
	private Intent cycleLocationService = null;
	private ProgressDialog progressDialog = null;
	private SharedPreferences share = null;
	
	//保存友邻位置的sp
	private SharedPreferences friendID_Sp;
	
	private Editor friendID_editor;

	/**
	 * 北斗有效卫星管理对象
	 */
	private BDAvailableStatelliteManager statellitesManager = null;
	private BackListener back;
	public boolean blEnableSearch = true;// 是否禁用地点搜索菜单

	private final static int BD_SATELLATE_ITEM = 0x10111,
			GPS_SATELLATE_ITEM = 0x10112;

	/**
	 * ??????????????
	 */
	private int bdCurrentAvailable = 0, gpsCurrentAvailable = 0;

	private BDEventListener localInfoListener = new BDEventListener.LocalInfoListener() {
		@Override
		public void onCardInfo(CardInfo card) {
			// 由于是融合两个工程,并且两个工程的该接口定义不一样，所以要设置两次。
			com.novsky.map.util.Utils.setCardInfo(card);
			BDCardInfoManager.getInstance().setCardInfo(card);
			Message message = mHandler.obtainMessage();
			message.obj = card;
			message.what = RECEIVE_CARD_INFO;
			mHandler.sendMessage(message);
		}
	};

	
	/**
	 * GPS 卫星状态监听
	 */
	private GPSatelliteListener mGPSatelliteListener = new GPSatelliteListener() {
		@Override
		public void onGpsStatusChanged(List<GPSatellite> arg0) {
			Message message = mHandler.obtainMessage();
			message.what = GPS_SATELLATE_ITEM;
			message.obj = arg0;
			mHandler.sendMessage(message);
		}
	};

	/**
	 * BD卫星状态监听
	 */
	private BDSatelliteListener mBDSatelliteListener = new BDSatelliteListener() {
		@Override
		public void onBDGpsStatusChanged(List<BDSatellite> arg0) {
			Message message = mHandler.obtainMessage();
			message.obj = arg0;
			message.what = BD_SATELLATE_ITEM;
			mHandler.sendMessage(message);
		}
	};


	private BDRNSSLocationListener mBDRNSSLocationListener = new BDRNSSLocationListener() {

		@Override
		public void onLocationChanged(BDRNSSLocation location) {
			com.novsky.map.util.Utils.LOCATION_REPORT_LON = location
					.getLongitude();
			com.novsky.map.util.Utils.LOCATION_LONDIR = location.getExtras().getString("londir");
			com.novsky.map.util.Utils.LOCATION_LATDIR = location.getExtras().getString("latdir");
			com.novsky.map.util.Utils.LOCATION_REPORT_LAT = location
					.getLatitude();
			com.novsky.map.util.Utils.LOCATION_REPORT_ALTITUDE = location
					.getAltitude();
			com.novsky.map.util.Utils.LOCATION_REPORT_SPEED = location
					.getSpeed();
			com.novsky.map.util.Utils.LOCATION_REPORT_BEARING = location
					.getBearing();
			com.novsky.map.util.Utils.LOCATION_REPORT_ACCURACY = location
					.getAccuracy();
			com.novsky.map.util.Utils.LOCATION_REPORT_TIME = location.getTime();
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String arg0) {
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}

	};
	/**
	 * 处理其他对象或者线程发送过来的消息
	 */
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REALTIMETRAFFIC:// 打开或者关闭实时交通
				MenuItem item = (MenuItem) msg.obj;
				if (!(TMCControl.TMC_STATUS == TMCControl.TMC_OPENED)
						&& ToolsUtils.checkNetWork(NaviStudioActivity.this)) {
					item.setTitle(R.string.menu_closeTMC);
					TMCControl.getInstance(mapView).startTMC();
				} else {
					item.setTitle(R.string.menu_openTMC);
					TMCControl.getInstance(mapView).stopTMC();
				}
				break;
			case ACCESS_CARD_INFO: {
				progressDialog = com.novsky.map.util.Utils
						.createProgressDialog(NaviStudioActivity.this, "提示",
								"正在读取北斗卡信息...");
				progressDialog.show();
				try {
					mananger.sendAccessCardInfoCmdBDV21(0, 0);
				} catch (BDUnknownException e) {
					e.printStackTrace();
				}
				break;
			}
			case RECEIVE_CARD_INFO: {
				progressDialog.dismiss();
				// 判断通讯录中是否有该卡号
				CardInfo cardInfo = com.novsky.map.util.Utils.getCardInfo();
				String name = com.novsky.map.util.Utils
						.getContactNameFromPhoneNum(
								mContext,
								cardInfo.getCardAddress() != null ? cardInfo
										.getCardAddress() : "");
				if (name == null
						&& !"2097151".equals(cardInfo.getCardAddress())) {
					com.novsky.map.util.Utils.deleteSamePhone(mContext,
							"本机北斗卡号");
					com.novsky.map.util.Utils.insertPhoneNumber(mContext,
							"本机北斗卡号", cardInfo.getCardAddress());
				}
				break;
			}
			//北斗 卫星条目数
			case BD_SATELLATE_ITEM: {
				List<BDSatellite> bdsatellites = (List<BDSatellite>) msg.obj;
				if (bdsatellites != null && bdsatellites.size() > 0) {
					
					 Iterator<BDSatellite> it = bdsatellites.iterator();
					while (it.hasNext()) {
						BDSatellite satellite = it.next();
						int statelliteID = satellite.getPrn();
						float zaizaobi = satellite.getSnr();// 载噪比
						if (statelliteID >= 160) {
							if (satellite.usedInFix() && (zaizaobi > 5)) {
								bdCurrentAvailable++;
							}
						}
						Log.i(TAG, "statelliteID=" + statelliteID
								+ ",zaizaobi=" + zaizaobi);
					}
					// Log.i(TAG, "bdCurrentAvailable="+bdCurrentAvailable);
					
					//TODO  通过修改该参数 改变信号量
					//bdCurrentAvailable = 9;
					int locationModel = share.getInt("LOCATION_MODEL", 0);
					if (locationModel == LocationStrategy.BD_ONLY_STRATEGY
							|| locationModel == LocationStrategy.HYBRID_STRATEGY) {
						Log.d(TAG, "handler 单北斗和混合模式");
						if (bdCurrentAvailable > 0) {
							if (bdCurrentAvailable > 0
									&& bdCurrentAvailable < 4) {
//								bdstate.setBackground(getResources().getDrawable(R.drawable.navistudio_bd_a_1));
								bdstate.setImageResource(R.drawable.navistudio_bd_a_1);
							} else if (bdCurrentAvailable >= 4
									&& bdCurrentAvailable < 6) {
								bdstate.setImageResource(R.drawable.navistudio_bd_a_2);
//								bdstate.setBackground(getResources().getDrawable(R.drawable.navistudio_bd_a_2));
							} else if (bdCurrentAvailable >= 6
									&& bdCurrentAvailable < 8) {
								bdstate.setImageResource(R.drawable.navistudio_bd_a_3);
//								bdstate.setBackground(getResources().getDrawable(R.drawable.navistudio_bd_a_3));
							} else if (bdCurrentAvailable >= 8) {
								bdstate.setImageResource(R.drawable.navistudio_bd_a_4);
//								bdstate.setBackground(getResources().getDrawable(R.drawable.navistudio_bd_a_4));
							}
							bdCurrentAvailable = 0;
						}
					} else {
						//bdstate.setImageResource(R.drawable.navistudio_bd_0_);
						//bdstate.setBackground(getResources().getDrawable(R.drawable.navistudio_bd_0_));
					}
				}
				break;
			}
			
			//GPS 卫星条目数
			case GPS_SATELLATE_ITEM: {
				List<GPSatellite> list = (List<GPSatellite>) msg.obj;
				if (list != null && list.size() > 0) {
					Iterator<GPSatellite> gpsInterator = list.iterator();
					while (gpsInterator.hasNext()) {
						GPSatellite satellite = gpsInterator.next();
						int statelliteID = satellite.getPrn();
						float zaizaobi = satellite.getSnr();// 载噪比
						if (statelliteID <= 32) {
							if (satellite.usedInFix()
									&& (satellite.getSnr() > 5)) {
								gpsCurrentAvailable++;
							}
						}
					}
					//Log.i(TAG, "gpscurrentAvailable=" + gpsCurrentAvailable);
					int locationModel1 = share.getInt("LOCATION_MODEL", 0);
					//Log.i(TAG, "locationModel=" + locationModel1);
					//TODO  通过修改该参数 改变信号量
					//gpsCurrentAvailable = 9;
					if (locationModel1 == LocationStrategy.GPS_ONLY_STRATEGY
							|| locationModel1 == LocationStrategy.HYBRID_STRATEGY) {
						Log.d(TAG, "handler 单GPS和混合模式");
						if (gpsCurrentAvailable > 0) {
							if (gpsCurrentAvailable > 0
									&& gpsCurrentAvailable < 4) {
								gpsstate.setImageResource(R.drawable.navistudio_gps_a_1_x);
//								gpsstate.setBackground(getResources().getDrawable(R.drawable.navistudio_gps_a_1_x));
							} else if (gpsCurrentAvailable >= 4
									&& gpsCurrentAvailable < 6) {
								gpsstate.setImageResource(R.drawable.navistudio_gps_a_2_x);
//								gpsstate.setBackground(getResources().getDrawable(R.drawable.navistudio_gps_a_2_x));
							} else if (gpsCurrentAvailable >= 6
									&& gpsCurrentAvailable < 8) {
								gpsstate.setImageResource(R.drawable.navistudio_gps_a_3_x);
//								gpsstate.setBackground(getResources().getDrawable(R.drawable.navistudio_gps_a_3_x));
							} else if (gpsCurrentAvailable >= 8) {
								gpsstate.setImageResource(R.drawable.navistudio_gps_a_4_x);
//								gpsstate.setBackground(getResources().getDrawable(R.drawable.navistudio_gps_a_4_x));
							}
							gpsCurrentAvailable = 0;
						}else {
							
							gpsstate.setImageResource(R.drawable.navistudio_gps_0_x);
//						gpsstate.setBackground(mContext.getResources()
//								.getDrawable(R.drawable.navistudio_gps_0_x));
						}
					} 
				}
				break;
			}
			default:
				break;
			}
		}
	};

	// 自动判断下昼夜模式监听
	DayOrNightListener dayOrNightListener = new DayOrNightListener() {
		@Override
		public void changestatus(boolean status) {
			// true白天，false黑夜
			updateUIStyle(status);
		}
	};
	
	

	/*
	 * 与黑夜白天相关控件样式更新
	 */
	public void updateUIStyle(boolean mdayOrNight) {
		currentPointListener.updateCalculateBtn_Style();
		currentPointListener.updateCurrentPoint_Style();
		this.zoomControl.initZoom();
		if (mdayOrNight) {
			// 白天
			if (ToolsUtils.isLand(this)) {
				mapbarLayout
						.setBackgroundResource(R.drawable.navistudio_roadname_land_day);
			} else {
				mapbarLayout
						.setBackgroundResource(R.drawable.navistudio_roadname_port_day);
			}

			scaleTextView.setTextColor(Color.BLACK);
			if (blEnableSearch) {
				back_car.setBackgroundResource(R.drawable.navistudio_backcar_day);
			} else {
				back_car.setBackgroundResource(R.drawable.navistudio_backbutton_day);
			}
		} else {
			// 黑夜
			if (ToolsUtils.isLand(this)) {
				mapbarLayout
						.setBackgroundResource(R.drawable.navistudio_roadname_land_night);
			} else {
				mapbarLayout
						.setBackgroundResource(R.drawable.navistudio_roadname_port_night);
			}
			scaleTextView.setTextColor(Color.WHITE);
			if (blEnableSearch) {
				back_car.setBackgroundResource(R.drawable.navistudio_backcar_night);
			} else {
				back_car.setBackgroundResource(R.drawable.navistudio_backbutton_night);
			}
		}
		// 更新声音按钮样式
		if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
			volume.setBackgroundResource(R.drawable.navistudio_volumemute);
		} else {
			volume.setBackgroundResource(R.drawable.navistudio_volumemax);
		}
		if (naviControl != null) {
			naviControl.setColor(mdayOrNight);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(com.novsky.map.util.Utils.isLand ? Configuration.ORIENTATION_LANDSCAPE
				: Configuration.ORIENTATION_PORTRAIT);
		audioManager = (AudioManager) getSystemService(Activity.AUDIO_SERVICE);
		statellitesManager = BDAvailableStatelliteManager.getInstance();
		share = mContext.getSharedPreferences("LOCATION_MODEL_ACTIVITY", 0);
		friendID_Sp = mContext.getSharedPreferences("LAST_SHOW_FRIEND_ID", Context.MODE_PRIVATE);
		friendID_editor = friendID_Sp.edit();
		initMapView();
		mananger = BDCommManager.getInstance(this);
		try {
			mananger.addBDEventListener(localInfoListener);
		} catch (BDParameterException e1) {
			e1.printStackTrace();
		} catch (BDUnknownException e1) {
			e1.printStackTrace();
		}
		if ("S500".equals(com.novsky.map.util.Utils.DEVICE_MODEL)) {

		} else {
			try {
				mananger.addBDEventListener(mBDSatelliteListener,mGPSatelliteListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		}
		// 增加在JS_OnInit方法中
		if ("S500".equals(com.novsky.map.util.Utils.DEVICE_MODEL)) {
			/* 发送开启BD和GPS共同显示星图的广播 */
			Intent intentReport = new Intent(
					"android.intent.action.BDGPS_REPORT");
			intentReport.putExtra("bdreport", 1);
			sendStickyBroadcast(intentReport);
		}
		try {
			mananger.sendRMOCmdBDV21("BSI", 2, 1);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(ACCESS_CARD_INFO);
		manager = new CustomLocationManager(this);
		reportLayer = new ReportLayer();
		back = new BackListener(this, true);
		ToolsUtils.continueRoute();
		startTimeService();
		/* 发送开启BD和GPS共同显示星图的广播 */
		Intent satelliteIntent = new Intent(
				"android.intent.action.BDGPS_REPORT");
		satelliteIntent.putExtra("bdreport", 1);
		sendStickyBroadcast(satelliteIntent);
		SharedPreferences reportCfgPrefs = getSharedPreferences(
				"LOCATION_REPORT_CFG", 0);
		String address = reportCfgPrefs.getString("USER_ADDRESS", "");
		SharedPreferences autoReportPreferences = getSharedPreferences(
				"AUTO_REPORT_TYPE_PREFS", 0);
		int autoReportType = autoReportPreferences
				.getInt("AUTO_REPORT_TYPE", 0);
		// 判断当前循环位置回报是否启动以及address是否为空并且是否设置为自动位置回报
		// if((autoReportType==0)&&address!=null&&!"".equals(address)&&(!com.novsky.map.util.Utils.isServiceRunning(this,
		// "CycleLocationReportService"))){
		// cycleLocationService = new
		// Intent(this,CycleLocationReportService.class);
		// startService(cycleLocationService);
		// reportCfgPrefs.edit().putString("REPORT_SWITCH", "on").commit();
		// }else{
		// reportCfgPrefs.edit().putString("REPORT_SWITCH", "off").commit();
		// }
		// 启动服务开始校时
		// Intent checkTimeIntent=new Intent(this,CheckTimeService.class);
		// startService(checkTimeIntent);
		// Date date=new Date();
		// try {
		// SystemDateTime.setDateTime(date.getTime()+8*60*60*1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	@Override
	protected void onPause() {
		mapListener.isStart = false;// 暂时关闭自动回车位功能
		/*************** 离开当前界面保存地图的比例尺 ******************/
		int scale = (int) MapAPI.getInstance().getMapScale();
		SettingForLikeTools.saveMapScale(this, scale);
		super.onPause();
		com.novsky.map.util.Utils.checkNaviMap = false;
        try {
            mananger.removeBDEventListener(mBDRNSSLocationListener);
        } catch (BDUnknownException e) {
            e.printStackTrace();
        } catch (BDParameterException e) {
            e.printStackTrace();
        }
    }

	@Override
	protected void onRestart() {
		super.onRestart();

		// 解决 轨迹预览返回 黑屏bug
		initMapView();
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		com.novsky.map.util.Utils.checkNaviMap = true;
		ViewParent parent = mapView.getParent();
		if (parent != null && (parent instanceof FrameLayout)) {
			((FrameLayout) parent).removeView(mapView);
		}
		// if(!Utils.isLoad){
		// Utils.isLoad=true;
		flo.addView(mapView, nIndex, mView.getLayoutParams());
		naviControl = NaviControl.getInstance();
		RouteAPI.getInstance().setCallBack(naviControl);
		naviControl.setLayout(findViewById(R.id.fl_navilayout), this, mapView);
		setAnimation();
		initComponents();
		addListener();
		int scale = SettingForLikeTools.getMapScale(this);
		MapAPI.getInstance().setMapScale(scale);
		// }
		// COUNT_LAUNCHER_TOTAL++;
		/* 检测北斗通信和北斗导航是否打开 */
		// LocationManager locationManager
		// =(LocationManager)this.getSystemService(LOCATION_SERVICE);
		// ContentResolver resolver = this.getContentResolver();
		// boolean bdMessageOn = Settings.Secure.isLocationProviderEnabled(
		// resolver, BDRDSSManager.BD_MESSAGE_PROVIDER);
		// final boolean gpsOn =
		// locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
		// if (!gpsOn) {
		// final AlertDialog alert = new AlertDialog.Builder(this)
		// .setTitle("提示")
		// .setMessage("北斗导航未启用,是否进入启用?")
		// .setCancelable(false)
		// .setNegativeButton("否",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int whichButton) {
		// Intent intent = new Intent(
		// Intent.ACTION_MAIN);
		// intent.addCategory(Intent.CATEGORY_HOME);
		// startActivity(intent);
		// // 关闭串口
		// System.exit(0);
		// }
		// })
		// .setPositiveButton("是",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int whichButton) {
		// Intent intent = new Intent(
		// Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		// startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
		// }
		// }).create();
		// alert.show();
		// return;
		// }else if (!bdMessageOn) {
		// final AlertDialog alert = new AlertDialog.Builder(this)
		// .setTitle("提示")
		// .setMessage("北斗短报文未启用,是否进入启用?")
		// .setCancelable(false)
		// .setNegativeButton("否",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int whichButton) {
		// Intent intent = new Intent(
		// Intent.ACTION_MAIN);
		// intent.addCategory(Intent.CATEGORY_HOME);
		// startActivity(intent);
		// // 关闭串口
		// System.exit(0);
		// }
		// })
		// .setPositiveButton("是",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int whichButton) {
		// Intent intent = new Intent(
		// "android.settings.BD_SETTINGS");
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(intent);
		// }
		// }).create();
		// alert.show();
		// return ;
		// }else{
		if ("S500".equals(com.novsky.map.util.Utils.DEVICE_MODEL)) {
			manager.initLocation();
		} else {
			try {
				mananger.addBDEventListener(mBDRNSSLocationListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		}
		// }
		ReportPosManager.addEventListener(new ReportPosListener() {
			@Override
			public void reportPos(BDLocationReport location) {
				Toast.makeText(NaviStudioActivity.this,
						"收到" + location.getUserAddress() + "位置报告!",
						Toast.LENGTH_SHORT).show();
				int count = MapAPI.getInstance().getOverlayCount(
						ReportLayer.REPORT_LAY);
				final FloatValue matchAngle = new FloatValue();
				final NSLonLat matchPos = new NSLonLat();
				NSLonLat vehiclePos = new NSLonLat();
				NSLonLat VPPos = new NSLonLat();
				vehiclePos.x = (float) location.mLongitude;
				vehiclePos.y = (float) location.mLatitude;
				RouteAPI.getInstance().matchProc(vehiclePos, 0, VPPos,
						matchPos, matchAngle);
				reportLayer.addReportPos(NaviStudioActivity.this, matchPos,
						count, location.getUserAddress());
			}

		});
		mapListener.isStart = true;// 重新开启自动回车位功能
		if (SettingForLikeTools.AUTOGOBACKTOCAR_BOOLEAN) {// 解决重置后不回车位的问题
			mapListener.resetTime();
		}
		mapModelControl.setMapMode();// 设置地图模式显示图片
		audioManager = (AudioManager) getSystemService(Activity.AUDIO_SERVICE);
		if (RouteAPI.getInstance().isRouteValid()) {
			if (NaviControl.getInstance().naviStatus == NaviControl.NAVI_STATUS_SIMNAVI) {
				// NaviControl.getInstance().guideBegin();
				// this.mapView.goBackCar();
			} else {
				if (NaviControl.getInstance().naviStatus != NaviControl.NAVI_STATUS_REALNAVI) {
					NaviControl.getInstance().startNavigate();
				} else {
					this.mapView.goBackCar();
					NaviControl.getInstance().showNaviInfo();
				}
			}
		}
		updateUIStyle(DayOrNightControl.mdayOrNight);
		currentPointListener.setFootView();
		setMapCenterRoadName();
		NaviControl.getInstance().fleshLane();// 解决从其他页面跳转到主界面时车道线不显示的问题
		// ///////注册陀螺仪////////////
		// OrientationSensorManager.getInstance(this,NaviControl.getInstance()).register();
		// Activity parentActivity = this.getParent();
		// if (parentActivity != null) {
		// NaviControl.getInstance().stopSimNavi();
		// NaviControl.getInstance().stopRealNavi();
		// RouteAPI.getInstance().clearRoute();
		// NaviControl.getInstance().guideEnd();
		// final FloatValue matchAngle = new FloatValue();
		// final NSLonLat matchPos = new NSLonLat();
		// NSLonLat vehiclePos = new NSLonLat();
		// NSLonLat VPPos = new NSLonLat();
		// int count =
		// MapAPI.getInstance().getOverlayCount(ReportLayer.REPORT_LAY);
		// 从位置报告列表中跳转到地图页面显示点
		// if (x != null && y != null && !"".equals(x) && !"".equals(y)){
		// vehiclePos.x = Float.valueOf(x);
		// vehiclePos.y = Float.valueOf(y);
		// RouteAPI.getInstance().matchProc(vehiclePos, 0, VPPos,matchPos,
		// matchAngle);
		// reportLayer.addReportPos(NaviStudioActivity.this, matchPos,count,
		// userAddress);
		// }
		// /* 从通知栏跳转到地图页面画点 */
		// if (com.novsky.map.util.Utils.LAT_VALUE != 0.0f
		// && com.novsky.map.util.Utils.LON_VALUE != 0.0f){
		// vehiclePos.x = Float.valueOf(com.novsky.map.util.Utils.LON_VALUE);
		// vehiclePos.y = Float.valueOf(com.novsky.map.util.Utils.LAT_VALUE);
		// String address = com.novsky.map.util.Utils.BD_REPORT_USER_ADDRESS;
		// RouteAPI.getInstance().matchProc(vehiclePos, 0, VPPos,matchPos,
		// matchAngle);
		// reportLayer.addReportPos(NaviStudioActivity.this, matchPos,count,
		// address);
		// com.novsky.map.util.Utils.LON_VALUE = 0.0f;
		// com.novsky.map.util.Utils.LAT_VALUE = 0.0f;
		// }
		// }
	}

	@Override
	protected void onStart() {
		super.onStart();
		com.novsky.map.util.Utils.checkNaviMap = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		com.novsky.map.util.Utils.checkNaviMap = false;
		ActivityStack.newInstance().setBlMapBack(true);// 来自地图返回
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DayOrNightControl.getIntance().removieDayOrNightListener(
				dayOrNightListener);
		mapView.hideTip();
		manager.removeLocation();
		mapListener = null;
		naviControl = null;
		if (mananger != null) {
			try {
				mananger.removeBDEventListener(localInfoListener,
						mBDSatelliteListener, mGPSatelliteListener);
			} catch (BDUnknownException e) {
				e.printStackTrace();
			} catch (BDParameterException e) {
				e.printStackTrace();
			}
		}
	}

	private FrameLayout flo = null;
	private View mView = null;
	private int nIndex = 0;

	/**
	 * 初始化layout type == 0 onCreate调用 type == 1 onConfigurationChanged调用
	 */
	private void initMapView() {
		if (ToolsUtils.isLand(this)) {
			// 横屏
			setContentView(R.layout.navistudio_map_land_layout);
		} else {
			// 竖屏
			setContentView(R.layout.navistudio_map_port_layout);
		}
		flo = (FrameLayout) findViewById(R.id.fl_mapview);
		mView = (View) findViewById(R.id.v_mapsview);
		nIndex = flo.indexOfChild(mView);
		flo.removeView(mView);
		mapView = AutoNaviMap.getInstance(this).getMapView();

		// 更多功能
		ImageButton moreFunctionImageBtn = (ImageButton) this
				.findViewById(R.id.more_function_imageButton);
		moreFunctionImageBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (com.novsky.map.util.Utils.isLand) {
					Intent intent = new Intent();
					intent.setClass(mContext, BDManagerHorizontalActivity.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent();
					intent.setClass(mContext, BDManagerActivity.class);
					startActivity(intent);
				}
			}
		});

		// 轨迹管理
		ImageButton routeImageButton = (ImageButton) this
				.findViewById(R.id.route_image_btn);
		routeImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityStack.newInstance().push(NaviStudioActivity.this);// 解决更多界面退出时报错的问题
				Intent routeIntent = new Intent(
						Constants.ACTIVITY_ROUTE_MANAGER);
				startActivity(routeIntent);
			}
		});
		//
		ImageButton locationResearchImageBtn = (ImageButton) this
				.findViewById(R.id.location_research_imagebtn);
		locationResearchImageBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityStack.newInstance().push(NaviStudioActivity.this);// 解决更多界面退出时报错的问题
				Intent locIntent = new Intent(
						Constants.ACTIVITY_SEARCH_SEARCHLOCATION);
				startActivity(locIntent);
			}
		});

		// 发送消息按钮
		ImageButton sendMessageBtn = (ImageButton) this
				.findViewById(R.id.send_message_imagebtn);
		sendMessageBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//横屏
				if (com.novsky.map.util.Utils.isLand) {
					Intent intent = new Intent();
					intent.setClass(NaviStudioActivity.this,
							BDSendMsgLandScapeActivity.class);
					startActivity(intent);
				} else {
					//竖屏
					Intent intent = new Intent();
					intent.setClass(NaviStudioActivity.this,
							BDSendMsgPortActivity.class);
					startActivity(intent);
				}
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			// adjustStreamVolume: 调整指定声音类型的音量
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);// 调低声音
			if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
				volume.setBackgroundResource(R.drawable.navistudio_volumemute);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
			// 第一个参数：声音类型
			// 第二个参数：调整音量的方向
			// 第三个参数：可选的标志位
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI); // 调高声音
			if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
				volume.setBackgroundResource(R.drawable.navistudio_volumemax);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			// mapView.getTip().hide();
		}
		return super.onKeyDown(keyCode, event);
	}

	// public boolean addReportPos(NSLonLat pos, int type) {
	// Overlay overlay = new Overlay();
	// overlay.id = 1;
	// overlay.type = Const.MAP_OVERLAY_POI;
	// overlay.hide = false;
	// overlay.lons = new float[] { pos.x };
	// overlay.lats = new float[] { pos.y };
	//
	// overlay.labelText = "你好111111111111111111";
	// overlay.painterName = "";
	// overlay.labelName = "poi_point";
	// boolean addret = MapAPI.getInstance().addOverlay(11, overlay);
	// return addret;
	// }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		/***************** 横竖屏切换时保存当前地图比例尺 *********************/
		int scale = (int) MapAPI.getInstance().getMapScale();
		SettingForLikeTools.saveMapScale(this, scale);
		initMapView();
		if (RouteAPI.getInstance().isRouteValid()) {
			if (MapAPI.getInstance().isCarInCenter()) {
				if (NaviControl.getInstance().naviStatus == NaviControl.NAVI_STATUS_SIMNAVI) {
					NaviControl.getInstance().guideBegin();
				} else {
					NaviControl.getInstance().startNavigate();
				}
			}
			NaviControl.getInstance().fleshLane();// 解决横竖屏切换时车道线不显示的问题
		}
		updateUIStyle(DayOrNightControl.mdayOrNight);
		currentPointListener.setFootView();
		setMapCenterRoadName();
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

		}
		setScaleText();
	}

	public void setAnimation() {
		mShowAction = new TranslateAnimation(70.0f, 0.0f, 0.0f, 0.0f);
		mShowAction.setDuration(700);
		mShowAction.setStartOffset(100);
		mShowAction.setInterpolator(AnimationUtils.loadInterpolator(this,
				android.R.anim.bounce_interpolator));
		mHiddenAction = new TranslateAnimation(0.0f, 70.0f, 0.0f, 0.0f);
		mHiddenAction.setDuration(700);
		mHiddenAction.setStartOffset(100);
		mHiddenAction.setInterpolator(AnimationUtils.loadInterpolator(this,
				android.R.anim.bounce_interpolator));
	}

	/**
	 * 初始化控件
	 */
	private void initComponents() {
		scaleTextView = (TextView) this.findViewById(R.id.tv_scale_num);
		zoomLayout = (LinearLayout) findViewById(R.id.ll_zoomlayout);
		zoomin = (ImageButton) this.findViewById(R.id.ib_btnZoomIn);
		zoomout = (ImageButton) this.findViewById(R.id.ib_btnZoomOut);
		volume = (ImageButton) findViewById(R.id.ib_btn_volume);
		ibMapModel = (ImageButton) findViewById(R.id.ib_map_model);
		gpsstate = (ImageButton) findViewById(R.id.ib_gpsstate);
		bdstate = (ImageView) findViewById(R.id.ib_bdstate);
		back_car = (ImageButton) findViewById(R.id.ib_btn_car);
		currentPointBtn = (ImageButton) findViewById(R.id.btn_current_position);
		plantRouteBtn = (ImageButton) findViewById(R.id.btn_calculate);
		mapbarLayout = (RelativeLayout) findViewById(R.id.rl_mapbar);
		currentPointListener = new CurrentPointListener(this, mapView);
	}

	/*
	 * 设置出发点
	 */
	public boolean setPoint(NSLonLat centerinfo) {
		NaviControl.getInstance().stopSimNavi();
		NaviControl.getInstance().stopRealNavi();
		RouteAPI.getInstance().clearRoute();
		NaviControl.getInstance().guideEnd();
		RouteLayer routeLayer = new RouteLayer();
		// routeLayer.deleteLayer();
		// RouteAPI.getInstance().deletePassPoint(-1);
		// RouteAPI.getInstance().deleteAvoidPoint(-1);
		routeLayer.addRoutePos(centerinfo, 10);
		return true;
	}

	/**
	 * 给控件添加点击监听事件
	 */
	private void addListener() {
		zoomControl = new ZoomControl(this, mapView);
		setScaleText();
		zoomin.setOnClickListener(zoomControl);
		zoomout.setOnClickListener(zoomControl);
		volumeControl = new VolumeControl(this, mapView);
		volume.setOnClickListener(volumeControl);

		gpsstate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent in = new Intent(Constants.ACTIVITY_GPSINFO);
				mContext.startActivity(in);
			}
		});
		bdstate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent in = new Intent(Constants.ACTIVITY_BDINFO);
				mContext.startActivity(in);
			}
		});

		gpsControl = new GpsControl(this, mapView);
		gpsstate.setOnClickListener(gpsControl);
		gpsstate.setVisibility(View.VISIBLE);
		BDControl bdControl = new BDControl(this, mapView);
		bdstate.setOnClickListener(bdControl);
		DisPatchInfo.getInstance().addGpsInfoListener("GpsControl", gpsControl);

		carBackEvent = new CarBackEvent(this, mapView);

		mapModelControl = new MapModelControl(this, mapView);
		ibMapModel.setOnTouchListener(mapModelControl);

		// 白天黑夜模式监听
		DayOrNightControl.getIntance()
				.addDayOrNightListener(dayOrNightListener);

		MapAPI.getInstance().addMapListenter(mapListener);
		// 长按短按事件监听
		mapTouchListener = new NaviMapTouchListener(this, mapView);
		mapView.setMapTouchListener(mapTouchListener);

		currentPointBtn.setOnTouchListener(currentPointListener);
		plantRouteBtn.setOnTouchListener(currentPointListener);

	}

	/*
	 * 设置比例尺TEXTVIEW的文本
	 */
	public void setScaleText() {
		int iScale = (int) MapAPI.getInstance().getMapScale();
		String sScaleText = UIResourceUtil.getScaleText(iScale);
		scaleTextView.setText(sScaleText);
		Log.e(TAG, "======Mapsize======" + iScale);
	}

	Handler h = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				NaviControl.getInstance().stopSimNavi();
				NaviControl.getInstance().stopRealNavi();
				VolumeControl.resetVolume();
			}
		}
	};

	@Override
	public void onBackPressed() {
		ExitDialog exitDailog = new ExitDialog(this);
		exitDailog.show();
	}

	public void resetStatus() {
		try {
			setIntent(null);
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					ActivityStack stack = ActivityStack.newInstance();
					stack.finishAllExcludeMap();// 清空其它界面
				}
			});
			// mapView.hideTip();
			// blEnableSearch = true;// 地点搜索菜单可用
		} catch (Exception e) {
			Log.e("MapActivity::resetStatus", e.toString());
		}
	}

	/**
	 * 启动服务
	 */
	private void startTimeService() {
		Intent timeService = new Intent(this, TimeService.class);
		this.startService(timeService);
	}

	/**
	 * 添加菜单
	 */
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.navimenu, menu);
	// return true;
	// // MenuInflater menuInflator = new MenuInflater(this);
	// // menuInflator.inflate(R.menu.navimenu, menu);
	// // return super.onCreateOptionsMenu(menu);
	// }

	/**
	 * 菜单点击事件
	 */
	// @Override
	// public boolean onMenuItemSelected(int featureId, final MenuItem item) {
	// Log.e(TAG, "onMenuItemSelected");
	// ActivityStack.newInstance().setBlMapBack(false);
	// int itemId = item.getItemId();
	// switch (itemId) {
	// case R.id.menu_itemLocationSearch:// 地点查找
	// Log.d(TAG, "search location");
	// ActivityStack.newInstance().push(this);// 解决更多界面退出时报错的问题
	// Intent locIntent = new Intent(Constants.ACTIVITY_SEARCH_SEARCHLOCATION);
	// startActivity(locIntent);
	// break;
	// case R.id.menu_itemSendMessage: {
	// Intent knownIntent = new Intent(this, BDSendMsgPortActivity.class);
	// startActivity(knownIntent);
	// break;
	// }
	// // case R.id.menu_itemLocationReport: {
	// // Intent locationIntent = new Intent();
	// // locationIntent.setClass(this, FriendsLocationActivity.class);
	// // startActivity(locationIntent);
	// // break;
	// // }
	// case R.id.menu_itemRouteManager:// 路径管理
	// ActivityStack.newInstance().push(this);// 解决更多界面退出时报错的问题
	// Intent routeIntent = new Intent(
	// Constants.ACTIVITY_ROUTE_MANAGER);
	// startActivity(routeIntent);
	// break;
	//
	// case R.id.menu_itemMoreMenu:// 更多功能
	// Log.d(TAG, "more menu");
	// Intent moreIntent = new Intent(Constants.ACTIVITY_OTHERFUNCTION);
	// startActivity(moreIntent);
	// ActivityStack.newInstance().push(this);// 解决更多界面退出时报错的问题
	//
	// break;
	//
	// case R.id.menu_itemSendAlarm: {
	// // ActivityStack.newInstance().push(this);// 解决更多界面退出时报错的问题
	// Intent sendAlarmIntent = new Intent(this, BDAlarmActivity.class);
	// startActivity(sendAlarmIntent);
	// break;
	// }
	// case R.id.menu_itemExitMenu:{
	// ExitDialog exitDailog = new ExitDialog(this);
	// exitDailog.show();
	// break;
	// }
	//
	// // case R.id.menu_itemRealTimeTraffic:// 开启实时交通
	// //// ActivityStack.newInstance().push(this);// 解决更多界面退出时报错的问题
	// // if (TMCControl.TMC_STATUS == TMCControl.TMC_CLOSED) {
	// // boolean hasNetWork = Utils.checkNetWork(this);
	// // if (!hasNetWork) {
	// // Toast.makeText(this,
	// // Utils.getValue(this, R.string.common_nonetwork),
	// // Toast.LENGTH_LONG).show();
	// //
	// // } else {
	// // Toast.makeText(
	// // NaviStudioActivity.this,
	// // Utils.getValue(
	// // NaviStudioActivity.this,
	// // R.string.navistudioactivity_loadingrealtimetraffic),
	// // Toast.LENGTH_LONG).show();
	// // executorService.execute(new Runnable() {
	// // @Override
	// // public void run() {
	// // Message msg = new Message();
	// // msg.what = REALTIMETRAFFIC;
	// // msg.obj = item;
	// // mHandler.sendMessage(msg);
	// // }
	// // });
	// // }
	// // } else {
	// // executorService.execute(new Runnable() {
	// // @Override
	// // public void run() {
	// // Message msg = new Message();
	// // msg.what = REALTIMETRAFFIC;
	// // msg.obj = item;
	// // mHandler.sendMessage(msg);
	// // }
	// // });
	// // }
	// // break;
	// default:
	// break;
	// }
	// return super.onMenuItemSelected(featureId, item);
	// }

	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// if (menu != null) {
	// //
	// menu.findItem(R.id.menu_itemLocationSearch).setEnabled(blEnableSearch);
	// // if (!(TMCControl.TMC_STATUS == TMCControl.TMC_OPENED)) {
	// //
	// menu.findItem(R.id.menu_itemRealTimeTraffic).setTitle(R.string.menu_openTMC);
	// // } else {
	// //
	// menu.findItem(R.id.menu_itemRealTimeTraffic).setTitle(R.string.menu_closeTMC);
	// // }
	// }
	//
	// return super.onPrepareOptionsMenu(menu);
	// }

	/**
	 * 由其它界面跳转到地图界面后进行的操作。
	 * 
	 * @param intent
	 */
	private void receiveEvent(Intent intent) {
		if (intent == null) {
			return;
		}
		Bundle extras = intent.getExtras();
		if (extras != null) {
			int intentAction = extras.getInt(Constants.INTENT_ACTION);
			switch (intentAction) {
			case Constants.INTENT_TYPE_FRIEND_LOCATION_ITEM:// 单个友邻位置
				
				FriendBDPoint friendBDPoint = (FriendBDPoint) extras.getSerializable("friendBDPoint");
				//showFriendLocation(friendBDPoint);
				showPOI(friendBDPoint);
				
				break;
			case Constants.INTENT_TYPE_FRIEND_LOCATION_LIST_NOTIFYCATION:// 多个友邻位置  在notification中
					
					ArrayList<FriendBDPoint> friendLocationList2 = (ArrayList<FriendBDPoint>) extras.getSerializable("friendLocationList");
					//多个友邻位置显示
					showFriendLocation(friendLocationList2);
					break;
			case Constants.INTENT_TYPE_FRIEND_LOCATION_LIST:// 多个友邻位置
				
				ArrayList<FriendBDPoint> friendLocationList = (ArrayList<FriendBDPoint>) extras.getSerializable("friendLocationList");
				//多个友邻位置显示
				showFriendLocation(friendLocationList);
				break;
			case Constants.INTENT_TYPE_LOOKPOI:// 查看POI点
				blEnableSearch = false;
				poiInfo = (SearchResultInfo) extras
						.getSerializable(Constants.POI_DATA);
				
				showPOI(poiInfo);
				
				break;
			case Constants.INTENT_TYPE_SETSTARTPOINT:// 设置起点
				NaviControl.getInstance().stopSimNavi();
				NaviControl.getInstance().stopRealNavi();
				RouteAPI.getInstance().clearRoute();
				NaviControl.getInstance().guideEnd();
				RouteLayer routeLayer = new RouteLayer();
				routeLayer.deleteLayer();
				RouteAPI.getInstance().deletePassPoint(-1);
				RouteAPI.getInstance().deleteAvoidPoint(-1);

				SearchResultInfo start_poiInfo = (SearchResultInfo) extras
						.getSerializable(Constants.POI_DATA);
				NSLonLat start_mPos = new NSLonLat();
				start_mPos.x = start_poiInfo.lon;
				start_mPos.y = start_poiInfo.lat;
				float fAngle = MapAPI.getInstance().getVehicleAngle();
				MapAPI.getInstance().setVehiclePosInfo(start_mPos, fAngle);
				mapView.goBackCar();
				resetStatus();
				break;
			case Constants.INTENT_TYPE_SETENDPOINT:// 设置终点
				// 设置终点
				SearchResultInfo end_poiInfo = (SearchResultInfo) extras
						.getSerializable(Constants.POI_DATA);
				NSLonLat end_mPos = new NSLonLat();
				end_mPos.x = end_poiInfo.lon;
				end_mPos.y = end_poiInfo.lat;
				naviControl.end_poiInfo = end_poiInfo;
				naviControl.calculatePath(end_mPos, 0);
				resetStatus();
				break;
			case Constants.INTENT_TYPE_START_SIMNAVI:// 开始模拟导航
				NaviControl.getInstance().startSimulate();
				NaviControl.getInstance().drawRoute();
				MapAPI.getInstance().setMapScale(16);
				mapView.goBackCar();
				break;
			case Constants.INTENT_TYPE_STOP_SIMNAVI:// 停止模拟导航
				// 停止模拟导航
				NaviControl.getInstance().stopSimNavi();
				NaviControl.getInstance().drawRoute();
				NaviControl.getInstance().routeInfo = null;
				mapView.goBackCar();
				break;
			case Constants.INTENT_TYPE_CHANGEROUTEMODE:
				mapView.goBackCar();
				break;
			case Constants.INTENT_TYPE_NEWNAVISTUDIO:// 第一次启动应用程序
				java.util.ArrayList list = ToolsUtils.continueRoute();
				if (list != null && list.size() == 5) {
					NaviControl.getInstance().continueRoute(list);
				}
				this.isHaveGPS();
				break;
			case Constants.INTENT_TYPE_LOOKDESTTASK:// 查看目的地任务
				// Log.e(TAG, "=============查看目的地任务================");
				// DestInfo
				// destInfo=(DestInfo)extras.getSerializable(Constants.WZT_DATA);
				// NSLonLat mPos = new NSLonLat();
				// mPos.x =destInfo.getX();
				// mPos.y =destInfo.getY();
				// NSLonLat mlonlat=new NSLonLat(destInfo.getX(),
				// destInfo.getY());
				// String roadName = "";
				// String destDes=destInfo.getDestDescription();
				// Log.e(TAG,
				// "=============查看目的地任务destDes================"+destDes);
				//
				// MapAPI.getInstance().setMapCenter(mPos);
				// blEnableSearch = false;
				// mapListener.onMapStatusChanged(MapListener.MAP_STATUS_MOVE);
				// mapListener.isStart=false; //暂时关闭自动回车位
				break;
			case Constants.INTENT_TYPE_NAVIGATION:// 从任务列表开启导航
				// Log.e(TAG, "=============从任务列表开启导航================");
				// RouteInfo
				// routeInfo=(RouteInfo)extras.getSerializable(Constants.WZT_DAVIGATE);
				// float[] lon=routeInfo.getLon();
				// float[] lat=routeInfo.getLat();
				//
				// if(lon.length==1){
				// NSLonLat mPos1 = new NSLonLat();
				// mPos1.x =lon[0];
				// mPos1.y =lat[0];
				// currentPointListener.setDestination(mPos1,1);
				// }else if(lon.length==2){
				// for(int i=0;i<lon.length;i++){
				// NSLonLat mPos1 = new NSLonLat();
				// mPos1.x =lon[i];
				// mPos1.y =lat[i];
				// if(i==0){
				// currentPointListener.setVehiclePosition(mPos1);
				// }else if(i==lon.length-1){
				// currentPointListener.setDestination(mPos1,1);
				// }else {
				//
				// }
				// }
				// }else {
				// NSLonLat mPos_start = new NSLonLat();
				// mPos_start.x =lon[0];
				// mPos_start.y =lat[0];
				// NSLonLat mPos_end = new NSLonLat();
				// mPos_end.x=lon[lon.length-1];
				// mPos_end.y=lat[lon.length-1];
				// currentPointListener.setVehiclePosition(mPos_start);
				// currentPointListener.setDestination(mPos_end,1);
				// for(int i=1;i<(lon.length-1);i++){
				// NSLonLat mPos_pass = new NSLonLat();
				// mPos_pass.x=lon[i];
				// mPos_pass.y=lat[i];
				// int nType =
				// SettingForLikeTools.getRouteCalcMode(NaviStudioActivity.this);
				// RouteAPI.getInstance().addPassPoint(mPos_pass);
				// }
				// }
				break;
			default:
				break;
			}
		}
		this.setIntent(null);
	}

	/**
	 * 展示poi
	 * @param poiInfo
	 */
	private void showPOI(SearchResultInfo poiInfo) {
		NSLonLat center = new NSLonLat(poiInfo.lon, poiInfo.lat);
		MapAPI.getInstance().setMapCenter(center);
		mapListener.onMapStatusChanged(MapListener.MAP_STATUS_MOVE);
		mapListener.isStart = false; // 暂时关闭自动回车位
		ToolsUtils.showPoiTip(mapView, this, poiInfo.lon, poiInfo.lat,
				poiInfo.name);
	}
	/**
	 * 展示poi
	 * @param poiInfo
	 */
	private void showPOI(FriendBDPoint poiInfo) {
		NSLonLat center = new NSLonLat(Float.parseFloat(poiInfo.getLon()), Float.parseFloat(poiInfo.getLat()));
		MapAPI.getInstance().setMapCenter(center);
		mapListener.onMapStatusChanged(MapListener.MAP_STATUS_MOVE);
		mapListener.isStart = false; // 暂时关闭自动回车位
		ToolsUtils.showPoiTip(mapView, this, Float.parseFloat(poiInfo.getLon()),Float.parseFloat(poiInfo.getLat()),
				poiInfo.getFriendID());
	}

	public void isHaveGPS() {
		// 判断系统的GPS位置服务是否打开
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			boolean bGpsEnable = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (!bGpsEnable) {
				enableGPS();
			}
		}
	}

	private void JT_OnStart() {
		// int lineId=0;
		// long id=0;
		// SharedPreferences sharePrefs =
		// this.getSharedPreferences("BD_NAV_PREF",0);
		// id=sharePrefs.getLong("NAVI_ID", 0);
		// lineId=sharePrefs.getInt("LINE_ID",0);
		// if(id != 0){
		// sharePrefs.edit().putLong("NAVI_ID", 0l).commit();
		// Toast.makeText(this, "NAVI_ID="+id+",", Toast.LENGTH_SHORT).show();
		// BDInstructionNavOperation operation=new
		// BDInstructionNavOperation(this);
		// BDInstructionNav nav = operation.get(id);
		// //从nav对象中获取指令导航的数据
		// double lon = nav.getTargetPoint().getLon();
		// double lat = nav.getTargetPoint().getLat();
		// // final CGeoPoint lonlat = new CGeoPoint((float)lon, (float)lat);
		// // JsNaviUiRl32PageNa.froMap = true;
		// // new Thread(){public void run(){
		// // JsNavi.mNe.mRt.JsRoute(null, lonlat);
		// // }}.start();
		// List<BDPoint> listPass=nav.getPassPoints();
		// if(listPass!=null){
		// for(BDPoint mPoint:listPass){
		// Toast.makeText(this,
		// mPoint.getLon()+","+mPoint.getLat(),Toast.LENGTH_LONG).show();
		// }
		// }
		// List<BDPoint> listAvoi =nav.getEvadePoints(); // avoid
		// if(listAvoi!=null){
		// for(BDPoint mPoint:listAvoi){
		// Toast.makeText(this,mPoint.getLon()+","+mPoint.getLat(),Toast.LENGTH_LONG).show();;
		// }
		// }
		// }
		// else if(lineId != 0){
		// sharePrefs.edit().putInt("LINE_ID", 0).commit();
		// BDLineNavOperation operation=new BDLineNavOperation(this);
		// BDLineNav line=operation.get(lineId+"");
		// Toast.makeText(this, "LINE_ID="+lineId, Toast.LENGTH_SHORT).show();
		// //从line对象中获取路线导航的数据
		// List<BDPoint> listRoute=line.getPassPoints();
		// for(BDPoint mPoint:listRoute){
		// //final CGeoPoint lonlat = new CGeoPoint((float)mPoint.getLon(),
		// (float)mPoint.getLat());
		// //JsNaviUiRl32PageNa.froMap = true;
		// //new Thread(){public void run(){
		// // JsNavi.mNe.mRt.JsRoute(null, lonlat);
		// //}}.start();
		// break;
		// //System.out.println(mPoint.getLon()+","+mPoint.getLat());
		// }
		// }
		// //增加在JS_OnStart方法中
		// SharedPreferences locationsharePrefs =
		// this.getSharedPreferences("BD_FRIEND_LOCATION_PREF",0);
		// int rowId=locationsharePrefs.getInt("RERPORT_ROW_ID", 0);
		// if(rowId!=0){
		// locationsharePrefs.edit().putInt("RERPORT_ROW_ID", 0).commit();
		// FriendsLocationDatabaseOperation friendOper=new
		// FriendsLocationDatabaseOperation(this);
		// FriendLocation mFriendLocation=friendOper.getById(rowId);
		// friendOper.close();
		// Toast.makeText(mContext,
		// "address="+mFriendLocation.getAddress()+",lon="+mFriendLocation.getFriendsLon()+",lat="+mFriendLocation.getFriendsLat()+",height="+mFriendLocation.getFriendsHeight(),Toast.LENGTH_SHORT).show();
		// }
	}

	// 启用GPS
	private void enableGPS() {
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(
				NaviStudioActivity.this);
		dlgBuilder.setTitle(R.string.navistudio_startGPSTitle);
		dlgBuilder.setMessage(R.string.navistudio_GPSTipMessage);
		dlgBuilder.setNegativeButton(
				ToolsUtils.getValue(this, R.string.common_btn_negative),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton(
				ToolsUtils.getValue(this, R.string.common_btn_positive),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivityForResult(intent, 0);
					}
				});
		dlgBuilder.create().show();
	}

	/**
	 * 
	 * @Copyright:mapabc
	 * @description:设置道路中心点名称
	 * @author fei.zhan
	 * @date 2012-9-7 void
	 */
	public void setMapCenterRoadName() {
		String roadName = "";
		roadName = ToolsUtils.getRoadName(MapAPI.getInstance().getMapCenter(), 100);
		if (!roadName.equals("")) {

		} else {
			roadName = this.getString(R.string.navistudio_road_has_no_name);
		}
		NaviControl.getInstance().m_roadName = "";
		NaviControl.getInstance().setRoadName(roadName);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		receiveEvent(intent);
		int reportId = intent.getIntExtra("RERPORT_ROW_ID", 0);
		if (reportId != 0) {
			FriendsLocationDatabaseOperation friendOper = new FriendsLocationDatabaseOperation(
					this);
			FriendLocation mFriendLocation = friendOper.getById(reportId);
			friendOper.close();
			showFriendLocation(mFriendLocation);
		}

		// 线路导航
		int lineId = intent.getIntExtra("LINE_ID", 0);
		if (lineId != 0) {
			BDLineNavOperation operation = new BDLineNavOperation(this);
			BDLineNav line = operation.get(lineId + "");

			// 移除上次导航 数据
			RoutUtils.removeCurrentRoute();
			NaviControl.getInstance().stopSimNavi();
			NaviControl.getInstance().stopRealNavi();
			RouteAPI.getInstance().clearRoute();
			NaviControl.getInstance().guideEnd();
			RouteLayer routeLayer = new RouteLayer();
			routeLayer.deleteLayer();
			RouteAPI.getInstance().deletePassPoint(-1);
			RouteAPI.getInstance().deleteAvoidPoint(-1);

			// Toast.makeText(this,
			// "LINE_ID="+lineId+",line="+line.getPassPointsString(),
			// Toast.LENGTH_SHORT).show();
			// 从line对象中获取路线导航的数据
			List<BDPoint> listRoute = line.getPassPoints();
			//去除重复数据

			List<BDPoint> listNew = CollectionUtils.removeDuplicateT(listRoute);

			NSLonLat[] nsLonLatList = new NSLonLat[listNew.size()];

			for (int i = 0; i < listNew.size(); i++) {
				BDPoint mPoint = listNew.get(i);
				NSLonLat nsLonLat = new NSLonLat();
				nsLonLat.x = (float) mPoint.getLon();
				nsLonLat.y = (float) mPoint.getLat();

				if (i == 0) {
					// 起始点
					RouteAPI.getInstance().setStartPoint(nsLonLat);
				} else if (i == listRoute.size() - 1) {
					// 结束点
					RouteAPI.getInstance().setEndPoint(nsLonLat);
				} else {
					boolean addres = RouteAPI.getInstance().addPassPoint(
							nsLonLat);
				}
			}
			currentPointListener.reCalculatePath(0);
		}

		// 指令导航
		long id = intent.getLongExtra("NAVI_ID", 0);
		if (id != 0) {

			// 先清除地图上的上次导航数据

			RoutUtils.removeCurrentRoute();

			NaviControl.getInstance().stopSimNavi();
			NaviControl.getInstance().stopRealNavi();
			RouteAPI.getInstance().clearRoute();
			NaviControl.getInstance().guideEnd();
			RouteLayer routeLayer = new RouteLayer();
			routeLayer.deleteLayer();
			RouteAPI.getInstance().deletePassPoint(-1);
			RouteAPI.getInstance().deleteAvoidPoint(-1);
			// Toast.makeText(this, "NAVI_ID="+id+",",
			// Toast.LENGTH_SHORT).show();
			BDInstructionNavOperation operation = new BDInstructionNavOperation(
					this);
			BDInstructionNav nav = operation.get(id);
			// 从nav对象中获取指令导航的数据
			// 终点
			double lon = nav.getTargetPoint().getLon();
			double lat = nav.getTargetPoint().getLat();

			// 根据数据在地图上画线。
			NSLonLat end_mPos = new NSLonLat();
			end_mPos.x = (float) lon;
			end_mPos.y = (float) lat;
			RouteAPI.getInstance().setEndPoint(end_mPos);
			resetStatus();

			// 开始点
			NSLonLat lonlatStr = MapAPI.getInstance().getMapCenter();
			RouteAPI.getInstance().setStartPoint(lonlatStr);

			if (!RouteAPI.getInstance().canAddAvoidPoint()) {
				ToolsUtils.showTipInfo(this,
						R.string.currentpoint_add_avoidpoint_fail);
				return;
			} else {

				// 规避点
				List<BDPoint> listAvoi = nav.getEvadePoints();
				if (listAvoi != null) {
					for (BDPoint mPoint : listAvoi) {
						NSLonLat nsLonLat = new NSLonLat();
						nsLonLat.x = (float) mPoint.getLon();
						nsLonLat.y = (float) mPoint.getLat();
						if (!RouteAPI.getInstance().addAvoidPoint(nsLonLat)) {
							break;
						}
					}
				}

			}

			if (!RouteAPI.getInstance().canAddPassPoint()) {
				ToolsUtils.showTipInfo(this,
						R.string.currentpoint_add_passpoint_fail);
				return;
			} else {

				// 途径点
				List<BDPoint> listPass = nav.getPassPoints();
				if (listPass != null) {
					int i = 0;
					for (BDPoint mBDPoint : listPass) {

						NSLonLat centerInfo = new NSLonLat();
						centerInfo.x = (float) mBDPoint.getLon();
						centerInfo.y = (float) mBDPoint.getLat();

						// 添加途经点
						boolean addres = RouteAPI.getInstance().addPassPoint(
								centerInfo);

					}
				}

			}

			currentPointListener.setDestination(end_mPos);
			currentPointListener.reCalculatePath(1);
		}
		
		
		//友邻位置
	}

	/**
	 * 显示友邻位置
	 * @param mFriendLocation
	 */
	private void showFriendLocation(FriendBDPoint mFriendLocation) {
		int count = MapAPI.getInstance().getOverlayCount(
				ReportLayer.REPORT_LAY);
		final FloatValue matchAngle = new FloatValue();
		final NSLonLat matchPos = new NSLonLat();
		NSLonLat vehiclePos = new NSLonLat();
		NSLonLat VPPos = new NSLonLat();
		vehiclePos.x = Float.valueOf(mFriendLocation.getLon());
		vehiclePos.y = Float.valueOf(mFriendLocation.getLat());
		RouteAPI.getInstance().matchProc(vehiclePos, 0, VPPos, matchPos,
				matchAngle);
		reportLayer.addReportPos(NaviStudioActivity.this, matchPos, count,
				mFriendLocation.getFriendID());
	}
	/**
	 * 显示友邻位置
	 * @param mFriendLocation
	 */
	private void showFriendLocation(FriendLocation mFriendLocation) {
		int count = MapAPI.getInstance().getOverlayCount(
				ReportLayer.REPORT_LAY);
		final FloatValue matchAngle = new FloatValue();
		final NSLonLat matchPos = new NSLonLat();
		NSLonLat vehiclePos = new NSLonLat();
		NSLonLat VPPos = new NSLonLat();
		vehiclePos.x = Float.valueOf(mFriendLocation.getFriendsLon());
		vehiclePos.y = Float.valueOf(mFriendLocation.getFriendsLat());
		RouteAPI.getInstance().matchProc(vehiclePos, 0, VPPos, matchPos,
				matchAngle);
		reportLayer.addReportPos(NaviStudioActivity.this, matchPos, count,
				mFriendLocation.getAddress());
	}
	/**
	 * 显示友邻位置
	 * @param friendLocationList
	 */
	private void showFriendLocation(ArrayList<FriendBDPoint> friendLocationList) {
		
		//从sp中获取 保存的友邻id
		String spToDelID = friendID_Sp.getString("Stored_friend_id", "");
		for (FriendBDPoint friendBDPoint : friendLocationList) {
			
			int count = MapAPI.getInstance().getOverlayCount(
					ReportLayer.REPORT_LAY);
			final FloatValue matchAngle = new FloatValue();
			final NSLonLat matchPos = new NSLonLat();
			NSLonLat vehiclePos = new NSLonLat();
			NSLonLat VPPos = new NSLonLat();
			vehiclePos.x = Float.valueOf(friendBDPoint.getLon());
			vehiclePos.y = Float.valueOf(friendBDPoint.getLat());
			RouteAPI.getInstance().matchProc(vehiclePos, 0, VPPos, matchPos,
					matchAngle);
			reportLayer.addReportPosDontDel(NaviStudioActivity.this, matchPos, count,
					friendBDPoint.getFriendID());
			
			//保存  friendID 数 根据friendID删除  根据保存的id去删除 指定的友邻  该方法作废  计划在RouteManagerActivity 中调用
			String friendID = friendBDPoint.getFriendID();
			spToDelID=spToDelID+"@"+friendID;
		}
		
		friendID_editor.putString("Stored_friend_id", spToDelID);
		friendID_editor.commit();
	}
}