package com.novsky.map.main;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.BDLocation;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDPlatformFKListener;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.bd.comm.protocal.BDSOSButtonListener;
import com.bd.comm.protocal.BDSatellite;
import com.bd.comm.protocal.BDSatelliteListener;
import com.bd.comm.protocal.GPSatellite;
import com.bd.comm.protocal.GPSatelliteListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.base.NaviStudioApplication;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.mapabc.android.activity.utils.FMSharedPreferenceUtils;
import com.mapabc.naviapi.TTSAPI;
import com.mapabc.naviapi.type.Const;
import com.novsky.map.hook.HookLocationManager;
import com.novsky.map.util.DatabaseOperation;
import com.novsky.map.util.DistanceLoc;
import com.novsky.map.util.LocationUtils;
import com.novsky.map.util.SCConstants;
import com.novsky.map.util.UpDisTime;
import com.novsky.map.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 循环上报坐标
 *
 * @author steve
 */
public class CycleAlarmServiceNew extends Service {

    private static final String TAG = "CycleAlarmServiceNew";

    private Context mContext = null;
    private BDCommManager bdManager = null;
    private static WakeLock wakeLock = null;

    private List<BDRNSSLocation> updateMultiPoiList = new ArrayList<>();
    private List<BDRNSSLocation> updateDistanceList = new ArrayList<>();

    private SimpleDateFormat sdf = null;
    private DatabaseOperation dbOper = null;

    private boolean isUpdateDisNow = false;
    private List<UpDisTime> upDisTimeList = null;
    private int mCurrentIndex = 0, timeOutSendNum = 0;
    private boolean isFirstLocation = true;

    private BDSatelliteListener bdSatelliteListener = new BDSatelliteListener() {
        @Override
        public void onBDGpsStatusChanged(List<BDSatellite> list) {
            if (list != null) {
                int svCount = 0;
                int[] prns = new int[list.size()];
                float[] srns = new float[list.size()];
                float[] elevations = new float[list.size()];
                float[] azimuths = new float[list.size()];

                for (int i = 0; i < list.size(); i++) {
                    BDSatellite gpSatellite = list.get(i);

                    elevations[svCount] = gpSatellite.getElevation();
                    prns[svCount] = gpSatellite.getPrn();
                    srns[svCount] = gpSatellite.getSnr();
                    azimuths[svCount] = gpSatellite.getAzimuth();

                    if (gpSatellite.getSnr() > 0) {
                        svCount += 1;
                    }
                }
                HookLocationManager.getInstance().notifyGpsStatusListener(svCount, prns, srns, elevations, azimuths, 1, 1, 1);
            }
        }
    };

    private GPSatelliteListener gpSatelliteListener = new GPSatelliteListener() {
        @Override
        public void onGpsStatusChanged(List<GPSatellite> list) {
            if (list != null) {
                int svCount = 0;
                int[] prns = new int[list.size()];
                float[] srns = new float[list.size()];
                float[] elevations = new float[list.size()];
                float[] azimuths = new float[list.size()];

                for (int i = 0; i < list.size(); i++) {
                    GPSatellite gpSatellite = list.get(i);

                    elevations[svCount] = gpSatellite.getElevation();
                    prns[svCount] = gpSatellite.getPrn();
                    srns[svCount] = gpSatellite.getSnr();
                    azimuths[svCount] = gpSatellite.getAzimuth();

                    if (gpSatellite.getSnr() > 0) {
                        svCount += 1;
                    }
                }
                HookLocationManager.getInstance().notifyGpsStatusListener(svCount, prns, srns, elevations, azimuths, 1, 1, 1);
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCConstants.UPDATE_TIME_OUT:
                    if (timeOutSendNum < 2) {
                        timeOutSendNum++;
                        doUpdateDistanceCmd(upDisTimeList.get(mCurrentIndex));
                        mHandler.sendEmptyMessageDelayed(SCConstants.UPDATE_TIME_OUT, 61000);
                    }
                    break;
                case SCConstants.OUT_LINE_TYPE:
                    try {
                        String welcome = mContext.getResources().getString(R.string.out_line_prompt);
                        Toast.makeText(mContext , welcome , Toast.LENGTH_SHORT).show();
                        TTSAPI.getInstance().addPlayContent(welcome, Const.AGPRIORITY_CRITICAL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case SCConstants.GET_LISTENER:
                    //doProcessLocation();
                    BDRNSSLocation bdrnssLocation = (BDRNSSLocation) msg.obj;
                    if (bdrnssLocation.isAvailable() && isFirstLocation) {
                        HookLocationManager.getInstance().notifyGpsStatusListener((int) bdrnssLocation.getTime());
                        isFirstLocation = false;
                    }
                    HookLocationManager.getInstance().notifyListener(bdrnssLocation);
                    break;

                case SCConstants.GET_4G_MESSAGE:
                    try {
                        bdManager.sendNetwork4GCmd(Utils.getQuery4GCmd());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessageDelayed(SCConstants.GET_4G_MESSAGE, 10 * 1000);
                    break;
                default:
                    break;
            }
        }
    };

    private BDSOSButtonListener bdsosButtonListener = new BDSOSButtonListener() {
        @Override
        public void onSosReceipt(int status) {
            Log.i(TAG, "========================>bdsosButtonListener status = " + status);
            switch (status) {
                case 0:
                    SCConstants.mLaunchSequence = SCConstants.DO_NOTHIING;
                    break;
                case 1:
                    SCConstants.mLaunchSequence = SCConstants.UPDATE_MULTI_POSITION;
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        }
    };

    private BDPlatformFKListener bdPlatformFKListener = new BDPlatformFKListener() {
        @Override
        public void onReceipt(String flag) {
            Log.i("TEST", "=============> bdPlatformFKListener flag =" + flag);
            switch (flag) {
                case "F3":
                    if (upDisTimeList != null) {
                        //清除数据
                        UpDisTime upDisTime = upDisTimeList.get(mCurrentIndex);
                        if (upDisTime.getId() > 0) {
                            dbOper.deleteUpDisTime(upDisTime.getId());
                        }
                        mCurrentIndex++;
                        mHandler.removeMessages(SCConstants.UPDATE_TIME_OUT);
                        Log.i("TEST", "=====================>mCurrentIndex =" + mCurrentIndex + "," + upDisTimeList.size());
                        if (mCurrentIndex < upDisTimeList.size()) {
                            timeOutSendNum = 0;
                            doUpdateDistanceCmd(upDisTimeList.get(mCurrentIndex));
                            mHandler.sendEmptyMessageDelayed(SCConstants.UPDATE_TIME_OUT, 61000);
                        } else {
                            SCConstants.mLaunchSequence = SCConstants.UPDATE_MULTI_POSITION;
                        }
                    }
                    break;
                case "F2":
                    //停止报警
                    SCConstants.mLaunchSequence = SCConstants.UPDATE_MULTI_POSITION;
                    Intent mIntent = new Intent(CycleAlarmServiceNew.this, CycleAlarmService.class);
                    stopService(mIntent);
                    break;

                case "F1":
                    mHandler.sendEmptyMessage(SCConstants.OUT_LINE_TYPE);
                    break;
                default:
                    break;
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateDistance();
        }
    };

    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {

        @Override
        public void onLocationChanged(BDRNSSLocation bdrnssLocation) {
            super.onLocationChanged(bdrnssLocation);
            //doRouteInfo();
            Message message = mHandler.obtainMessage();
            message.what = SCConstants.GET_LISTENER;
            message.obj = bdrnssLocation;
            mHandler.sendMessage(message);
            switch (SCConstants.mLaunchSequence) {
                case SCConstants.CHECK_TIME:
                    checkTime(bdrnssLocation);
                    break;
                case SCConstants.UPDATE_DISTANCE:
                    //1.从数据库中读取位置点
                    //2.计算距离。
                    //3.上传距离。如果有前几天的数据，则先上传前几天的数据。
                    if (!isUpdateDisNow) {
                        isUpdateDisNow = true;
                        new Thread(runnable).start();
                    }
                    break;
                case SCConstants.UPDATE_MULTI_POSITION:
                    updateMultiPoiList.add(bdrnssLocation);
                    updateDistanceList.add(bdrnssLocation);
                    int intervalNum = Integer.parseInt(FMSharedPreferenceUtils.getInstance().getString("MY_SECOND", "0"));
                    if (updateMultiPoiList.size() >= intervalNum) {
                        updateMultiPosition(updateMultiPoiList);
                        updateMultiPoiList.clear();
                    }
                    if (updateDistanceList.size() == 30) {
                        writeLocToDB(updateDistanceList);
                        updateDistanceList.clear();
                    }
                    break;
                default:
                    break;
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

    private void updateDistance() {
        upDisTimeList = dbOper.queryUpDisTimeList();
        for (UpDisTime disTime : upDisTimeList) {
            String locTime = disTime.getLocTime();
            String distanceNum = disTime.getDistanceNum();
            Log.i(TAG, "===========================>updateDistance locTime = " + locTime + ", distanceNum =" + distanceNum);
            if (TextUtils.isEmpty(distanceNum)) {
                if ((System.currentTimeMillis() - getMillsTime(locTime)) > SCConstants.ONE_DAY_MILLSECOND) {
                    disTime.setDistanceNum(String.valueOf(calcDistance(locTime)));
                    dbOper.updateUpDisTime(disTime);
                    dbOper.deleteDistanceLoc(locTime);
                }
            }
        }
        //获取最后一条的时间。
        if (upDisTimeList.size() > 0) {
            String lastLocTime = upDisTimeList.get(upDisTimeList.size() - 1).getLocTime();
            if (System.currentTimeMillis() - getMillsTime(lastLocTime) > SCConstants.ONE_DAY_MILLSECOND) {
                mCurrentIndex = 0;
                timeOutSendNum = 0;
                doUpdateDistanceCmd(upDisTimeList.get(mCurrentIndex));
                mHandler.sendEmptyMessageDelayed(SCConstants.UPDATE_TIME_OUT, 61000);
            }
        } else {
            SCConstants.mLaunchSequence = SCConstants.UPDATE_MULTI_POSITION;
        }
    }

    private long getMillsTime(String locTime) {
        long millsTime = 0;
        try {
            millsTime = sdf.parse(locTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millsTime;
    }

    private double calcDistance(String locTime) {
        double distance = 0;
        List<DistanceLoc> disLocList = dbOper.queryDistanceLocList(locTime);
        String mFirstPoc = "";
        for (DistanceLoc disLoc : disLocList) {
            String locTxt = disLoc.getLocText();
            Log.i("TEST", "==========================>locText =" + locTxt);
            if (!TextUtils.isEmpty(locTxt)) {
                if (!TextUtils.isEmpty(mFirstPoc)) {
                    locTxt = mFirstPoc +","+locTxt;
                }
                String[] params = locTxt.split(",");
                Log.i("TEST", "=========================>params " + params.length);
                for (int index = 0; index < params.length; index += 2) {
                    double currentLon = Utils.string2Double(params[index]);
                    double currentLat = Utils.string2Double(params[index + 1]);
                    if (index + 3 < params.length) {
                        double nextLon = Utils.string2Double(params[index + 2]);
                        double nextLat = Utils.string2Double(params[index + 3]);
                        distance += LocationUtils.getDistance(currentLat, currentLon
                                , nextLat, nextLon);
                    } else {
                         mFirstPoc = currentLon+","+currentLat;
                    }
                }
            }
        }
        Log.i("TEST", "========================>calcDistance distance =" + distance);
        return distance;
    }

    private void doUpdateDistanceCmd(UpDisTime upDisTime) {
        String address = FMSharedPreferenceUtils.getInstance().getString("MY_ADDRESS_WZ", "");
        if (address == null || "".equals(address)) {
            address = "145432";
        }

        String msg = Utils.getDistanceCmd(upDisTime);
        Log.i(TAG, "doUpdateDistanceCmd msg=" + msg + ",address=" + address);
        try {
            bdManager.sendSMSCmdBDV21(address, 1, 2, "N", msg);
        } catch (BDUnknownException e) {
            e.printStackTrace();
        } catch (BDParameterException e) {
            e.printStackTrace();
        }
    }

    private void writeLocToDB(List<BDRNSSLocation> updateMultiPoiList) {
        String locStr = "";
        for (int i = 0; i < (updateMultiPoiList.size() / 10); i++) {
            BDRNSSLocation location = updateMultiPoiList.get(i * 10);
            locStr += (location.getLongitude() + "," + location.getLatitude() + ",");
        }
        locStr = locStr.substring(0, locStr.length() - 1);
        String date = sdf.format(new Date());

        Log.i("TEST", "====================>writeLocToDB date =" + date + ", locStr =" + locStr);

        DistanceLoc distanceLoc = new DistanceLoc();
        distanceLoc.setLocText(locStr);
        distanceLoc.setLocTime(date);
        dbOper.insertDistanceLoc(distanceLoc);
        List<UpDisTime> list = dbOper.queryUpDisTimeList(date);
        if (list == null || list.isEmpty()) {
            UpDisTime upDisTime = new UpDisTime();
            upDisTime.setLocTime(date);
            dbOper.insertDistanceTime(upDisTime);
        }
    }

    private void checkTime(BDRNSSLocation bdrnssLocation) {
        Calendar calendar = Calendar.getInstance();
        long locTime = bdrnssLocation.getTime() + SCConstants.TIME_ZONE * 60 * 60 * 1000;
        calendar.setTimeInMillis(locTime);
        int year = calendar.get(Calendar.YEAR);
        Log.i("TEST", "================>checkTime year =" + year);
        if (year > 2000) {
            try {
                SystemDateTime.setDateTime(locTime);
                SCConstants.mLaunchSequence = SCConstants.UPDATE_DISTANCE;
            } catch (Exception e) {
                SCConstants.mLaunchSequence = SCConstants.CHECK_TIME;
                e.printStackTrace();
            }
        }
    }

    private void updateMultiPosition(List<BDRNSSLocation> list) {
        String address = FMSharedPreferenceUtils.getInstance().getString("MY_ADDRESS_WZ", "");
        if (address == null || "".equals(address)) {
            address = "145432";
        }
        //for (BDRNSSLocation location : list) {
        //    Log.i("TEST" ,"============>lon = "+location.getLongitude() +",lat =" +location.getLatitude() +",speed =" + location.getSpeed() +"，bearing ="+ location.getBearing());
        //}
        String msg = Utils.getPuShiCRCAlarm2(getLocationList(list));
        Log.i(TAG, "msg=" + msg + ",address=" + address);
        try {
            bdManager.sendSMSCmdBDV21(address, 1, 2, "N", msg);
        } catch (BDUnknownException e) {
            e.printStackTrace();
        } catch (BDParameterException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private List<BDRNSSLocation> getLocationList(List<BDRNSSLocation> mList) {
        List<BDRNSSLocation> list = new ArrayList<>();
        if (mList == null || mList.size() < SCConstants.MULTI_POINT_NUM) {
            return list;
        }
        int intervalValue = mList.size() / SCConstants.MULTI_POINT_NUM;
        list.add(mList.get(0));
        list.add(mList.get(intervalValue * 1));
        list.add(mList.get(intervalValue * 2));
        list.add(mList.get(intervalValue * 3));
        list.add(mList.get(intervalValue * 4));
        return list;
    }

    @NonNull
    private BDLocation getBdLocation(BDRNSSLocation arg0) {
        BDLocation bdLocation = new BDLocation();
        bdLocation.setLongitude(arg0.getLongitude());
        bdLocation.setLatitude(arg0.getLatitude());
        bdLocation.setEarthHeight(arg0.getAltitude());
        return bdLocation;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SCConstants.mLaunchSequence = SCConstants.CHECK_TIME;
        isUpdateDisNow = false;
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        dbOper = DatabaseOperation.getInstance();
        bdManager = BDCommManager.getInstance(NaviStudioApplication.getContext());
        try {
            bdManager.addBDEventListener(mBDRNSSLocationListener, bdPlatformFKListener, bdsosButtonListener, gpSatelliteListener, bdSatelliteListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessageDelayed(SCConstants.GET_4G_MESSAGE, 10 * 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CycleLocationReportService");
        if (null != wakeLock) {
            wakeLock.acquire();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bdManager != null) {
            try {
                bdManager.removeBDEventListener(mBDRNSSLocationListener, bdPlatformFKListener, bdsosButtonListener, gpSatelliteListener, bdSatelliteListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
