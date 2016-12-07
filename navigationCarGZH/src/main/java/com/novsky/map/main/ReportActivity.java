package com.novsky.map.main;

import java.io.UnsupportedEncodingException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.BDEventListener;
import android.location.BDMessageInfo;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

/**
 * 位置报送
 * @author steve
 */
public class ReportActivity extends Activity {
	
	private static final String TAG = "ReportActivity";
	/* 定位反馈的标识符 */
	private final static int CMD_LOCATION_SUCCESS = 0;
	private final static int CMD_LOCATION_FAIL = 1;
	private final static int CMD_SYSTEM_LAUNCHER = 2;
	private final static int CMD_NO_POWER = 3;
	private final static int CMD_SILENCE = 4;
	private final static int CMD_TIME = 5;
	
	private final int REQUEST_CONTACT = 1;
	private  int msgComType=1;
	private String fequency = "0"; // 服务频率
	private String mUserAddress = "";
	private BDRDSSManager mananger = null;
	private EditText addressEditTx = null;
	private Context mContext = this;
	private TextView counterNum = null;
	private Intent timeService = null;
	private Intent cycleLocationService = null;
	private CustomListView  customListView=null;
	
	private AutoReportReceiver receiver = null;
	private ImageView selectAddress = null;
	//private EditText feq = null;
	private Button sendBtn = null;
	private Button settingBtn=null; //Button cancleBtn = null,settingBtn=null;
	private CustomLocationManager manager=null;
	private int mReportTypeIndex=0;
	private SharedPreferences  autoReportPreferences=null;
	

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 指令发送成功
			case CMD_LOCATION_SUCCESS: {
				String mark = (String) msg.obj;
				if (mark != null && "DWA".equals(mark)) {
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_success)
									.replace("CMD", "定位命令"), Toast.LENGTH_SHORT)
							.show();
				} else if (mark != null && "WAA".equals(mark)) {
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_success)
									.replace("CMD", "位置报告1"),
							Toast.LENGTH_SHORT).show();
				} else if (mark != null && "WBA".equals(mark)) {
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_success)
									.replace("CMD", "位置报告2"),
							Toast.LENGTH_SHORT).show();
				} else if (mark != null && "TXA".equals(mark)) {
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_success)
									.replace("CMD", "位置报告"), Toast.LENGTH_SHORT)
							.show();
				}
				Utils.CARD_FREQ = Utils.getCardInfo().mSericeFeq;
				break;
			}
			// 指令发送失败
			case CMD_LOCATION_FAIL:
				String mark = (String) msg.obj;
				if (mark != null && "DWA".equals(mark)) {
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_fail)
									.replace("CMD", "定位命令"), Toast.LENGTH_SHORT)
							.show();
				}
				if (mark != null && "WAA".equals(mark)) {
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_fail)
									.replace("CMD", "位置报告"), Toast.LENGTH_SHORT)
							.show();
				}
				if (mark != null && "WBA".equals(mark)) {
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_fail)
									.replace("CMD", "位置报告"), Toast.LENGTH_SHORT)
							.show();
				}
				if (mark != null && "TXA".equals(mark)) {
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_fail)
									.replace("CMD", "位置自动回报"), Toast.LENGTH_SHORT)
							.show();
				}
				Utils.CARD_FREQ = Utils.getCardInfo().mSericeFeq;
				break;
			// 系统的抑制命令，发射被抑制
			case CMD_SYSTEM_LAUNCHER:
				Toast.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.system_un_launch), Toast.LENGTH_SHORT)
						.show();
				break;
			// 电量不足,发射被抑制
			case CMD_NO_POWER:
				Toast.makeText(mContext,
						mContext.getResources().getString(R.string.no_power),
						Toast.LENGTH_SHORT).show();

				break;
			// 无线电静默，发射被抑制
			case CMD_SILENCE:
				Toast.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.wireless_silence), Toast.LENGTH_SHORT)
						.show();
				break;
			// 超频
			case CMD_TIME:
				Toast.makeText(
						mContext,
						mContext.getResources()
								.getString(R.string.service_feq_no)
								.replace("TIME", msg.arg1 + ""),
						Toast.LENGTH_SHORT).show();
				Utils.CARD_FREQ = (Utils.getCardInfo().mSericeFeq + msg.arg1);
				break;
			default:
				break;
			}
		}
	};


	private BDEventListener fkilistener = new BDEventListener.BDFKIListener() {
		@Override
		public void onTime(int time) {
			Message msg = new Message();
			msg.what = CMD_TIME;
			msg.arg1 = time;
			handler.sendMessage(msg);
		}

		@Override
		public void onSystemLauncher() {
			handler.sendEmptyMessage(CMD_SYSTEM_LAUNCHER);
		}

		@Override
		public void onSilence() {
			handler.sendEmptyMessage(CMD_SILENCE);
		}

		@Override
		public void onPower() {
			handler.sendEmptyMessage(CMD_NO_POWER);
		}

		@Override
		public void onCmd(String arg0, boolean istrue) {
			if (arg0 != null
					&& ("DWA".equals(arg0) || "WAA".equals(arg0)
							|| "WBA".equals(arg0) || "TXA".equals(arg0))) {
				Message msg = new Message();
				msg.obj = arg0;
				if (istrue) {
					msg.what = CMD_LOCATION_SUCCESS;
					handler.sendMessage(msg);
				} else {
					msg.what = CMD_LOCATION_FAIL;
					handler.sendMessage(msg);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_report);
		/*RDSS中间件初始化*/
		mananger = BDRDSSManager.getDefault(this);
		cycleLocationService = new Intent(this,CycleLocationReportService.class);
		initUI();
		autoReportPreferences= getSharedPreferences("AUTO_REPORT_TYPE_PREFS", 0);
		/*保存循环位置报告的状态*/
		SharedPreferences reportCfgPrefs = getSharedPreferences("LOCATION_REPORT_CFG", 0);
		//1.是否打开
		String flag = reportCfgPrefs.getString("REPORT_SWITCH", "off");
		//2.用户地址
		String address = reportCfgPrefs.getString("USER_ADDRESS", "");
		addressEditTx.setText(address);
		if(flag != null && flag.equals("on")) {
			sendBtn.setText(this.getResources().getString(R.string.loc_auto_stop_btn_str));
		}
		manager=new CustomLocationManager(mContext);
		manager.initLocation();
		/* 坐标增加数据 */
		customListView = (CustomListView) this.findViewById(R.id.bd_coodr_type);
		customListView.setData(getResources().getStringArray(R.array.report_location_array));
		int autoReportType=autoReportPreferences.getInt("AUTO_REPORT_TYPE",0);
		customListView.setIndex(autoReportType);
		customListView.setOnCustomListener(new OnCustomListListener() {
			@Override
			public void onListIndex(int num) {
				autoReportPreferences.edit().putInt("AUTO_REPORT_TYPE", num).commit();
			}
		});
		/*注册并启动计时器service*/
		registerBroadcastReceiver();
		//startTimeService();
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			mananger.addBDEventListener(fkilistener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		
		selectAddress.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_PICK);
				intent.setData(ContactsContract.Contacts.CONTENT_URI);
				ReportActivity.this.startActivityForResult(intent,REQUEST_CONTACT);
			}
		});

		sendBtn.setOnClickListener(new OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				/*保存循环位置报告的状态*/
				SharedPreferences reportCfgPrefs = getSharedPreferences("LOCATION_REPORT_CFG", 0);
				//1.是否打开
				String flag = reportCfgPrefs.getString("REPORT_SWITCH", "off");
				//2.定位的点数
				int reportPointNum=reportCfgPrefs.getInt("REPORT_POINT_NUM", 5);
				//3.获取频度
				fequency = reportCfgPrefs.getInt("REPORT_FEQ", 18)+"";
				
				if(flag!=null&&"off".equals(flag)){
					/*判断是否安装北斗SIM卡*/
					if (Utils.getCardInfo().mSericeFeq == 9999) {
						Toast.makeText(mContext,
								mContext.getResources().getString(R.string.have_not_bd_sim),
								Toast.LENGTH_SHORT).show();
						return;
					}
					mUserAddress = addressEditTx.getText().toString();
					/*判断用户地址是否为空*/
					if (mUserAddress == null || ("").equals(mUserAddress)) {
						Toast.makeText(mContext,mContext.getResources().getString(
								R.string.bd_address_no_content),
								Toast.LENGTH_SHORT).show();
						return;
					}
	                /*判断用户地址是否是name(address)格式*/
					if (mUserAddress.contains("(")) {
						mUserAddress = mUserAddress.substring(
								mUserAddress.lastIndexOf("(") + 1,
								mUserAddress.lastIndexOf(")"));
					}
					//Utils.BD_USER_ADDRESS = mUserAddress;
					if(Utils.LOCATION_REPORT_LON==0.0||Utils.LOCATION_REPORT_LAT==0.0){
						Toast.makeText(mContext, "获取当前位置信息失败,请重新发送!", Toast.LENGTH_SHORT).show();
						return;
					}
					Toast.makeText(mContext,
							mContext.getResources().getString(
									R.string.set_success_cycle_report),
							Toast.LENGTH_SHORT).show();
					sendBtn.setText(mContext.getResources().getString(R.string.loc_auto_stop_btn_str));
					reportCfgPrefs.edit().putString("REPORT_SWITCH", "on").commit();
					reportCfgPrefs.edit().putString("USER_ADDRESS", mUserAddress).commit();
					startCycleLocationService();
				}else{
					sendBtn.setText(mContext.getResources().getString(R.string.loc_auto_start_btn_str));
					reportCfgPrefs.edit().putString("REPORT_SWITCH", "off").commit();
					//reportCfgPrefs.edit().putString("USER_ADDRESS", "").commit();
					stopCycleLocationService();
				}
			}
		});
		
		settingBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//设置位置自动回报的频度以及包含的点数
				final SharedPreferences reportPrefs=mContext.getSharedPreferences("LOCATION_REPORT_CFG", MODE_PRIVATE);		
				final AlertDialog.Builder builder=new AlertDialog.Builder(ReportActivity.this);
				builder.setTitle(ReportActivity.this.getResources().getString(R.string.loc_auto_send_string));
				LayoutInflater inflater=LayoutInflater.from(ReportActivity.this);
				final View view=inflater.inflate(R.layout.activity_report_setting, null);
				final EditText edit=(EditText)view.findViewById(R.id.relayStationNum);
				final EditText reportLocationNum=(EditText)view.findViewById(R.id.report_point_num);
				int freq=reportPrefs.getInt("REPORT_FEQ",18);
				edit.setText(freq+"");
				int locationnum=reportPrefs.getInt("REPORT_POINT_NUM", 5);
				reportLocationNum.setText(locationnum+"");
				builder.setView(view);
				builder.setCancelable(false);
				builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						String reportFreqStr= edit.getText().toString();
						String reportLocStr=reportLocationNum.getText().toString();
						//reportPrefs.edit().putInt("REPORT_POINT_NUM",(reportLocStr!=null&&!"".equals(reportLocStr))?Integer.valueOf(reportLocStr):5).commit();
						reportPrefs.edit().putInt("REPORT_POINT_NUM",1).commit();
						reportPrefs.edit().putInt("REPORT_FEQ",(reportFreqStr!=null&&!"".equals(reportFreqStr))?Integer.valueOf(reportFreqStr):18).commit();
					}
				});
				builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
					}
				});
				AlertDialog dialog=builder.create();
				dialog.show();
			}
		});
	}

	public void initUI(){
		counterNum = (TextView) this.findViewById(R.id.counter_tv);
		addressEditTx = (EditText) this.findViewById(R.id.bdloc_userAddress_et);
		selectAddress = (ImageView) this.findViewById(R.id.bdloc_linker);
		sendBtn = (Button) this.findViewById(R.id.bdloc_report_submit_btn);
		settingBtn = (Button) this.findViewById(R.id.bdloc_report_setting_btn);
//		if (Utils.BD_USER_ADDRESS != null && !"".equals(Utils.BD_USER_ADDRESS)) {
//			addressEditTx.setText(Utils.BD_USER_ADDRESS);
//		}
		Button sendAuthorBtn=(Button)this.findViewById(R.id.send_shouquan_btn);
		sendAuthorBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(BDRDSSManager.PROVIDERS_CHANGED_ACTION);
				BDMessageInfo mBDMessageInfo=new BDMessageInfo();
				mBDMessageInfo.setmUserAddress("455911");
				mBDMessageInfo.setMsgType(1);
				mBDMessageInfo.setMsgCharset(1);
				try {
					//mBDMessageInfo.setMessage("00B10301022C3F011FF76501022C030E0C010E1209".getBytes("GBK"));
					mBDMessageInfo.setMessage("00F10E0C07161B2901000A45740D26004E280224020CEA0065".getBytes("GBK"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				intent.putExtra(BDRDSSManager.BDRDSS_MESSAGE, mBDMessageInfo);
                mContext.sendBroadcast(intent);
			}
		});
		Button sendAutoReportBtn=(Button)this.findViewById(R.id.send_report_cfg_btn);
		sendAutoReportBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				Intent intent = new Intent(BDRDSSManager.PROVIDERS_CHANGED_ACTION);
				BDMessageInfo mBDMessageInfo=new BDMessageInfo();
				mBDMessageInfo.setmUserAddress("142399");
				mBDMessageInfo.setMsgType(1);
				mBDMessageInfo.setMsgCharset(1);
				try {
					mBDMessageInfo.setMessage("00B400030506F4E70E0C010E1208".getBytes("GBK"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				intent.putExtra(BDRDSSManager.BDRDSS_MESSAGE, mBDMessageInfo);
                mContext.sendBroadcast(intent);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CONTACT) {
			if (resultCode == RESULT_OK) {
				if (data == null) {
					return;
				}
				Uri result = data.getData();
				Cursor mCursor = this.getContentResolver().query(result, null,null, null, null);
				String name = "";
				String phoneNumber = "";
				if (mCursor.moveToFirst()) {
					name = mCursor.getString(mCursor
							.getColumnIndex(Phone.DISPLAY_NAME));
					String contactId = mCursor.getString(mCursor
							.getColumnIndex(ContactsContract.Contacts._ID));
					Cursor phones = getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + contactId, null, null);

					while (phones.moveToNext()) {
						phoneNumber += phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					phones.close();
				}
				mCursor.close();
				Log.i("BD1", name + "(" + phoneNumber.replaceAll("-", "") + ")");
				mUserAddress = name + "(" + phoneNumber.replaceAll(" ", "")
						+ ")";
				if (addressEditTx != null) {
					addressEditTx.setText(mUserAddress);
				}
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			mananger.removeBDEventListener(fkilistener);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(receiver);
		manager.removeLocation();
	}
	

	/**
	 * 注册广播
	 */
	private void registerBroadcastReceiver() {
		receiver = new AutoReportReceiver();
		IntentFilter filter = new IntentFilter(Utils.AUTO_REPORT_CHANGED_ACTION);
		registerReceiver(receiver, filter);
	}
	
	
	/**
	 * 启动服务
	 */
//	private void startTimeService() {
//		timeService = new Intent(this, TimeService.class);
//		this.startService(timeService);
//	}

	private void startCycleLocationService() {
		mContext.startService(cycleLocationService);
	}

	private void stopCycleLocationService() {
		mContext.stopService(cycleLocationService);
	}
	
	private class AutoReportReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Utils.AUTO_REPORT_CHANGED_ACTION.equals(action)) {
				Bundle bundle = intent.getExtras();
				String address = bundle.getString("AUTO_REPORT_ADDRESS");
				addressEditTx.setText(address);
				//Toast.makeText(context, "address="+address, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 把GPS定位的值封装到BDLocationReport实体类中
	 */
//	private BDLocationReport addGPSLocationToBDLocationReport() {
//		BDLocationReport report = new BDLocationReport();
//		report.setHeightUnit("m");
//		report.setLongitude(Utils.LOCATION_REPORT_LON);
//		report.setLongitudeDir("");
//		report.setHeight(Utils.LOCATION_REPORT_ALTITUDE);
//		report.setLatitude(Utils.LOCATION_REPORT_LAT);
//		report.setLatitudeDir("");
//		report.setMsgType(1);
//		report.setReportFeq(Integer.valueOf(fequency));
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss)");
//		String time = sdf.format(new Date());
//		report.setReportTime(time);
//		report.setUserAddress(mUserAddress);
//		return report;
//	}
}
