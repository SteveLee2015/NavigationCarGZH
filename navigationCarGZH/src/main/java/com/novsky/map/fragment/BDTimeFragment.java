package com.novsky.map.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDLocationZDAListener;
import com.bd.comm.protocal.ZDATime;
import com.mapabc.android.activity.R;
import com.novsky.map.main.SystemDateTime;
import com.novsky.map.util.BDLocationManager;
import com.novsky.map.util.Utils;

import java.io.IOException;
import java.util.Calendar;

/**
 * 北斗时间
 * 功能描述:1.首次进入校时界面,实时显示本机时间。
 * 2.点击"卫星校验"后,设置当前设备的时间。
 *
 * @author steve
 */
public class BDTimeFragment extends Fragment {
    /**
     * 上下文对象
     */
    private Context mContext;
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
     * RDSS中间件对象
     */
    private BDCommManager mBDCommManager = null;

    /**
     * 定位时间
     */
    private long locationTime = 0;

    String[] dt = null;
    String[] ymd = null;
    String[] hms = null;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Calendar mCalendar = Calendar.getInstance();
            if (ymd == null || hms == null) {
                String year = String.valueOf(mCalendar.get(Calendar.YEAR));
                year = year.substring(year.length() - 2);

                mYearTx.setText(year);
                mMonthTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.MONTH) + 1));
                mDayTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.DAY_OF_MONTH)));
                mHourTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.HOUR_OF_DAY)));
                mMinuteTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.MINUTE)));
                mSecondTx.setText(Utils.showTwoBitNum(mCalendar.get(Calendar.SECOND)));
            } else {
                mYearTx.setText(ymd[0]);
                mMonthTx.setText(ymd[1]);
                mDayTx.setText(ymd[2]);
                mHourTx.setText(hms[0]);
                mMinuteTx.setText(hms[1]);
                mSecondTx.setText(hms[2]);
            }
            mWeekDayTx.setText(Utils.getCurrentWeekDay(mCalendar.get(mCalendar.DAY_OF_WEEK) - 1));
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    private Calendar calendar = Calendar.getInstance();

    private BDLocationZDAListener mBDLocationZDAListener = new BDLocationZDAListener() {

        @Override
        public void onZDATime(ZDATime mZDATime) {
            if (mZDATime != null) {
                if (calendar != null) {
                    calendar.clear();
                    calendar.set(Calendar.YEAR ,Integer.valueOf((!TextUtils.isEmpty(mZDATime.getYear())) ? mZDATime.getYear():"1970"));
                    calendar.set(Calendar.MONTH , Integer.valueOf((!TextUtils.isEmpty(mZDATime.getMonth()))? mZDATime.getMonth(): "1"));
                    calendar.set(Calendar.DAY_OF_MONTH , Integer.valueOf((!TextUtils.isEmpty(mZDATime.getDay()))? mZDATime.getDay(): "1"));

                    String hMs = (!TextUtils.isEmpty(mZDATime.getUtcTime())) ? mZDATime.getUtcTime():"000000.000";
                    if (!TextUtils.isEmpty(hMs) && hMs.length() > 6) {
                        int hour = Integer.valueOf(hMs.substring(0, 2));
                        int minute = Integer.valueOf(hMs.substring(2, 4));
                        int second = Integer.valueOf(hMs.substring(4,6));

                        String timeZone = mZDATime.getTimeZone();
                        if (!TextUtils.isEmpty(timeZone)) {
                            if (Integer.valueOf((!TextUtils.isEmpty(timeZone)) ? timeZone:"0") == 0) {
                                 hour += 8;
                            }
                        }
                        calendar.set(Calendar.HOUR_OF_DAY , hour);
                        calendar.set(Calendar.MINUTE , minute);
                        calendar.set(Calendar.SECOND , second);
                    }
                    locationTime = calendar.getTimeInMillis();
                }
                Log.i(TAG ,"=======================>mBDLocationZDAListener locationTime =" +locationTime);
            }
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
                    Calendar mCalendar = Calendar.getInstance();
                    try {
                        mCalendar.setTimeInMillis(locationTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView;
        mContext = getActivity();
        contentView = View.inflate(mContext, R.layout.activity_bdtime, null);
        initUI(contentView);
        checkTimeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                handleTimeChange(locationTime);
            }
        });
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 修改时间
     *
     * @param time
     */
    private void handleTimeChange(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int year = calendar.get(Calendar.YEAR);
        // 由于模块在启动后，未与卫星通信时，默认的时间是2000-1-1 00:00:00,
        // 所以判断如果年份小于等于2000，则提示用户"请到空旷的地方，方便卫星校时"
        if (year <= 2000) {
            mHandler.sendEmptyMessage(PROMPT_CHECK_BD_TIME);
        } else {
            mHandler.sendEmptyMessage(COMPLETE_CHECK_BD_TIME);
            try {
                SystemDateTime.setDateTime(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void initUI(View contentView) {
        //RDSS中间件对象
        mBDCommManager = BDCommManager.getInstance(mContext);
        if ("S500".equals(Utils.DEVICE_MODEL)) {
            BDLocationManager.getInstance(mContext).requestLocationUpdate(locationListener);
        } else {
            try {
                mBDCommManager.addBDEventListener(mBDLocationZDAListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
        mYearTx = (TextView) contentView.findViewById(R.id.time_year_tx);
        mMonthTx = (TextView) contentView.findViewById(R.id.time_month_tx);
        mDayTx = (TextView) contentView.findViewById(R.id.time_day_tx);
        mHourTx = (TextView) contentView.findViewById(R.id.time_hour_tx);
        mMinuteTx = (TextView) contentView.findViewById(R.id.time_minute_tx);
        mSecondTx = (TextView) contentView.findViewById(R.id.time_second_tx);
        mWeekDayTx = (TextView) contentView.findViewById(R.id.time_weekday_tx);
        checkTimeBtn = (Button) contentView.findViewById(R.id.bd_check_time_btn);
		/*设置字体*/
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/DIGIFACEWIDE.TTF");
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
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        if (!"S500".equals(Utils.DEVICE_MODEL)) {
            if (mBDCommManager != null) {
                try {
                    mBDCommManager.removeBDEventListener(mBDLocationZDAListener);
                } catch (BDUnknownException e) {
                    e.printStackTrace();
                } catch (BDParameterException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}