package com.novsky.map.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.BDEventListener;
import android.location.BDLocationReport;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.mapabc.android.activity.R;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.BDLocationTabManager;
import com.novsky.map.util.BDTimeCountManager;
import com.novsky.map.util.BDTimeFreqChangedListener;
import com.novsky.map.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 位置报送
 * @author steve
 */
public class BDLocationReportActivity extends Activity implements 
                                  OnClickListener,
                                  OnCheckedChangeListener,
                                  OnTabActivityResultListener{	
	/**
	 * 日志标识
	 */
	private static final String TAG = "ReportActivity";
	
	/**
	 *上下文对象 
	 */
	private Context mContext = this;
	
	/**
	 * 通讯录标识
	 */
	private final int REQUEST_CONTACT = 1;
	
	/**
	 * 位置报告类型
	 */
	//private static int LOCATION_REPORT_TYPE = 0;
	
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
	
	/**
	 * 位置报告类型
	 */
	//private CustomListView reportTypeListView = null;
	
	
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
	
	/**
	 * 定位设置图标
	 */
	private ImageView locationSettingBtn = null;

	/**
	 * 位置报告图标
	 */
	private ImageView locationReportBtn = null;

	/**
	 * 友邻位置图标
	 */
	private ImageView locationFriendsBtn = null;

	/**
	 * 中间菜单图标
	 */
	private ImageView menuLoc = null;

	/**
	 * TabHost对象
	 */
	private TabHost tabHost = BDLocationPortActivity.tabHost;

	/**
	 * tab管理类
	 */
	private BDLocationTabManager tabManager = null;
	
	
	private BDTimeCountManager timeManager=null;
	
	/**
	 * 反馈信息监听器
	 */
	private BDEventListener fkilistener = null;
	
	
	private BDTimeFreqChangedListener timeFreqListener=
		     new BDTimeFreqChangedListener(){
			@Override
			public void onTimeChanged(int remainder_time) {
				Message msg = new Message();
				msg.arg1=remainder_time;
				mHandler.sendMessage(msg);
			}
    };
	   
	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {	super.handleMessage(msg);
		int remainder_time=msg.arg1;
		int frequency=reportSwitch.getInt("REPORT_FREQUENCY",0);
		//循环报位
		if(frequency>0){
			if(sendBtn!=null){
			    sendBtn.setText("结束报位("+remainder_time+"秒)");	
			}
		}
		//单次报位
		else{
			if(remainder_time!=0){
				if(sendBtn!=null&&mContext!=null){
					sendBtn.setText("确  定("+remainder_time+"秒)");
				}
			}else{
				sendBtn.setText("确  认");
			}
		}
	   }
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_report);
		initUI();
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationManager.initLocation();
		try {
			mananger.addBDEventListener(fkilistener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}

	
	public void initUI(){
		cardManager = BDCardInfoManager.getInstance();
		fkilistener = new BDResponseListener(mContext);
	    timeManager=BDTimeCountManager.getInstance();
		mananger = BDCommManager.getInstance(this);
		//reportTypeListView = (CustomListView)this.findViewById(R.id.bd_report_type_list);
		//reportTypeListView.setData(getResources().getStringArray(R.array.bd_report_type_array));
		addressEditText = (EditText) this.findViewById(R.id.bdloc_userAddress_et);
		selectAddress = (ImageView) this.findViewById(R.id.bdloc_linker);
		frequncyEditText = (EditText) this.findViewById(R.id.bdloc_report_feq);
		sendBtn = (Button) this.findViewById(R.id.bdloc_report_submit_btn);
		layout = (LinearLayout) this.findViewById(R.id.set_cycle_loc);
		checkBox=(CheckBox)findViewById(R.id.bdloc_report_checkbox);
		locationSettingBtn =(ImageView)findViewById(R.id.report_loc_set);
        locationReportBtn = (ImageView)findViewById(R.id.report_loc_report);
        locationFriendsBtn =(ImageView)findViewById(R.id.report_loc_friends);
        menuLoc =(ImageView)findViewById(R.id.report_loc_menu);
        tabManager = new BDLocationTabManager(this, tabHost);
        progressDialog=new ProgressDialog(this);
		progressDialog.setTitle("RDSS定位");
		progressDialog.setMessage("正在定位中...");
		
		locationManager=new CustomLocationManager(mContext);
		cardManager=BDCardInfoManager.getInstance();
		reportSwitch = mContext.getSharedPreferences("LOCATION_REPORT_SWITCH", 0);
		String mTempUserAddress= reportSwitch.getString("USER_ADDRESS", "");
		int mTempFrequency=reportSwitch.getInt("REPORT_FREQUENCY", 0);
		if (mTempUserAddress != null&&!"".equals(mTempUserAddress)){
			addressEditText.setText(mTempUserAddress);
		}
		if(mTempFrequency>0){
			boolean isStart=Utils.isServiceRunning(mContext, "com.novsky.map.main.CycleLocationReportService");
			if(isStart){
				sendBtn.setText(mContext.getResources().getString(R.string.location_report_cycle));
			}else{
				sendBtn.setText(mContext.getResources().getString(R.string.common_submit_btn));
			}
			frequncyEditText.setText(mTempFrequency+"");
			layout.setVisibility(View.VISIBLE);
			checkBox.setChecked(true);
		} 
		selectAddress.setOnClickListener(this);
		sendBtn.setOnClickListener(this);
		//reportTypeListView.setOnCustomListener(this);
		checkBox.setOnCheckedChangeListener(this);
		locationSettingBtn.setOnClickListener(this);
		locationReportBtn.setOnClickListener(this);
		locationFriendsBtn.setOnClickListener(this);
		menuLoc.setOnClickListener(this);
		timeManager.registerBDTimeFreqListener(BDLocationReportActivity.class.getSimpleName()
				,timeFreqListener);
	}
		
	@Override
	public void onTabActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CONTACT) {
			if (resultCode == RESULT_OK) {
				if (data == null){
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
			    addressEditText.setText(mUserAddress);
			}
		}
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
		locationManager.removeLocation();
		try {
			mananger.removeBDEventListener(fkilistener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}



	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.bdloc_linker:{
				Intent intent = new Intent();
				intent.setClass(mContext, BDContactActivity.class);
				intent.setData(BDContactColumn.CONTENT_URI);
				BDLocationReportActivity.this.getParent().startActivityForResult(intent,REQUEST_CONTACT);
			   break;
			}
			case R.id.bdloc_report_submit_btn:{
				boolean isStart=Utils.isServiceRunning(mContext, "com.novsky.map.main.CycleLocationReportService");
				if(isStart){
					sendBtn.setText(mContext.getResources().getString(R.string.common_submit_btn));
					reportSwitch.edit().putInt("REPORT_FREQUENCY", 0).commit();
					reportSwitch.edit().putString("USER_ADDRESS", "").commit();
					Intent mIntent=new Intent();
					mIntent.setClass(mContext, CycleLocationReportService.class);
					mContext.stopService(mIntent);
				}else{
					/*判断是否安装北斗SIM卡*/
					if(!Utils.checkBDSimCard(mContext))return;
					mUserAddress = addressEditText.getText().toString();
					frequency =Integer.valueOf(frequncyEditText.getText().toString());
					/*判断用户地址是否为空*/
					if (mUserAddress == null || ("").equals(mUserAddress)){
						Toast.makeText(mContext.getApplicationContext(),mContext.getResources().getString(R.string.bd_address_no_content),Toast.LENGTH_SHORT).show();
						return;
					}
					/*判断频率是否为空*/
					if (frequncyEditText == null || "".equals(frequncyEditText.getText())){
						Toast.makeText(mContext.getApplicationContext(),mContext.getResources().getString(R.string.bd_fequency_no_content),Toast.LENGTH_SHORT).show();
						return;
					}
					/*判断频度是否大于当前北斗SIM卡*/
	                if(checkBox.isChecked()){
						if (frequency<= cardManager.getCardInfo().mSericeFeq) {
							Toast.makeText(mContext.getApplicationContext(),"报告频度必须大于" + cardManager.getCardInfo().mSericeFeq+"秒", Toast.LENGTH_SHORT).show();
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
								sendBtn.setText(mContext.getResources().getString(R.string.location_report_cycle));
								reportSwitch.edit().putInt("REPORT_FREQUENCY", frequency).commit();
								reportSwitch.edit().putString("USER_ADDRESS", mUserAddress).commit();
								Utils.COUNT_DOWN_TIME=frequency;
								Intent mIntent=new Intent();
								mIntent.setClass(mContext, CycleLocationReportService.class);
								mContext.startService(mIntent);
							}else{
								 reportSwitch.edit().putInt("REPORT_FREQUENCY", 0).commit();
								 reportSwitch.edit().putString("USER_ADDRESS", mUserAddress).commit();
								 Utils.COUNT_DOWN_TIME=cardManager.getCardInfo().mSericeFeq;
							}
							try{
								 mananger.sendSMSCmdBDV21(locationReport.getUserAddress(), msgComType,1,"N", Utils.buildeLocationReport1(locationReport));
							} catch (BDUnknownException e){
									e.printStackTrace();
							} catch (BDParameterException e){
									e.printStackTrace();
							}
                           
					}else{
							Toast.makeText(mContext.getApplicationContext(), "未获得当前位置信息!",Toast.LENGTH_LONG).show();
					}
				 }
				break;
			}
			case R.id.report_loc_friends: {
				Intent intent = new Intent();
				//mInstance.setTabFlag(1);
				intent.setClass(mContext,FriendsLocationActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.report_loc_report: {
				tabHost.clearAllTabs();
				tabManager.addTab("bd1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("report_location", mContext.getResources().getString(R.string.bdloc_loc_report_str),BDLocationReportActivity.class);
				tabHost.setCurrentTab(1);
				break;
			}
			case R.id.report_loc_set: {
				tabHost.clearAllTabs();
				tabManager.addTab("bd1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("locationset",mContext.getResources().getString(R.string.bdloc_1_top_tab_msg),BDRDSSLocationSetActivity.class);
				tabHost.setCurrentTab(1);
				break;
			}
			case R.id.report_loc_menu: {
				tabHost.clearAllTabs();
				tabManager.addTab("bd1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("bd2",mContext.getResources().getString(R.string.bdloc_1_top_tab_msg),BD1LocActivity.class);
				tabHost.setCurrentTab(0);
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
	private BDLocationReport addGPSLocationToBDLocationReport() {
		BDLocationReportManager manager=BDLocationReportManager.getInstance();
		if("S500".equals(Utils.DEVICE_MODEL)){
			Location location=manager.getLocation();
			if(location!=null){
				BDLocationReport report = new BDLocationReport();
				report.setHeightUnit("m");
				report.setLongitude(location.getLongitude());
				report.setHeight(location.getAltitude());
				report.setLatitude(location.getLatitude());
				report.setLongitudeDir(location.getExtras().getString("londir"));
				report.setLatitudeDir(location.getExtras().getString("latdir"));
				report.setMsgType(1);
				report.setReportFeq(Integer.valueOf(frequency));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss)");
				String time = sdf.format(new Date());
				report.setReportTime(time);
				report.setUserAddress(mUserAddress);
				return report;
			}else{
				return null;	
			}
		}else{
			BDRNSSLocation location=manager.getBDRNSSLocation();
			if(location!=null){
				BDLocationReport report = new BDLocationReport();
				report.setHeightUnit("m");
				report.setLongitude(location.getLongitude());
				report.setHeight(location.getAltitude());
				report.setLatitude(location.getLatitude());
				report.setLongitudeDir(location.getExtras().getString("londir"));
				report.setLatitudeDir(location.getExtras().getString("latdir"));
				report.setMsgType(1);
				report.setReportFeq(Integer.valueOf(frequency));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss)");
				String time = sdf.format(new Date());
				report.setReportTime(time);
				report.setUserAddress(mUserAddress);
				return report;
			}else{
				return null;	
			}
		}
	}
}
