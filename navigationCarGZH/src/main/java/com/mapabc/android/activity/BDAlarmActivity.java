package com.mapabc.android.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.novsky.map.main.CycleAlarmService;
import com.novsky.map.util.Utils;

/**
 * 报警功能
 * @author steve
 */
public class BDAlarmActivity extends Activity {
	
   private BDRDSSManager mananger=null;
   private Context mContext=this;
   private Spinner alarmItem=null;
   private Button sendAlarmBtn=null,cancleAlarmBtn=null;
   private SharedPreferences alarmPrefs =null;
   private Intent alarmServiceIntent = null;
   private SharedPreferences reportSwitch = null;
   private BDAlarmReceiver receiver = null;
   private final static int CMD_LOCATION_SUCCESS = 0x10001;
   private final static int CMD_LOCATION_FAIL = 0x10002;
   private final static int CMD_SYSTEM_LAUNCHER = 0x10003;
   private final static int CMD_NO_POWER = 0x10004;
   private final static int CMD_SILENCE = 0x10005;
   private final static int CMD_TIME = 0x10006;
   
   
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
//					Toast.makeText(
//							mContext,
//							mContext.getResources()
//									.getString(R.string.cmd_success)
//									.replace("CMD", "报警"), Toast.LENGTH_SHORT)
//							.show();
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
//					Toast.makeText(
//							mContext,
//							mContext.getResources()
//									.getString(R.string.cmd_fail)
//									.replace("CMD", "报警"), Toast.LENGTH_SHORT)
//							.show();
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bdalarm);
		/* 界面分辨率 */
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager m = getWindowManager();
		m.getDefaultDisplay().getMetrics(metrics);
		LayoutParams p = getWindow().getAttributes();
		p.height = (int) ((metrics.heightPixels * 1) / 3);// 高度设置为屏幕的1/2
		p.width = (int) (metrics.widthPixels);
		reportSwitch = getSharedPreferences("LOCATION_REPORT_CFG", 0);
		getWindow().setAttributes(p);
		LinearLayout loclayout = (LinearLayout)this.findViewById(R.id.activity_alarm);				
		/*RDSS中间件初始化*/
		mananger = BDRDSSManager.getDefault(this);
		try {
			mananger.addBDEventListener(fkilistener);
		} catch (BDParameterException e1) {
			e1.printStackTrace();
		} catch (BDUnknownException e1) {
			e1.printStackTrace();
		}
		Utils.setActivityBackgroud(mContext, loclayout);
		alarmServiceIntent = new Intent(this,CycleAlarmService.class);
		alarmItem=(Spinner)this.findViewById(R.id.spinner_alarm);
		sendAlarmBtn=(Button)this.findViewById(R.id.send_alarm_btn);
		alarmPrefs = getSharedPreferences("BD_ALARM_CFG", 0);
		cancleAlarmBtn=(Button)this.findViewById(R.id.cancle_alarm_btn);
		boolean isStart=alarmPrefs.getBoolean("CHECK_START", false);
		if(isStart){
			int index=alarmPrefs.getInt("ALARM_TYPE_SELECTED_INDEX", 0);
			alarmItem.setSelection(index);
			alarmItem.setEnabled(false);
			sendAlarmBtn.setText(mContext.getResources().getString(R.string.stop_alarm));
		}
		sendAlarmBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				boolean isStart=alarmPrefs.getBoolean("CHECK_START", false);
				if(!isStart){
					/*判断是否安装北斗SIM卡*/
					if (Utils.getCardInfo().mSericeFeq == 9999) {
						Toast.makeText(mContext,
								mContext.getResources().getString(R.string.have_not_bd_sim),
								Toast.LENGTH_SHORT).show();
						return;
					}
					if(Utils.LOCATION_REPORT_LON==0.0||Utils.LOCATION_REPORT_LAT==0.0){
						Toast.makeText(mContext, "获取当前位置信息失败,请重新发送!", Toast.LENGTH_SHORT).show();
						return;
					}
					alarmItem.setEnabled(false);
					alarmPrefs.edit().putInt("ALARM_TYPE", Utils.getAlarmType(alarmItem.getSelectedItem().toString())).commit();
					alarmPrefs.edit().putBoolean("CHECK_START", true).commit();
					alarmPrefs.edit().putInt("ALARM_TYPE_SELECTED_INDEX", alarmItem.getSelectedItemPosition()).commit();
					sendAlarmBtn.setText(mContext.getResources().getString(R.string.stop_alarm));
					//启动循环
					startCycleAlarmService();
					Toast.makeText(mContext, "开始循环报警!", Toast.LENGTH_LONG).show();
				}else{
					alarmPrefs.edit().putBoolean("CHECK_START", false).commit();
					String address = reportSwitch.getString("USER_ADDRESS", "");
					if(address==null||"".equals(address)){
						address="455911";
					}
					alarmItem.setEnabled(true);
					String msg=Utils.getPuShiCRCAlarm(Utils.addGPSLocationToAlarm(60,address),(byte)0xAE);
					try {
						mananger.sendSMSCmdBDV21(address, 1,1,"N", msg);
					} catch (BDUnknownException e) {
						e.printStackTrace();
					} catch (BDParameterException e) {
						e.printStackTrace();
					}
					stopCycleAlarmService();
					sendAlarmBtn.setText(mContext.getResources().getString(R.string.start_alarm));
					Toast.makeText(mContext, "停止循环报警!", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		cancleAlarmBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				BDAlarmActivity.this.finish();
//				Intent intent = new Intent(BDRDSSManager.PROVIDERS_CHANGED_ACTION);
//				BDMessageInfo mBDMessageInfo=new BDMessageInfo();
//				mBDMessageInfo.setmUserAddress("455911");
//				mBDMessageInfo.setMsgType(1);
//				mBDMessageInfo.setMsgCharset(1);
//				try {
//					//mBDMessageInfo.setMessage("00B10301022C3F011FF76501022C030E0C010E1209".getBytes("GBK"));
//					mBDMessageInfo.setMessage("00A2AC0C07161B2901000A45740D26004E280224020CEA0065".getBytes("GBK"));
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//				intent.putExtra(BDRDSSManager.BDRDSS_MESSAGE, mBDMessageInfo);
//                mContext.sendBroadcast(intent);
//				Intent mInent=new Intent(mContext,BDAssistAlarmActivity.class);
//				mContext.startActivity(mInent);
			}
		});
		registerBroadcastReceiver();
	}

	private void startCycleAlarmService() {
		mContext.startService(alarmServiceIntent);
	}

	private void stopCycleAlarmService() {
		mContext.stopService(alarmServiceIntent);
	}
	
	
	private class BDAlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Utils.ALARM_CHANGED_ACTION.equals(action)) {
				Bundle bundle = intent.getExtras();
				boolean stopFlag = bundle.getBoolean("CYCLE_ALARM_FLAG");
				if(!stopFlag){
					sendAlarmBtn.setText(mContext.getResources().getString(R.string.start_alarm));
					alarmItem.setEnabled(true);
				}
			}
		}
	}

	/**
	 * 注册广播
	 */
	private void registerBroadcastReceiver() {
		receiver = new BDAlarmReceiver();
		IntentFilter filter = new IntentFilter(Utils.ALARM_CHANGED_ACTION);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(receiver);
	}
	
}
