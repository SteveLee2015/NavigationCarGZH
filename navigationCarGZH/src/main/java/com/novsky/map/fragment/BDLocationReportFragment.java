package com.novsky.map.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.BDEventListener;
import android.location.BDLocationReport;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.utils.DateUtils;
import com.novsky.map.main.BDContactActivity;
import com.novsky.map.main.BDResponseListener;
import com.novsky.map.main.CustomListView;
import com.novsky.map.main.CustomLocationManager;
import com.novsky.map.main.CycleLocationReportService;
import com.novsky.map.main.CycleLocationReportServiceRD;
import com.novsky.map.main.CycleLocationReportServiceRN;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.BDTimeCountManager;
import com.novsky.map.util.BDTimeFreqChangedListener;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 位置报送
 * @author steve
 */
public class BDLocationReportFragment extends Fragment implements OnClickListener,OnCheckedChangeListener{	
	
	/**
	 * 日志标识
	 */
	private static final String TAG = "ReportActivity";
	/**
	 *短信通信类型
	 */
	private static int msgComType =1;
	/**
	 * 服务频度
	 */
	private int frequency = 0; 
	/**
	 * 用户地址
	 */
	private String mUserAddress = "";
	
	private SharedPreferences reportSwitch = null;
	/**
	 * RDSS管理类
	 */
	private BDCommManager mananger = null;
	/**
	 *用户地址编辑框 
	 */
	private EditText addressEditText = null;
	
	private LinearLayout layout = null;
	/**
	 * 选择用户的ImageView 
	 */
	private ImageView selectAddress = null;
	/**
	 * 服务去频度编辑框
	 */
	private EditText frequncyEditText = null;
	/**
	 * 发送按钮
	 */
	private Button sendBtn = null;
	/**
	 * 北斗卡管理类
	 */
	private BDCardInfoManager  cardManager=null;
	/**
	 * 自定义LocationManager类
	 */
	private CustomLocationManager locationManager=null;
	/**
	 * 是否启动循环策略
	 */
	private CheckBox   checkBox=null;
	
	
	private ProgressDialog progressDialog=null;
	
	
	private BDTimeCountManager timeManager=null;
	/**
	 * 反馈信息监听器
	 */
	private BDEventListener fkilistener = null;

	BDRNSSLocation rnsslocation = null;

	/**
	 * 定义访问模式为私有模式
	 */
	public static int MODE = Context.MODE_PRIVATE;

	/**
	 * 设置保存时的文件的名称
	 */
	public static final String PREFERENCE_NAME = "REPORT_MODEL_ACTIVITY";
	public static final String REPORT_MODEL = "REPORT_MODEL";
	public static final String REPORT_TIANXIAN_VALUE = "REPORT_TIANXIAN_VALUE";

	private BDRNSSLocationListener mBDRNSSLocationListener=new BDRNSSLocationListener(){
		@Override
		public void onLocationChanged(BDRNSSLocation arg0) {
			rnsslocation = arg0;
		}

		@Override
		public void onProviderDisabled(String arg0) {

		}

		@Override
		public void onProviderEnabled(String arg0) {

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

		}
	};

	
	private BDTimeFreqChangedListener timeFreqListener=new BDTimeFreqChangedListener(){
			@Override
			public void onTimeChanged(int remainder_time){
				/*用消息传递数据*/
				Message msg = new Message();
				msg.arg1=remainder_time;
				mHandler.sendMessage(msg);
			}
    };
	   
	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int remainder_time=msg.arg1;
			int frequ=reportSwitch.getInt("REPORT_FREQUENCY",0);
			//循环报位
			if(frequ>0){
				if(sendBtn!=null){
				    sendBtn.setText("结束报位("+remainder_time+"秒)");	
				}
			}
			//单次报位
			else{
				if(remainder_time!=0){
					if(sendBtn!=null&&getActivity()!=null){
						if(frequency > 0 && checkBox.isChecked()) {
							if (frequency - cardManager.getCardInfo().mSericeFeq > remainder_time) {
								Utils.COUNT_DOWN_TIME = 0;
							} else {
								Utils.COUNT_DOWN_TIME = cardManager.getCardInfo().mSericeFeq - (frequency - remainder_time);
								frequency = 0;
							}
						}
						sendBtn.setText("确  定("+remainder_time+"秒)");
					}
				}else{
					sendBtn.setText("确  认");
				}
			}
		}
	};
	private CustomListView mySpinner;

	private int FLAG;

	private  Context mContext;
	private LinearLayout tianxianSettings;
	private EditText tianxianValue;
	private LinearLayout paramControl;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		mContext = getActivity();
		View rooter=inflater.inflate(R.layout.activity_location_report,null);
		initUI(rooter);
		initData();
		return rooter;
	}

	private void initData() {


		SharedPreferences shareTianxian = mContext.getSharedPreferences(PREFERENCE_NAME, MODE);
		float tianxian = shareTianxian.getFloat(REPORT_TIANXIAN_VALUE,45.0f);
		tianxianValue.setText(tianxian+"");

		mySpinner.setData(new String[]{"RD位置报告","RN位置报告","自定义位置报告"});
		mySpinner.setOnCustomListener(new OnCustomListListener(){
			public void onListIndex(int index) {
				if(index==0){
					FLAG = 0;
					tianxianSettings.setVisibility(View.VISIBLE);
				}else if(index==1){
					FLAG = 1;
					tianxianSettings.setVisibility(View.GONE);
				}else if(index==2){
					FLAG = 2;
					tianxianSettings.setVisibility(View.GONE);
				}
			}
		});

		SharedPreferences share = mContext.getSharedPreferences(PREFERENCE_NAME, MODE);
		FLAG=share.getInt(REPORT_MODEL,0);
		int index=0;
		if(FLAG== 0){
			index=0;
			tianxianSettings.setVisibility(View.VISIBLE);
		}else if(FLAG== 1){
			index=1;
			tianxianSettings.setVisibility(View.GONE);
		}else if(FLAG== 2){
			index=2;
			tianxianSettings.setVisibility(View.GONE);
		}
		mySpinner.setIndex(index);
	}

	@Override
	public void onResume(){
		super.onResume();
		locationManager=new CustomLocationManager(getActivity());
		cardManager=BDCardInfoManager.getInstance();
		SharedPreferences sharedPreferences=getActivity().getSharedPreferences("BD_REPORT_CONTACT_PREF",getActivity().MODE_PRIVATE);
	    String contactName=sharedPreferences.getString("CONTACT_NAME", mUserAddress);
	    if(contactName!=null&&!"".equals(contactName)){
	    	addressEditText.setText(contactName);
	    }
		/*保存循环位置报告的状态*/
		reportSwitch = getActivity().getSharedPreferences("LOCATION_REPORT_SWITCH", 0);
		String mTempUserAddress= reportSwitch.getString("USER_ADDRESS", "");
		int mTempFrequency=reportSwitch.getInt("REPORT_FREQUENCY", 0);
		if (mTempUserAddress != null&&!"".equals(mTempUserAddress)){
			addressEditText.setText(mTempUserAddress);
		}
		if(mTempFrequency>0){
			boolean isStart=Utils.isServiceRunning(getActivity(), "com.novsky.map.main.CycleLocationReportService");
			if(isStart){
				sendBtn.setText(getActivity().getResources().getString(R.string.location_report_cycle));
			}else{
				sendBtn.setText(getActivity().getResources().getString(R.string.common_submit_btn));
			}
			frequncyEditText.setText(mTempFrequency+"");
			layout.setVisibility(View.VISIBLE);
			checkBox.setChecked(true);
		}
		selectAddress.setOnClickListener(this);
		sendBtn.setOnClickListener(this);
		checkBox.setOnCheckedChangeListener(this);
		timeManager.registerBDTimeFreqListener(BDLocationReportFragment.class.getSimpleName()
				,timeFreqListener);
		locationManager.initLocation();
		try {
			mananger.addBDEventListener(fkilistener,mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}
	
	public void initUI(View view){
		cardManager = BDCardInfoManager.getInstance();
		fkilistener = new BDResponseListener(getActivity());
	    timeManager=BDTimeCountManager.getInstance();
		mananger = BDCommManager.getInstance(getActivity());
		addressEditText = (EditText) view.findViewById(R.id.bdloc_userAddress_et);
		tianxianValue = (EditText) view.findViewById(R.id.tianxian_height_value);
		selectAddress = (ImageView) view.findViewById(R.id.bdloc_linker);
		mySpinner =(CustomListView) view.findViewById(R.id.bd_report_model);
		frequncyEditText = (EditText) view.findViewById(R.id.bdloc_report_feq);
		sendBtn = (Button) view.findViewById(R.id.bdloc_report_submit_btn);
		layout = (LinearLayout) view.findViewById(R.id.set_cycle_loc);
		paramControl = (LinearLayout) view.findViewById(R.id.ll_paramControl);
		tianxianSettings = (LinearLayout) view.findViewById(R.id.ll_tianxian_setting);
		checkBox=(CheckBox)view.findViewById(R.id.bdloc_report_checkbox);
        progressDialog=new ProgressDialog(getActivity());
		progressDialog.setTitle("RDSS定位");
		progressDialog.setMessage("正在定位中...");
	}
	

	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeLocation();
		try {
			timeManager.unRegisterBDTimeFreqListener(BDLocationReportFragment.class.getSimpleName());
			mananger.removeBDEventListener(fkilistener,mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.bdloc_linker:{
				Intent intent = new Intent();
				intent.setClass(getActivity(), BDContactActivity.class);
				intent.setData(BDContactColumn.CONTENT_URI);
				Utils.BD_MANAGER_PAGER_INDEX=4;
				startActivityForResult(intent,getActivity().RESULT_FIRST_USER);
			    break;
			}
			case R.id.bdloc_report_submit_btn:{


				SharedPreferences share = mContext.getSharedPreferences(PREFERENCE_NAME, MODE);
				share.edit().putInt(REPORT_MODEL, FLAG).commit();

				boolean isStart=Utils.isServiceRunning(getActivity(), CycleLocationReportService.class.getName());
				boolean isStartRN=Utils.isServiceRunning(getActivity(), CycleLocationReportServiceRN.class.getName());
				boolean isStartRD=Utils.isServiceRunning(getActivity(), CycleLocationReportServiceRD.class.getName());

				if(isStart){
					sendBtn.setText(getActivity().getResources().getString(R.string.common_submit_btn));
					reportSwitch.edit().putInt("REPORT_FREQUENCY", 0).commit();
					reportSwitch.edit().putString("USER_ADDRESS", "").commit();
					Intent mIntent=new Intent();
					mIntent.setClass(getActivity(), CycleLocationReportService.class);
					getActivity().stopService(mIntent);
					paramControl.setClickable(true);
				}else if (isStartRN){
					sendBtn.setText(getActivity().getResources().getString(R.string.common_submit_btn));
					reportSwitch.edit().putInt("REPORT_FREQUENCY", 0).commit();
					reportSwitch.edit().putString("USER_ADDRESS", "").commit();
					Intent mIntent=new Intent();
					mIntent.setClass(getActivity(), CycleLocationReportServiceRN.class);
					getActivity().stopService(mIntent);
					paramControl.setClickable(true);

				}else if (isStartRD){
					sendBtn.setText(getActivity().getResources().getString(R.string.common_submit_btn));
					reportSwitch.edit().putInt("REPORT_FREQUENCY", 0).commit();
					reportSwitch.edit().putString("USER_ADDRESS", "").commit();
					Intent mIntent=new Intent();
					mIntent.setClass(getActivity(), CycleLocationReportServiceRD.class);
					getActivity().stopService(mIntent);
					paramControl.setClickable(true);
				}


				else{
					/*判断是否安装北斗SIM卡*/
					if(!Utils.checkBDSimCard(getActivity()))return;
					mUserAddress = addressEditText.getText().toString();
					frequency =Integer.valueOf(frequncyEditText.getText().toString());
					/*判断用户地址是否为空*/
					if (mUserAddress == null || ("").equals(mUserAddress)){
						Toast.makeText(getActivity().getApplicationContext(),getActivity().getResources().getString(R.string.bd_address_no_content),Toast.LENGTH_SHORT).show();
						return;
					}
					/*判断频率是否为空*/
					if (frequncyEditText == null || "".equals(frequncyEditText.getText())){
						Toast.makeText(getActivity().getApplicationContext(),getActivity().getResources().getString(R.string.bd_fequency_no_content),Toast.LENGTH_SHORT).show();
						return;
					}
					/*判断频度是否大于当前北斗SIM卡*/
	                if(checkBox.isChecked()){
						if (frequency<= cardManager.getCardInfo().mSericeFeq) {
							Toast.makeText(getActivity().getApplicationContext(),"报告频度必须大于" + cardManager.getCardInfo().mSericeFeq+"秒", Toast.LENGTH_SHORT).show();
							return;
						}
					}
	                /*判断用户地址是否是name(address)格式*/
					if (mUserAddress.contains("(")){
						mUserAddress = mUserAddress.substring(mUserAddress.lastIndexOf("(") + 1,mUserAddress.lastIndexOf(")"));
					}
					BDLocationReport locationReport = addGPSLocationToBDLocationReport();


					if(locationReport!=null){
							if(checkBox.isChecked()&&frequency>0){									
								sendBtn.setText(getActivity().getResources().getString(R.string.location_report_cycle));
								reportSwitch.edit().putInt("REPORT_FREQUENCY", frequency).commit();
								reportSwitch.edit().putString("USER_ADDRESS", mUserAddress).commit();
								Utils.COUNT_DOWN_TIME=frequency;

								switch (FLAG){
									case 0:{
										Intent mIntent=new Intent();
										mIntent.setClass(getActivity(), CycleLocationReportServiceRD.class);
										getActivity().startService(mIntent);
										paramControl.setClickable(false);
										break;
									}
									case 1:{
										Intent mIntent=new Intent();
										mIntent.setClass(getActivity(), CycleLocationReportServiceRN.class);
										getActivity().startService(mIntent);
										paramControl.setClickable(false);
										break;
									}
									case 2:{
										Intent mIntent=new Intent();
										mIntent.setClass(getActivity(), CycleLocationReportService.class);
										getActivity().startService(mIntent);
										paramControl.setClickable(false);
										break;
									}
								}

							}else{
								 reportSwitch.edit().putInt("REPORT_FREQUENCY", 0).commit();
								 reportSwitch.edit().putString("USER_ADDRESS", mUserAddress).commit();
								 Utils.COUNT_DOWN_TIME=cardManager.getCardInfo().mSericeFeq;
							}


							//判断 FLAG
							switch (FLAG){
								case 0:{//rd
									// 读取天线高度
									String tianxian2 = tianxianValue.getText().toString().trim();
									float tianxianNum = Float.parseFloat(tianxian2);
									//保留一位小数
									tianxian2 = String.format("%.1f", tianxianNum);
									tianxianNum = Float.parseFloat(tianxian2);

									if (tianxian2 == null || "".equals(tianxian2)){
										Toast.makeText(getActivity().getApplicationContext(),getActivity().getResources().getString(R.string.report_tianxian_no_content),Toast.LENGTH_SHORT).show();
										return;
									}

									// 保存天线高度 到sp中
									SharedPreferences shareT = mContext.getSharedPreferences(PREFERENCE_NAME, MODE);
									shareT.edit().putFloat(REPORT_TIANXIAN_VALUE,tianxianNum).commit();
									//FLAG=share.getFloat(REPORT_TIANXIAN_VALUE,(Float)tianxianNum);

									try {
										mananger.sendLocationReport2CmdBDV21(locationReport.getUserAddress(), BDRDSSManager.HeightFlag.COMMON_USER,tianxianNum,0);
									} catch (BDUnknownException e) {
										e.printStackTrace();
									} catch (BDParameterException e) {
										e.printStackTrace();
									}

									break;
								}
								case 1:{//rn

									try {
										double longitude = locationReport.getLongitude();
										double latitude = locationReport.getLatitude();
										//重现编辑
										//locationReport.setLatitude(latitude*100);
										//locationReport.setLongitude(longitude*100);

										double latitudeNew = Utils.changeLonLatMinuteToDegreeReverse(locationReport.mLatitude);
										double longitudeNew = Utils.changeLonLatMinuteToDegreeReverse(locationReport.mLongitude);

										locationReport.setLatitude(latitudeNew*100);
										locationReport.setLongitude(longitudeNew*100);

										mananger.sendLocationReport1CmdBDV21(locationReport);
									} catch (BDUnknownException e) {
										e.printStackTrace();
									} catch (BDParameterException e) {
										e.printStackTrace();
									} finally {
										//double longitude = locationReport.getLongitude();
										//double latitude = locationReport.getLatitude();
										//重现编辑
										//locationReport.setLatitude(latitude/100);
										//locationReport.setLongitude(longitude/100);
									}
									break;
								}
								case 2:{//自定义

									try{
										mananger.sendSMSCmdBDV21(locationReport.getUserAddress(), msgComType,1,"N", Utils.buildeLocationReport1(locationReport));
									} catch (BDUnknownException e){
										e.printStackTrace();
									} catch (BDParameterException e){
										e.printStackTrace();
									}
									break;
								}
							}
					}else{
							Toast.makeText(getActivity().getApplicationContext(), "未获得当前位置信息!",Toast.LENGTH_LONG).show();
					}
				 }
				break;
			}
			default:
				break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		layout.setVisibility(isChecked?View.VISIBLE:View.GONE);
	}
	
	/**
	 * 把GPS定位的值封装到BDLocationReport实体类中
	 */
	private BDLocationReport addGPSLocationToBDLocationReport(){

			if(rnsslocation!=null){
				BDLocationReport report = new BDLocationReport();
				report.setHeightUnit("M");
				report.setLongitude(rnsslocation.getLongitude());
				report.setLongitudeDir(rnsslocation.getExtras().getString("londir"));
				report.setHeight(rnsslocation.getAltitude());
				report.setLatitude(rnsslocation.getLatitude());
				report.setLatitudeDir(rnsslocation.getExtras().getString("latdir"));
				report.setMsgType(1);
				//report.setReportFeq(Integer.valueOf(frequency));
				report.setReportFeq(0);
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss)");
//				String time = sdf.format(new Date());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date = sdf.format(new Date());
				long time = rnsslocation.getTime();
				String rnDateTimeStr = DateUtils.getRNDateTimeStr(time);
				report.setReportTime(rnDateTimeStr);
				report.setUserAddress(mUserAddress);
				return report;
			}else{
				return null;
			}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == getActivity().RESULT_FIRST_USER){
			if (resultCode == getActivity().RESULT_OK){
				if (data == null) {
					return;
				}
				Uri result = data.getData();
			    Cursor cursor=getActivity().getContentResolver().query(result, BDContactColumn.COLUMNS, null, null, null);
                String mUserAddress="";
			    if(cursor.moveToFirst()){
                	final String name=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME));
                	final String num=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_NUM));
                	mUserAddress=name+"("+num+")";
					new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
							addressEditText.setText(num);
							addressEditText.postInvalidate();
						}
					},500);

			    }
			    cursor.close();
			    SharedPreferences sharedPreferences=getActivity().getSharedPreferences("BD_REPORT_CONTACT_PREF",getActivity().MODE_PRIVATE);
			    sharedPreferences.edit().putString("CONTACT_NAME", mUserAddress).commit();
			}
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if(hidden){
			try {
				timeManager.unRegisterBDTimeFreqListener(BDLocationReportFragment.class.getSimpleName());
				mananger.removeBDEventListener(fkilistener,mBDRNSSLocationListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		}else{
			timeManager.registerBDTimeFreqListener(BDLocationReportFragment.class.getSimpleName()
					,timeFreqListener);
			try {
				mananger.addBDEventListener(fkilistener,mBDRNSSLocationListener);
			} catch (BDParameterException e) {
				e.printStackTrace();
			} catch (BDUnknownException e) {
				e.printStackTrace();
			}
		}
		super.onHiddenChanged(hidden);
	}
}
