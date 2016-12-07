package com.novsky.map.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.BDRNSSManager;
import android.location.BDRNSSManager.LocationStrategy;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

/**
 * 设置当前北斗模块的定位模式(单北斗,单GPS,北斗GPS混合)
 * @author steve
 */
public class LocationModelActivity extends Activity implements OnClickListener{
	
	private Context mContext=this;
	
	/**
	 * 日志标识
	 */
	private static final String TAG="LocationModelActivity";
	

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
	public static int MODE = MODE_PRIVATE;
	 
	/**
	 * 设置保存时的文件的名称
	 */
    public static final String PREFERENCE_NAME = "LOCATION_MODEL_ACTIVITY";
	
	/**
	 * 定位模式的标识
	 */
    private int FLAG=0;
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.setRequestedOrientation(JsNavi.horizon?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_location_model);
		LinearLayout layout=(LinearLayout)this.findViewById(R.id.location_mode_id);
		DisplayMetrics metrics=new DisplayMetrics();
		WindowManager m = getWindowManager();      
	    m.getDefaultDisplay().getMetrics(metrics);//为获取屏幕宽、高                
		LayoutParams p = getWindow().getAttributes();//获取对话框当前的参数值
		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
			p.height = (int) ((metrics.heightPixels*1)/3);//高度设置为屏幕的1.0     
			p.width = (int) ((metrics.widthPixels*2)/3);    //宽度设置为屏幕的0.8   
		}else if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){
			p.height = (int) ((metrics.heightPixels * 7)/24);   //高度设置为屏幕的1.0     
			p.width = (int) (metrics.widthPixels);    //宽度设置为屏幕的0.8   
		}
		getWindow().setAttributes(p);     //设置生效  
		initUI();
	}
	/**
	 * 初始化UI
	 */
	public void initUI(){
		if("S500".equals(Utils.DEVICE_MODEL)){
			mRnssManager=BDRNSSManager.getInstance(this);
		}else{
			manager=BDCommManager.getInstance(this);	
		}
		mySpinner=(CustomListView)this.findViewById(R.id.bd_location_model);
		setButton=(Button)this.findViewById(R.id.setBtn);
		mySpinner.setData(new String[]{"单GPS","单北斗","北斗GPS混合"});
		mySpinner.setOnCustomListener(new OnCustomListListener(){
			public void onListIndex(int index) {
				if(index==0){
					FLAG=LocationStrategy.GPS_ONLY_STRATEGY;
				}else if(index==1){
					FLAG=LocationStrategy.BD_ONLY_STRATEGY;
				}else if(index==2){
					FLAG=LocationStrategy.HYBRID_STRATEGY;
				}	
			}
		});
		SharedPreferences share = getSharedPreferences(PREFERENCE_NAME, MODE);  
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
					SharedPreferences share = getSharedPreferences(PREFERENCE_NAME, MODE);  
			        share.edit().putInt("LOCATION_MODEL", FLAG).commit();
					Toast.makeText(this, "设置定位模式成功!", Toast.LENGTH_SHORT).show();
					LocationModelActivity.this.finish();
				} catch (Exception e) {
					Toast.makeText(this, "设置定位模式失败!", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
	}
}
