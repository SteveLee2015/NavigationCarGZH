package com.novsky.map.main;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.BDMSG;
import com.novsky.map.util.BDMessageManager;
import com.novsky.map.util.BDTimeCountManager;
import com.novsky.map.util.BDTimeFreqChangedListener;
import com.novsky.map.util.DatabaseOperation;
import com.novsky.map.util.MessgeUsualOperation;
import com.novsky.map.util.Utils;

/**
 * 新建短信 功能描述: 1.点击"通讯录"按钮,选择要发送短信的人,并返回现在在收信人中。
 * 2.点击"发送"按钮,提示发送成功并跳转至短信列表，第一条显示显示发送的短信内容。 3.点击"管理"按钮，功能暂不知.
 * @author steve
 */
public class SendMsgRequestActivity extends Activity implements
		OnTabActivityResultListener, OnClickListener {

	/**
	 * 日志标识
	 */
	private final  String TAG = "SendMsgRequestActivity";
	
	/**
	 * 用户地址
	 */
	private static EditText userAddress = null;
	
	/** 
	 * 发送短信内容 
	 * */
	private static EditText msgContent = null;
	
	/**
	 *  通讯录 
	 */
	private ImageView addressBook = null;
	
	/**
	 *  短信管理 
	 */
	private ImageView manageMessage = null;
	
	/**
	 * 发送短信按钮 
	 */
	private ImageView sendMsgBtn = null;

	
	private LinearLayout msgLayout = null;

	
	private final int REQUEST_CONTACT = 1;

	/**
	 * 短信通信模式
	 */
	private int mMsgCommunicationType =1;

	
	private Context mContext = SendMsgRequestActivity.this;

	/**
	 * RDSS管理类
	 */
	private BDRDSSManager manager = null;

	/**
	 *频度按钮 
	 */
	private ImageView frequencyBtn = null;

	/**
	 * 频度数值
	 */
	private static int frequency = 0;

	/**
	 * 用户地址
	 */
	private String sendAddress = "";

	/**
	 * 传输类型
	 */
	private int mTranslateType = 0;
    
	/**
	 * 短信最大长度提示
	 */
	private TextView showNum=null;

	/**
	 * 北斗卡信息
	 */
	private BDCardInfoManager cardManager=null;
	
	/**
	 * 超频提示
	 */
	private TextView showFeqTextView =null;
	
	/**
	 * 短息内容
	 */
	private String message="";
	
	/**
	 * 剪切板管理对象
	 */
	private ClipboardManager clipboardManager=null;
	
	/**
	 * 北斗时间频率管理类对象
	 */
	private BDTimeCountManager timeInstance=null;
	
	/**
	 * 检查是否发送短信至手机
	 */
	private CheckBox  checkSendPhoneSMS=null;
	
	
	private boolean isSendPhoneSMS=false;
	
	
	/**
	 * 反馈信息监听器
	 */
	private BDEventListener fkilistener = null;
	
	
	private BDTimeFreqChangedListener timeFreqListener=
		     new BDTimeFreqChangedListener(){
		@Override
		public void onTimeChanged(int remainder_time) {
			/* 用消息传递数据 */
			Log.i(TAG, "2="+System.currentTimeMillis()+"==remainder_time="+remainder_time);
			Message msg = new Message();
			msg.arg1=remainder_time;
			mHandler.sendMessage(msg);
		}
    };

	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);
			int remainder_time=message.arg1;
			if(Utils.isStopCycleMessage){
				if(remainder_time!=0){
					showFeqTextView.setText("剩余:"+remainder_time+"秒");
					sendMsgBtn.setEnabled(true);
					sendMsgBtn.setImageResource(R.drawable.msg_cycle_send_btn);
				}else{
					showFeqTextView.setText("");
					BDMessageManager messageManager=BDMessageManager.getInstance();					
					String mMessageContenet=messageManager.getMessage();
					sendAddress=messageManager.getUserAddress();
					//Toast.makeText(mContext, "mMessageContenet="+mMessageContenet+",sendAddress="+sendAddress, Toast.LENGTH_LONG).show();
					if(isSendPhoneSMS){
						SharedPreferences pref=mContext.getSharedPreferences("BD_RELAY_STATION_PREF", mContext.MODE_PRIVATE);	
					    String address=pref.getString("BD_RELAY_STATION_NUM", "");
					    String phoneNum=sendAddress;
					    if(!address.equals("")){
					    	sendAddress=address;
					    }
					    mMessageContenet=Utils.buildSendPhoneSMS(phoneNum, mMessageContenet);
					}
					try {
						//manager.sendSMSCmdBDV21(arg0, arg1, arg2, arg3);
						manager.sendSMSCmdBDV21(sendAddress,mMsgCommunicationType,Utils.checkMsg(mMessageContenet),"N", mMessageContenet);
						Utils.COUNT_DOWN_TIME =Utils.BD_MESSAGE_FREQUNENCY;
					} catch (BDUnknownException e) {
						e.printStackTrace();
					} catch (BDParameterException e) {
						e.printStackTrace();
					}
				}
			}else{
				if(remainder_time!=0){
					showFeqTextView.setText("剩余:"+remainder_time+"秒");
					//sendMsgBtn.setEnabled(false);
					//sendMsgBtn.setImageResource(R.drawable.msg_not_send_btn);						
				}else{
					showFeqTextView.setText("");
					sendMsgBtn.setEnabled(true);
					sendMsgBtn.setImageResource(R.drawable.msg_send_btn);	
				}	
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.communication_request);
		//AgentApp.getInstance().addActivity(this);
		initUI();
		addBackground();	
	}
	
	public void addBackground(){
		/* 增加背景图片 */
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		bd.setDither(true);
		msgLayout.setBackgroundDrawable(bd);
	}

	
	public void initUI(){
		checkSendPhoneSMS=(CheckBox)this.findViewById(R.id.checkSendPhoneSMS);
		SharedPreferences pref=mContext.getSharedPreferences("BD_RELAY_STATION_PREF", mContext.MODE_PRIVATE);	
        boolean checked=pref.getBoolean("BD_RELAY_STATUS", false);
        checkSendPhoneSMS.setChecked(checked);
        isSendPhoneSMS=checked;
		fkilistener = new BDResponseListener(mContext);
		manager=BDRDSSManager.getDefault(mContext);
		timeInstance=BDTimeCountManager.getInstance();
		clipboardManager=(ClipboardManager)getSystemService(this.CLIPBOARD_SERVICE);		
		userAddress = (EditText) this.findViewById(R.id.msg_user_address_edittxt);
		msgContent = (EditText) this.findViewById(R.id.bd_msg_content_edittxt);
		addressBook = (ImageView) this.findViewById(R.id.msg_user_address_book);
		manageMessage = (ImageView)this.findViewById(R.id.msg_manager_imageview);
		msgLayout = (LinearLayout) this.findViewById(R.id.bd_msg_layout);
		sendMsgBtn = (ImageView) this.findViewById(R.id.bd_msg_send_btn);
		frequencyBtn = (ImageView) this.findViewById(R.id.bd_msg_feq_btn);
		cardManager=BDCardInfoManager.getInstance();
		showFeqTextView =(TextView) this.findViewById(R.id.bd_msg_feq_textview);
		showNum=(TextView)this.findViewById(R.id.show_msg_num);
		showNum.setText("当前输入：0/"+Utils.getMessageMaxLength()+"bit");
		frequencyBtn.setOnClickListener(this);
		manageMessage.setOnClickListener(this);
		addressBook.setOnClickListener(this);
		sendMsgBtn.setOnClickListener(this);
		//if(Utils.BD_MESSAGE_FREQUNENCY>0){
		BDMessageManager messageManager=BDMessageManager.getInstance();
		msgContent.setText(messageManager.getMessage());
		userAddress.setText(messageManager.getUserAddress());
		//sendMsgBtn.setEnabled(false);
		//}
		userAddress.setOnLongClickListener(new OnLongClickListener(){
			@SuppressLint("NewApi")
			@Override
			public boolean onLongClick(View arg0) {
				int startIndex=userAddress.getSelectionStart();
				userAddress.requestFocus();
				if(!userAddress.getText().toString().equals("")&&startIndex<userAddress.getText().toString().length()){
					AlertDialog.Builder dialog=new AlertDialog.Builder(SendMsgRequestActivity.this.getParent());
					dialog.setMessage(userAddress.getText().toString());
					dialog.setPositiveButton("复制北斗SIM卡号", new DialogInterface.OnClickListener() {					
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ClipData data=ClipData.newPlainText("发短信北斗SIM卡号", userAddress.getText().toString());
							clipboardManager.setPrimaryClip(data);
							Toast.makeText(mContext, "已复制到剪贴板!", Toast.LENGTH_SHORT).show();
						}
					});
					dialog.show();
					return true;
				}else{
					return false;
				}	
		    }			
		});
		msgContent.setOnLongClickListener(new OnLongClickListener(){
			@SuppressLint("NewApi")
			@Override
			public boolean onLongClick(View arg0) {	
			   int startIndex=msgContent.getSelectionStart();
			   msgContent.requestFocus();
			   if(!msgContent.getText().toString().equals("")&&startIndex<msgContent.getText().toString().length()){
				    AlertDialog.Builder dialog=new AlertDialog.Builder(SendMsgRequestActivity.this.getParent());				
					dialog.setPositiveButton("复制北斗短报文内容!", new DialogInterface.OnClickListener() {					
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ClipData data=ClipData.newPlainText("短信内容", msgContent.getText().toString());
							clipboardManager.setPrimaryClip(data);
							Toast.makeText(mContext, "已复制到剪贴板!", Toast.LENGTH_SHORT).show();
						}
					});
					dialog.show();
					return true;
			   }else{
				   return false;
			   }
			}
		});
		
		checkSendPhoneSMS.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean check) {
				   SharedPreferences pref=mContext.getSharedPreferences("BD_RELAY_STATION_PREF", mContext.MODE_PRIVATE);	
	               pref.edit().putBoolean("BD_RELAY_STATUS", check).commit();
	               isSendPhoneSMS=check;
	               userAddress.setText("");	
	               if(check){
					   userAddress.setHint("请输入手机号码!");
			       }else{
			       	   userAddress.setHint("请输入北斗卡号!");
			       }
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		timeInstance.registerBDTimeFreqListener(SendMsgRequestActivity.class.getSimpleName(),timeFreqListener);
 	    try {
			manager.addBDEventListener(fkilistener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		msgContent.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;
			private Toast mToast=null;
			private boolean isOver=false;
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				temp = s;
			}
			public void afterTextChanged(Editable s) {
				/*判断写入的汉字的个数和数字字母个数,汉字个数*14 数字字母个数×4*/
				int num=Utils.checkStrBits(s.toString());
				int flag=Utils.checkMsg(s.toString());
				int temp=Utils.getMessageMaxLength();
				//if(flag==2){
				//	temp=(Utils.getMessageMaxLength()*2)/3;
				//}
				showNum.setText("当前输入："+num+"/"+temp+"bit");
				selectionStart = msgContent.getSelectionStart();
				selectionEnd = msgContent.getSelectionEnd();
				if (num> temp) {
					if(!isOver){
						mToast=Toast.makeText(mContext, "输入超过最长字符，将不能发送短信!",Toast.LENGTH_SHORT);
						mToast.show();
						isOver=true;
					}
					//sendMsgBtn.setEnabled(false);
					//sendMsgBtn.setImageResource(R.drawable.msg_not_send_btn);
				}else{
					if(isOver){
						mToast.cancel();
						isOver=false;
					}
					if(!sendMsgBtn.isEnabled()){
						//sendMsgBtn.setEnabled(true);
						//sendMsgBtn.setImageResource(R.drawable.msg_send_btn);
					}
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			manager.removeBDEventListener(fkilistener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		//BDMessageCurrentTab.getInstance().setCurrentIndex(0);
		BDMessageManager bdmanager=BDMessageManager.getInstance();
		if(userAddress.getText()!=null){
			bdmanager.setUserAddress(userAddress.getText().toString());
		}
		if(msgContent.getText()!=null){
		    bdmanager.setMessage(msgContent.getText().toString());	
		}
	}

	@Override
	public void onTabActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CONTACT) {
			if (resultCode == RESULT_OK) {
				if (data == null) {
					return;
				}
				Uri result = data.getData();
			    Cursor cursor=mContext.getContentResolver().query(result, BDContactColumn.COLUMNS, null, null, null);
                String mUserAddress="";
			    if(cursor.moveToFirst()){
                	String name=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME));
                	String num=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_NUM));
                	mUserAddress=name+"("+num+")";
			    }
			    cursor.close();
			    userAddress.setText(mUserAddress);
			  }
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.bd_msg_feq_btn:{
//				LayoutInflater inflater = getLayoutInflater();
//				View layout = inflater.inflate(R.layout.custom_fequecy_msg,null);
//				final EditText msgfeqbtn = (EditText) layout.findViewById(R.id.etname);
//				msgfeqbtn.setText(Utils.BD_MESSAGE_FREQUNENCY+"");
//				final AlertDialog dialog = new AlertDialog.Builder(
//						SendMsgRequestActivity.this.getParent())
//						.setTitle("设置频度").setView(layout)	
//						.setPositiveButton("确定",new DialogInterface.OnClickListener() {
//									@Override
//									public void onClick(DialogInterface dialog,
//											int which) {
//										boolean isTrue=false;
//										try {
//											isTrue = cardManager.checkBDSimCard();
//										} catch (BDParameterException e) {
//											e.printStackTrace();
//										}
//										if (!isTrue) {
//											Toast.makeText(SendMsgRequestActivity.this.getParent(),
//													SendMsgRequestActivity.this.getParent().getResources().getString(R.string.have_not_bd_sim),
//													Toast.LENGTH_SHORT).show();
//											return;
//										}
//
//										if (msgfeqbtn == null|| "".equals(msgfeqbtn.getText().toString())) {
//											Toast.makeText(SendMsgRequestActivity.this.getParent(),
//													SendMsgRequestActivity.this.getParent().getResources().getString(R.string.bd_fequency_no_content),
//													Toast.LENGTH_SHORT).show();
//											return;
//										}
//										frequency = Integer.valueOf(msgfeqbtn.getText().toString());
//									    if(frequency>0){
//											if (frequency <= cardManager.getCardInfo().mSericeFeq) {
//													Toast.makeText(SendMsgRequestActivity.this.getParent(),
//															"报告频度必须大于"+ cardManager.getCardInfo().mSericeFeq+ "秒",Toast.LENGTH_SHORT).show();
//													return;
//											} else{
//											       Utils.BD_MESSAGE_FREQUNENCY=frequency;
//											}
//									    }else{
//									    	Utils.BD_MESSAGE_FREQUNENCY=0;
//									    }
//									}
//								})
//
//						.setNegativeButton("取消",
//								new DialogInterface.OnClickListener() {
//									@Override
//									public void onClick(DialogInterface dialog,int which) {
//										dialog.dismiss();
//									}
//								}).show();
//				dialog.setCancelable(false);
				break;
			}
			case R.id.msg_manager_imageview:{
				createMessageWordsDialog();
			   break;
			}
			
			/*调用通讯录*/
			case R.id.msg_user_address_book:{
				Intent intent = new Intent();
				intent.setClass(mContext, BDContactActivity.class);
				intent.setData(BDContactColumn.CONTENT_URI);
				//TabSwitchActivityData mInstance = TabSwitchActivityData.getInstance();
				//mInstance.setTabFlag(2);
				SendMsgRequestActivity.this.getParent().startActivityForResult(intent, REQUEST_CONTACT);
				break;
			}
			
			case R.id.bd_msg_send_btn:{
				if(!Utils.isStopCycleMessage){
					boolean isCheck=false;
					try{
						isCheck = cardManager.checkBDSimCard();
					} catch (BDParameterException e1) {
						e1.printStackTrace();
					}
					if (!isCheck) {
						Toast.makeText(mContext,mContext.getResources().getString(R.string.have_not_bd_sim),Toast.LENGTH_SHORT).show();
						return;
					}
					/* 发送通讯申请 */
					boolean isSend = true;
					try {
						message= msgContent.getText().toString();
						sendAddress = userAddress.getText().toString();
						/* 判断用户地址是否为空! */
						if (sendAddress.equals("")) {
							Toast.makeText(mContext,mContext.getResources().getString(R.string.bd_address_no_content),Toast.LENGTH_SHORT).show();
							return;
						}
						/* 判断短信内容是否为空 */
						if (message.equals("")) {
							Toast.makeText(mContext,mContext.getResources().getString(R.string.bd_msg_no_content),Toast.LENGTH_SHORT).show();
							return;
						}
						if (sendAddress.contains("(")) {
							sendAddress = sendAddress.substring(sendAddress.lastIndexOf("(") + 1,sendAddress.lastIndexOf(")"));
						}
						mTranslateType=Utils.checkMsg(message);
						BDMessageManager messageManager=BDMessageManager.getInstance();
						messageManager.setMessage(message);
						messageManager.setMsgContentType(mMsgCommunicationType);
						messageManager.setUserAddress(sendAddress);
						
						
						if(isSendPhoneSMS){
							//获得设置的中继站的号码
							SharedPreferences pref=mContext.getSharedPreferences("BD_RELAY_STATION_PREF", mContext.MODE_PRIVATE);	
						    String address=pref.getString("BD_RELAY_STATION_NUM", "");
						    //手机地址
						    String phoneNum=sendAddress;
						    message=Utils.buildSendPhoneSMS(phoneNum, message);
						    if(!address.equals("")){
						    	sendAddress=address;
						    }else{
						       Utils.createAlertDialog(SendMsgRequestActivity.this.getParent(), "提示", "请先设置'系统'->'中继站管理'!", false, 
							        new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int arg1) {
											dialog.dismiss();
										}
							   }, "取消").show();
						       return;
						   }
						}
						if (Utils.BD_MESSAGE_FREQUNENCY> 0) {		
							Utils.isStopCycleMessage=true;
							Utils.COUNT_DOWN_TIME=Utils.BD_MESSAGE_FREQUNENCY;
							Toast.makeText(mContext, "设置循环发送短信成功!", Toast.LENGTH_SHORT).show();
							sendMsgBtn.setImageResource(R.drawable.msg_cycle_send_btn);
	                    }else{
	                    	sendMsgBtn.setImageResource(R.drawable.msg_not_send_btn);
	                        Utils.COUNT_DOWN_TIME=cardManager.getCardInfo().mSericeFeq; 
	                    }
						manager.sendSMSCmdBDV21(sendAddress,mMsgCommunicationType,Utils.checkMsg(message),"N", message);
					} catch (Exception e) {
						isSend = false;
						e.printStackTrace();
					}
					saveToDatabase();
				}else{
					Utils.isStopCycleMessage=false;
					sendMsgBtn.setImageResource(R.drawable.msg_send_btn);
				}
				break;
			}
			default:
				break;
		}	
	}
	/**
	 * 保存至数据库
	 */
	public void saveToDatabase(){
		/* 在数据库中保存该数据 */
		DatabaseOperation operation = new DatabaseOperation(SendMsgRequestActivity.this);
		BDMSG msg = new BDMSG();
		String address = userAddress.getText().toString();
		msg.setColumnsUserAddress(address);
		msg.setColumnsMsgType(mMsgCommunicationType + "," + mTranslateType);
		msg.setColumnsSendAddress(address);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		msg.setColumnsSendTime(sdf.format(new Date()));
		msg.setColumnsMsgLen(msgContent.getText().toString().length()+ "");
		msg.setColumnsMsgContent(msgContent.getText().toString());
		msg.setColumnsCrc("0");
		msg.setColumnsMsgFlag("1");
		long id = operation.insert(msg);
		operation.close();
	}	

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	public void createMessageWordsDialog() {
		MessgeUsualOperation oper = new MessgeUsualOperation(mContext);
		final String[] arrayWord = oper.getAllMessagesArray();
		oper.close();
		Dialog dialog = new AlertDialog.Builder(
				SendMsgRequestActivity.this.getParent()).setTitle("常用短语")
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
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}
}
