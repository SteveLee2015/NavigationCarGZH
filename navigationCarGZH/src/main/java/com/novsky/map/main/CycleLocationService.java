package com.novsky.map.main;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.location.BDParameterException;
import android.location.BDRDSSManager.ImmediateLocState;
import android.location.BDUnknownException;
import android.location.CardInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.log.Logger;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.LocSetDatabaseOperation;
import com.novsky.map.util.LocationSet;
import com.novsky.map.util.Utils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 循环位置报告服务
 * 
 * @author steve
 */
public class CycleLocationService extends Service {

	private final static String TAG = "CycleLocationService";
	private Timer timer = null;
	private int locationFequency = 0;
	private static WakeLock wakeLock = null;
	private int mCountNum = 0;
	private int cardFreq = 0;
	private LocationSet mLocationSet = null;
	private LocSetDatabaseOperation oper = null;
	private BDCommManager mBDCommManager = null;
	private MediaPlayer mediaPlayer;

	@Override
	public void onCreate() {
		super.onCreate();
		CardInfo cardInfo = BDCardInfoManager.getInstance().getCardInfo();
		if (cardInfo != null) {
			cardFreq = cardInfo.getSericeFeq();
		}
		initSound();
		mBDCommManager = BDCommManager.getInstance(this);
		oper = new LocSetDatabaseOperation(this);
		mLocationSet = oper.getFirst();
		locationFequency = Integer.valueOf(mLocationSet.getLocationFeq());
		mCountNum = locationFequency;
		// 初始化
		timer = new Timer();
		// 定时器发送广播
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (mCountNum > 0) {
					mCountNum--;
				} else if (mCountNum == 0) {
					mLocationSet = oper.getFirst();
					locationFequency = Integer.valueOf(mLocationSet
							.getLocationFeq());
					mCountNum = locationFequency;
					Utils.COUNT_DOWN_TIME = locationFequency;
					try {
						mBDCommManager.sendLocationInfoReqCmdBDV21(
								ImmediateLocState.LOC_NORMAL_FLAG, Integer
										.valueOf(mLocationSet.getHeightType()),
								"L",
								Double.valueOf(mLocationSet.getHeightValue())
										.doubleValue(),
								Double.valueOf(mLocationSet.getTianxianValue())
										.doubleValue(), 0);
						playSoundAndVibrate();
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (BDParameterException e) {
						e.printStackTrace();
					} catch (BDUnknownException e) {
						e.printStackTrace();
					}
				}
			}
		}, 0, 1000);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"CycleLocationService");
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
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
		if (oper != null) {
			oper.close();
			oper = null;
		}
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	private void initSound() {
		if (mediaPlayer == null) {
			//this.getApplication().setVolumeControlStream(AudioManager.STREAM_MUSIC);
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

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	private void playSoundAndVibrate() {
		Logger.e("CycleLocationService","CycleLocationService mediaPlayer start2222");
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
	}

}
