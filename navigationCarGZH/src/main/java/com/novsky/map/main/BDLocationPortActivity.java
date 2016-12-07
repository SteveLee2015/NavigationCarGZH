package com.novsky.map.main;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.location.BDEventListener;
import android.location.BDLocationReport;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TabHost;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.novsky.map.util.BDLocationTabManager;
import com.novsky.map.util.FriendsLocation;
import com.novsky.map.util.FriendsLocationDatabaseOperation;
import com.novsky.map.util.Utils;

/**
 * 北斗定位Tab页面
 * @author steve
 */
public class BDLocationPortActivity extends TabActivity {
	
	public static  TabHost tabHost = null;
	
	private Context mContext = this;
	
	private BDCommManager mBDCommManager=null;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			BDLocationReport report=(BDLocationReport)msg.obj;
			/* 1.位置报告保存的数据库 */
			boolean isAdd = mAddLocationReportToDatabase(report);
			report.setLatitude(Utils.changeLonLatMinuteToDegree(report
					.getLatitude()));
			report.setLongitude(Utils.changeLonLatMinuteToDegree(report
					.getLongitude()));
			/* 2.采用NotificationManager来显示 */
			Utils.mShowLocationReportNotification(mContext, report);
		}
	};
	
	/**
	 * 位置报告
	 */
	private BDEventListener reportListener = new BDEventListener.BDLocReportListener() {
		@Override
		public void onLocReport(BDLocationReport report) {
			Message message=mHandler.obtainMessage();
			message.obj=report;
			mHandler.sendMessage(message);
		}
	};
	
	/**
	 * 增加位置报告数据到数据库
	 */
	private boolean mAddLocationReportToDatabase(BDLocationReport report) {
		FriendsLocationDatabaseOperation oper = new FriendsLocationDatabaseOperation(
				mContext);
		FriendsLocation fl = new FriendsLocation();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		fl.setUserId(report.mUserAddress);
		fl.setLat(String.valueOf(Utils
				.changeLonLatMinuteToDegree(report.mLatitude)));
		fl.setLon(String.valueOf(Utils
				.changeLonLatMinuteToDegree(report.mLongitude)));
		fl.setHeight(String.valueOf(report.mHeight));
		fl.setReportTime(sdf.format(new Date()));
		boolean isTrue = oper.insert(fl);
		oper.close();
		return isTrue;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bdlocation_port);
		tabHost = this.getTabHost();
		mBDCommManager=BDCommManager.getInstance(this);
		try {
			mBDCommManager.addBDEventListener(reportListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		BDLocationTabManager manager=new BDLocationTabManager(mContext,tabHost);
	    manager.addTab("bd1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
        manager.addTab("bd2",mContext.getResources().getString(R.string.bdloc_1_top_tab_msg),BD1LocActivity.class);        
        /* 读取Tab图片 */
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.tab_bg);
		final BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeX(TileMode.REPEAT);
		bd.setDither(true);		
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		
	}
		
	@Override
	protected void onResume() {		
		super.onResume();
		//tabHost.setCurrentTab(BDLocationCurrentTab.getInstance().getCurrentIndex());
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Activity subActivity = getLocalActivityManager().getCurrentActivity();
		/*判断是否实现返回值接口*/
		if (subActivity instanceof OnTabActivityResultListener) {
			// 获取返回值接口实例
			OnTabActivityResultListener listener = (OnTabActivityResultListener) subActivity;
			// 转发请求到子Activity
			listener.onTabActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	protected void onStop() {
		super.onStop();
		//TabSwitchActivityData.getInstance().setTabFlag(1);	
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			mBDCommManager.removeBDEventListener(reportListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}
}
