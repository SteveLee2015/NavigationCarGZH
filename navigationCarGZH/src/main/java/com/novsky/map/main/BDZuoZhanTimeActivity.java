package com.novsky.map.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.novsky.map.util.BDLocationManager;
import com.novsky.map.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 北斗作战时间
 * 功能描述:1.默认情况下,显示本机时间。
 * 2.如果有作战时间则显示作战时间。
 *
 * @author steve
 */
public class BDZuoZhanTimeActivity extends Activity {

    /**
     * 上下文对象
     */
    private Context mContext = this;
    /**
     * 日志标识
     */
    private static final String TAG = "BDTimeActivity";
    /**
     * 显示'年'
     */
    private TextView mYearTx = null;
    /**
     * 显示’月‘
     */
    private TextView mMonthTx = null;
    /**
     * 显示'日'
     */
    private TextView mDayTx = null;
    /**
     * 显示'小时'
     */
    private TextView mHourTx = null;
    /**
     * 显示'分钟'
     */
    private TextView mMinuteTx = null;
    /**
     * 显示'秒'
     */
    private TextView mSecondTx = null;

    /**
     * 显示步长
     */
    private TextView mStepTx = null;

    /**
     * 完成校验北斗时间
     */
    private final int COMPLETE_CHECK_BD_TIME = 1;


    private final int PROMPT_CHECK_BD_TIME = 2;
    /**
     * 时间格式
     */
    private SimpleDateFormat sdf = null;
    /**
     * RDSS中间件对象
     */
    private BDCommManager mBDCommManager = null;
    /**
     * 定位时间
     */
    private long locationTime = 0;

    private boolean isShow = false, isZuoZhanShow = false;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            SharedPreferences prefs = mContext.getSharedPreferences("BD_ZUO_ZHAN_TIME_PREFS", 0);
            long zuozhanTime = prefs.getLong("BD_ZUO_ZHAN_TIME", 1428654131598l);
            long bjTime = prefs.getLong("BD_BJ_TIME", 1428654131598l);
            int step = prefs.getInt("BD_ZUO_ZHAN_STEP", 2);
            if ((locationTime != 0) && (zuozhanTime != 0) && (bjTime != 0) && (step != 0)) {
                //已设置作战时间
                long currentTime = locationTime + 8 * 3600 * 1000;
                long temp = currentTime - bjTime;
                zuozhanTime = zuozhanTime + temp * step;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(zuozhanTime);
                int year = calendar.get(Calendar.YEAR);
                if (year <= 2000) {
                    if (!isZuoZhanShow) {
                        isZuoZhanShow = true;
                        mHandler.sendEmptyMessage(PROMPT_CHECK_BD_TIME);
                    }
                } else {
                    //进行计算
                    int tempyear = year - 2000;
                    if (tempyear <= 0) {
                        tempyear = 0;
                    }
                    mYearTx.setText(Utils.showTwoBitNum(tempyear));
                    mMonthTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.MONTH) + 1));
                    mDayTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.DAY_OF_MONTH)));
                    mHourTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.HOUR_OF_DAY)));
                    mMinuteTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.MINUTE)));
                    mSecondTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.SECOND)));
                    mStepTx.setText(String.valueOf(step));
                    mHandler.postDelayed(mRunnable, 1000);
                }
            } else {
                //未设置作战时间
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                if (year <= 2000) {
                    if (!isShow) {
                        isShow = true;
                        mHandler.sendEmptyMessage(PROMPT_CHECK_BD_TIME);
                    }
                }
                int tempyear = year - 2000;
                if (tempyear <= 0) {
                    tempyear = 0;
                }
                mYearTx.setText(Utils.showTwoBitNum(tempyear));
                mMonthTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.MONTH) + 1));
                mDayTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.DAY_OF_MONTH)));
                mHourTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.HOUR_OF_DAY)));
                mMinuteTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.MINUTE)));
                mSecondTx.setText(Utils.showTwoBitNum(calendar.get(Calendar.SECOND)));
                mStepTx.setText("1");
                mHandler.postDelayed(mRunnable, 1000);
            }
        }
    };

    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {
        @Override
        public void onLocationChanged(BDRNSSLocation arg0) {
            super.onLocationChanged(arg0);
            locationTime = arg0.getTime();
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

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                locationTime = location.getTime();
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROMPT_CHECK_BD_TIME: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("提示");
                    builder.setMessage("当前时间不正确,请先进行校时!");
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    });
                    builder.create().show();
                    break;
                }
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bd_zuozhan_time);
        initUI();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    private void initUI() {
        //RDSS中间件对象
        mBDCommManager = BDCommManager.getInstance(mContext);
        if ("S500".equals(Utils.DEVICE_MODEL)) {
            BDLocationManager.getInstance(mContext).requestLocationUpdate(locationListener);
        } else {
            try {
                mBDCommManager.addBDEventListener(mBDRNSSLocationListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
        mYearTx = (TextView) this.findViewById(R.id.zuozhan_time_year_tx);
        mMonthTx = (TextView) this.findViewById(R.id.zuozhan_time_month_tx);
        mDayTx = (TextView) this.findViewById(R.id.zuozhan_time_day_tx);
        mHourTx = (TextView) this.findViewById(R.id.zuozhan_time_hour_tx);
        mMinuteTx = (TextView) this.findViewById(R.id.zuozhan_time_minute_tx);
        mSecondTx = (TextView) this.findViewById(R.id.zuozhan_time_second_tx);
        mStepTx = (TextView) this.findViewById(R.id.zuozhan_time_step_tx);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /*设置字体*/
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/DIGIFACEWIDE.TTF");
        mYearTx.setTypeface(typeface);
        mMonthTx.setTypeface(typeface);
        mDayTx.setTypeface(typeface);
        mHourTx.setTypeface(typeface);
        mMinuteTx.setTypeface(typeface);
        mSecondTx.setTypeface(typeface);
        mStepTx.setTypeface(typeface);
        mHandler.post(mRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        if (!"S500".equals(Utils.DEVICE_MODEL)) {
            if (mBDCommManager != null) {
                try {
                    mBDCommManager.removeBDEventListener(mBDRNSSLocationListener);
                } catch (BDUnknownException e) {
                    e.printStackTrace();
                } catch (BDParameterException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}