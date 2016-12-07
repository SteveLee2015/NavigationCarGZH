package com.mapabc.android.activity.base;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mapabc.android.activity.R;
import com.mapabc.android.activity.log.Logger;
import com.mapabc.android.activity.utils.ActivityStack;
import com.mapabc.android.activity.utils.SettingForLikeTools;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.mapabc.naviapi.MapEngineManager;
import com.novsky.map.main.CheckTimeService;
import com.novsky.map.main.CycleAlarmService;
import com.novsky.map.main.CycleLocationReportService;
import com.novsky.map.main.TimeService;

/**
 * @description: 系统退出资源销毁
 * @author: changbao.wang 2011-10-17
 * @version:
 * @modify:
 * @Copyright: mapabc.com
 */
public class ExitDialog{
	private String TAG = "ExitDialog";
	private AlertDialog.Builder builder;
    Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what==1){
				if(NaviControl.getInstance().naviStatus == NaviControl.NAVI_STATUS_SIMNAVI){
					//停止模拟导航
					NaviControl.getInstance().stopSimNavi();
					NaviControl.getInstance().routeInfo = null;
				}
				NaviControl.getInstance().stopRealNavi();
				VolumeControl.resetVolume();
			}
		}
    };
    
	public ExitDialog(final Context context){
		builder = new AlertDialog.Builder(context);
		builder.setTitle(ToolsUtils.getValue(context, R.string.navistudio_txtExit));
		builder.setMessage(ToolsUtils.getValue(context,
				R.string.navistudio_txtExitDesc));
		builder.setNegativeButton(
				ToolsUtils.getValue(context, R.string.common_btn_negative),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						builder.create().dismiss();
					}
				}).setPositiveButton(
				ToolsUtils.getValue(context,R.string.common_btn_positive),
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences exitPrefs = context.getSharedPreferences("APP_EXIT_CFG", 0);
						exitPrefs.edit().putBoolean("IS_EXIT", true).commit();
						synchronized (context.getApplicationContext()){
							/* 停止同时显示北斗和GPS卫星状态 */
							Intent mIntent = new Intent("android.intent.action.BDGPS_REPORT");
							mIntent.putExtra("bdreport", 0);
							context.sendStickyBroadcast(mIntent);
							// 初始化超频数
							com.novsky.map.util.Utils.CARD_FREQ = 0;
							// 注销读北斗卡监听
							Intent cycleLocationService = new Intent(context,CycleLocationReportService.class);
							context.stopService(cycleLocationService);
							Intent timeService = new Intent(context, TimeService.class);
							context.stopService(timeService);
							Intent alarmServiceIntent = new Intent(context,CycleAlarmService.class);
							context.stopService(alarmServiceIntent);
							
							SharedPreferences alarmPrefs =context.getSharedPreferences("BD_ALARM_CFG", 0);
							alarmPrefs.edit().putBoolean("CHECK_START", false).commit();
							//停止校时服务
							Intent checkTimeIntent=new Intent(context,CheckTimeService.class);
							context.stopService(checkTimeIntent);
							// 释放锁
							com.novsky.map.util.Utils.releaseWakeLock();
							// 设置位置报告为单次
							SharedPreferences reportSwitch =context.getSharedPreferences("LOCATION_REPORT_CFG", 0);
							reportSwitch.edit().putString("REPORT_SWITCH", "off").commit();
							//reportSwitch.edit().putInt("FEQ", 0).commit();
                            //reportSwitch.edit().putString("USER_ADDRESS", "").commit();
							/*如果当前是模拟导航,则停止模拟导航*/
							// if (!blEnableSearch) {
							// ActivityStack.newInstance().setBlMapBack(true);// 来自地图返回
							// blEnableSearch = true;
							// mapView.hideTip();
							// mapListener.isStart = true;// 重新开启自动回车位功能
							// super.onBackPressed();
							// return;
							// }
							//
							//if (!MapAPI.getInstance().isCarInCenter()) {
							//	mapView.goBackCar();
							//	return;
							//}
							SettingForLikeTools.reSettingScreenBrightness(context);
							new Thread() {
								public void run(){
									try {
										Logger.e(TAG, "正常退出导航。。。。");
										h.sendEmptyMessage(1);
										ToolsUtils.saveRoute();
										ActivityStack.newInstance().finishAll();
										SettingForLikeTools.saveSystem(context);
										ToolsUtils.restoreScreenOff(context);
										MapEngineManager.release();
										Log.e(TAG, "MYPID:"+ android.os.Process.myPid());
										android.os.Process.killProcess(android.os.Process.myPid());
										System.exit(0);
									} catch (Exception ex){
										Log.e(TAG, "ERROR", ex);
									}
								}
							}.start();
						}
					}
				});
	}

	public void show() {
		builder.show();
	}
}
