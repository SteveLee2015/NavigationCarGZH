package com.novsky.map.main;

import android.content.Context;
import android.location.BDEventListener.BDFKIListener;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.Utils;

public class BDMessageQueryResponseListener implements BDFKIListener {

	private Context mContext = null;

	/**
	 * 反馈信息解析-定位成功标识
	 */
	private final static int BD_LOCATION_SUCCESS = 14;

	/**
	 * 反馈信息解析-定位失败标识
	 */
	private final static int BD_LOCATION_FAIL = 15;

	/**
	 * 反馈信息解析-系统的抑制命令,发射被抑制标识
	 */
	private final static int BD_SYSTEM_LAUNCHER = 16;

	/**
	 * 反馈信息解析-电量低标识
	 */
	private final static int BD_NO_POWER = 17;

	/**
	 * 反馈信息解析-无线电静默，发射被抑制标识
	 */
	private final static int BD_KEEP_SILENCE = 18;

	/**
	 * 反馈信息解析-超频标识
	 */
	private final static int BD_OVER_TIME = 19;

	/**
	 * 北斗卡信息
	 */
	private BDCardInfoManager cardManager = null;

	public BDMessageQueryResponseListener(Context mContext) {
		this.mContext = mContext;
		cardManager = BDCardInfoManager.getInstance();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			// 指令发送成功
				case BD_LOCATION_SUCCESS:
					Toast.makeText(mContext,mContext.getResources().getString(R.string.cmd_success)
									.replace("CMD", "通信查询命令"), Toast.LENGTH_SHORT)
							.show();
					Utils.COUNT_DOWN_TIME = cardManager.getCardInfo().mSericeFeq;	
				
					break;
				// 指令发送失败
				case BD_LOCATION_FAIL:
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.cmd_fail)
									.replace("CMD", "通信查询命令"), Toast.LENGTH_SHORT)
							.show();
					break;
				// 系统的抑制命令，发射被抑制
				case BD_SYSTEM_LAUNCHER:
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.system_un_launch),
							Toast.LENGTH_SHORT).show();
					break;
				// 电量不足,发射被抑制
				case BD_NO_POWER:
					Toast.makeText(
							mContext,
							mContext.getResources()
									.getString(R.string.no_power),
							Toast.LENGTH_SHORT).show();

					break;
				// 无线电静默，发射被抑制
				case BD_KEEP_SILENCE:
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.wireless_silence),
							Toast.LENGTH_SHORT).show();

					break;
				// 超频
				case BD_OVER_TIME:
					Utils.COUNT_DOWN_TIME = cardManager.getCardInfo().mSericeFeq
							+ msg.arg1;
					break;
				default:
					break;
			}

		}
	};

	
	public void onTime(int time) {
		Message msg = new Message();
		msg.what = BD_OVER_TIME;
		msg.arg1 = time;
		mHandler.sendMessage(msg);
	}

	
	public void onSystemLauncher() {
		mHandler.sendEmptyMessage(BD_SYSTEM_LAUNCHER);
	}

	
	public void onSilence() {
		mHandler.sendEmptyMessage(BD_KEEP_SILENCE);
	}

	
	public void onPower() {
		mHandler.sendEmptyMessage(BD_NO_POWER);
	}

	
	public void onCmd(String arg0, boolean istrue) {
		if (arg0 != null && arg0.equals("CXA")) {
			if (istrue) {
				mHandler.sendEmptyMessage(BD_LOCATION_SUCCESS);
			} else {
				mHandler.sendEmptyMessage(BD_LOCATION_FAIL);
			}
		}
	}

}
