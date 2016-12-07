package com.novsky.map.main;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.location.BDBeam;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.novsky.map.util.Utils;

/**
 * 波速状态
 * @author steve
 */
public class AutoCheckedActivity extends Activity {

	/**
	 * 波束状态上下文对象
	 */
	private Context mContext = this;
	/**
	 * 日志标识
	 */
	private static final String TAG = "AutoCheckedActivity";
	/**
	 *波速1 
	 */
	private CustomProgressView mAutoProgress1 = null;
	/**
	 *波速2 
	 */
	private CustomProgressView mAutoProgress2 = null;
	
	/**
	 *波速3 
	 */
	private CustomProgressView mAutoProgress3 = null;

	/**
	 *波速4 
	 */
	private CustomProgressView mAutoProgress4 = null;

	/**
	 *波速5 
	 */
	private CustomProgressView mAutoProgress5 = null;

	/**
	 *波速6 
	 */
	private CustomProgressView mAutoProgress6 = null;

	/**
	 *波速7 
	 */
	private CustomProgressView mAutoProgress7 = null;

	/**
	 *波速8 
	 */
	private CustomProgressView mAutoProgress8 = null;

	/**
	 *波速9 
	 */
	private CustomProgressView mAutoProgress9 = null;

	/**
	 *波速10 
	 */
	private CustomProgressView mAutoProgress10 = null;
	
	/**
	 * 北斗RDSS管理类
	 */
	private BDCommManager manager = null;

	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			BDBeam beam = (BDBeam) msg.obj;
			int[] beamArray=beam.getBeamWaves();
			mAutoProgress1.setProgress(beamArray[0]);
			mAutoProgress2.setProgress(beamArray[1]);
			mAutoProgress3.setProgress(beamArray[2]);
			mAutoProgress4.setProgress(beamArray[3]);
			mAutoProgress5.setProgress(beamArray[4]);
			mAutoProgress6.setProgress(beamArray[5]);
			mAutoProgress7.setProgress(beamArray[6]);
			mAutoProgress8.setProgress(beamArray[7]);
			mAutoProgress9.setProgress(beamArray[8]);
			mAutoProgress10.setProgress(beamArray[9]);
		}
	};

	/**
	 * 波束监听器
	 */
	BDEventListener listener = new BDEventListener.BDBeamStatusListener() {
		public void onBeamStatus(BDBeam beam) {
			Message msg = new Message();
			msg.obj = beam;
			mHandler.sendMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(Utils.isLand?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		if("AOSP on wing".equals(Utils.DEVICE_MODEL)){
			setContentView(R.layout.activity_auto_checked_car_7);	
		}else{
			setContentView(R.layout.activity_auto_checked);	
		}
		initUI();
		/* 发送波速状态命令 */
		manager = BDCommManager.getInstance(mContext);
		try {
			manager.addBDEventListener(listener);
			//manager.sendRMOCmdBDV21("BSI", 2, 1);//第二个参数2表示打开BSI命令，第三个参数表示频度是1
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		try{
			manager.sendRMOCmdBDV21("BSI", 2, 1);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* 注销监听器 */
		try {
			manager.removeBDEventListener(listener);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 初始化UI
	 */
	public void initUI() {
		LinearLayout autoCheckedLayout=(LinearLayout)this.findViewById(R.id.auto_checked_layout);
		autoCheckedLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				AutoCheckedActivity.this.finish();
			}
		});
		mAutoProgress1 = (CustomProgressView) this.findViewById(R.id.auto_progress_1);
		mAutoProgress2 = (CustomProgressView) this.findViewById(R.id.auto_progress_2);
		mAutoProgress3 = (CustomProgressView) this.findViewById(R.id.auto_progress_3);
		mAutoProgress4 = (CustomProgressView) this.findViewById(R.id.auto_progress_4);
		mAutoProgress5 = (CustomProgressView) this.findViewById(R.id.auto_progress_5);
		mAutoProgress6 = (CustomProgressView) this.findViewById(R.id.auto_progress_6);
		mAutoProgress7 = (CustomProgressView) this.findViewById(R.id.auto_progress_7);
		mAutoProgress8 = (CustomProgressView) this.findViewById(R.id.auto_progress_8);
		mAutoProgress9 = (CustomProgressView) this.findViewById(R.id.auto_progress_9);
		mAutoProgress10 = (CustomProgressView)this.findViewById(R.id.auto_progress_10);
	}

}
