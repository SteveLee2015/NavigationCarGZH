package com.novsky.map.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.location.BDEventListener;
import android.location.BDLocation;
import android.location.BDParameterException;
import android.location.BDRDSSManager.HeightType;
import android.location.BDRDSSManager.ImmediateLocState;
import android.location.BDUnknownException;
import android.location.CardInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.log.Logger;
import com.novsky.map.main.BDResponseListener;
import com.novsky.map.main.CustomListView;
import com.novsky.map.main.CycleLocationService;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDRDSSLocationInfoManager;
import com.novsky.map.util.BDTimeCountManager;
import com.novsky.map.util.BDTimeFreqChangedListener;
import com.novsky.map.util.LocSetDatabaseOperation;
import com.novsky.map.util.LocationSet;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.mapabc.android.activity.R.id.bd1LocationBtn;

/**
 * 北斗I代定位  RDSS
 *
 * @author steve
 */
public class BD1LocFragment extends Fragment implements
        OnClickListener, OnCustomListListener {

    /**
     * Log日志标识
     */
    private static final String TAG = "BD1LocActivity";
    /**
     * 北斗I代定位数据经度的文本组件
     */
    private TextView mLongitude = null;
    /**
     * 北斗I代定位数据纬度的文本组件
     */
    private TextView mLatitude = null;

    /**
     * 北斗I代定位数据高程的文本组件
     */
    private TextView mHeight = null;
    /**
     * 北斗I代定位数据时间的文本组件
     */
    private TextView mTime = null;
    /**
     * 显示“经度”或"X"字样的文本组件
     */
    private TextView mLongitudeLable = null;
    /**
     * 显示"维度"或"Y"字样的文本组件
     */
    private TextView mLatitudeLable = null;

    /**
     * 显示"高程"或"Z"字样的文本组件
     */
    private TextView mHeightLable = null;

    /**
     * 紧急定位Button
     */
    private Button immediateLocationBtn = null;

    /**
     * 开始定位Button
     */
    private Button startLocationBtn = null;

    /**
     * RDSS管理类
     */
    private BDCommManager manager = null;

    /**
     * 坐标类型
     */
    private CustomListView customeListView = null;

    /**
     * 坐标类型的标识
     */
    private static int COODRINATE_FLAG = 0;

    /**
     * 循环定位的服务频度
     */
    private int frequency = 0;


    private ProgressDialog progressDialog = null;

    /**
     * 音频播放对象
     */
    private MediaPlayer mediaPlayer;

    /**
     * 定位设置
     */
    private LocationSet set = null;

    /**
     * 北斗时间频率管理类对象
     */
    private BDTimeCountManager timeInstance = null;


    private static final int BD_LOCATION_ITEM = 0x100;


    private static final int BD_TIME_FREQ_ITEM = 0x101;

    /**
     * 反馈信息监听器
     */
    private BDEventListener fkilistener = null;


    private BDCardInfoManager cardManager = null;

    private LocSetDatabaseOperation operation = null;

    private MyCount myCount;//定位倒计时

    private ImmediateCount mImmediateCount;//紧急定位倒计时

    private int sericeFeq = 10;

    private BDTimeFreqChangedListener timeLocFreqListener = new BDTimeFreqChangedListener() {
        @Override
        public void onTimeChanged(int remainder_time) {
            /* 用消息传递数据 */
            Message msg = new Message();
            msg.arg1 = remainder_time;
            msg.what = BD_TIME_FREQ_ITEM;
            mhandler.sendMessage(msg);
        }
    };

    /**
     * 定位数据显示
     */
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BD_LOCATION_ITEM:
                    progressDialog.dismiss();
                    playSoundAndVibrate();
                    Utils.isProgressDialogShowing = false;
                    BDLocation location = (BDLocation) msg.obj;
                    mLongitude.setText(Utils.setDoubleNumberDecimalDigit(location.mLongitude, 10));
                    mLatitude.setText(Utils.setDoubleNumberDecimalDigit(location.mLatitude, 10));
                    mHeight.setText(String.valueOf(location.earthHeight));

                    //系统时间
                    /**
                     *
                     SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     String time=sdf.format(new Date());
                     mTime.setText(time);
                     */

                    //卫星时间
                    String locationTime = location.mLocationTime;
                    String time = "00:00:00.00";
                    if (locationTime.length() >= 6) {
                        String hh = locationTime.substring(0, 2);
                        int anInt = Integer.parseInt(hh);
                        int beijingTime = (anInt + 8) % 24;
                        String mm = locationTime.substring(2, 4);
                        String ss = locationTime.substring(4, locationTime.length());
                        time = beijingTime + ":" + mm + ":" + ss;
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String systemTime = sdf.format(new Date());
                        mTime.setText(systemTime);
                    }

                    mTime.setText(time);
                    break;
                case BD_TIME_FREQ_ITEM:
                    int remainder_time = msg.arg1;
                    if (Utils.isCycleLocation) {
                        startLocationBtn.setText("结束定位(" + remainder_time + "秒)");
                    } else {
                        if (remainder_time == 0) {
                            startLocationBtn.setText("开始定位");
                            Utils.isImmediateLocation = false;
                        } else {
                            startLocationBtn.setText("开始定位(" + remainder_time + "秒)");
                            if (Utils.isImmediateLocation) {
                                immediateLocationBtn.setEnabled(true);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 定位监听器
     */
    private BDEventListener listener = new BDEventListener.BDLocationListener() {
        @Override
        public void onLocationChange(BDLocation location) {
            if (location != null) {
                BDRDSSLocationInfoManager singleton = BDRDSSLocationInfoManager.getInstance();
                singleton.setBDLocation(location);
                BDLocation bdlocation = Utils.translate(singleton.getBDLocatoion(), COODRINATE_FLAG);
				/* 用消息传递数据 */
                Message msg = new Message();
                msg.obj = bdlocation;
                msg.what = BD_LOCATION_ITEM;
                mhandler.sendMessage(msg);
            }
        }
    };
    private CardInfo cardInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rooter = inflater.inflate(R.layout.activity_bd1_loc, null);
        operation = new LocSetDatabaseOperation(getActivity());
		/* 判断是否安装北斗卡 */
        //if (!Utils.checkBDSimCard(getActivity()))return;
		/* 定位设置查询 */

        //如何读取卡频

        cardInfo = BDCardInfoManager.getInstance().getCardInfo();

        initUI(rooter);
        return rooter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (cardInfo != null) {
            sericeFeq = cardInfo.getSericeFeq();
        }

        myCount = new MyCount(sericeFeq * 1000, 1000);
        mImmediateCount = new ImmediateCount(sericeFeq * 2 * 1000, 1000);

        BDRDSSLocationInfoManager singleton = BDRDSSLocationInfoManager.getInstance();
        BDLocation location = singleton.getBDLocatoion();
        if (location != null) {
            if (location.mLongitude != 0) {
                mLongitude.setText(location.mLongitude + "");
            }
            if (location.mLongitude != 0) {
                mLatitude.setText(location.mLatitude + "");
            }
            if (location.earthHeight != 0) {
                mHeight.setText(location.earthHeight + "");
            }
            if (!location.mLocationTime.equals("")) {
                mTime.setText(location.mLocationTime);
            }
        }
        manager = BDCommManager.getInstance(getActivity());
        try {
            manager.addBDEventListener(fkilistener, listener);
        } catch (BDUnknownException e) {
            e.printStackTrace();
        } catch (BDParameterException e) {
            e.printStackTrace();
        }
        //timeInstance.registerBDTimeFreqListener(BD1LocFragment.class.getSimpleName(),timeLocFreqListener);
        boolean isStart = Utils.isServiceRunning(getActivity(), "com.novsky.map.main.CycleLocationService");
        if (isStart) {
            startLocationBtn.setText(getActivity().getResources().getString(R.string.bdloc_stop_loc_str));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            timeInstance.unRegisterBDTimeFreqListener(BD1LocFragment.class.getSimpleName());
            manager.removeBDEventListener(fkilistener, listener);
        } catch (BDUnknownException e) {
            e.printStackTrace();
        } catch (BDParameterException e) {
            e.printStackTrace();
        }
    }

    /**
     * 经纬度显示
     */
    public void mSwitchCoodriate(BDLocation location) {
        // 设置经度
        mLongitude.setText((location != null && location.mLongitude != 0.0) ? String.valueOf(location.mLongitude)
                : getActivity().getResources().getString(R.string.common_lon_value));
        // 设置纬度
        mLatitude.setText((location != null && location.mLatitude != 0.0) ? String.valueOf(location.mLatitude)
                : getActivity().getResources().getString(R.string.common_lat_value));
        // 设置高程
        mHeight.setText((location != null && location.getEarthHeight() != 0.0) ? String.valueOf(location.getEarthHeight())
                : getActivity().getResources().getString(R.string.common_dadi_heigh_value));
    }

    /**
     * 修改Lable
     */
    public void mSwitchCoodriateLabel(String longitudeLableText,
                                      String mLatitudeLableText, String mHeightLableText) {
        mLongitudeLable.setText(longitudeLableText);
        mLatitudeLable.setText(mLatitudeLableText);
        mHeightLable.setText(mHeightLableText);
    }


    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (operation != null) {
            operation.close();
        }
    }

    public void initUI(View view) {

        initSound();
        cardManager = BDCardInfoManager.getInstance();
        fkilistener = new BDResponseListener(getActivity());
        timeInstance = BDTimeCountManager.getInstance();
        mLongitude = (TextView) view.findViewById(R.id.bdloc_lon);
        mLatitude = (TextView) view.findViewById(R.id.bdloc_lat);
        mHeight = (TextView) view.findViewById(R.id.bdloc_height);
        mTime = (TextView) view.findViewById(R.id.bdloc_time);
        immediateLocationBtn = (Button) view.findViewById(R.id.bd1ImmediateLocBtn);
        startLocationBtn = (Button) view.findViewById(bd1LocationBtn);
        mLongitudeLable = (TextView) view.findViewById(R.id.bdloc_lon_lable);
        mLatitudeLable = (TextView) view.findViewById(R.id.bdloc_lat_lable);
        mHeightLable = (TextView) view.findViewById(R.id.bdloc_height_lable);
        customeListView = (CustomListView) view.findViewById(R.id.bd_coodr_type);
        customeListView.setData(getResources().getStringArray(R.array.bdloc_zuobiao_array));
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("RDSS定位");
        progressDialog.setMessage("正在定位中...");
        startLocationBtn.setOnClickListener(this);
        immediateLocationBtn.setOnClickListener(this);
        customeListView.setOnCustomListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case bd1LocationBtn: {
                boolean isStart = Utils.isServiceRunning(getActivity(), "com.novsky.map.main.CycleLocationService");
                if (!isStart) {
					/* 判断是否安装北斗卡 */
                    if (!Utils.checkBDSimCard(getActivity())) return;
					/* 定位设置查询 */
                    set = operation.getFirst();
                    frequency = Integer.valueOf(set.getLocationFeq());
					/*检查是否是循环定位*/
                    if (frequency > 0) {
                        startLocationBtn.setText(getActivity().getResources().getString(R.string.bdloc_stop_loc_str));
                        Utils.COUNT_DOWN_TIME = frequency;
                        Utils.isCycleLocation = true;
                        Intent mIntent = new Intent();
                        mIntent.setClass(getActivity(), CycleLocationService.class);
                        getActivity().startService(mIntent);
                    } else {
                        Utils.COUNT_DOWN_TIME = cardManager.getCardInfo().mSericeFeq;
                        if (myCount != null) {
                            myCount.start();
                        }
                        startLocationBtn.setClickable(false);
                    }
                    try {
                        manager.sendLocationInfoReqCmdBDV21(ImmediateLocState.LOC_NORMAL_FLAG, Integer.valueOf(set.getHeightType()), "L", Double.valueOf(set.getHeightValue()).doubleValue(),
                                Double.valueOf(set.getTianxianValue()).doubleValue(), 0);
                        progressDialog.show();
                        Utils.isProgressDialogShowing = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (Utils.isProgressDialogShowing) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity().getApplicationContext(), "未接收到定位信息!", Toast.LENGTH_LONG).show();
                                    Utils.isProgressDialogShowing = false;
                                }
                            }
                        }, 8000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //结束循环定位
                    Intent mIntent = new Intent();
                    mIntent.setClass(getActivity(), CycleLocationService.class);
                    getActivity().stopService(mIntent);
                    Utils.isCycleLocation = false;
                    startLocationBtn.setText(getActivity().getResources().getString(R.string.bdloc_common_loc_str));
                }
                break;
            }
            case R.id.bd1ImmediateLocBtn: {
                try {
					/* 判断是否安装北斗卡 */
                    if (!Utils.checkBDSimCard(getActivity())) return;
                    Utils.isImmediateLocation = true;
                    set = operation.getFirst();
                    if (mImmediateCount != null) {
                        mImmediateCount.start();
                    }
                    immediateLocationBtn.setClickable(false);
                    manager.sendLocationInfoReqCmdBDV21(ImmediateLocState.LOC_IMMEDIATE_FLAG, HeightType.HAVE_HEIGHT_VALUE, "L", Double.valueOf(set.getHeightValue()).doubleValue(),
                            Double.valueOf(set.getTianxianValue()).doubleValue(), 0);
                    Utils.COUNT_DOWN_TIME = (cardManager.getCardInfo().mSericeFeq) * 2;
                    progressDialog.show();
                    Utils.isProgressDialogShowing = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Utils.isProgressDialogShowing) {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), "未接收到定位信息!", Toast.LENGTH_LONG).show();
                                Utils.isProgressDialogShowing = false;
                            }
                        }
                    }, 8000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onListIndex(int num) {
        COODRINATE_FLAG = num;
        BDLocation param = null;
        BDRDSSLocationInfoManager singleton = BDRDSSLocationInfoManager.getInstance();
        BDLocation bdlocation = singleton.getBDLocatoion();
        if (bdlocation != null && bdlocation.getLongitude() != 0.0 && bdlocation.getLatitude() != 0.0) {
            param = Utils.translate(bdlocation, num);
            mSwitchCoodriate(param);
        }
        switch (num) {
            case 0://2000大地
                mSwitchCoodriateLabel(getActivity().getResources().getString(R.string.common_lon_str),
                        getActivity().getResources().getString(R.string.common_lat_str),
                        getActivity().getResources().getString(R.string.common_height_str));
                break;
            case 1://高斯
                mSwitchCoodriateLabel(getActivity().getResources().getString(R.string.common_x_cood),
                        getActivity().getResources().getString(R.string.common_y_cood),
                        getActivity().getResources().getString(R.string.common_z_cood));
                break;
            case 2://麦卡托
                mSwitchCoodriateLabel(getActivity().getResources().getString(R.string.common_x_cood),
                        getActivity().getResources().getString(R.string.common_y_cood),
                        getActivity().getResources().getString(R.string.common_z_cood));
                break;
            case 3://空间直角
                mSwitchCoodriateLabel(getActivity().getResources().getString(R.string.common_x_cood),
                        getActivity().getResources().getString(R.string.common_y_cood),
                        getActivity().getResources().getString(R.string.common_z_cood));
                break;
            case 4://beijing 54
                mSwitchCoodriateLabel(getActivity().getResources().getString(R.string.common_lon_str),
                        getActivity().getResources().getString(R.string.common_lat_str),
                        getActivity().getResources().getString(R.string.common_height_str));
                break;
            default:
                break;
        }
    }

    private void initSound() {
        if (mediaPlayer == null) {
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.location);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(0.1f, 0.1f);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private void playSoundAndVibrate() {
        Logger.e("BD1LocFragment","mediaPlayer start11111");
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            startLocationBtn.setText("剩余:" + millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            startLocationBtn.setText("开始定位");
            startLocationBtn.setClickable(true);
        }

    }

    class ImmediateCount extends CountDownTimer {

        public ImmediateCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            immediateLocationBtn.setText("剩余:" + millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            immediateLocationBtn.setText("紧急定位");
            immediateLocationBtn.setClickable(true);
        }

    }


}