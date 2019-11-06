package com.novsky.map.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.BDParameterException;
import android.location.BDRNSSManager;
import android.location.BDUnknownException;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.bd.comm.protocal.BDSatellite;
import com.bd.comm.protocal.BDSatelliteListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.mapabc.android.activity.utils.ReceiverAction;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.type.NSLonLat;
import com.novsky.map.util.CollectionUtils;
import com.novsky.map.view.CustomSatelliateMap;
import com.novsky.map.view.CustomSatelliateSnr;

import java.util.ArrayList;
import java.util.List;


/**
 * GPS卫星状态
 *
 * @author llg
 */
public class BD2StatusActivity extends Activity implements View.OnClickListener {

    public static final String TAG = "BD2StatusActivity";

    private final static int LOCATION_RESULT = 0x1000,
            GP_SATELLIATE_STATUS = 0x1001, BD_SATELLIATE_STATUS = 0x1002;

    private CustomSatelliateSnr mCustomBDSnr = null;

    private TextView mGPSLocationResult, mGPSLocationStatus, mGPSLocationHeight;

    private CustomSatelliateMap mCustomBDMap = null;

    private Context mContext;

    private LinearLayout ll_gps_bd2;


    private TextView mStatellite;

    private RelativeLayout mRLchangeStatellite;

    private TextView mTvChange;

    private BDCommManager mBDCommManager = null;

    /**
     * 设置保存时的文件的名称
     */
    public static final String PREFERENCE_NAME = "LOCATION_MODEL_ACTIVITY";
    public static final String LOCATION_MODEL = "LOCATION_MODEL";

    /**
     * 定位模式的标识
     */
    private int FLAG = 0;

    /**
     * 定义访问模式为私有模式
     */
    public static int MODE = Context.MODE_PRIVATE;

    List<BDSatellite> gplist = new ArrayList<>();

    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {
        @Override
        public void onLocationChanged(BDRNSSLocation arg0) {
            super.onLocationChanged(arg0);
            if (BDRNSSManager.LocationStrategy.GPS_ONLY_STRATEGY != FLAG) {

                Message message = mHandler.obtainMessage();
                message.what = LOCATION_RESULT;
                message.obj = arg0;
                mHandler.sendMessage(message);
            }
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

    private BDSatelliteListener mGPSatelliteListener = new BDSatelliteListener() {

        @Override
        public void onBDGpsStatusChanged(List<BDSatellite> list) {
            Message message = mHandler.obtainMessage();
            message.obj = list;
            message.what = GP_SATELLIATE_STATUS;
            mHandler.sendMessage(message);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOCATION_RESULT:
                    BDRNSSLocation mBDRNSSLocation = (BDRNSSLocation) msg.obj;
                    if (true) {//???
                        if (mBDRNSSLocation.isAvailable()) {
                            mGPSLocationStatus.setText("状态:已定位");
                            MapAPI.getInstance().setVehicleGPS(3);
                            double[] lonlat = ToolsUtils.wgs84togcj02((float) mBDRNSSLocation.getLongitude(), (float) mBDRNSSLocation.getLatitude());
                            NSLonLat vehicleLonLat = new NSLonLat((float) lonlat[0],(float) lonlat[1] );
                            MapAPI.getInstance().setVehiclePosInfo(vehicleLonLat, 0);
                        } else {
                            mGPSLocationStatus.setText("状态:未定位");
                        }
                        int lon = (int) (mBDRNSSLocation.getLongitude() * 100000);
                        int lat = (int) (mBDRNSSLocation.getLatitude() * 100000);
                        int height = (int) (mBDRNSSLocation.getAltitude() * 100);
                        //mGPSLocationResult.setText("("+lon/100000.0+","+lat/100000.0+","+height/100.0+")");
                        mGPSLocationResult.setText((lon / 100000.0) + " , " + (lat / 100000.0));
                        mGPSLocationHeight.setText((height / 100.0) + "m");
                    } else {
                        mGPSLocationStatus.setText("状态:未定位");
                        mGPSLocationResult.setText("0,0");
                        mGPSLocationHeight.setText(0 + "m");
                    }
                    break;
                case GP_SATELLIATE_STATUS:

                    gplist = (List<BDSatellite>) msg.obj;
                    //gplist= (List<BDSatellite>) msg.obj;
                    //排序
                    //Collections.sort(gplist);
                    showMap(gplist);
                    break;
                case BD_SATELLIATE_STATUS:
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 展示数据
     *
     * @param gplist
     */
    private void showMap(List<BDSatellite> gplist) {
        List newList = CollectionUtils.removeDuplicate(gplist);
        if (mCustomBDMap == null || mCustomBDMap == null) {
            return;
        }
        mCustomBDSnr.showMap2(newList);//载噪比
        mCustomBDMap.showMap2(newList);//星图
    }

    private void addReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ReceiverAction.ACTION_LOCATION_STRATEGY);
        mContext.registerReceiver(mReceiver, filter);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statellite_status);

        Log.d(TAG, "onCreateView");
        mContext = this;
        mBDCommManager = BDCommManager.getInstance(mContext);
        addReceiver();

        ll_gps_bd2 = (LinearLayout) findViewById(R.id.ll_gps_bd2);

        ll_gps_bd2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCustomBDSnr = (CustomSatelliateSnr) findViewById(R.id.gps_snr_view);
        mCustomBDMap = (CustomSatelliateMap) findViewById(R.id.gps_map_view);
        mStatellite = (TextView) findViewById(R.id.tv_Statellite);
        mRLchangeStatellite = (RelativeLayout) findViewById(R.id.rl_change_statellite);
        mGPSLocationResult = (TextView) findViewById(R.id.gps_location_result);
        mGPSLocationStatus = (TextView) findViewById(R.id.gps_location_status);
        mGPSLocationHeight = (TextView) findViewById(R.id.gps_location_height_value);
        mTvChange = (TextView) findViewById(R.id.tv_change);
        mTvChange.setOnClickListener(this);
        mRLchangeStatellite.setOnClickListener(this);
        mStatellite.setText("北斗II星图");

    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        try {
            mBDCommManager.addBDEventListener(mGPSatelliteListener, mBDRNSSLocationListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //读取定位设置 参数 是单北斗 单gps 还是混合定位
        SharedPreferences share = mContext.getSharedPreferences(PREFERENCE_NAME, MODE);
        FLAG = share.getInt("LOCATION_MODEL", 0);
        gplist.clear();
        //通知更新
        showMap(gplist);
        updateView(null);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        try {
            mBDCommManager.removeBDEventListener(mGPSatelliteListener, mBDRNSSLocationListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mContext.unregisterReceiver(mReceiver);
    }

    //更新显示内容的方法
    public void updateView(Location location) {
        if (location == null) {
            if (mGPSLocationStatus != null) {
                mGPSLocationStatus.setText("未定位");
            }
            if (mGPSLocationResult != null) {
                mGPSLocationResult.setText("0,0");
            }
            if (mGPSLocationHeight != null) {
                mGPSLocationHeight.setText(0 + "m");
            }

            //清除  最后数据
            gplist.clear();
            //通知更新
            showMap(gplist);

            return;
        }
        Message msg = Message.obtain();
        msg.what = LOCATION_RESULT;
        msg.obj = location;
        mHandler.sendMessage(msg);
    }


    @Override
    public void onClick(View v) {


    }

    /**
     * 接收 定位策略变化广播
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            String action = intent.getAction();
            switch (action) {
                case ReceiverAction.ACTION_LOCATION_STRATEGY: {

                    //读取定位设置 参数 是单北斗 单gps 还是混合定位
                    SharedPreferences share = mContext.getSharedPreferences(PREFERENCE_NAME, MODE);
                    FLAG = share.getInt("LOCATION_MODEL", 0);
                    onResume();

                    break;
                }
            }

        }
    };
}
