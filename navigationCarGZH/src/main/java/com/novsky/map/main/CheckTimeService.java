package com.novsky.map.main;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.novsky.map.util.Utils;

/**
 * 校时服务
 * @author steve
 */
public class CheckTimeService extends Service {

	private final static String TAG = "CheckTimeService";
	private Timer timer = null;
	private static WakeLock wakeLock = null;
	private boolean isLocation=false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.init();
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				if(!isLocation){
					Calendar calendar = Calendar.getInstance();
					long currentTime =Utils.LOCATION_REPORT_TIME;
					calendar.setTimeInMillis(currentTime);
					int year = calendar.get(Calendar.YEAR);
					if (year>2000) {
						try {
							SystemDateTime.setDateTime(Utils.LOCATION_REPORT_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						isLocation=true;
					}
				}
			}
		}, 0, 3000);//每隔3秒进行校时
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
