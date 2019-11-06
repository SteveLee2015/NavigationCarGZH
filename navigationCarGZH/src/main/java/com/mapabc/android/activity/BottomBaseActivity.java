package com.mapabc.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.BDBeam;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDRNSSManager.LocationStrategy;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.bd.comm.protocal.BDSatellite;
import com.bd.comm.protocal.BDSatelliteListener;
import com.bd.comm.protocal.GPSatellite;
import com.bd.comm.protocal.GPSatelliteListener;
import com.mapabc.android.activity.base.BaseActivity;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.novsky.map.main.AutoCheckedActivity;
import com.novsky.map.main.BD2StatusActivity;
import com.novsky.map.main.BDAvailableStatelliteManager;
import com.novsky.map.main.GPSStatusActivity;
import com.novsky.map.main.LocationStatusManager;
import com.novsky.map.util.Utils;

import java.util.List;

/**
 * 底部按钮显示界面 的基类
 *
 * @author Administrator
 */
public abstract class BottomBaseActivity extends BaseActivity {
    public static final String PREFERENCE_NAME = "LOCATION_MODEL_ACTIVITY";
    public static final String LOCATION_MODEL = "LOCATION_MODEL";
    /**
     * rnss 数目
     */
    private int rnssNum;
    /**
     * gps数据
     */
    int gpsNum = 0;
    /**
     * bdii数目
     */
    int bdiiNum = 0;

    public Context mContext = this;
    /**
     * RDSS对象
     */
    protected BDCommManager manager = null;

    /**
     * 定位状态的管理类
     */
    protected LocationStatusManager locationStatusManager = null;

    /**
     * 北斗有效卫星管理对象
     */
    protected BDAvailableStatelliteManager statellitesManager = null;

    protected static final int BD_RDSS_SATELLITE_NUM_ITEM = 0x100001,
            BD_RNSS_LOC_NUM_ITEM = 0x10002, BD_SATELLITE_STATUS_ITEM = 0x10003,
            GPS_SATELLITE_STATUS_ITEM = 0x10005, GPS_ONLY = 0x10004, BD_ONLY = 0x10006;

    protected boolean isLocation = false;

    /**
     * 北斗I卫星数目
     */
    public TextView homeTitleBD1Num = null;
    /**
     * 北斗II卫星数目
     * <p>
     * 2代卫星数+gps卫星数
     */
    protected TextView homeTitleBD2Num = null;

    /**
     * 页面标题
     */
    protected TextView title_name;

    /**
     * 返回键
     */
    protected ImageView back;

    protected LinearLayout mRDSSLayout = null, mRNSSLayout = null;

    protected Handler handler = new Handler() {

        private int gpsNumOnly;
        private int bdNumOnly;

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                // 北斗I卫星数目
                case BD_RDSS_SATELLITE_NUM_ITEM:
                    int count = msg.arg1;
                    int num = msg.arg2;
                    if (count >= 1) {
                        // 当前可发送定位/短报文
                        homeTitleBD1Num.setTextColor(Color.BLACK);
                        // homeTitleBD1Num.setTextColor(Color.GREEN);
                    } else {
                        // 当前只能发送短报文
                        homeTitleBD1Num.setTextColor(mContext.getResources()
                                .getColor(R.color.bd2_unenable_loc));
                    }
                    homeTitleBD1Num.setText(String.valueOf(num));
                    break;
                // 二代是否定位
                case BD_RNSS_LOC_NUM_ITEM:
                    BDRNSSLocation location = (BDRNSSLocation) msg.obj;
                    isLocation = location.isAvailable();
                    break;
                // 北斗II卫星数目 + gps数目
                case BD_SATELLITE_STATUS_ITEM:

                    // bd数
                    int currentAvailableBDii = 0;
                    List<BDSatellite> list = (List<BDSatellite>) msg.obj;
                    for (BDSatellite satellite : list) {
                        int statelliteID = satellite.getPrn();
                        float zaizaobi = satellite.getSnr();// 载噪比
                        if (statelliteID >= 160) {
                            if (satellite.usedInFix() && (zaizaobi > 5)) {
                                currentAvailableBDii++;
                                bdiiNum = currentAvailableBDii;
                            }
                        } else {
                            if (satellite.usedInFix() && (satellite.getSnr() > 5)) {
                                currentAvailableBDii++;
                                bdiiNum = currentAvailableBDii;
                            }
                        }
                    }
                    // currentAvailableBDii 表示2代bd可用信号信息

                    rnssNum = gpsNum + bdiiNum;

                    homeTitleBD2Num.setText("" + rnssNum);

                    if (isLocation) {
                        homeTitleBD2Num.setTextColor(Color.BLACK);
                        // homeTitleBD2Num.setTextColor(Color.GREEN);
                    } else {
                        homeTitleBD2Num.setTextColor(mContext.getResources()
                                .getColor(R.color.bd2_unenable_loc));
                    }
                    break;
                // 北斗II卫星数目 + gps数目
                case GPS_SATELLITE_STATUS_ITEM:
                    int currentAvailableBDGPS = 0;
                    // gps 数
                    List<GPSatellite> list1 = (List<GPSatellite>) msg.obj;
                    for (GPSatellite satellite : list1) {
                        int statelliteID = satellite.getPrn();
                        float zaizaobi = satellite.getSnr();// 载噪比
                        if (statelliteID >= 160) {
                            if (satellite.usedInFix() && (zaizaobi > 5)) {
                                currentAvailableBDGPS++;
                                gpsNum = currentAvailableBDGPS;
                            }
                        } else {
                            if (satellite.usedInFix()
                                    && (satellite.getSnr() > 5)) {
                                currentAvailableBDGPS++;
                                gpsNum = currentAvailableBDGPS;
                            }
                        }
                    }

                    rnssNum = gpsNum + bdiiNum;

                    homeTitleBD2Num.setText("" + rnssNum);

                    if (isLocation) {
                        homeTitleBD2Num.setTextColor(Color.BLACK);
                        // homeTitleBD2Num.setTextColor(Color.GREEN);
                    } else {
                        homeTitleBD2Num.setTextColor(mContext.getResources()
                                .getColor(R.color.bd2_unenable_loc));
                    }
                    break;

                // 尽gps定位
                case GPS_ONLY:

                    List<GPSatellite> list2 = (List<GPSatellite>) msg.obj;
                    int currentAvailableBDgpsOnly = 0;
                    for (GPSatellite satellite : list2) {
                        int statelliteID = satellite.getPrn();
                        float zaizaobi = satellite.getSnr();// 载噪比
                        if (statelliteID >= 160) {
                            if (satellite.usedInFix() && (zaizaobi > 5)) {
                                currentAvailableBDgpsOnly++;
                                gpsNumOnly = currentAvailableBDgpsOnly;
                            }
                        } else {
                            if (satellite.usedInFix() && (satellite.getSnr() > 5)) {
                                currentAvailableBDgpsOnly++;
                                gpsNumOnly = currentAvailableBDgpsOnly;
                            }
                        }

                        homeTitleBD2Num.setText("" + gpsNumOnly);
                        if (locationStatusManager.getLocationStatus()) {
                            homeTitleBD2Num.setTextColor(Color.BLACK);
                        } else {
                            homeTitleBD2Num.setTextColor(mContext.getResources()
                                    .getColor(R.color.bd2_unenable_loc));
                        }
                    }

                    break;

                // 仅北斗
                case BD_ONLY:

                    List<BDSatellite> list3 = (List<BDSatellite>) msg.obj;
                    int currentAvailableBDOnly = 0;
                    for (BDSatellite satellite : list3) {
                        int statelliteID = satellite.getPrn();
                        float zaizaobi = satellite.getSnr();// 载噪比
                        if (statelliteID >= 160) {
                            if (satellite.usedInFix() && (zaizaobi > 5)) {
                                currentAvailableBDOnly++;
                                bdNumOnly = currentAvailableBDOnly;
                            }
                        } else {
                            if (satellite.usedInFix() && (satellite.getSnr() > 5)) {
                                currentAvailableBDOnly++;
                                bdNumOnly = currentAvailableBDOnly;
                            }
                        }

                        homeTitleBD2Num.setText("" + bdNumOnly);
                        if (locationStatusManager.getLocationStatus()) {
                            homeTitleBD2Num.setTextColor(Color.BLACK);
                        } else {
                            homeTitleBD2Num.setTextColor(mContext.getResources()
                                    .getColor(R.color.bd2_unenable_loc));
                        }
                    }

                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 波束监听器
     */
    protected BDEventListener mBDBeamStatusListener = new BDEventListener.BDBeamStatusListener() {
        @Override
        public void onBeamStatus(BDBeam beam) {
            Message msg = new Message();
            int count = 0;
            int num = 0;
            for (int index = 0; index < beam.getBeamWaves().length; index++) {
                if (beam.getBeamWaves()[index] > 0) {
                    num++;
                }
            }
            count = Utils.checkCurrentRDSSStatus(beam.getBeamWaves());
            msg.what = BD_RDSS_SATELLITE_NUM_ITEM;
            msg.arg1 = count;
            msg.arg2 = num;
            handler.sendMessage(msg);
        }
    };

    private GPSatelliteListener mGPSatelliteListener = new GPSatelliteListener() {
        @Override
        public void onGpsStatusChanged(List<GPSatellite> arg0) {

            if (Utils.RNSS_CURRENT_LOCATION_MODEL == LocationStrategy.HYBRID_STRATEGY) {
                // 混合模式
                Message message = handler.obtainMessage();
                message.what = GPS_SATELLITE_STATUS_ITEM;
                message.obj = arg0;
                handler.sendMessage(message);

            } else {
                // 单 gps模式
                Message message = handler.obtainMessage();
                message.what = GPS_ONLY;
                message.obj = arg0;
                handler.sendMessage(message);
            }

        }
    };

    // 定位
    protected BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {
        @Override
        public void onLocationChanged(BDRNSSLocation arg0) {
            super.onLocationChanged(arg0);
            Message message = handler.obtainMessage();
            message.what = BD_RNSS_LOC_NUM_ITEM;
            message.obj = arg0;
            handler.sendMessage(message);
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

    /**
     * 卫星状态
     */
    protected BDSatelliteListener mBDSatelliteListener = new BDSatelliteListener() {
        @Override
        public void onBDGpsStatusChanged(List<BDSatellite> arg0) {


            if (Utils.RNSS_CURRENT_LOCATION_MODEL == LocationStrategy.HYBRID_STRATEGY) {
                // 混合模式
                Message message = handler.obtainMessage();
                message.what = BD_SATELLITE_STATUS_ITEM;
                message.obj = arg0;
                handler.sendMessage(message);

            } else {
                // 单 北斗模式
                Message message = handler.obtainMessage();
                message.what = BD_ONLY;
                message.obj = arg0;
                handler.sendMessage(message);
            }
        }
    };

    abstract protected int getContentView();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getContentView());
        statellitesManager = BDAvailableStatelliteManager.getInstance();
        manager = BDCommManager.getInstance(this);
        locationStatusManager = LocationStatusManager.getInstance();


        initView();
        initListener();

    }

    private void initListener() {

        mRDSSLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent mIntent = new Intent(mContext, AutoCheckedActivity.class);
                startActivity(mIntent);
            }
        });

        mRNSSLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                alertChooseDia();
            }
        });

    }

    @Override
    protected void onResume() {
        if ("S500".equals(Utils.DEVICE_MODEL)) {

        } else {
            try {
                manager.addBDEventListener(mBDBeamStatusListener,
                        mBDRNSSLocationListener, mBDSatelliteListener,
                        mGPSatelliteListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    /**
     * 星图选择对话框
     */
    protected void alertChooseDia() {
        SharedPreferences share = mContext.getSharedPreferences(PREFERENCE_NAME, LocationStrategy.HYBRID_STRATEGY);
        int mode = share.getInt(LOCATION_MODEL, LocationStrategy.HYBRID_STRATEGY);
        if (mode == LocationStrategy.GPS_ONLY_STRATEGY) {
            Intent mIntent = new Intent(mContext, GPSStatusActivity.class);
            startActivity(mIntent);
        } else {
            Intent mIntent = new Intent(mContext, BD2StatusActivity.class);
            startActivity(mIntent);
        }
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(R.string.navicontrol_select);
//
//		builder.setPositiveButton(R.string.navicontrol_bd_state, new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//
//				Intent mIntent = new Intent(mContext, BD2StatusActivity.class);
//				startActivity(mIntent);
//
//			}
//		});
//
//		builder.setNegativeButton(R.string.navicontrol_gps_state, new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//
//				Intent mIntent = new Intent(mContext, GPSStatusActivity.class);
//				startActivity(mIntent);
//
//			}
//		});
//
//		builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ("S500".equals(Utils.DEVICE_MODEL)) {

        } else {
            try {
                manager.removeBDEventListener(mBDBeamStatusListener,
                        mBDRNSSLocationListener, mBDSatelliteListener,
                        mGPSatelliteListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {

        homeTitleBD1Num = (TextView) this.findViewById(R.id.home_title_bd1_num);
        homeTitleBD2Num = (TextView) this.findViewById(R.id.home_title_bd2_num);
        title_name = (TextView) this.findViewById(R.id.title_name);
        back = (ImageView) this.findViewById(R.id.home_title_flag_img);

        mRDSSLayout = (LinearLayout) this.findViewById(R.id.rdss_num_layout);
        mRNSSLayout = (LinearLayout) this.findViewById(R.id.rnss_num_layout);

    }
}
