package com.novsky.map.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.main.BDLocationPortActivity;
import com.novsky.map.main.CustomListView;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDLocationTabManager;
import com.novsky.map.util.LocSetDatabaseOperation;
import com.novsky.map.util.LocationSet;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

/**
 * 北斗RDSS定位设置
 * @author steve
 */
public class BDRDSSLocationSetFragment extends Fragment implements
		OnClickListener {
	/**
	 * 日志标识
	 */
	private static final String TAG = "BDRDSSLocationSetActivity";
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.activity_location_set,null);
		initUI(view);
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
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * 初始化UI
	 */
	public void initUI(View view) {
		locationStep = (EditText) view.findViewById(R.id.location_step);
		height = (EditText) view.findViewById(R.id.height_value);
		antenna = (EditText) view.findViewById(R.id.tianxian_height_value);
		altimetryType = (CustomListView) view.findViewById(R.id.bd_check_height_type);
		setBtn = (Button) view.findViewById(R.id.bdset_submit_btn);
		coodrinateType = (CustomListView) view.findViewById(R.id.bd_report_coodr_type);
		coodrinateType.setData(getActivity().getResources().getStringArray(R.array.bdloc_type_array));
		altimetryType.setData(this.getResources().getStringArray(R.array.test_height_spinner));
		cardManager = BDCardInfoManager.getInstance();
		setBtn.setOnClickListener(this);
		settingDatabaseOper = new LocSetDatabaseOperation(getActivity());
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
				if (!Utils.checkBDSimCard(getActivity()))return;
				/*校验定位频率数据*/
				if (locationStep == null|| "".equals(locationStep.getText().toString())) {
					Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.bd_fequency_no_content),Toast.LENGTH_SHORT).show();
					return;
				}
				/*校验高程数据*/
				if (height == null || "".equals(height.getText().toString())) {
					Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.height_no_empty),Toast.LENGTH_SHORT).show();
					return;
				}
                /*校验天线数据*/ 
				if (antenna == null || "".equals(antenna.getText().toString())) {
					Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.tixian_no_empty),Toast.LENGTH_SHORT).show();
					return;
				}

				int frequency = Integer.valueOf(locationStep.getText().toString());
                /*校验定位频度数据是否大于卡频度*/
				if ((coodrinateType.getCurrentIndex() != 0)&& (frequency <= cardManager.getCardInfo().mSericeFeq)) {
					Toast.makeText(getActivity(),"报告频度必须大于" + cardManager.getCardInfo().mSericeFeq + "秒",Toast.LENGTH_SHORT).show();
					return;
				}
				/* 保存至数据库 */
				saveToDataBase(frequency);
				break;
			}
			default:
				break;
		}
	}

	
	public void saveToDataBase(int frequency) {
		boolean istrue = false;
		/*如果数据库中没有定位设置的数据*/
		if (locationSettingTotal== 0){
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
			Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.bdloc_set_success), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.bdloc_set_fail),Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(settingDatabaseOper!=null){
		   settingDatabaseOper.close();
		}
	}
}
