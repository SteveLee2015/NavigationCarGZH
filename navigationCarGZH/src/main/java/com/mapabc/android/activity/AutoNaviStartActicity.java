package com.mapabc.android.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.BDEventListener;
import android.location.BDLocation;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.TextView;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.base.Constants;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.mapabc.android.activity.utils.FMSharedPreferenceUtils;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.type.NSLonLat;
import com.mapabc.naviapi.utils.SysParameterManager;
import com.novsky.map.main.BDResponseListener;
import com.novsky.map.main.CycleAlarmServiceNew;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDLocationManager;
import com.novsky.map.util.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @description: 系统启动
 * @author: zhuhao 2012-08-09
 * @version:
 * @modify:
 * @Copyright: mapabc.com
 */
public class AutoNaviStartActicity extends Activity {

    private String sendAddress, message;
    /**
     * 传输类型
     */
    private int mTranslateType = 0;
    /**
     * 短信通信模式
     */
    private int mMsgCommunicationType = 1;
    /**
     * RDSS管理类
     */
    private BDCommManager manager = null;

    /**
     * 北斗卡信息
     */
    private BDCardInfoManager cardManager = null;
    /**
     * 反馈信息监听器
     */
    private BDEventListener fkilistener = null;
    ;
    /**
     * 定位时间
     */
    private TextView mTime = null;


    private Intent alarmServiceIntent;

    Context context;

    private int COOD_FLAG = 0;
    private SimpleDateFormat sdf = null;

    private int counter = 0;
    private BDLocation bdloc = null;

    private void startCycleAlarmService() {
        alarmServiceIntent = new Intent(this, CycleAlarmServiceNew.class);
        startService(alarmServiceIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> permissions = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.READ_LOGS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_LOGS);
        }
        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
        }

        startCycleAlarmService();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cardManager = BDCardInfoManager.getInstance();
        fkilistener = new BDResponseListener(this);
        manager = BDCommManager.getInstance(getBaseContext());
        try {
            manager.addBDEventListener(fkilistener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
        //遍历mnt，查找数据文件所在目录
        File file = new File("/mnt");
        File files[] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                String mapabcpath = files[i].getAbsolutePath();
                if (new File(mapabcpath + "/MapABC").exists()) {
                    SysParameterManager.setBasePath(mapabcpath);
                    break;
                }
            }
        }
        final Context context = this;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                context.startActivity(new Intent(Constants.ACTIVITY_START_SPLASH));
                finish();
            }
        }, 50);

        if ("".equals(FMSharedPreferenceUtils.getInstance().getString("MY_ADDRESS_SOS", ""))) {
            FMSharedPreferenceUtils.getInstance().putString("MY_ADDRESS_SOS", "459540");
        }
        if ("".equals(FMSharedPreferenceUtils.getInstance().getString("MY_ADDRESS_WZ", ""))) {
            FMSharedPreferenceUtils.getInstance().putString("MY_ADDRESS_WZ", "459540");
        }
        if ("".equals(FMSharedPreferenceUtils.getInstance().getString("MY_SECOND", ""))) {
            FMSharedPreferenceUtils.getInstance().putString("MY_SECOND", "180");
        }
        String sosSetAddress = FMSharedPreferenceUtils.getInstance().getString("MY_ADDRESS_SOSSET", "");
        String sosSetContent = FMSharedPreferenceUtils.getInstance().getString("MY_JYXX", "");
        if ("".equals(sosSetContent)) {
            FMSharedPreferenceUtils.getInstance().putString("MY_JYXX", "行车安全警情，请求立刻救援！");
        }
        if ("".equals(sosSetAddress)) {
            sosSetAddress = "459540";
            FMSharedPreferenceUtils.getInstance().putString("MY_ADDRESS_SOSSET", sosSetAddress);
        }
        try {
            manager.sendSOSSettingsCmd(sosSetAddress, 2, Utils.getSOSSettings(sosSetContent));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ("S500".equals(Utils.DEVICE_MODEL)) {
            BDLocationManager.getInstance(this).requestLocationUpdate(locationListener);
        } else {
            try {
                manager.addBDEventListener(mBDRNSSLocationListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {
        @Override
        public void onLocationChanged(BDRNSSLocation arg0) {
            super.onLocationChanged(arg0);
            try {
                MapAPI mapAPI = MapAPI.getInstance();
                if (mapAPI != null) {
                    mapAPI.setVehicleGPS(3);
                    double[] lonlat = ToolsUtils.wgs84togcj02((float) arg0.getLongitude(), (float) arg0.getLatitude());
                    NSLonLat vehicleLonLat = new NSLonLat((float) lonlat[0], (float) lonlat[1]);
                    mapAPI.setVehiclePosInfo(vehicleLonLat, 0);
                    if (counter < 10) {
                        counter++;
                        mapAPI.setMapCenter(vehicleLonLat);
                    }
                }
            } catch (Exception e) {
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

    /**
     * 定位监听
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            Utils.checkBDLocationPort(AutoNaviStartActicity.this, false,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
                        }
                    }, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            startActivity(intent);
                        }
                    });
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                bdloc = new BDLocation();
                bdloc.setLongitude(location.getLongitude());
                bdloc.setLatitude(location.getLatitude());
                bdloc.setEarthHeight(location.getAltitude());
                BDLocation bdlocation = Utils.translate(bdloc, COOD_FLAG);
                mSwitchCoodriate(bdlocation);
                mTime.setText(sdf.format(new Date(location.getTime() + 8 * 60 * 60 * 1000)));
            }
        }
    };

    public void mSwitchCoodriate(BDLocation bdlocation) {
        // 设置经度
        String Longitude = (bdlocation != null && bdlocation.mLongitude != 0.0) ? Utils.setDoubleNumberDecimalDigit(bdlocation.mLongitude, 10) : AutoNaviStartActicity.this
                .getResources().getString(R.string.common_lon_value);
        // 设置纬度
        String Latitude = (bdlocation != null && bdlocation.mLatitude != 0.0) ? Utils.setDoubleNumberDecimalDigit(bdlocation.mLatitude, 10) : AutoNaviStartActicity.this
                .getResources().getString(R.string.common_lat_value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (!"S500".equals(Utils.DEVICE_MODEL)) {
//            if (manager != null) {
//                try {
//                    manager.removeBDEventListener(mBDRNSSLocationListener);
//                } catch (BDUnknownException e) {
//                    e.printStackTrace();
//                } catch (BDParameterException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}

