package com.novsky.map.main;

import android.content.Context;
import android.location.BDEventListener.BDFKIListener;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.Utils;

/**
 * 北斗I代反馈信息监听
 * @author steve
 */
public class BDResponseListener implements BDFKIListener {

	private Context mContext = null;

	/**
	 * 反馈信息解析-定位成功标识
	 */
	private final static int BD_SEND_SUCCESS = 0x10000;

	/**
	 * 反馈信息解析-定位失败标识
	 */
	private final static int BD_SEND_FAIL =0x10001;

	/**
	 * 反馈信息解析-系统的抑制命令,发射被抑制标识
	 */
	private final static int BD_SYSTEM_LAUNCHER =0x10002;

	/**
	 * 反馈信息解析-电量低标识
	 */
	private final static int BD_NO_POWER = 0x10003;

	/**
	 * 反馈信息解析-无线电静默，发射被抑制标识
	 */
	private final static int BD_KEEP_SILENCE = 0x10004;

	/**
	 * 反馈信息解析-超频标识
	 */
	private final static int BD_OVER_TIME = 0x10005;

	
	private BDCardInfoManager cardManager = null;


	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			/* 北斗I代定位或位置报告成功 */
				case BD_SEND_SUCCESS:{
					String mark = (String) msg.obj;
					if (mark != null && "DWA".equals(mark)) {
						Toast.makeText(mContext, "定位命令发送成功!",Toast.LENGTH_SHORT).show();					
					} else if (mark != null && "WAA".equals(mark)) {						
						Toast.makeText(mContext, "位置报告1发送成功!",Toast.LENGTH_SHORT).show();
					} else if (mark != null && "WBA".equals(mark)) {						
						Toast.makeText(mContext, "位置报告2发送成功!",Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(mContext, "北斗命令发送成功!",Toast.LENGTH_SHORT).show();
					}
                    break;
				}

				/* 北斗I代定位或位置报告失败 */
				case BD_SEND_FAIL:
					String mark = (String) msg.obj;
					if (mark != null && "DWA".equals(mark)) {
						Toast.makeText(mContext, "定位命令发送失败!",Toast.LENGTH_SHORT).show();
					}else if (mark != null && "WAA".equals(mark)) {
						Toast.makeText(mContext, "位置报告1命令发送失败!",Toast.LENGTH_SHORT).show();
					}else if (mark != null && "WBA".equals(mark)) {
						Toast.makeText(mContext, "位置报告2命令发送失败!",Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(mContext, "北斗命令发送失败!",Toast.LENGTH_SHORT).show();						
					}
					break;
				/* 系统的抑制命令，发射被抑制 */
				case BD_SYSTEM_LAUNCHER:
					Toast.makeText(mContext, "系统抑制!", Toast.LENGTH_SHORT).show();
					break;

				/* 电量不足,发射被抑制 */
				case BD_NO_POWER:
					Toast.makeText(mContext, "电量不足,发射抑制!", Toast.LENGTH_SHORT).show();
					break;
				/* 无线电静默，发射被抑制 */
				case BD_KEEP_SILENCE:
					Toast.makeText(mContext, "无线静默!", Toast.LENGTH_SHORT).show();
					break;
				/* 超频 */
				case BD_OVER_TIME:
					if (cardManager != null && cardManager.getCardInfo() != null) {
						Utils.COUNT_DOWN_TIME = (cardManager.getCardInfo().mSericeFeq + msg.arg1);
					} else {
						Utils.COUNT_DOWN_TIME = (60 + msg.arg1);
					}
					break;
				default:
					break;
			}
		}
	};
	
	public BDResponseListener(Context mContext) {
		this.mContext = mContext;
		cardManager = BDCardInfoManager.getInstance();
	}

	
	public void onTime(int time) {
		Message msg = new Message();
		msg.what = BD_OVER_TIME;
		msg.arg1 = time;
		handler.sendMessage(msg);
	}
	
	public void onSystemLauncher() {
		handler.sendEmptyMessage(BD_SYSTEM_LAUNCHER);
	}

	
	public void onSilence() {
		handler.sendEmptyMessage(BD_KEEP_SILENCE);
	}

	
	public void onPower() {
		handler.sendEmptyMessage(BD_NO_POWER);
	}

	
	public void onCmd(String arg0, boolean istrue) {
		Message msg = new Message();
		msg.obj = arg0;
		if (istrue) {
			msg.what =BD_SEND_SUCCESS;
			handler.sendMessage(msg);
		} else {
			msg.what =BD_SEND_FAIL;
			handler.sendMessage(msg);
		}
	}
}
