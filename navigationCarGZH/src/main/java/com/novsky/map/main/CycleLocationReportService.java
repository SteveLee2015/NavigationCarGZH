package com.novsky.map.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.BDLocationReport;
import android.location.BDParameterException;
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
 * 循环位置报告服务
 * @author steve
 */
public class CycleLocationReportService extends BaseReportService {

	private final static String TAG = "CycleLocationReportService";
	private Timer timer = null;
	private SharedPreferences reportSwitch = null;
	private int fequency = 0;
	private String address = "";
	private BDCommManager mananger = null;
	private static WakeLock wakeLock = null;
	private int mCountNum=0;
	private int cardFreq=0;
	private static int msgComType =1;
	
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
						BDLocationReport locationReport = addGPSLocationToBDLocationReport(fequency,address);
						try{
							mananger.sendSMSCmdBDV21(address, msgComType,1,"N", Utils.buildeLocationReport1(locationReport));
						}catch (BDUnknownException e){
							e.printStackTrace();
						}catch (BDParameterException e){
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
	
	/**
	 * 把GPS定位的值封装到BDLocationReport实体类中
	 */
//	private BDLocationReport addGPSLocationToBDLocationReport(int reportFreq,String userAddress) {
//		BDLocationReportManager manager=BDLocationReportManager.getInstance();
//		if("S500".equals(Utils.DEVICE_MODEL)){
//				return null;
//		}else{
//			BDRNSSLocation location=manager.getBDRNSSLocation();
//			if(location!=null){
//				BDLocationReport report = new BDLocationReport();
//				report.setHeightUnit("m");
//				report.setLongitude(location.getLongitude());
//				report.setLongitudeDir("");
//				report.setHeight(location.getAltitude());
//				report.setLatitude(location.getLatitude());
//				report.setLatitudeDir("");
//				report.setMsgType(1);
//				report.setReportFeq(reportFreq);
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss)");
//				String time = sdf.format(new Date());
//				report.setReportTime(time);
//				report.setUserAddress(userAddress);
//				return report;
//			}else{
//				return null;
//			}
//		}
//	}
}
