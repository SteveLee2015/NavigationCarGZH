package com.novsky.map.main;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.location.CardInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.TabSwitchActivityData;

/**
 * 采用RDSS中间件实现访问北斗信息
 * 
 * @author steve
 */
public class LocalMachineInfoActivity extends Activity {

	private static final String TAG = "LocalMachineInfoActivity";

	private Context mContext = this;
	
	private ClipboardManager clipboardManager=null;
	

	// 本机地址
	private TextView localAddressTx = null;

	// 序列号
	private TextView serialNumTx = null;

	// 服务频度
	private TextView serviceFeqTx = null;

	// 通讯级别
	private TextView communicationLevelTx = null;

	// 本机类型
	private TextView cardTypeTx = null;

	// IC卡状态
	private TextView ICCardStatus = null;


	private TabSwitchActivityData mInstance = TabSwitchActivityData
			.getInstance();

	private BDRDSSManager manager = null;

	/**
	 * 卡信息监听器
	 */
	private BDEventListener listener = new BDEventListener.LocalInfoListener() {
		@Override
		public void onCardInfo(CardInfo card) {
			Log.d(TAG, "card.mCardAddress=" + card.getCardAddress()
					+ ",card.mSerialNum=" + card.mSerialNum
					+ ",card.mSericeFeq=" + card.mSericeFeq
					+ ",card.mCommLevel=" + card.mCommLevel
					+ ",card.checkEncryption=" + card.checkEncryption
					+ ",card.mCardAddress=" + card.mCardAddress);

			localAddressTx.setText(card.getCardAddress());
			serialNumTx.setText(card.mSerialNum);
			serviceFeqTx.setText(card.mSericeFeq + "");
			communicationLevelTx.setText(String.valueOf(card.mCommLevel));
			if ("N".equals(card.checkEncryption)) {
				cardTypeTx.setText("非密卡");
			} else if ("E".equals(card.checkEncryption)) {
				cardTypeTx.setText("加密卡");
			}
			String cardStatus = "";
			if ("2097151".equals(card.mCardAddress)) {
				cardStatus = "无卡";
			} else {
				cardStatus = "有卡";
			}
			ICCardStatus.setText(cardStatus);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* 界面分辨率 */
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager m = getWindowManager();
		m.getDefaultDisplay().getMetrics(metrics);
		LayoutParams p = getWindow().getAttributes();
		p.height = (int) ((metrics.heightPixels * 1) / 2);// 高度设置为屏幕的1/2
		p.width = (int) (metrics.widthPixels);
		getWindow().setAttributes(p);
		setContentView(R.layout.activity_local_machine_info);
		/* 初始化参数 */
		clipboardManager=(ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
		localAddressTx = (TextView) this.findViewById(R.id.local_address);
		serialNumTx = (TextView) this.findViewById(R.id.serial_num);
		serviceFeqTx = (TextView) this.findViewById(R.id.service_frequency);
		communicationLevelTx = (TextView) this
				.findViewById(R.id.communication_level);
		cardTypeTx = (TextView) this.findViewById(R.id.card_type);
		ICCardStatus = (TextView) this.findViewById(R.id.local_iccard_status);
		manager = BDRDSSManager.getDefault(this);
		try {
			manager.addBDEventListener(listener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		
		localAddressTx.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				AlertDialog.Builder dialog=new AlertDialog.Builder(LocalMachineInfoActivity.this);
		        dialog.setMessage(localAddressTx.getText().toString());
		        dialog.setPositiveButton("复制北斗SIM卡号", new DialogInterface.OnClickListener() {
					
					@SuppressLint("NewApi")
					@Override
					public void onClick(DialogInterface arg0, int arg1) {	
						ClipData data=ClipData.newPlainText("北斗SIM卡号", localAddressTx.getText().toString());
						clipboardManager.setPrimaryClip(data);
						Toast.makeText(LocalMachineInfoActivity.this ,"已经复制到剪贴板", Toast.LENGTH_SHORT).show();
					}
				}); 
		        dialog.show();
				return true;
			}			
		});
		
		
		serialNumTx.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View arg0) {
				AlertDialog.Builder dialog=new AlertDialog.Builder(LocalMachineInfoActivity.this);
		        dialog.setMessage(serialNumTx.getText().toString());
		        dialog.setPositiveButton("复制北斗序列号", new DialogInterface.OnClickListener() {
		        	@SuppressLint("NewApi")
					@Override
					public void onClick(DialogInterface arg0, int arg1) {	
						ClipData data=ClipData.newPlainText("北斗序列号", serialNumTx.getText().toString());
						clipboardManager.setPrimaryClip(data);
						
						Toast.makeText(LocalMachineInfoActivity.this ,"已经复制到剪贴板", Toast.LENGTH_SHORT).show();
					}
				}); 
		        dialog.show();
				return true;
			}
			
		});
				
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			manager.sendAccessCardInfoCmdBDV21(0, 0);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		mInstance.setTabFlag(3);
	}

	/**
	 * 销毁方法
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			manager.removeBDEventListener(listener);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}
}
