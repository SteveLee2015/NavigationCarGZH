package com.novsky.map.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.BDEventListener;
import android.location.BDMessageInfo;
import android.location.BDParameterException;
import android.location.BDRDSSManager.ManagerMode;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.novsky.map.util.Utils;

/**
 * 管理信息
 * @author steve
 */
public class ManagerInfoFragment extends Fragment implements OnClickListener {

	private final static String TAG = "ManagerInfoActivity";
	/**
	 * 管理信息文本框
	 * */
	private EditText managerInfo = null;
	private EditText managerInfo2 = null;
	private EditText managerInfo3 = null;
	private EditText managerInfo4 = null;
	private EditText managerInfo5 = null;
	private EditText managerInfo6 = null;
	private EditText managerInfo7 = null;
	private EditText managerInfo8 = null;
	private EditText managerInfo9 = null;
	private EditText managerInfo10 = null;
	private EditText managerInfo11 = null;
	private EditText managerInfo12 = null;
	private EditText managerInfo13 = null;
	private EditText managerInfo14 = null;
	private EditText managerInfo15 = null;
	private EditText managerInfo16 = null;
	private EditText managerInfo17 = null;
	private EditText managerInfo18 = null;
	private EditText managerInfo19 = null;
	private EditText managerInfo20 = null;
	private EditText managerInfo21 = null;
	private EditText managerInfo22 = null;
	private EditText managerInfo23 = null;
	private EditText managerInfo24 = null;
	private EditText managerInfo25 = null;
	private EditText managerInfo26 = null;
	private EditText managerInfo27 = null;
	private EditText managerInfo28 = null;
	private EditText managerInfo29 = null;
	private EditText managerInfo30 = null;
	private EditText managerInfo31 = null;
	private EditText managerInfo32 = null;

	/**
	 * 设置管信图标
	 */
	private Button setManagerInfo = null;

	/**
	 * 读取管理信息图标
	 */
	private Button readManagerInfo = null;

	/**
	 * 写入序列号图标
	 */
	private Button writeSerialNum = null;

	/**
	 * RDSS管理类
	 */
	private BDCommManager manager = null;

	/**
	 * 布局对象
	 */
	private LinearLayout mLayout = null;

	/**
	 * 序列号文本框
	 */
	private EditText serialNum = null;

	private int index_manager = 0;

	
	private Button sendButton=null,cmdButton=null,reportButton=null;
	/**
	 * 保存所有管信的列表
	 */
	private List<EditText> list = new ArrayList<EditText>();


	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			String manager = (String) msg.obj;
			if (manager != null && !"".equals(manager)) {
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setText(
							"" + manager.substring(i * 2, (i + 1) * 2));
				}
			}
		}
	};

	private BDEventListener managerListener = new BDEventListener.ManagerInfoListener() {
		
		public void onManagerInfo(String manager) {
			Message msg = new Message();
			msg.obj = manager;
			mHandler.sendMessage(msg);
		}
	};

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager = BDCommManager.getInstance(getActivity());
		try {
			manager.addBDEventListener(managerListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.activity_manager_info,null);
		initUI(view);
		return view;
	}
	
	
	public void onStart() {
		super.onStart();
		for (index_manager = 0; index_manager < (list.size() - 1); index_manager++) {
			list.get(index_manager).addTextChangedListener(new TextWatcher() {
				final int temp = index_manager;
				final int nexttmp = index_manager + 1;

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}

				public void afterTextChanged(Editable s) {
					if (s.length() == 2) {
						list.get(temp).clearFocus();
						list.get(nexttmp).requestFocus();
					}
				}
			});
		}
	}

	
	public void onDestroy() {
		super.onDestroy();
		managerInfo = null;
		serialNum = null;
		try {
			manager.removeBDEventListener(managerListener);
		} catch (BDUnknownException e) {
			e.printStackTrace();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化组件
	 */
	public void initUI(View view) {
		managerInfo = (EditText) view.findViewById(R.id.manager_info);
		managerInfo2 = (EditText) view.findViewById(R.id.manager_info2);
		managerInfo3 = (EditText) view.findViewById(R.id.manager_info3);
		managerInfo4 = (EditText) view.findViewById(R.id.manager_info4);
		managerInfo5 = (EditText) view.findViewById(R.id.manager_info5);
		managerInfo6 = (EditText) view.findViewById(R.id.manager_info6);
		managerInfo7 = (EditText) view.findViewById(R.id.manager_info7);
		managerInfo8 = (EditText) view.findViewById(R.id.manager_info8);
		managerInfo9 = (EditText) view.findViewById(R.id.manager_info9);
		managerInfo10 = (EditText) view.findViewById(R.id.manager_info10);
		managerInfo11 = (EditText) view.findViewById(R.id.manager_info11);
		managerInfo12 = (EditText) view.findViewById(R.id.manager_info12);
		managerInfo13 = (EditText) view.findViewById(R.id.manager_info13);
		managerInfo14 = (EditText) view.findViewById(R.id.manager_info14);
		managerInfo15 = (EditText) view.findViewById(R.id.manager_info15);
		managerInfo16 = (EditText) view.findViewById(R.id.manager_info16);
		managerInfo17 = (EditText) view.findViewById(R.id.manager_info17);
		managerInfo18 = (EditText) view.findViewById(R.id.manager_info18);
		managerInfo19 = (EditText) view.findViewById(R.id.manager_info19);
		managerInfo20 = (EditText) view.findViewById(R.id.manager_info20);
		managerInfo21 = (EditText) view.findViewById(R.id.manager_info21);
		managerInfo22 = (EditText) view.findViewById(R.id.manager_info22);
		managerInfo23 = (EditText) view.findViewById(R.id.manager_info23);
		managerInfo24 = (EditText) view.findViewById(R.id.manager_info24);
		managerInfo25 = (EditText) view.findViewById(R.id.manager_info25);
		managerInfo26 = (EditText) view.findViewById(R.id.manager_info26);
		managerInfo27 = (EditText) view.findViewById(R.id.manager_info27);
		managerInfo28 = (EditText) view.findViewById(R.id.manager_info28);
		managerInfo29 = (EditText) view.findViewById(R.id.manager_info29);
		managerInfo30 = (EditText) view.findViewById(R.id.manager_info30);
		managerInfo31 = (EditText) view.findViewById(R.id.manager_info31);
		managerInfo32 = (EditText) view.findViewById(R.id.manager_info32);
		setManagerInfo = (Button) view.findViewById(R.id.manager_set_manage_btn);
		readManagerInfo = (Button) view.findViewById(R.id.manager_read_manage_btn);
		writeSerialNum = (Button) view.findViewById(R.id.write_serial_num);
		serialNum = (EditText) view.findViewById(R.id.serial_num);
		setManagerInfo.setOnClickListener(this);
		readManagerInfo.setOnClickListener(this);
		writeSerialNum.setOnClickListener(this);
		list.add(managerInfo);
		list.add(managerInfo2);
		list.add(managerInfo3);
		list.add(managerInfo4);
		list.add(managerInfo5);
		list.add(managerInfo6);
		list.add(managerInfo7);
		list.add(managerInfo8);
		list.add(managerInfo9);
		list.add(managerInfo10);
		list.add(managerInfo11);
		list.add(managerInfo12);
		list.add(managerInfo13);
		list.add(managerInfo14);
		list.add(managerInfo15);
		list.add(managerInfo16);
		list.add(managerInfo17);
		list.add(managerInfo18);
		list.add(managerInfo19);
		list.add(managerInfo20);
		list.add(managerInfo21);
		list.add(managerInfo22);
		list.add(managerInfo23);
		list.add(managerInfo24);
		list.add(managerInfo25);
		list.add(managerInfo26);
		list.add(managerInfo27);
		list.add(managerInfo28);
		list.add(managerInfo29);
		list.add(managerInfo30);
		list.add(managerInfo31);
		list.add(managerInfo32);
		sendButton=(Button)view.findViewById(R.id.send_broadcast_btn);
		sendButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
//				AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
//				builder.setTitle("路线导航");
//				LayoutInflater inflater=LayoutInflater.from(getActivity());
//				final View view=inflater.inflate(R.layout.activity_navi_line, null);
//				final EditText navLineIdEditText=(EditText)view.findViewById(R.id.nav_line_id);
//				final EditText navLineNumEditText=(EditText)view.findViewById(R.id.nav_line_num);
//				final EditText navLineTotalEditText=(EditText)view.findViewById(R.id.nav_line_total);
//				final Spinner navLineTotalSpinner=(Spinner)view.findViewById(R.id.nav_coodr_spinner);
//				final LinearLayout lonlat1=(LinearLayout)view.findViewById(R.id.lon_lat_1);
//				final EditText lonEditText1=(EditText)view.findViewById(R.id.nav_lon_1);
//				final EditText latEditText1=(EditText)view.findViewById(R.id.nav_lat_1);
//				final LinearLayout lonlat2=(LinearLayout)view.findViewById(R.id.lon_lat_2);
//				final EditText lonEditText2=(EditText)view.findViewById(R.id.nav_lon_2);
//				final EditText latEditText2=(EditText)view.findViewById(R.id.nav_lat_2);
//				final LinearLayout lonlat3=(LinearLayout)view.findViewById(R.id.lon_lat_3);
//				final EditText lonEditText3=(EditText)view.findViewById(R.id.nav_lon_3);
//				final EditText latEditText3=(EditText)view.findViewById(R.id.nav_lat_3);
//				final LinearLayout lonlat4=(LinearLayout)view.findViewById(R.id.lon_lat_4);
//				final EditText lonEditText4=(EditText)view.findViewById(R.id.nav_lon_4);
//				final EditText latEditText4=(EditText)view.findViewById(R.id.nav_lat_4);
//				final LinearLayout lonlat5=(LinearLayout)view.findViewById(R.id.lon_lat_5);
//				final EditText lonEditText5=(EditText)view.findViewById(R.id.nav_lon_5);
//				final EditText latEditText5=(EditText)view.findViewById(R.id.nav_lat_5);
//				navLineTotalSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
//					@Override
//					public void onItemSelected(AdapterView<?> parent, View arg1,
//							int position, long arg3) {
//						 String str=parent.getItemAtPosition(position).toString();
//						 lonlat1.setVisibility(View.GONE);
//						 lonlat2.setVisibility(View.GONE);
//						 lonlat3.setVisibility(View.GONE);
//						 lonlat4.setVisibility(View.GONE);
//						 lonlat5.setVisibility(View.GONE);
//						 if("1".equals(str)){
//							 lonlat1.setVisibility(View.VISIBLE);
//						 }else if("2".equals(str)){
//							 lonlat1.setVisibility(View.VISIBLE);
//							 lonlat2.setVisibility(View.VISIBLE);
//						 }else if("3".equals(str)){
//							 lonlat1.setVisibility(View.VISIBLE);
//							 lonlat2.setVisibility(View.VISIBLE);
//							 lonlat3.setVisibility(View.VISIBLE);
//						 }else if("4".equals(str)){
//							 lonlat1.setVisibility(View.VISIBLE);
//							 lonlat2.setVisibility(View.VISIBLE);
//							 lonlat3.setVisibility(View.VISIBLE);
//							 lonlat4.setVisibility(View.VISIBLE);
//						 }else if("5".equals(str)){
//							 lonlat1.setVisibility(View.VISIBLE);
//							 lonlat2.setVisibility(View.VISIBLE);
//							 lonlat3.setVisibility(View.VISIBLE);
//							 lonlat4.setVisibility(View.VISIBLE);
//							 lonlat5.setVisibility(View.VISIBLE);
//						 }
//					}
//
//					@Override
//					public void onNothingSelected(AdapterView<?> arg0) {
//						
//					}
//				});
//
//				
//				builder.setView(view);
//				builder.setPositiveButton("发送", new DialogInterface.OnClickListener(){
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						Intent intent = new Intent(BDCommManager.PROVIDERS_CHANGED_ACTION);
//						BDMessageInfo mBDMessageInfo=new BDMessageInfo();
//						mBDMessageInfo.setmUserAddress("142399");
//						mBDMessageInfo.setMsgType(1);
//						mBDMessageInfo.setMsgCharset(2);
//						try {
//							String navId=navLineIdEditText.getText().toString();
//							String navNum=navLineNumEditText.getText().toString();
//							String navTotal=navLineTotalEditText.getText().toString();
//							String navCoodrNum=(navLineTotalSpinner.getSelectedItem()).toString();
//							String message="$BDNAL,"+navId+","+navNum+","+navTotal+","+navCoodrNum+",";
//							int navCoodrNumInt=Integer.valueOf(navCoodrNum);
//							if(navCoodrNumInt==1){
//								double lon1=Double.valueOf(lonEditText1.getText().toString());
//								double lat1=Double.valueOf(latEditText1.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon1)+"E,"+Utils.lonlatDouble2Str(lat1)+"N");
//							}else if(navCoodrNumInt==2){
//								double lon1=Double.valueOf(lonEditText1.getText().toString());
//								double lat1=Double.valueOf(latEditText1.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon1)+"E,"+Utils.lonlatDouble2Str(lat1)+"N,");
//							    double lon2=Double.valueOf(lonEditText2.getText().toString());
//								double lat2=Double.valueOf(latEditText2.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon2)+"E,"+Utils.lonlatDouble2Str(lat2)+"N");
//							}else if(navCoodrNumInt==3){
//								double lon1=Double.valueOf(lonEditText1.getText().toString());
//								double lat1=Double.valueOf(latEditText1.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon1)+"E,"+Utils.lonlatDouble2Str(lat1)+"N,");
//							    double lon2=Double.valueOf(lonEditText2.getText().toString());
//								double lat2=Double.valueOf(latEditText2.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon2)+"E,"+Utils.lonlatDouble2Str(lat2)+"N,");
//							    double lon3=Double.valueOf(lonEditText3.getText().toString());
//								double lat3=Double.valueOf(latEditText3.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon3)+"E,"+Utils.lonlatDouble2Str(lat3)+"N");
//							}else if(navCoodrNumInt==4){
//								double lon1=Double.valueOf(lonEditText1.getText().toString());
//								double lat1=Double.valueOf(latEditText1.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon1)+"E,"+Utils.lonlatDouble2Str(lat1)+"N,");
//							    double lon2=Double.valueOf(lonEditText2.getText().toString());
//								double lat2=Double.valueOf(latEditText2.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon2)+"E,"+Utils.lonlatDouble2Str(lat2)+"N,");
//							    double lon3=Double.valueOf(lonEditText3.getText().toString());
//								double lat3=Double.valueOf(latEditText3.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon3)+"E,"+Utils.lonlatDouble2Str(lat3)+"N,");
//							    double lon4=Double.valueOf(lonEditText4.getText().toString());
//								double lat4=Double.valueOf(latEditText4.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon4)+"E,"+Utils.lonlatDouble2Str(lat4)+"N");
//							}else if(navCoodrNumInt==5){
//								double lon1=Double.valueOf(lonEditText1.getText().toString());
//								double lat1=Double.valueOf(latEditText1.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon1)+"E,"+Utils.lonlatDouble2Str(lat1)+"N,");
//							    double lon2=Double.valueOf(lonEditText2.getText().toString());
//								double lat2=Double.valueOf(latEditText2.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon2)+"E,"+Utils.lonlatDouble2Str(lat2)+"N,");
//							    double lon3=Double.valueOf(lonEditText3.getText().toString());
//								double lat3=Double.valueOf(latEditText3.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon3)+"E,"+Utils.lonlatDouble2Str(lat3)+"N,");
//							    double lon4=Double.valueOf(lonEditText4.getText().toString());
//								double lat4=Double.valueOf(latEditText4.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon4)+"E,"+Utils.lonlatDouble2Str(lat4)+"N,");
//							    double lon5=Double.valueOf(lonEditText5.getText().toString());
//								double lat5=Double.valueOf(latEditText5.getText().toString());
//							    message+=(Utils.lonlatDouble2Str(lon5)+"E,"+Utils.lonlatDouble2Str(lat5)+"N");
//							}
//							message+="*41";
//							Log.i(TAG, ""+message);
//							mBDMessageInfo.setMessage(message.getBytes("GBK"));
//						} catch (UnsupportedEncodingException e) {
//							e.printStackTrace();
//						}
//						intent.putExtra(BDCommManager.BDRDSS_MESSAGE, mBDMessageInfo);
//		                getActivity().sendBroadcast(intent);
//					}
//				});
//				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						arg0.dismiss();
//					}
//				});
//				builder.create().show();
				Intent intent = new Intent(BDCommManager.PROVIDERS_CHANGED_ACTION);
				BDMessageInfo mBDMessageInfo=new BDMessageInfo();
				mBDMessageInfo.setmUserAddress("142399");
				mBDMessageInfo.setMsgType(1);
				mBDMessageInfo.setMsgCharset(2);
				String msgStr="$BDNAL,7,0,1,3,11614907E,4004521N,11617520E,4005617N,11620868E,4001294N*4C";
				try{
					mBDMessageInfo.setMessage(msgStr.getBytes("GBK"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				intent.putExtra(BDCommManager.BDRDSS_MESSAGE, mBDMessageInfo);
                getActivity().sendBroadcast(intent);
			}
		});
		cmdButton=(Button)view.findViewById(R.id.send_cmd_btn);
		cmdButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
				builder.setTitle("指令导航");
				LayoutInflater inflater=LayoutInflater.from(getActivity());
				final View view=inflater.inflate(R.layout.activity_navi_cmd, null);
				final EditText navLineIdEditText=(EditText)view.findViewById(R.id.nav_line_id);
				final EditText navTargetLonEditText=(EditText)view.findViewById(R.id.target_lon_1);
				final EditText navTargetLatEditText=(EditText)view.findViewById(R.id.target_lat_1);
				final EditText navMustGoLonEditText=(EditText)view.findViewById(R.id.must_go_lon_2);
				final EditText navMustGoLatEditText=(EditText)view.findViewById(R.id.must_go_lat_2);
				final EditText navAvoid1LonEditText=(EditText)view.findViewById(R.id.avoid_lon_3);
				final EditText navAvoid1LatEditText=(EditText)view.findViewById(R.id.avoid_lat_3);
				final EditText navAvoid2LonEditText=(EditText)view.findViewById(R.id.avoid_lon_4);
				final EditText navAvoid2LatEditText=(EditText)view.findViewById(R.id.avoid_lat_4);
				
				builder.setView(view);
				builder.setPositiveButton("发送", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Intent intent = new Intent(BDCommManager.PROVIDERS_CHANGED_ACTION);
						BDMessageInfo mBDMessageInfo=new BDMessageInfo();
						mBDMessageInfo.setmUserAddress("142399");
						mBDMessageInfo.setMsgType(1);
						mBDMessageInfo.setMsgCharset(2);
						try {
							String message="$BDNAC,"+navLineIdEditText.getText().toString()+",";
							double targetlon=Double.valueOf(navTargetLonEditText.getText().toString());
							double targetlat=Double.valueOf(navTargetLatEditText.getText().toString());
							message+=(Utils.lonlatDouble2Str(targetlon)+"E,"+Utils.lonlatDouble2Str(targetlat)+"N,");
							String mustGolonStr=navMustGoLonEditText.getText().toString();
							String mustGolatStr=navMustGoLatEditText.getText().toString();
							if(mustGolonStr!=null&&!"".equals(mustGolonStr)&&mustGolatStr!=null&&!"".equals(mustGolatStr)){
							   double mustGolon=Double.valueOf(mustGolonStr);
							   message+="1,"+(Utils.lonlatDouble2Str(mustGolon)+"E,");
							   double mustGolat=Double.valueOf(mustGolatStr);
						       message+=(Utils.lonlatDouble2Str(mustGolat)+"N,");
							}else{
								message+="0,,";
							}
							String avoid1lonStr=navAvoid1LonEditText.getText().toString();
							String avoid1latStr=navAvoid1LatEditText.getText().toString();
							String avoid2lonStr=navAvoid2LonEditText.getText().toString();
							String avoid2latStr=navAvoid2LatEditText.getText().toString();
							
							if(avoid1lonStr!=null&&!"".equals(avoid1lonStr)&&avoid1latStr!=null&&!"".equals(avoid1latStr)
									&&avoid2lonStr!=null&&!"".equals(avoid2lonStr)&&avoid2latStr!=null&&!"".equals(avoid2latStr)){
							   double avoid1lon=Double.valueOf(avoid1lonStr);
							   message+="2,"+(Utils.lonlatDouble2Str(avoid1lon)+"E,");
							   double avoid1lat=Double.valueOf(avoid1latStr);
						       message+=(Utils.lonlatDouble2Str(avoid1lat)+"N,");
						       
						       double avoid2lon=Double.valueOf(avoid2lonStr);
							   message+=(Utils.lonlatDouble2Str(avoid2lon)+"E,");
							   double avoid2lat=Double.valueOf(avoid2latStr);
						       message+=(Utils.lonlatDouble2Str(avoid2lat)+"N");
							}else if(avoid1lonStr!=null&&!"".equals(avoid1lonStr)&&avoid1latStr!=null&&!"".equals(avoid1latStr)){
								   double avoid1lon=Double.valueOf(avoid1lonStr);
								   message+="1,"+(Utils.lonlatDouble2Str(avoid1lon)+"E,");
								   double avoid1lat=Double.valueOf(avoid1latStr);
							       message+=(Utils.lonlatDouble2Str(avoid1lat)+"N");
							}else if(avoid2lonStr!=null&&!"".equals(avoid2lonStr)&&avoid2latStr!=null&&!"".equals(avoid2latStr)){
								   double avoid2lon=Double.valueOf(avoid2lonStr);
								   message+=(Utils.lonlatDouble2Str(avoid2lon)+"E,");
								   double avoid2lat=Double.valueOf(avoid2latStr);
							       message+=(Utils.lonlatDouble2Str(avoid2lat)+"N");
							}else{
								message+="0,,";
							}
							message+="*41";		
//							mBDMessageInfo.setMessage("$BDNAC,12,11634262E,4056782N,1,11634278E,4056782N,0,,*41".getBytes("GBK"));
							Log.i(TAG, ""+message);
							
							mBDMessageInfo.setMessage(message.getBytes("GBK"));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						intent.putExtra(BDCommManager.BDRDSS_MESSAGE, mBDMessageInfo);
						getActivity().sendBroadcast(intent);
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});
				builder.create().show();
			}
		});
		reportButton=(Button)view.findViewById(R.id.send_report_btn);
		reportButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(BDCommManager.PROVIDERS_CHANGED_ACTION);
				BDMessageInfo mBDMessageInfo=new BDMessageInfo();
				mBDMessageInfo.setmUserAddress("142399");
				mBDMessageInfo.setMsgType(1);
				mBDMessageInfo.setMsgCharset(2);
				try {
					mBDMessageInfo.setMessage("A0671B00743658A04024005600".getBytes("GBK"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				intent.putExtra(BDCommManager.BDRDSS_MESSAGE, mBDMessageInfo);
                getActivity().sendBroadcast(intent);
			}
		});
		
	}
	
	public void onClick(View view) {
		switch (view.getId()) {
		/* 设置管理信息操作 */
		case R.id.manager_set_manage_btn:
			try {
				StringBuffer hexString = new StringBuffer();
				for (int i = 0; i < list.size(); i++) {
					if ("".equals(list.get(i).getText().toString())) {
						Toast.makeText(
								getActivity(),
								getActivity().getResources().getString(
										R.string.manager_error_prompt),
								Toast.LENGTH_LONG).show();
						return;
					} else {
						hexString.append(list.get(i).getText().toString());
					}
				}
				manager.sendManagerInfoCmdBDV21(ManagerMode.SET_USERDEVICE,
						hexString.toString());
				
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(
								R.string.write_manager_info_success),
						Toast.LENGTH_LONG).show();

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		/* 读取管理信息操作 */
		case R.id.manager_read_manage_btn:
			try {
				manager.sendManagerInfoCmdBDV21(ManagerMode.READ_USERDEVICE, "SET_DEVICE");
			} catch (BDUnknownException e1) {
				e1.printStackTrace();
			} catch (BDParameterException e1) {
				e1.printStackTrace();
			}
			break;

		/* 写入序列号的操作*/
		case R.id.write_serial_num:
			if (serialNum != null && !"".equals(serialNum.getText().toString())) {
				try {
					manager.sendAccessSerialInfoCmdBDV40(1, serialNum.getText()
							.toString());
				} catch (BDUnknownException e) {
					e.printStackTrace();
				} catch (BDParameterException e) {
					e.printStackTrace();
				}
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(
								R.string.write_serial_num_success),
						Toast.LENGTH_LONG).show();

			} else {
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(
								R.string.serial_num_is_null), Toast.LENGTH_LONG)
						.show();
			}
			break;

		default:
			break;
		}
	}
}
