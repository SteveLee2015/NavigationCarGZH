package com.novsky.map.main;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.novsky.map.util.Utils;

/**
 * 循环报警服务
 * @author steve
 */
public class CycleAlarmService extends Service {

	private final static String TAG = "CycleAlarmService";
	private Timer timer = null;
	private SharedPreferences reportSwitch = null,alarmPrefs=null;
	private String address = "";
	private BDRDSSManager mananger = null;
	private static WakeLock wakeLock = null;
    
	@Override
	public void onCreate() {
		super.onCreate();
		alarmPrefs = getSharedPreferences("BD_ALARM_CFG", 0);
		reportSwitch = getSharedPreferences("LOCATION_REPORT_CFG", 0);
		mananger = BDRDSSManager.getDefault(this);
		//初始化
		this.init();
		// 定时器发送广播
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				address = reportSwitch.getString("USER_ADDRESS", "");
				if(address==null||"".equals(address)){
					address="455911";
				}
				int type=alarmPrefs.getInt("ALARM_TYPE", 1);
				String msg=Utils.getPuShiCRCAlarm(Utils.addGPSLocationToAlarm(60,address),(byte)type);
				Log.i(TAG, "msg="+msg+",address="+address);
				try {
					mananger.sendSMSCmdBDV21(address, 1,1,"N", msg);
				} catch (BDUnknownException e) {
					e.printStackTrace();
				} catch (BDParameterException e) {
					e.printStackTrace();
				}
			}
		}, 0, 1000*60);
	}

	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CycleLocationReportService");
		if (null != wakeLock) {
			wakeLock.acquire();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	
	/**
	 * 相关变量初始化
	 */
	private void init() {
		timer = new Timer();
	}


	@Override
	public ComponentName startService(Intent service) {
		return super.startService(service);
	}

	
	
	@Override
	public boolean stopService(Intent name) {
		return super.stopService(name);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
		if(wakeLock!=null){
			wakeLock.release();
			wakeLock=null;
		}
	}
}
