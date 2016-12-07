package com.novsky.map.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.CardInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.novsky.map.util.BDCardInfoManager;

/**
 * 采用RDSS中间件实现访问北斗信息
 * 
 * @author steve
 */
public class LocalMachineInfoFragment extends Fragment {
	
	private static final String TAG = "LocalMachineInfoActivity";

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

	
	private BDCommManager manager = null;

	/**
	 * 卡信息监听器
	 */
	private BDEventListener listener = new BDEventListener.LocalInfoListener() {
		public void onCardInfo(CardInfo card) {
			BDCardInfoManager.getInstance().setCardInfo(card);
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
			//prmTX.setText("无PRM");
			String cardStatus = "";
			if ("2097151".equals(card.mCardAddress)) {
				cardStatus = "无卡";
			} else {
				cardStatus = "有卡";
			}
			ICCardStatus.setText(cardStatus);
		}
	};

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager = BDCommManager.getInstance(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			manager.addBDEventListener(listener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		try {
			manager.sendAccessCardInfoCmdBDV21(0, 0);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
			manager.removeBDEventListener(listener);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.activity_local_machine_info,null);
		clipboardManager=(ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		localAddressTx = (TextView) view.findViewById(R.id.local_address);
		serialNumTx = (TextView) view.findViewById(R.id.serial_num);
		serviceFeqTx = (TextView) view.findViewById(R.id.service_frequency);
		communicationLevelTx = (TextView) view.findViewById(R.id.communication_level);
		cardTypeTx =(TextView)view.findViewById(R.id.card_type);
		ICCardStatus = (TextView) view.findViewById(R.id.local_iccard_status);
		
		localAddressTx.setOnLongClickListener(new OnLongClickListener(){
			
			public boolean onLongClick(View arg0) {
				AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
		        dialog.setMessage(localAddressTx.getText().toString());
		        dialog.setPositiveButton("复制北斗SIM卡号", new DialogInterface.OnClickListener() {
					
					@SuppressLint("NewApi")
					
					public void onClick(DialogInterface arg0, int arg1) {	
						ClipData data=ClipData.newPlainText("北斗SIM卡号", localAddressTx.getText().toString());
						clipboardManager.setPrimaryClip(data);
						Toast.makeText(getActivity() ,"已经复制到剪贴板", Toast.LENGTH_SHORT).show();
					}
				}); 
		        dialog.show();
				return true;
			}			
		});
		
		
		serialNumTx.setOnLongClickListener(new OnLongClickListener(){
			public boolean onLongClick(View arg0) {
				AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
		        dialog.setMessage(serialNumTx.getText().toString());
		        dialog.setPositiveButton("复制北斗序列号", new DialogInterface.OnClickListener() {
		        	@SuppressLint("NewApi")
					
					public void onClick(DialogInterface arg0, int arg1) {	
						ClipData data=ClipData.newPlainText("北斗序列号", serialNumTx.getText().toString());
						clipboardManager.setPrimaryClip(data);
						Toast.makeText(getActivity() ,"已经复制到剪贴板", Toast.LENGTH_SHORT).show();
					}
				}); 
		        dialog.show();
				return true;
			}
			
		});
		return view;
	}



	public void onStart() {
		super.onStart();
	}

	/**
	 * 销毁方法
	 */
	public void onDestroy() {
		super.onDestroy();
	}
}
