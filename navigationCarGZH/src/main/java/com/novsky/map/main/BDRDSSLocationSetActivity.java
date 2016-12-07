package com.novsky.map.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDLocationTabManager;
import com.novsky.map.util.LocSetDatabaseOperation;
import com.novsky.map.util.LocationSet;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

/**
 * 北斗RDSS定位设置
 * 
 * @author steve
 */
public class BDRDSSLocationSetActivity extends Activity implements
		OnClickListener {
	/**
	 * 日志标识
	 */
	private static final String TAG = "BDRDSSLocationSetActivity";

	
	private Context mContext = this;

	/**
	 * 定位频度
	 */
	private EditText locationStep = null;

	/**
	 * 高程数据
	 */
	private EditText height = null;

	/**
	 * 天线数据
	 */
	private EditText antenna = null;

	/**
	 * 测高方式
	 */
	private CustomListView altimetryType = null;

	/**
	 * 设置按钮
	 */
	private Button setBtn = null;

	/**
	 * 坐标类型
	 */
	private CustomListView coodrinateType = null;

	/**
	 * 北斗卡信息管理
	 */
	private BDCardInfoManager cardManager = null;

	/**
	 * RDSS定位设置数据操作
	 */
	private LocSetDatabaseOperation settingDatabaseOper = null;

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
	
	
	private ImageView menuBtn=null;
	
	/**
	 * 数据库数据的总数和
	 */
	private int locationSettingTotal = 0;
	
	/**
	 * TabHost对象
	 */
	private TabHost tabHost = BDLocationPortActivity.tabHost;

	/**
	 * tab管理类
	 */
	private BDLocationTabManager tabManager = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_set);
		//AgentApp.getInstance().addActivity(this);
		initUI();
		locationSettingTotal= settingDatabaseOper.getSize();
		LocationSet set =settingDatabaseOper.getFirst();
		/* 如果数据库中有数据,则在界面上显示数据库存储数据 */
		if (set != null) {
			locationStep.setEnabled(Integer.valueOf(set.getLocationFeq())>0?true:false);
			locationStep.setText(set.getLocationFeq());
			height.setText(set.getHeightValue());
			antenna.setText(set.getTianxianValue());
			altimetryType.setIndex(Integer.valueOf(set.getHeightType()));
			setComponeStatusByAltimetry(Integer.valueOf(set.getHeightType()));
		    coodrinateType.setIndex(Integer.valueOf(set.getLocationFeq())>0?1:0);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		altimetryType.setOnCustomListener(new OnCustomListListener() {
			@Override
			public void onListIndex(int num) {
				setComponeStatusByAltimetry(num);
			}
		});
		
		coodrinateType.setOnCustomListener(new OnCustomListListener() {
			@Override
			public void onListIndex(int num) {
				locationStep.setEnabled(num==0?false:true);
			}
		});

	}

	/**
	 * 初始化UI
	 */
	public void initUI() {
		locationStep = (EditText) this.findViewById(R.id.location_step);
		height = (EditText) this.findViewById(R.id.height_value);
		antenna = (EditText) this.findViewById(R.id.tianxian_height_value);
		altimetryType = (CustomListView) this.findViewById(R.id.bd_check_height_type);
		setBtn = (Button) this.findViewById(R.id.bdset_submit_btn);
		coodrinateType = (CustomListView) this.findViewById(R.id.bd_report_coodr_type);
		coodrinateType.setData(this.getResources().getStringArray(R.array.bdloc_type_array));
		altimetryType.setData(this.getResources().getStringArray(R.array.test_height_spinner));
		cardManager = BDCardInfoManager.getInstance();
		locationSettingBtn=(ImageView)findViewById(R.id.setting_loc_set);
		locationReportBtn=(ImageView)findViewById(R.id.setting_loc_report);
		locationFriendsBtn=(ImageView)findViewById(R.id.setting_loc_friends);
		menuBtn=(ImageView)findViewById(R.id.setting_loc_menu);
		locationSettingBtn.setOnClickListener(this);
		locationReportBtn.setOnClickListener(this);
		locationFriendsBtn.setOnClickListener(this);
		menuBtn.setOnClickListener(this);
		setBtn.setOnClickListener(this);
		settingDatabaseOper = new LocSetDatabaseOperation(this);
		tabManager = new BDLocationTabManager(this, tabHost);
		//mInstance = TabSwitchActivityData.getInstance();
		//bdLocationCurrentTab=BDLocationCurrentTab.getInstance();
	}

	/**
	 * 各种测高方式下高程和天线组件的状态 
	 * @param num
	 */
	public void setComponeStatusByAltimetry(int num){
		switch (num) {
			case 0:
				height.setEnabled(true);
				antenna.setEnabled(false);
				break;
			case 1:
				height.setEnabled(false);
				antenna.setEnabled(true);
				break;
			case 2:
				height.setEnabled(false);
				antenna.setEnabled(true);
				break;
			case 3:
				height.setEnabled(true);
				antenna.setEnabled(true);
				break;
			default:
				break;
		}
	}
	
	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.bdset_submit_btn:{
				if (!Utils.checkBDSimCard(mContext))return;
				/*校验定位频率数据*/
				if (locationStep == null|| "".equals(locationStep.getText().toString())) {
					Toast.makeText(mContext,mContext.getResources().getString(R.string.bd_fequency_no_content),Toast.LENGTH_SHORT).show();
					return;
				}
				/*校验高程数据*/
				if (height == null || "".equals(height.getText().toString())) {
					Toast.makeText(mContext,mContext.getResources().getString(R.string.height_no_empty),Toast.LENGTH_SHORT).show();
					return;
				}
                /*校验天线数据*/ 
				if (antenna == null || "".equals(antenna.getText().toString())) {
					Toast.makeText(mContext,mContext.getResources().getString(R.string.tixian_no_empty),Toast.LENGTH_SHORT).show();
					return;
				}

				int frequency = Integer.valueOf(locationStep.getText().toString());
                /*校验定位频度数据是否大于卡频度*/
				if ((coodrinateType.getCurrentIndex() != 0)&& (frequency <= cardManager.getCardInfo().mSericeFeq)) {
					Toast.makeText(mContext,"报告频度必须大于" + cardManager.getCardInfo().mSericeFeq + "秒",Toast.LENGTH_SHORT).show();
					return;
				}
				/* 保存至数据库 */
				saveToDataBase(frequency);
				break;
			}
			case R.id.setting_loc_friends:
				Intent intent = new Intent();
				//mInstance.setTabFlag(1);
				intent.setClass(this,FriendsLocationActivity.class);
				startActivity(intent);
				break;
			case R.id.setting_loc_menu:
				tabHost.clearAllTabs();
				tabManager.addTab("bd1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("bd2",mContext.getResources().getString(R.string.bdloc_1_top_tab_msg),BD1LocActivity.class);
				tabHost.setCurrentTab(0);
				break;
			case R.id.setting_loc_report:
				tabHost.clearAllTabs();
				tabManager.addTab("bd1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("report_location", mContext.getResources().getString(R.string.bdloc_loc_report_str),BDLocationReportActivity.class);
				tabHost.setCurrentTab(1);
				break;
			case R.id.setting_loc_set:
				tabHost.clearAllTabs();
				tabManager.addTab("bd1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
				tabManager.addTab("locationset",mContext.getResources().getString(R.string.bdloc_1_top_tab_msg),BDRDSSLocationSetActivity.class);
				tabHost.setCurrentTab(1);
				break;
			default:
				break;
		}
	}

	
	public void saveToDataBase(int frequency) {
		boolean istrue = false;
		/*如果数据库中没有定位设置的数据*/
		if (locationSettingTotal== 0) {
			LocationSet set = new LocationSet();
		    set.setLocationFeq(coodrinateType.getCurrentIndex()== 0?"0":String.valueOf(frequency));
			set.setHeightType(String.valueOf(altimetryType.getCurrentIndex()));
			set.setHeightValue(height.isEnabled()?String.valueOf(height.getText().toString()):"0");
			set.setTianxianValue(antenna.isEnabled()?String.valueOf(antenna.getText().toString()):"0");
			istrue = settingDatabaseOper.insert(set);
		} else {
			LocationSet set = settingDatabaseOper.getFirst();
			set.setLocationFeq(coodrinateType.getCurrentIndex()==0?"0":String.valueOf(frequency));
			set.setHeightType(String.valueOf(altimetryType.getCurrentIndex()));
			set.setHeightValue(height.isEnabled()?String.valueOf(height.getText().toString()):"0");
			set.setTianxianValue(antenna.isEnabled()?String.valueOf(antenna.getText().toString()):"0");
			istrue = settingDatabaseOper.update(set);
		}
		if (istrue) {
			Toast.makeText(mContext,mContext.getResources().getString(R.string.bdloc_set_success), Toast.LENGTH_SHORT).show();
			tabHost.clearAllTabs();
			tabManager.addTab("bd1",mContext.getResources().getString(R.string.bdloc_2_top_tab_msg),BD2LocActivity.class);
			tabManager.addTab("bd2",mContext.getResources().getString(R.string.bdloc_1_top_tab_msg),BD1LocActivity.class);
			tabHost.setCurrentTab(1);
		} else {
			Toast.makeText(mContext,mContext.getResources().getString(R.string.bdloc_set_fail),Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(settingDatabaseOper!=null){
			settingDatabaseOper.close();
		}
	}
}
