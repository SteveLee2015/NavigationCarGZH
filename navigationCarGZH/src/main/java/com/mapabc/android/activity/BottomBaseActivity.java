package com.mapabc.android.activity;

import java.util.Iterator;
import java.util.List;
import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.bd.comm.protocal.BDSatellite;
import com.bd.comm.protocal.BDSatelliteListener;
import com.bd.comm.protocal.GPSatellite;
import com.bd.comm.protocal.GPSatelliteListener;
import com.novsky.map.main.AutoCheckedActivity;
import com.novsky.map.main.BD2StatusActivity;
import com.novsky.map.main.BDAvailableStatelliteManager;
import com.novsky.map.main.GPSStatusActivity;
import com.novsky.map.main.LocationStatusManager;
import com.novsky.map.util.Utils;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.BDBeam;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.location.BDRNSSManager.LocationStrategy;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 底部按钮显示界面 的基类
 * 
 * @author Administrator
 * 
 */
public abstract class BottomBaseActivity extends Activity {

	/**
	 * rnss 数目
	 */
	private int rnssNum;
	/**
	 * gps数据
	 */
	int gpsNum = 0;
	/**
	 * bdii数目
	 */
	int bdiiNum = 0;

	public Context mContext = this;
	/**
	 * RDSS对象
	 */
	protected BDCommManager manager = null;

	/**
	 * 定位状态的管理类
	 */
	protected LocationStatusManager locationStatusManager = null;
	/**
	 * RNSS对象
	 */
	protected LocationManager locationManager = null;

	/**
	 * 北斗有效卫星管理对象
	 */
	protected BDAvailableStatelliteManager statellitesManager = null;

	protected static final int BD_RDSS_SATELLITE_NUM_ITEM = 0x100001,
			BD_RNSS_LOC_NUM_ITEM = 0x10002, BD_SATELLITE_STATUS_ITEM = 0x10003,
			GPS_SATELLITE_STATUS_ITEM = 0x10005, GPS_ONLY = 0x10004;

	protected boolean isLocation = false;

	/**
	 * 北斗I卫星数目
	 */
	public TextView homeTitleBD1Num = null;
	/**
	 * 北斗II卫星数目
	 * 
	 * 2代卫星数+gps卫星数
	 */
	protected TextView homeTitleBD2Num = null;

	/**
	 * 页面标题
	 */
	protected TextView title_name;

	/**
	 * 返回键
	 */
	protected ImageView back;

	protected LinearLayout mRDSSLayout = null, mRNSSLayout = null;

	protected Handler handler = new Handler() {

		private int gpsNumOnly;

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			// 北斗I卫星数目
			case BD_RDSS_SATELLITE_NUM_ITEM:
				int count = msg.arg1;
				int num = msg.arg2;
				if (count >= 1) {
					// 当前可发送定位/短报文
					homeTitleBD1Num.setTextColor(Color.BLACK);
					// homeTitleBD1Num.setTextColor(Color.GREEN);
				} else {
					// 当前只能发送短报文
					homeTitleBD1Num.setTextColor(mContext.getResources()
							.getColor(R.color.bd2_unenable_loc));
				}
				homeTitleBD1Num.setText(String.valueOf(num));
				break;
			// 二代是否定位
			case BD_RNSS_LOC_NUM_ITEM:
				BDRNSSLocation location = (BDRNSSLocation) msg.obj;
				isLocation = location.isAvailable();
				break;
			// 北斗II卫星数目 + gps数目
			case BD_SATELLITE_STATUS_ITEM:

				// bd数
				int currentAvailableBDii = 0;
				List<BDSatellite> list = (List<BDSatellite>) msg.obj;
				for (BDSatellite satellite : list) {
					int statelliteID = satellite.getPrn();
					float zaizaobi = satellite.getSnr();// 载噪比
					if (statelliteID >= 160) {
						if (satellite.usedInFix() && (zaizaobi > 5)) {
							currentAvailableBDii++;
							bdiiNum = currentAvailableBDii;
						}
					} else {
						if (satellite.usedInFix() && (satellite.getSnr() > 5)) {
							currentAvailableBDii++;
							bdiiNum = currentAvailableBDii;
						}
					}
				}
				// currentAvailableBDii 表示2代bd可用信号信息

				rnssNum = gpsNum + bdiiNum;

				homeTitleBD2Num.setText("" + rnssNum);

				if (isLocation) {
					homeTitleBD2Num.setTextColor(Color.BLACK);
					// homeTitleBD2Num.setTextColor(Color.GREEN);
				} else {
					homeTitleBD2Num.setTextColor(mContext.getResources()
							.getColor(R.color.bd2_unenable_loc));
				}
				break;
			// 北斗II卫星数目 + gps数目
			case GPS_SATELLITE_STATUS_ITEM:
				int currentAvailableBDGPS = 0;
					// gps 数
					List<GPSatellite> list1 = (List<GPSatellite>) msg.obj;
					for (GPSatellite satellite : list1) {
						int statelliteID = satellite.getPrn();
						float zaizaobi = satellite.getSnr();// 载噪比
						if (statelliteID >= 160) {
							if (satellite.usedInFix() && (zaizaobi > 5)) {
								currentAvailableBDGPS++;
								gpsNum = currentAvailableBDGPS;
							}
						} else {
							if (satellite.usedInFix()
									&& (satellite.getSnr() > 5)) {
								currentAvailableBDGPS++;
								gpsNum = currentAvailableBDGPS;
							}
						}
				}

				rnssNum = gpsNum + bdiiNum;

				homeTitleBD2Num.setText("" + rnssNum);

				if (isLocation) {
					homeTitleBD2Num.setTextColor(Color.BLACK);
					// homeTitleBD2Num.setTextColor(Color.GREEN);
				} else {
					homeTitleBD2Num.setTextColor(mContext.getResources()
							.getColor(R.color.bd2_unenable_loc));
				}
				break;

			// 尽gps定位
			case GPS_ONLY:

				List<GPSatellite> list2 = (List<GPSatellite>) msg.obj;
				int currentAvailableBDgpsOnly = 0;
				for (GPSatellite satellite : list2) {
					int statelliteID = satellite.getPrn();
					float zaizaobi = satellite.getSnr();// 载噪比
					if (statelliteID >= 160) {
						if (satellite.usedInFix() && (zaizaobi > 5)) {
							currentAvailableBDgpsOnly++;
							gpsNumOnly = currentAvailableBDgpsOnly;
						}
					} else {
						if (satellite.usedInFix() && (satellite.getSnr() > 5)) {
							currentAvailableBDgpsOnly++;
							gpsNumOnly = currentAvailableBDgpsOnly;
						}
					}

					homeTitleBD2Num.setText("" + gpsNumOnly);
					if (locationStatusManager.getLocationStatus()) {
						homeTitleBD2Num.setTextColor(Color.BLACK);
					} else {
						homeTitleBD2Num.setTextColor(mContext.getResources()
								.getColor(R.color.bd2_unenable_loc));
					}
				}

			default:
				break;
			}

		}
	};

	/**
	 * 波束监听器
	 */
	protected BDEventListener mBDBeamStatusListener = new BDEventListener.BDBeamStatusListener() {
		@Override
		public void onBeamStatus(BDBeam beam) {
			Message msg = new Message();
			int count = 0;
			int num = 0;
			for (int index = 0; index < beam.getBeamWaves().length; index++) {
				if (beam.getBeamWaves()[index] > 0) {
					num++;
				}
			}
			count = Utils.checkCurrentRDSSStatus(beam.getBeamWaves());
			msg.what = BD_RDSS_SATELLITE_NUM_ITEM;
			msg.arg1 = count;
			msg.arg2 = num;
			handler.sendMessage(msg);
		}
	};

	private GPSatelliteListener mGPSatelliteListener = new GPSatelliteListener() {
		@Override
		public void onGpsStatusChanged(List<GPSatellite> arg0) {

			if (Utils.RNSS_CURRENT_LOCATION_MODEL == LocationStrategy.HYBRID_STRATEGY) {
				// 混合模式
				Message message = handler.obtainMessage();
				message.what = GPS_SATELLITE_STATUS_ITEM;
				message.obj = arg0;
				handler.sendMessage(message);

			} else {
				// 单 gps模式
				Message message = handler.obtainMessage();
				message.what = GPS_ONLY;
				message.obj = arg0;
				handler.sendMessage(message);
			}

		}
	};

	// 定位
	protected BDRNSSLocationListener mBDRNSSLocationListener = new BDRNSSLocationListener() {
		@Override
		public void onLocationChanged(BDRNSSLocation arg0) {
			Message message = handler.obtainMessage();
			message.what = BD_RNSS_LOC_NUM_ITEM;
			message.obj = arg0;
			handler.sendMessage(message);
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
	 * 卫星状态
	 */
	protected BDSatelliteListener mBDSatelliteListener = new BDSatelliteListener() {
		@Override
		public void onBDGpsStatusChanged(List<BDSatellite> arg0) {
			Message message = handler.obtainMessage();
			message.what = BD_SATELLITE_STATUS_ITEM;
			message.obj = arg0;
			handler.sendMessage(message);
		}
	};

	/**
	 * 在7寸屏中 没有改功能
	 * 获得RNSS卫星数据的监听器 1.如果当前单北斗模式，则每次获得的数据都是北斗卫星的数据。
	 * 对北斗卫星数据进行遍历，获得当前北斗参与定位的卫星数目并显示出来。 2.如果当前单GPS模式，则每次获得的数据都是GPS卫星的数据。
	 * 对GPS卫星数据进行遍历，获得当前GPS参与定位的卫星数目并显示出来。 3.如果当前是混合模式，则一次获得GPS卫星数据，一次获得北斗卫星数据。
	 * 先解析GPS卫星数据，获得GPS参与定位的卫星数目，然后解析北斗卫星 数据获得北斗参与定位的卫星数目,把两次解析出来的卫星数目增加并显示出来。
	 */
	protected GpsStatus.Listener mGpslistener = new GpsStatus.Listener() {
		int available = 0;
		GpsStatus mGpsStatus;

		@Override
		public void onGpsStatusChanged(int event) {
			if (locationManager != null) {
				int currentAvailable = 0;
				mGpsStatus = locationManager.getGpsStatus(null);
				switch (event) {
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					Iterable<GpsSatellite> allStatellites = mGpsStatus
							.getSatellites();
					Iterator<GpsSatellite> it = allStatellites.iterator();
					while (it.hasNext()) {
						GpsSatellite satellite = it.next();
						int statelliteID = satellite.getPrn();
						float zaizaobi = satellite.getSnr();// 载噪比
						if (statelliteID >= 160) {
							if (statellitesManager.usedInFix(statelliteID)
									&& (zaizaobi > 5)) {
								// ??
								currentAvailable++;
							}
						} else {
							if (satellite.usedInFix()
									&& (satellite.getSnr() > 5)) {
								// ??
								currentAvailable++;
							}
						}
					}

					// 混合模式
					if (Utils.RNSS_CURRENT_LOCATION_MODEL == LocationStrategy.HYBRID_STRATEGY) {
						if (available == 0) {
							available = currentAvailable + 1;
						} else {
							available = available + currentAvailable - 1;
							// 还要加上gps卫星数

							// 发消息到handler
							Message msg = Message.obtain();
							msg.what = BD_SATELLITE_STATUS_ITEM;
							msg.obj = available;
							handler.sendMessage(msg);

							// homeTitleBD2Num.setText("" + available);
							// if (locationStatusManager.getLocationStatus()) {
							// homeTitleBD2Num.setTextColor(Color.GREEN);
							// } else {
							// homeTitleBD2Num.setTextColor(mContext
							// .getResources().getColor(
							// R.color.bd2_unenable_loc));
							// }
							available = 0;
						}
					} else {
						// 单GPS 模式

						Message msg = Message.obtain();
						msg.what = GPS_ONLY;
						msg.obj = available;
						handler.sendMessage(msg);

						// homeTitleBD2Num.setText("" + currentAvailable);
						// if (locationStatusManager.getLocationStatus()) {
						// homeTitleBD2Num.setTextColor(Color.BLACK);
						// // homeTitleBD2Num.setTextColor(Color.GREEN);
						// } else {
						// homeTitleBD2Num.setTextColor(mContext
						// .getResources().getColor(
						// R.color.bd2_unenable_loc));
						// }
					}
					break;
				default:
					break;
				}
			}
		}
	};

	abstract protected int getContentView();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(getContentView());
		statellitesManager = BDAvailableStatelliteManager.getInstance();
		manager = BDCommManager.getInstance(this);
		locationStatusManager = LocationStatusManager.getInstance();
		if ("S500".equals(Utils.DEVICE_MODEL)) {
			locationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
			locationManager.addGpsStatusListener(mGpslistener);
			try {
				manager.addBDEventListener(mBDBeamStatusListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		} else {
			try {
				manager.addBDEventListener(mBDBeamStatusListener,
						mBDRNSSLocationListener, mBDSatelliteListener,
						mGPSatelliteListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		}

		initView();
		initListener();

	}

	private void initListener() {

		mRDSSLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent(mContext, AutoCheckedActivity.class);
				startActivity(mIntent);
			}
		});

		mRNSSLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				alertChooseDia();
			}
		});

	}

	/**
	 * 星图选择对话框
	 */
	protected void alertChooseDia() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.navicontrol_select);
		
		builder.setPositiveButton(R.string.navicontrol_bd_state, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				Intent mIntent = new Intent(mContext, BD2StatusActivity.class);
				startActivity(mIntent);
				
			}
		});
		
		builder.setNegativeButton(R.string.navicontrol_gps_state, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				Intent mIntent = new Intent(mContext, GPSStatusActivity.class);
				startActivity(mIntent);
				
			}
		});
		
		builder.show();
	}

	/**
	 * 初始化控件
	 */
	private void initView() {

		homeTitleBD1Num = (TextView) this.findViewById(R.id.home_title_bd1_num);
		homeTitleBD2Num = (TextView) this.findViewById(R.id.home_title_bd2_num);
		title_name = (TextView) this.findViewById(R.id.title_name);
		back = (ImageView) this.findViewById(R.id.home_title_flag_img);

		mRDSSLayout = (LinearLayout) this.findViewById(R.id.rdss_num_layout);
		mRNSSLayout = (LinearLayout) this.findViewById(R.id.rnss_num_layout);

	};

}
