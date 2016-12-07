package com.mapabc.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.novsky.map.util.Utils;

/**
 * 协助搜救
 * @author steve
 */
public class BDAssistAlarmActivity extends Activity {

	private Button sendAssistAlarmBtn=null,cancleAssistAlarmBtn=null;
	private TextView messageText=null;
	private SharedPreferences reportSwitch=null;
	private Context mContext=this;
	private BDRDSSManager mananger=null;
	   private final static int CMD_LOCATION_SUCCESS = 0x10007;
	   private final static int CMD_LOCATION_FAIL = 0x10008;
	   private final static int CMD_SYSTEM_LAUNCHER = 0x10009;
	   private final static int CMD_NO_POWER = 0x1000A;
	   private final static int CMD_SILENCE = 0x1000B;
	   private final static int CMD_TIME = 0x1000C;
	   
	   
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
										.replace("CMD", "回复协助搜救"), Toast.LENGTH_SHORT)
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
										.replace("CMD", "回复协助搜救"), Toast.LENGTH_SHORT)
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bdassist_alarm);
		/* 界面分辨率 */
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager m = getWindowManager();
		m.getDefaultDisplay().getMetrics(metrics);
		LayoutParams p = getWindow().getAttributes();
		p.height = (int) ((metrics.heightPixels * 1) / 3);// 高度设置为屏幕的1/2
		p.width = (int) (metrics.widthPixels);
		reportSwitch = getSharedPreferences("LOCATION_REPORT_CFG", 0);
		getWindow().setAttributes(p);
		LinearLayout loclayout = (LinearLayout)this.findViewById(R.id.activity_assist_alarm);
		Utils.setActivityBackgroud(mContext, loclayout);
		mananger=BDRDSSManager.getDefault(this);
		try {
			mananger.addBDEventListener(fkilistener);
		} catch (BDParameterException e1) {
			e1.printStackTrace();
		} catch (BDUnknownException e1) {
			e1.printStackTrace();
		}
		sendAssistAlarmBtn=(Button)this.findViewById(R.id.reply_assist_alarm_btn);
		cancleAssistAlarmBtn=(Button)this.findViewById(R.id.cancle_assist_alarm_btn);
		messageText=(TextView)this.findViewById(R.id.bd_alarm_message);
		messageText.setText("是否回复协助搜救?");
		sendAssistAlarmBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				String address = reportSwitch.getString("USER_ADDRESS", "");
				if(address==null||"".equals(address)){
					address="455911";
				}
				String msg=Utils.getPuShiCRCAlarm(Utils.addGPSLocationToAlarm(60,address),(byte)0xAD);
				try {
					mananger.sendSMSCmdBDV21(address, 1,1,"N", msg);
				} catch (BDUnknownException e) {
					e.printStackTrace();
				} catch (BDParameterException e) {
					e.printStackTrace();
				}
			}
		});
		cancleAssistAlarmBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				BDAssistAlarmActivity.this.finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.bdassist_alarm, menu);
		return true;
	}
}
