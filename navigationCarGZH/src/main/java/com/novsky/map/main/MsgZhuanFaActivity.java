package com.novsky.map.main;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.BDMSG;
import com.novsky.map.util.BDTimeCountManager;
import com.novsky.map.util.BDTimeFreqChangedListener;
import com.novsky.map.util.DatabaseHelper.CustomColumns;
import com.novsky.map.util.DatabaseOperation;
import com.novsky.map.util.MessgeUsualOperation;
import com.novsky.map.util.Utils;

public class MsgZhuanFaActivity extends Activity {
	
	private final String TAG = "MsgZhuanFaActivity";
	
	private EditText userAddress,msgContent;

	private ImageView addressBook,manageMessage,sendMsgBtn,huifuMsgBtn,messageButton;
	
	private TextView bdMsgDate ,showFeqTextView;

	private LinearLayout msgLayout = null;
	
	private final int REQUEST_CONTACT = 1;

	private Context mContext = this;
	
	private BDCommManager manager = null;
	
	private BDEventListener fkilistener =null;
	
	private BDTimeCountManager timeManager=null;
	
	/**
	 * 检查是否发送短信至手机
	 */
	private CheckBox  checkSendPhoneSMS=null;
	
	private DatabaseOperation smsOperation=null;
	
	private boolean isSendPhoneSMS=false;
	
	private Cursor smsCursor=null;

	
	private BDTimeFreqChangedListener timeFreqListener=
		     new BDTimeFreqChangedListener(){
			
			public void onTimeChanged(int remainder_time) {
				/* 用消息传递数据 */
				Message msg = new Message();
				msg.arg1=remainder_time;
				mHandler.sendMessage(msg);
			}
    };
    
    private Handler mHandler=new Handler(){
		
		public void handleMessage(Message message) {	
			super.handleMessage(message);
			int remainder_time=message.arg1;
			if(remainder_time!=0){
				showFeqTextView.setText("剩余:"+remainder_time+"秒");
				//messageButton.setEnabled(false);
				messageButton.setImageResource(R.drawable.msg_not_send_btn);						
			}else{
				showFeqTextView.setText("");
				//messageButton.setEnabled(true);
				messageButton.setImageResource(R.drawable.msg_send_btn);
			}	
		}
    };
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_msg_zhuan_fa);
		this.setRequestedOrientation(Utils.isLand?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		Intent intent = getIntent();
		int index = Integer.valueOf(intent.getStringExtra("MSG_DEL_ID"));
		smsOperation= new DatabaseOperation(this);
		smsCursor= smsOperation.get(index);
		timeManager=BDTimeCountManager.getInstance();
		timeManager.registerBDTimeFreqListener(MsgZhuanFaActivity.class.getSimpleName(),timeFreqListener);
		fkilistener=new BDMessageQueryResponseListener(mContext);
		checkSendPhoneSMS=(CheckBox)this.findViewById(R.id.checkSendPhoneSMS);
		SharedPreferences pref=mContext.getSharedPreferences("BD_RELAY_STATION_PREF", mContext.MODE_PRIVATE);
		boolean checked=pref.getBoolean("BD_ZHUANFA_RELAY_STATUS", false);
        checkSendPhoneSMS.setChecked(checked);
        isSendPhoneSMS=checked;
		userAddress = (EditText) this.findViewById(R.id.msg_user_address_edittxt);
		msgContent = (EditText) this.findViewById(R.id.bd_msg_content_edittxt);
		addressBook = (ImageView) this.findViewById(R.id.msg_user_address_book);
		manageMessage = (ImageView) this.findViewById(R.id.msg_manager_imageview);
		msgLayout = (LinearLayout) this.findViewById(R.id.bd_msg_send_layout);
		sendMsgBtn = (ImageView) this.findViewById(R.id.bd_msg_send_btn);
		bdMsgDate = (TextView) this.findViewById(R.id.bd_msg_date);
		huifuMsgBtn=(ImageView)this.findViewById(R.id.msg_huifu_imageview);
		messageButton=(ImageView)this.findViewById(R.id.bd_msg_btn);
		showFeqTextView =(TextView) this.findViewById(R.id.bd_msg_feq_textview);
		manager = BDCommManager.getInstance(this);
		String address = "",msg = "",time = "",flag="";
		if (smsCursor.moveToFirst()) {
			if (smsCursor.getString(smsCursor
					.getColumnIndex(CustomColumns.COLUMNS_USER_ADDRESS)) != null) {
				address = smsCursor.getString(smsCursor
						.getColumnIndex(CustomColumns.COLUMNS_USER_ADDRESS));
			}
			if (smsCursor.getString(smsCursor
					.getColumnIndex(CustomColumns.COLUMNS_MSG_CONTENT)) != null) {
				msg = smsCursor.getString(smsCursor
						.getColumnIndex(CustomColumns.COLUMNS_MSG_CONTENT));
			}
			if (smsCursor.getString(smsCursor
					.getColumnIndex(CustomColumns.COLUMNS_SEND_TIME)) != null) {
				time = smsCursor.getString(smsCursor
						.getColumnIndex(CustomColumns.COLUMNS_SEND_TIME));
			}
			if(smsCursor.getString(smsCursor.getColumnIndex(CustomColumns.COLUMNS_FLAG))!=null){
				flag=smsCursor.getString(smsCursor.getColumnIndex(CustomColumns.COLUMNS_FLAG));	
			}
		}
		if(flag!=null&&"3".equals(flag)){
	 	     smsOperation.updateMessageStatus(Long.valueOf(index), 0);
		}
		userAddress.setText(address);
		msgContent.setText(msg);
		bdMsgDate.setText(time);
		
		checkSendPhoneSMS.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean check) {
				   SharedPreferences pref=mContext.getSharedPreferences("BD_RELAY_STATION_PREF", mContext.MODE_PRIVATE);	
	               pref.edit().putBoolean("BD_ZHUANFA_RELAY_STATUS", check).commit();
	               isSendPhoneSMS=check;
	               userAddress.setText("");	
	               if(check){
					   userAddress.setHint("请输入手机号码!");
			       }else{
			       	   userAddress.setHint("请输入北斗卡号!");
			       }
			}
		});
		
		huifuMsgBtn.setOnClickListener(new OnClickListener(){
			
			public void onClick(View arg0) {
			   		manageMessage.setVisibility(View.VISIBLE);
			   		messageButton.setVisibility(View.VISIBLE);
			   		huifuMsgBtn.setVisibility(View.GONE);
			   		sendMsgBtn.setVisibility(View.GONE);
			   		msgContent.setText("");	   		
			}
		});
		/* 发送通讯请求 */
		sendMsgBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {									
			   		manageMessage.setVisibility(View.VISIBLE);
			   		messageButton.setVisibility(View.VISIBLE);
			   		huifuMsgBtn.setVisibility(View.GONE);
			   		sendMsgBtn.setVisibility(View.GONE);
			   		userAddress.setText("");
			}
		});
		
		/*发送*/
		messageButton.setOnClickListener(new OnClickListener(){
			
			public void onClick(View arg0) {
			     
				send();
								
			}
		});
	}
	
	public void send(){
		int translateType = 0;
		String msgstr = msgContent.getText().toString();
		String sendAddress = userAddress.getText().toString();
		/* 判断用户地址是否为空! */
		if (sendAddress.equals("")) {
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.bd_address_no_content),
					Toast.LENGTH_SHORT).show();
			return;
		}
		/* 判断短信内容是否为空 */
		if (msgstr.equals("")) {
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.bd_msg_no_content),
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (sendAddress.contains("(")) {
			sendAddress = sendAddress.substring(
					sendAddress.lastIndexOf("(") + 1,
					sendAddress.lastIndexOf(")"));
		}
		try {
			if(isSendPhoneSMS){
				//获得设置的中继站的号码
				SharedPreferences pref=mContext.getSharedPreferences("BD_RELAY_STATION_PREF", mContext.MODE_PRIVATE);	
			    String address=pref.getString("BD_RELAY_STATION_NUM", "");
			    //手机地址
			    String phoneNum=sendAddress;
			    msgstr=Utils.buildSendPhoneSMS(phoneNum, msgstr);
			    if(!address.equals("")){
			    	sendAddress=address;
			    }else{
			    	 com.novsky.map.util.Utils.createAlertDialog(MsgZhuanFaActivity.this.getParent(), "提示", "请先设置'系统'->'中继站管理'!", false, 
						        new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int arg1) {
										dialog.dismiss();
									}
						   }, "取消").show();
			       return;
			   }
			}
			translateType = Utils.checkMsg(msgstr);
			manager.sendSMSCmdBDV21(sendAddress, 1,translateType,"N", msgstr);
			Utils.COUNT_DOWN_TIME=BDCardInfoManager.getInstance().getCardInfo().mSericeFeq;
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
		/* 在数据库中保存该数据 */
		DatabaseOperation operation = new DatabaseOperation(mContext);
		BDMSG msg = new BDMSG();
		String address = userAddress.getText().toString();
		msg.setColumnsUserAddress(address);
		msg.setColumnsMsgType(1 + "," + translateType);
		msg.setColumnsSendAddress(address);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		msg.setColumnsSendTime(sdf.format(new Date()));
		msg.setColumnsMsgLen(msgContent.getText().toString().length()
				+ "");
		msg.setColumnsMsgContent(msgContent.getText().toString());
		msg.setColumnsCrc("0");
		msg.setColumnsMsgFlag("1");
		long id = operation.insert(msg);
		operation.close();		
	}

	
	protected void onStart() {
		super.onStart();
		try {
			manager.addBDEventListener(fkilistener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		
		addressBook.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				//调用Android通讯录
				Intent intent = new Intent();
				intent.setClass(mContext, BDContactActivity.class);
				intent.setData(BDContactColumn.CONTENT_URI);
				MsgZhuanFaActivity.this.startActivityForResult(intent, REQUEST_CONTACT);
			}
		});
		manageMessage.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				createMessageWordsDialog();
			}
		});

	}
	
	protected void onResume() {
		super.onResume();
	}

	
	protected void onPause() {
		super.onPause();
	}


	
	protected void onStop() {
		super.onStop();
		try {
			manager.removeBDEventListener(fkilistener);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	protected void onDestroy() {
		super.onDestroy();
		if(smsCursor!=null){
			smsCursor.close();
		}
		if(smsOperation!=null){
		   smsOperation.close();	
		}
		timeManager.unRegisterBDTimeFreqListener(MsgZhuanFaActivity.class.getSimpleName());
	}

	public void createMessageWordsDialog() {
		MessgeUsualOperation oper = new MessgeUsualOperation(mContext);
		final String[] arrayWord = oper.getAllMessagesArray();
		oper.close();
		Dialog dialog = new AlertDialog.Builder(
				MsgZhuanFaActivity.this).setTitle("常用短语")
				.setItems(arrayWord, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						int startIndex=msgContent.getSelectionStart();
						String headerStr=msgContent.getText().toString().substring(0, startIndex);
						String footerStr=msgContent.getText().toString().substring(startIndex);
						String word=arrayWord[which];
						if(word.contains(".")){
							word=word.substring(word.indexOf(".")+1).replaceAll(" ", "");
						}
						msgContent.setText(headerStr+word+footerStr);
						msgContent.setSelectAllOnFocus(true);
					    msgContent.setSelection((headerStr+word).length());
						//msgContent.setText(msgContent.getText().toString()+arrayWord[which]);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}


	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CONTACT) {
			if (resultCode == RESULT_OK) {
				if (data == null) {
					return;
				}
				Uri result = data.getData();
				Log.i(TAG, "uri="+result);
			    Cursor mCursor=mContext.getContentResolver().query(result, BDContactColumn.COLUMNS, null, null, null);
                String mUserAddress="";
			    while(mCursor.moveToNext()){
                	String name=mCursor.getString(mCursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME));
                	String num=mCursor.getString(mCursor.getColumnIndexOrThrow(BDContactColumn.CARD_NUM));
                	mUserAddress=name+"("+num+")";
			    }
			    mCursor.close();
				userAddress.setText(mUserAddress);
			}
		}
	}
	
}
