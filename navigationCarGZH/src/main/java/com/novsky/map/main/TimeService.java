package com.novsky.map.main;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.CardInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDTimeCountManager;
import com.novsky.map.util.BDTimeFreqChangedListener;
import com.novsky.map.util.Utils;



public class TimeService extends Service {
	
	/**
	 * 日志标识
	 */
	private final static String TAG = "TimeService";

	/**
	 * 计时器对象
	 */
	private Timer timer = null;

	/**
	 * 保持在待机状态下,服务继续运行
	 */
	private PowerManager pm;

	/**
	 * 电源管理唤醒锁
	 */
	private PowerManager.WakeLock wakeLock = null;

	
	private BDTimeCountManager timeCountMananger = null;

	@Override
	public void onCreate() {
		super.onCreate();
		this.init();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CardInfo cardInfo=BDCardInfoManager.getInstance().getCardInfo();
		pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		// 保持cpu一直运行，不管屏幕是否黑屏
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"CPUKeepRunning");
		wakeLock.acquire();
		// 定时器发送监听器
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Log.i(TAG, "start time:"+sdf.format(new Date())+",Utils.COUNT_DOWN_TIME="+Utils.COUNT_DOWN_TIME);
				if (Utils.COUNT_DOWN_TIME >= 0){
					Map<String, BDTimeFreqChangedListener> map = timeCountMananger
							.getTimeFreqListeners();
					if (map != null) {
						for (String key : map.keySet()) {
							BDTimeFreqChangedListener listener = map.get(key);
							listener.onTimeChanged(Utils.COUNT_DOWN_TIME);
						}
					}
					Utils.COUNT_DOWN_TIME--;
				}else{
					
				}
				Log.i(TAG, "stop time:"+sdf.format(new Date())+",Utils.COUNT_DOWN_TIME="+Utils.COUNT_DOWN_TIME);
			}
		},1000,1000);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 相关变量初始化
	 */
	private void init() {
		timer = new Timer();
		timeCountMananger = BDTimeCountManager.getInstance();
	}

	@Override
	public ComponentName startService(Intent service) {
		return super.startService(service);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (wakeLock != null&&wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}
