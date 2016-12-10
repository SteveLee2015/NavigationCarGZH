package com.novsky.map.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.location.CardInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.bd.comm.protocal.BDCommManager;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.Utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 循环位置报告服务RD
 * @author llg
 */
public class CycleLocationReportServiceRD extends BaseReportService {

	private final static String TAG = "CycleLocationReportServiceRN";
	private Timer timer = null;
	private SharedPreferences reportSwitch = null;
	private int fequency = 0;
	private String address = "";
	private BDCommManager mananger = null;
	private static WakeLock wakeLock = null;
	private int mCountNum=0;
	private int cardFreq=0;
	private static int msgComType =1;
	public static final String PREFERENCE_NAME = "REPORT_MODEL_ACTIVITY";
	public static final String REPORT_TIANXIAN_VALUE = "REPORT_TIANXIAN_VALUE";
	public static int MODE = Context.MODE_PRIVATE;
	
	@Override
	public void onCreate(){
		super.onCreate();
		reportSwitch = getSharedPreferences("LOCATION_REPORT_SWITCH", 0);
		CardInfo cardInfo=BDCardInfoManager.getInstance().getCardInfo();
		if(cardInfo!=null){
			cardFreq=cardInfo.getSericeFeq();
		}
		fequency = reportSwitch.getInt("REPORT_FREQUENCY", cardFreq);
		mCountNum=fequency;
		address = reportSwitch.getString("USER_ADDRESS", "");
		mananger = BDCommManager.getInstance(this);

		//获取 天线高度
		SharedPreferences shareTianxian = getSharedPreferences(PREFERENCE_NAME, MODE);
		final float tianxian = shareTianxian.getFloat(REPORT_TIANXIAN_VALUE,45.0f);
		//初始化
		timer = new Timer();
		//定时器发送广播
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				if (mCountNum> 0){
					mCountNum--;
				} else if(mCountNum == 0){
					if(fequency>0){
						try {
							mananger.sendLocationReport2CmdBDV21(address, BDRDSSManager.HeightFlag.COMMON_USER,tianxian,0);
						} catch (BDUnknownException e) {
							e.printStackTrace();
						} catch (BDParameterException e) {
							e.printStackTrace();
						}

						fequency = reportSwitch.getInt("REPORT_FREQUENCY", cardFreq);
						address = reportSwitch.getString("USER_ADDRESS", "");
						mCountNum = fequency;
						Utils.COUNT_DOWN_TIME=fequency;
					}
				}
			}
		}, 1000, 1000);
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
