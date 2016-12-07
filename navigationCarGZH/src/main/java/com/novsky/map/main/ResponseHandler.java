package com.novsky.map.main;


import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.Utils;

/**
 * 反馈信息的Hanlder
 * @author steve
 */
public class ResponseHandler extends Handler {
	
	private Context mContext=null;
	
	private final static int BD_COMMAND_SUCCESS = 1;
	private final static int BD_COMMAND_FAIL = 2;
	private final static int BD_COMMAND_SYSTEM_LAUNCHER = 3;
	private final static int BD_COMMAND_NO_POWER = 4;
	private final static int BD_COMMAND_SILENCE = 5;
	private final static int BD_COMMAND_TIME = 6;

	
	public ResponseHandler(Context mContext){
         this.mContext=mContext;
	}
	
	
	
	public void handleMessage(android.os.Message msg) {
		switch (msg.what) {
		// 指令发送成功
		case BD_COMMAND_SUCCESS:
			Toast.makeText(
					mContext,
					mContext.getResources().getString(R.string.cmd_success)
							.replace("CMD", "通信命令"), Toast.LENGTH_SHORT)
					.show();
			Utils.CARD_FREQ = Utils.getCardInfo().mSericeFeq;
			break;
		// 指令发送失败
		case BD_COMMAND_FAIL:
			Toast.makeText(
					mContext,
					mContext.getResources().getString(R.string.cmd_fail)
							.replace("CMD", "通信命令"), Toast.LENGTH_SHORT)
					.show();
			Utils.CARD_FREQ = Utils.getCardInfo().mSericeFeq;
			break;
		// 系统的抑制命令，发射被抑制
		case BD_COMMAND_SYSTEM_LAUNCHER:
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.system_un_launch), Toast.LENGTH_SHORT)
					.show();
			break;
		// 电量不足,发射被抑制
		case BD_COMMAND_NO_POWER:
			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.no_power),
					Toast.LENGTH_SHORT).show();

			break;
		// 无线电静默，发射被抑制
		case BD_COMMAND_SILENCE:
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.wireless_silence), Toast.LENGTH_SHORT)
					.show();

			break;
		// 超频
		case BD_COMMAND_TIME:
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
}
