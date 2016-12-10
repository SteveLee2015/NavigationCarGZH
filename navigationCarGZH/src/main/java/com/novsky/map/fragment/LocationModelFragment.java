package com.novsky.map.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.BDRNSSManager;
import android.location.BDRNSSManager.LocationStrategy;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.utils.ReceiverAction;
import com.novsky.map.main.CustomListView;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

/**
 * 设置当前北斗模块的定位模式(单北斗,单GPS,北斗GPS混合)
 * @author steve
 */
public class LocationModelFragment extends Fragment implements OnClickListener{	
	
	private Context mContext;
	
	/**
	 * 日志标识
	 */
	private static final String TAG="LocationModelFragment";
	

	/**
	 * RDSS中间件
	 */
    private  BDCommManager manager=null;

    private BDRNSSManager mRnssManager=null;
    
    /**
     * 下拉菜单组件
     */
	private  CustomListView mySpinner=null;
	
	/**
	 * 设置定位模式按钮
	 */
	private  Button  setButton=null;
	
	
	//private TabSwitchActivityData mInstance=null;
	
	/**
	 * 定义访问模式为私有模式
	 */
	public static int MODE = Context.MODE_PRIVATE;
	 
	/**
	 * 设置保存时的文件的名称
	 */
    public static final String PREFERENCE_NAME = "LOCATION_MODEL_ACTIVITY";
    public static final String LOCATION_MODEL = "LOCATION_MODEL";

	/**
	 * 定位模式的标识
	 */
    private int FLAG=0;
    
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView;
		mContext = getActivity();
		contentView = View.inflate(mContext, R.layout.fragment_location_model, null);
		initUI(contentView);
		return contentView;
	}
	
	/**
	 * 初始化UI
	 */
	public void initUI(View contentView){
		if("S500".equals(Utils.DEVICE_MODEL)){
			mRnssManager=BDRNSSManager.getInstance(mContext);
		}else{
			manager=BDCommManager.getInstance(mContext);	
		}
		mySpinner=(CustomListView)contentView.findViewById(R.id.bd_location_model);
		setButton=(Button)contentView.findViewById(R.id.setBtn);
		mySpinner.setData(new String[]{"单GPS","单北斗","北斗GPS混合"});
		mySpinner.setOnCustomListener(new OnCustomListListener(){
			public void onListIndex(int index) {
				if(index==0){
					FLAG=LocationStrategy.GPS_ONLY_STRATEGY;//2
				}else if(index==1){
					FLAG=LocationStrategy.BD_ONLY_STRATEGY;//1
				}else if(index==2){
					FLAG=LocationStrategy.HYBRID_STRATEGY;//0
				}	
			}
		});
		SharedPreferences share = mContext.getSharedPreferences(PREFERENCE_NAME, MODE);  
        FLAG=share.getInt("LOCATION_MODEL",0);  
        int index=0;
        if(FLAG==LocationStrategy.GPS_ONLY_STRATEGY){
        	index=0;
        }else if(FLAG==LocationStrategy.BD_ONLY_STRATEGY){
        	index=1;
        }else if(FLAG==LocationStrategy.HYBRID_STRATEGY){
        	index=2;
        }
        mySpinner.setIndex(index);
		setButton.setOnClickListener(this);
	}
	
	
	
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.setBtn:
				try {
					if("S500".equals(Utils.DEVICE_MODEL)){
						mRnssManager.setLocationStrategy(FLAG);
						mRnssManager.setLocationStrategy(FLAG);
					}else{
						manager.setLocationStrategy(FLAG);
					}
					Utils.RNSS_CURRENT_LOCATION_MODEL=FLAG;
					SharedPreferences share = mContext.getSharedPreferences(PREFERENCE_NAME, MODE);  
			        share.edit().putInt(LOCATION_MODEL, FLAG).commit();
					Toast.makeText(mContext, "设置定位模式成功!", Toast.LENGTH_SHORT).show();

					//设置成功后 如何 通知界面
					//还是发广播吧
					Intent locationModeIntent = new Intent();
					locationModeIntent.setAction(ReceiverAction.ACTION_LOCATION_STRATEGY);
					locationModeIntent.putExtra(ReceiverAction.KEY_LOCATION_MODEL,FLAG);
					getActivity().sendBroadcast(locationModeIntent);


				} catch (Exception e) {
					Toast.makeText(mContext, "设置定位模式失败!", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
	}
}
