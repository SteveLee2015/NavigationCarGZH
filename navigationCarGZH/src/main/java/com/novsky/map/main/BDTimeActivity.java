package com.novsky.map.main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.novsky.map.util.BDLocationManager;
import com.novsky.map.util.Utils;

/**
 * 北斗时间
 * 功能描述:1.首次进入校时界面,实时显示本机时间。
 * 2.点击"卫星校验"后,设置当前设备的时间。
 *
 * @author steve
 */
public class BDTimeActivity extends Activity {

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
     * 显示'周'
     */
    private TextView mWeekDayTx = null;


    /**
     * 校验时间按钮
     */
    private Button checkTimeBtn = null;

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

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Calendar mCalendar = Calendar.getInstance();
            String year = String.valueOf(mCalendar.get(Calendar.YEAR));
            year = year.substring(year.length() - 2);
            mYearTx.setText(year);
            mMonthTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.MONTH) + 1));
            mDayTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.DAY_OF_MONTH)));
            mHourTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.HOUR_OF_DAY)));
            mMinuteTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.MINUTE)));
            mSecondTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.SECOND)));
            mWeekDayTx.setText(Utils.getCurrentWeekDay(mCalendar.get(mCalendar.DAY_OF_WEEK) - 1));
            mHandler.postDelayed(mRunnable, 1000);
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
                case COMPLETE_CHECK_BD_TIME: {
                    Toast.makeText(mContext, "卫星校时完成!", Toast.LENGTH_SHORT).show();
                    break;
                }
                case PROMPT_CHECK_BD_TIME: {
                    Toast.makeText(mContext, "当前卫星信号差,校时失败!", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_bdtime);
        initUI();
        checkTimeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if ("S500".equals(Utils.DEVICE_MODEL)) {
                    handleTimeChange(locationTime);
                } else {
                    handleTimeChange(locationTime + 8 * 3600 * 1000);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 修改时间
     *
     * @param time
     */
    private void handleTimeChange(long time) {
        Calendar calendar = Calendar.getInstance();
        long currentTime = time;
        calendar.setTimeInMillis(currentTime);
        int year = calendar.get(Calendar.YEAR);
        // 由于模块在启动后，未与卫星通信时，默认的时间是2000-1-1 00:00:00,
        // 所以判断如果年份小于等于2000，则提示用户"请到空旷的地方，方便卫星校时"
        if (year <= 2000) {
            mHandler.sendEmptyMessage(PROMPT_CHECK_BD_TIME);
        } else {
            mHandler.sendEmptyMessage(COMPLETE_CHECK_BD_TIME);
            try {
                SystemDateTime.setDateTime(currentTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        mYearTx = (TextView) this.findViewById(R.id.time_year_tx);
        mMonthTx = (TextView) this.findViewById(R.id.time_month_tx);
        mDayTx = (TextView) this.findViewById(R.id.time_day_tx);
        mHourTx = (TextView) this.findViewById(R.id.time_hour_tx);
        mMinuteTx = (TextView) this.findViewById(R.id.time_minute_tx);
        mSecondTx = (TextView) this.findViewById(R.id.time_second_tx);
        mWeekDayTx = (TextView) this.findViewById(R.id.time_weekday_tx);
        checkTimeBtn = (Button) this.findViewById(R.id.bd_check_time_btn);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /*设置字体*/
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/DIGIFACEWIDE.TTF");
        mYearTx.setTypeface(typeface);
        mMonthTx.setTypeface(typeface);
        mDayTx.setTypeface(typeface);
        mHourTx.setTypeface(typeface);
        mMinuteTx.setTypeface(typeface);
        mSecondTx.setTypeface(typeface);
        mWeekDayTx.setTypeface(typeface);
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