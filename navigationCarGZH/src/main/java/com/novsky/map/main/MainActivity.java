package com.novsky.map.main;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.location.CardInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.mapabc.android.activity.utils.ActivityStack;
import com.mapabc.android.activity.utils.SettingForLikeTools;
import com.mapabc.naviapi.MapEngineManager;
import com.mapabc.naviapi.TTSAPI;
import com.mapabc.naviapi.UtilAPI;
import com.mapabc.naviapi.map.MapOptions;
import com.mapabc.naviapi.route.RouteOptions;
import com.mapabc.naviapi.search.SearchOption;
import com.mapabc.naviapi.tts.TTSOptions;
import com.mapabc.naviapi.type.Const;
import com.mapabc.naviapi.utils.SysParameterManager;
import com.novsky.map.util.TabSwitchActivityData;
import com.novsky.map.util.Utils;

/**
 * 主界面
 * 
 * @author steve
 */
public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private Context mContext = this;

	private boolean isExit;
	private Button locationKnownBtn = null;
	private Button locationSendedBtn = null;
	private Button messageManagerBtn = null;

	private TextView localAddressTx = null;// 本机地址
	private TextView serviceFeqTx = null;// 服务频度
	private TextView communicationLevelTx = null;// 通讯级别
	private TextView cardTypeTx = null;// 本机类型
	private TextView ICCardStatus = null;// IC卡状态

	private ProgressDialog progressDialog = null;
	private BDRDSSManager mananger = null;

	private final static int ACCESS_CARD_INFO = 0xD0;
	private final static int INIT_MAP_ENGINE = 0xD1;
	private final static int RECEIVE_CARD_INFO = 0xD2;
	private final static int FINISH_MAP_ENGINE = 0xD3;
	private final static int UN_REGISTER_MAP = 0xD4;

	private TTSOptions ttsOptions = new TTSOptions();

	private Button rdssBtn = null;
	private Button rnssBtn = null;
	private CustomLocationManager manager = null;

	/* 退出程序 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			isExit = false;
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ACCESS_CARD_INFO: {
				progressDialog = Utils.createProgressDialog(mContext, "提示",
						"正在读取北斗卡信息...");
				progressDialog.show();
				try {
					mananger.sendAccessCardInfoCmdBDV21(0, 0);
				} catch (BDUnknownException e) {
					e.printStackTrace();
				};
				break;
			}
			case INIT_MAP_ENGINE: {
				progressDialog = Utils.createProgressDialog(mContext, "提示",
						"正在加载地图引擎...");
				progressDialog.show();
				break;
			}
			case RECEIVE_CARD_INFO: {
				CardInfo card = (CardInfo) msg.obj;
				progressDialog.dismiss();
				localAddressTx.setText("卡地址:" + card.mCardAddress);
				serviceFeqTx.setText("卡频度:" + card.mSericeFeq + "");
				communicationLevelTx.setText("通信级别:"
						+ String.valueOf(card.mCommLevel));
				if ("N".equals(card.checkEncryption)) {
					cardTypeTx.setText("非密卡");
				} else if ("E".equals(card.checkEncryption)) {
					cardTypeTx.setText("加密卡");
				}
				String cardStatus = "";
				if ("2097151".equals(card.mCardAddress)) {
					cardStatus = "卡状态:无卡";
				} else {
					cardStatus = "卡状态:有卡";
				}
				ICCardStatus.setText(cardStatus);
				MyRunnable mRunnable = new MyRunnable(mContext);
				new Thread(mRunnable).start();
				break;
			}
			case FINISH_MAP_ENGINE: {
				progressDialog.dismiss();
				break;
			}
			case UN_REGISTER_MAP: {
				showExitTip(mContext.getResources().getString(
						R.string.asyncforward_registe_tip));
				break;
			}
			default: {
				break;
			}
			}
		}
	};

	/**
	 * 加载地图引擎
	 */
	private class MyRunnable implements Runnable {

		private Context mContext = null;

		public MyRunnable(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public void run() {

			mHandler.sendEmptyMessage(INIT_MAP_ENGINE);
			MapEngineManager mapEngineManager = new MapEngineManager(mContext);
			MapOptions mapOptions = new MapOptions();
			RouteOptions routeOption = new RouteOptions();
			SearchOption clsSearchOption = new SearchOption();
			mapOptions.screenWidth = com.mapabc.android.activity.utils.ToolsUtils
					.getCurScreenWidth(mContext);
			mapOptions.screenHeight = com.mapabc.android.activity.utils.ToolsUtils
					.getCurScreenHeight(mContext);
			SysParameterManager.getCMapOption(mapOptions, mContext);
			SysParameterManager.getSearchOpt(clsSearchOption);
			SysParameterManager.getTTSOptions(ttsOptions);
			String adcode = SettingForLikeTools.getADCode(mContext);
			clsSearchOption.strADCode = adcode;
			// 获取路口放大图路径
			SysParameterManager.getRouteOpt(routeOption);
			routeOption.httpCrossImgPath = routeOption.crossImgPath;
			String x = SettingForLikeTools.getVehicleX(mContext, "0");
			String y = SettingForLikeTools.getVehicleY(mContext, "0");
			float x0 = Float.parseFloat(x);
			float y0 = Float.parseFloat(y);
			if (x0 > 0) {
				mapOptions.mapCenter.x = x0;
				mapOptions.mapCenter.y = y0;
			}
			// 初始化
			boolean res = mapEngineManager.initMapEngine(mapOptions,
					clsSearchOption, routeOption);
			if (!res) {
				Log.e(TAG, "initeMapEngine false");
			} else {
				Log.e(TAG, "initeMapEngine true");
			}
			// 加载库
			if (UtilAPI.getInstance().verifyUserKey() != 0) {
				mHandler.sendEmptyMessage(UN_REGISTER_MAP);
				return;
			}
			// 初始化系统参数
			boolean tts_res = TTSAPI.getInstance().init(ttsOptions);
			Log.e(TAG, "tts init is:" + tts_res);
			 String welcome =
			 mContext.getString(R.string.navilogo_welcomevoice);
			 TTSAPI.getInstance().addPlayContent(welcome,
			 Const.AGPRIORITY_CRITICAL);
			mHandler.sendEmptyMessageDelayed(FINISH_MAP_ENGINE, 500);
		}
	}

	/**
	 * 卡信息监听器
	 */
	private BDEventListener localInfoListener = new BDEventListener.LocalInfoListener() {
		@Override
		public void onCardInfo(CardInfo card) {
			Utils.setCardInfo(card);
			Message message = mHandler.obtainMessage();
			message.obj = card;
			message.what = RECEIVE_CARD_INFO;
			mHandler.sendMessage(message);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		mananger = BDRDSSManager.getDefault(this);
		try {
			mananger.addBDEventListener(localInfoListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(ACCESS_CARD_INFO);

		/* 发送开启BD和GPS共同显示星图的广播 */
		Intent intent = new Intent("android.intent.action.BDGPS_REPORT");
		intent.putExtra("bdreport", 1);
		sendStickyBroadcast(intent);

		manager = new CustomLocationManager(mContext);
		manager.initLocation();
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationKnownBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(mContext, LocationKnownActivity.class);
				startActivity(intent);
			}
		});

		locationSendedBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mContext, ReportActivity.class);
				startActivity(intent);
			}
		});

		messageManagerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TabSwitchActivityData mInstance = TabSwitchActivityData
						.getInstance();
				mInstance.setTabFlag(0);
				Intent intent = new Intent();
				intent.setClass(mContext, BDSendMsgPortActivity.class);
				startActivity(intent);
			}
		});
		rnssBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(mContext, BD2StatusActivity.class);
				startActivity(intent);
			}
		});

		rdssBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(mContext, AutoCheckedActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		manager.initLocation();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			/* 是否退出判断 */
			if (!isExit) {
				isExit = true;
				Toast.makeText(this, "再按一次退出程序!", Toast.LENGTH_LONG).show();
				handler.sendEmptyMessageDelayed(0, 2000);
			} else {
				/* 停止同时显示北斗和GPS卫星状态 */
				Intent mIntent = new Intent(
						"android.intent.action.BDGPS_REPORT");
				mIntent.putExtra("bdreport", 0);
				sendStickyBroadcast(mIntent);
				// 初始化超频数
				Utils.CARD_FREQ = 0;
				// 注销读北斗卡监听
				try {
					mananger.removeBDEventListener(localInfoListener);
				} catch (BDUnknownException e) {
					e.printStackTrace();
				} catch (BDParameterException e) {
					e.printStackTrace();
				}
				Intent cycleLocationService = new Intent(this,
						CycleLocationReportService.class);
				this.stopService(cycleLocationService);

				Intent timeService = new Intent(this, TimeService.class);
				this.stopService(timeService);

				// 释放锁
				Utils.releaseWakeLock();
				// 设置位置报告为单次
				SharedPreferences reportSwitch = getSharedPreferences(
						"LOCATION_REPORT_SWITCH", 0);
				reportSwitch.edit().putString("SWITCH", "off").commit();
				reportSwitch.edit().putInt("FEQ", 0).commit();
				reportSwitch.edit().putString("USER_ADDRESS", "").commit();

				/* 释放所有地图资源 */
				SettingForLikeTools.reSettingScreenBrightness(mContext);
				new Thread() {
					public void run() {
						try {
							if (Utils.checkNaviMap) {
								com.mapabc.android.activity.utils.ToolsUtils
										.saveRoute();
								ActivityStack.newInstance().finishAll();
								SettingForLikeTools.saveSystem(mContext);
								com.mapabc.android.activity.utils.ToolsUtils
										.restoreScreenOff(mContext);
							}
							MapEngineManager.release();
							android.os.Process.killProcess(android.os.Process
									.myPid());
							System.exit(0);
						} catch (Exception ex) {
							Log.e(TAG, "ERROR", ex);
						}
					}
				}.start();
			}
			return false;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	private void showExitTip(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle(R.string.common_tip)
				.setIcon(R.drawable.alert_dialog_icon)
				.setMessage(msg)
				.setPositiveButton(R.string.common_confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								MapEngineManager.release();
								// mContext.finish();
								System.exit(0);
							}
						});
		final AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * 初始化UI
	 * */
	private void init() {
		LinearLayout layout = (LinearLayout) this
				.findViewById(R.id.main_linear_layout);
		locationKnownBtn = (Button) this.findViewById(R.id.location_known);
		locationSendedBtn = (Button) this.findViewById(R.id.location_send);
		messageManagerBtn = (Button) this.findViewById(R.id.message_manager);
		rnssBtn = (Button) this.findViewById(R.id.rnss_btn);
		rdssBtn = (Button) this.findViewById(R.id.rdss_btn);
		localAddressTx = (TextView) this.findViewById(R.id.local_address);
		serviceFeqTx = (TextView) this.findViewById(R.id.service_frequency);
		communicationLevelTx = (TextView) this
				.findViewById(R.id.communication_level);
		cardTypeTx = (TextView) this.findViewById(R.id.card_type);
		ICCardStatus = (TextView) this.findViewById(R.id.local_iccard_status);
		Utils.setActivityBackgroud(mContext, layout);
	}
}
