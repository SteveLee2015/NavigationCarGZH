package com.bd.comm.protocal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.BDBeam;
import android.location.BDEventListener;
import android.location.BDEventListener.AutoDestroyListener;
import android.location.BDEventListener.BDBeamStatusListener;
import android.location.BDEventListener.BDFKIListener;
import android.location.BDEventListener.BDKLTListener;
import android.location.BDEventListener.BDLocReportListener;
import android.location.BDEventListener.BDLocationListener;
import android.location.BDEventListener.LocalInfoListener;
import android.location.BDEventListener.ManagerInfoListener;
import android.location.BDEventListener.OutputTimeListener;
import android.location.BDEventListener.VersionListener;
import android.location.BDEventListener.ZeroInfoListener;
import android.location.BDEventListener.ZhiHuiListener;
import android.location.BDLocation;
import android.location.BDLocationReport;
import android.location.BDMessageInfo;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.location.CardInfo;
import android.net.ParseException;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import android_serialport_api.SerialPort;

/**
 * 北斗命令管理类
 * @author steve
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class BDCommManager {
	
	private static String TAG="BDCommManager";
	private static BDCommManager instance=null;
	private static String DEVICE_MODEL="";
	private Context mContext=null;
	private SerialApplication mApplication =null;
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	protected InputStream mInputStream;
	/**一个读取线程 读取串口 解析数据*/
	protected ReadThread mReadThread;
	
	private BDRDSSManager mBDRDSSManager=null;
	
	private BDRNSSLocation bdLocation, gpLocation, gnLocation;
	
	/**短报文解析完成 发送广播通知*/
	public static final String PROVIDERS_CHANGED_ACTION = "android.location.PROVIDERS_CHANGED";
	/**广播通知携带的数据***/
	public static final String BDRDSS_MESSAGE = "bdrdss_message";
	
	private ArrayList<BDLocationListener> mBDLocationListeners = new ArrayList<BDLocationListener>();//定位申请
	private ArrayList<VersionListener> mVersionListeners = new ArrayList<VersionListener>();
	private ArrayList<BDKLTListener> mBDKLTListeners = new ArrayList<BDKLTListener>();
	private ArrayList<ZhiHuiListener> mZhiHuiListeners = new ArrayList<ZhiHuiListener>();
	private ArrayList<BDFKIListener> mBDFKIListeners = new ArrayList<BDFKIListener>();
	private ArrayList<OutputTimeListener> mOutputTimeListeners = new ArrayList<OutputTimeListener>();
	private ArrayList<LocalInfoListener> mLocalInfoListeners = new ArrayList<LocalInfoListener>();
	private ArrayList<ManagerInfoListener> mManagerInfoListeners = new ArrayList<ManagerInfoListener>();
	private ArrayList<BDBeamStatusListener> mBDBeamStatusListeners = new ArrayList<BDBeamStatusListener>();
	private ArrayList<BDLocReportListener> mBDLocReportListeners = new ArrayList<BDLocReportListener>();//位置报告1
	private ArrayList<ZeroInfoListener> mZeroInfoListeners = new ArrayList<ZeroInfoListener>();
	private ArrayList<AutoDestroyListener> mAutoDestroyListeners = new ArrayList<AutoDestroyListener>();
	private ArrayList<BDRNSSLocationListener> mBDRNSSLocationListeners=new ArrayList<BDRNSSLocationListener>();
	private ArrayList<BDSatelliteListener> mBDSatelliteListeners=new ArrayList<BDSatelliteListener>();
	private ArrayList<GPSatelliteListener> mGPSatelliteListeners=new ArrayList<GPSatelliteListener>();
	private ArrayList<BDLocationStrategyListener> mBDLocationStrategyListeners=new ArrayList<BDLocationStrategyListener>();
	private ArrayList<BDLocationZDAListener> mBDLocationZDAListeners=new ArrayList<BDLocationZDAListener>();

	
	private int[]  mBDFixNumberArray=new int[33];
	private int[]  mGPSFixNumberArray=new int[33];
	private List<BDSatellite> bdlist = null;
	private List<GPSatellite> gplist = null;

    public static BDCommManager getInstance(Context context) {
        if (instance == null) {
            synchronized (BDCommManager.class) {
                if (instance == null)
                    instance = new BDCommManager(context);
            }
        }
        return instance;
    }

	private BDCommManager() {
		super();
	}
	
	/**
	 * 注册监听类
	 * 
	 * @param listeners
	 */
	public void addBDEventListener(BDEventListener... listeners)
			throws BDParameterException, BDUnknownException {
		Log.i(TAG,"addBDEventListener(listeners = " + listeners + ")...");
		BDEventListener[] listener = listeners;
		try {
			if("S500".equals(DEVICE_MODEL)){
				mBDRDSSManager.addBDEventListener(listeners);
			}else{
				for (int i = 0; i < listener.length; i++) {
					Log.i(TAG,"listener[" + i + "]=" + listener[i]);
					// 检查是否已经添加过相同的监听类，如已添加跳过本次循环
					if (mBDLocationListeners.contains(listener[i])
							|| mVersionListeners.contains(listener[i])
							|| mBDKLTListeners.contains(listener[i])
							|| mZhiHuiListeners.contains(listener[i])
							|| mBDFKIListeners.contains(listener[i])
							|| mOutputTimeListeners.contains(listener[i])
							|| mLocalInfoListeners.contains(listener[i])
							|| mManagerInfoListeners.contains(listener[i])
							|| mBDBeamStatusListeners.contains(listener[i])
							|| mBDLocReportListeners.contains(listener[i])
							|| mZeroInfoListeners.contains(listener[i])
							|| mBDLocationZDAListeners.contains(listener[i])
							|| mAutoDestroyListeners.contains(listener[i])) {
						continue;
					}
					if (listener[i] instanceof BDLocationListener) {
						mBDLocationListeners.add((BDLocationListener) listener[i]);
					} else if (listener[i] instanceof VersionListener) {
						mVersionListeners.add((VersionListener) listener[i]);
					} else if (listener[i] instanceof BDLocationZDAListener) {
						mBDLocationZDAListeners.add((BDLocationZDAListener) listener[i]);
					} else if (listener[i] instanceof BDKLTListener) {
						mBDKLTListeners.add((BDKLTListener) listener[i]);
					} else if (listener[i] instanceof ZhiHuiListener) {
						mZhiHuiListeners.add((ZhiHuiListener) listener[i]);
					} else if (listener[i] instanceof BDFKIListener) {
						mBDFKIListeners.add((BDFKIListener) listener[i]);
					} else if (listener[i] instanceof OutputTimeListener) {
						mOutputTimeListeners.add((OutputTimeListener) listener[i]);
					} else if (listener[i] instanceof LocalInfoListener) {
						mLocalInfoListeners.add((LocalInfoListener) listener[i]);
					} else if (listener[i] instanceof ManagerInfoListener) {
						mManagerInfoListeners.add((ManagerInfoListener) listener[i]);
					} else if (listener[i] instanceof BDBeamStatusListener) {
						mBDBeamStatusListeners.add((BDBeamStatusListener) listener[i]);
					} else if (listener[i] instanceof BDLocReportListener) {
						mBDLocReportListeners
								.add((BDLocReportListener) listener[i]);
					} else if (listener[i] instanceof ZeroInfoListener) {
						mZeroInfoListeners.add((ZeroInfoListener) listener[i]);
					} else if (listener[i] instanceof AutoDestroyListener) {
						mAutoDestroyListeners
								.add((AutoDestroyListener) listener[i]);
					}else if  (listener[i] instanceof BDRNSSLocationListener ){
						mBDRNSSLocationListeners.add((BDRNSSLocationListener) listener[i]);
					}else if(listener[i] instanceof BDSatelliteListener){
						mBDSatelliteListeners.add((BDSatelliteListener) listener[i]);
					}else if(listener[i] instanceof GPSatelliteListener){
						mGPSatelliteListeners.add((GPSatelliteListener) listener[i]);
					}else if(listener[i] instanceof BDLocationStrategyListener){
						mBDLocationStrategyListeners.add((BDLocationStrategyListener)listener[i]);
					}else {
						Log.i(TAG,"android.location.BDParameterException: addBDEventListener() lister does not exist!");
						throw new BDParameterException(
								"android.location.BDParameterException: addBDEventListener() lister does not exist!");
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG,
					"eandroid.location.BDUnknownException:addBDEventListener()",
					e);
			throw new BDUnknownException(
					"eandroid.location.BDUnknownException:addBDEventListener()",
					e);
		}

	}

	/**
	 * 注销监听类
	 * 
	 * @param listeners
	 */
	public void removeBDEventListener(BDEventListener... listeners)
			throws BDUnknownException, BDParameterException {
		// log("removeBDEventListener(listeners = " + listeners + ")...");
		BDEventListener[] listener = listeners;
		try {
			if("S500".equals(DEVICE_MODEL)){
				mBDRDSSManager.removeBDEventListener(listeners);
			}else{
			for (int i = 0; i < listener.length; i++) {
				// log("listener["+i+"]=" + listener[i]);
				if (listener[i] instanceof BDLocationListener) {
					mBDLocationListeners.remove(listener[i]);
				} else if (listener[i] instanceof VersionListener) {
					mVersionListeners.remove(listener[i]);
				} else if (listener[i] instanceof BDKLTListener) {
					mBDKLTListeners.remove(listener[i]);
				} else if (listener[i] instanceof ZhiHuiListener) {
					mZhiHuiListeners.remove(listener[i]);
				} else if (listener[i] instanceof BDFKIListener) {
					mBDFKIListeners.remove(listener[i]);
				} else if (listener[i] instanceof OutputTimeListener) {
					mOutputTimeListeners.remove(listener[i]);
				} else if (listener[i] instanceof LocalInfoListener) {
					mLocalInfoListeners.remove(listener[i]);
				} else if (listener[i] instanceof ManagerInfoListener) {
					mManagerInfoListeners.remove(listener[i]);
				} else if (listener[i] instanceof BDBeamStatusListener) {
					mBDBeamStatusListeners.remove(listener[i]);
				} else if (listener[i] instanceof BDLocationZDAListener) {
					mBDLocationZDAListeners.remove(listener[i]);
				} else if (listener[i] instanceof BDLocReportListener) {
					mBDLocReportListeners.remove(listener[i]);
				} else if (listener[i] instanceof ZeroInfoListener) {
					mZeroInfoListeners.remove(listener[i]);
				} else if (listener[i] instanceof AutoDestroyListener) {
					mAutoDestroyListeners.remove(listener[i]);
				}else if  (listener[i] instanceof BDRNSSLocationListener){
					mBDRNSSLocationListeners.remove(listener[i]);
				}else if(listener[i] instanceof BDSatelliteListener){
					mBDSatelliteListeners.remove(listener[i]);
				}else if(listener[i] instanceof GPSatelliteListener){
					mGPSatelliteListeners.remove(listener[i]);
				}else if(listener[i] instanceof BDLocationStrategyListener){
					mBDLocationStrategyListeners.remove(listener[i]);
				}
				else {
					Log.i(TAG,"android.location.BDParameterException: removeBDEventListener() lister does not exist!");
					throw new BDParameterException(
							"android.location.BDParameterException: removeBDEventListener() lister does not exist!");
				}
			  }
			}
		} catch (Exception e) {
			Log.e(TAG,
					"eandroid.location.BDUnknownException:removeBDEventListener()",
					e);
			throw new BDUnknownException(
					"eandroid.location.BDUnknownException:removeBDEventListener()",
					e);
		}
	}
	
	private BDCommManager(Context mContext) {
		super();
		this.mContext=mContext;
		DEVICE_MODEL=new Build().MODEL;
		if("S500".equals(DEVICE_MODEL)){
			mBDRDSSManager=BDRDSSManager.getDefault(mContext);
		}else{
			mApplication = (SerialApplication)mContext.getApplicationContext();
			try {
				//此处需要填写串口路路径
				//根据配置 选择串口路径  和  波特率
				//String comPath = mApplication.getString(R.string.device_com_path);
				//String baudratePath = mApplication.getString(R.string.device_com_baudrate);
				//int baudrate = Integer.parseInt(baudratePath);
				//mSerialPort = mApplication.getSerialPort(comPath,baudrate);
				//mSerialPort = mApplication.getSerialPort("/dev/ttyS3",115200);
				
				// 方式1外部配置文件配置
				Properties properties = MyProperUtil.getProperties(mContext);
				String serialport = properties.getProperty("serialport");
				String baudrate = properties.getProperty("baudrate");
				
				if (baudrate.isEmpty()) {
					Toast.makeText(mContext, "请配置串口波特率", Toast.LENGTH_SHORT).show();
					return;
				}
				
				int IntBaudrate = Integer.parseInt(baudrate);
				
				if (serialport.isEmpty()) {

					Toast.makeText(mContext, "请配置串口", Toast.LENGTH_SHORT).show();
					throw new RuntimeException("assett未配置串口" + TAG);

				} else {

					if ("ttyS0".equals(serialport)) {

						mSerialPort = mApplication.getSerialPort("/dev/ttyS0",IntBaudrate);
						Log.e(TAG, "配置串口为:/dev/ttyS0  波特率为:IntBaudrate");

					} else if ("ttyS3".equals(serialport)) {
						
						mSerialPort = mApplication.getSerialPort("/dev/ttyS3",IntBaudrate);
						Log.e(TAG, "配置串口为:/dev/ttyS3  波特率为:IntBaudrate");
						
					} else {
						//默认连接串口0
						mSerialPort = mApplication.getSerialPort("/dev/ttyS0",IntBaudrate);
						Log.e(TAG, "配置串口为:/dev/ttyS0  波特率为:IntBaudrate");
					}
				}
				
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();
				mReadThread = new ReadThread();
				mReadThread.start();
			} catch (InvalidParameterException e) {
				Log.i(TAG, "You do not have read/write permission to the serial port.");
			} catch (SecurityException e) {
				Log.i(TAG, "The serial port can not be opened for an unknown reason.");
			} catch (IOException e) {
				Log.i(TAG, "Please configure your serial port first.");
			} catch (Exception e) {
				Log.i(TAG, "The serial port can not be opened for an unknown reason.");
			}
		}
		
	}

	/**
	 * UM220
	 */
	public void openZDA_UM220(){

		String msg = "0,6,1";
		//校验码 *00
		try {
			String info = new String(bd2SendPackage("$CFGMSG", msg),"GBK");
			Log.d(TAG,info);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		write(bd2SendPackage("$CFGMSG", msg));

	}


	public void closeZDA_UM220(){

		String msg = "0,6,1";
		write(bd2SendPackage("$CFGMSG", msg));

	}
	/**
	 * 发送读取北斗卡的命令
	 * 
	 * @param type
	 * @param frameNo
	 * @throws BDUnknownException
	 */
	public void sendAccessCardInfoCmdBDV21(int type, int frameNo)
			throws BDUnknownException {
		if("S500".equals(DEVICE_MODEL)){
			try {
				mBDRDSSManager.sendAccessCardInfoCmdBDV21(type, frameNo);
			} catch (android.location.BDUnknownException e) {
				e.printStackTrace();
			}
		}else{
			try {
				StringBuffer sb = new StringBuffer();
				sb.append(type + ",");
				sb.append(frameNo);
				write(bd2SendPackage("$CCICA", sb.toString()));
			} catch (Exception e) {
				Log.e("BDRDSS",
						"android.location.BDUnknownException:sendAccessCardInfoCmdBDV21()",
						e);
				throw new BDUnknownException(
						"android.location.BDUnknownException:sendAccessCardInfoCmdBDV21()",
						e);
			}
		}
	}

	private void sendAccessVersionInfoCmdBDV40() throws BDUnknownException {
		try {

		} catch (Exception e) {
			Log.e("BDRDSS",
					"android.location.BDUnknownException:sendAccessVersionInfoCmdBDV40()",
					e);
			throw new BDUnknownException(
					"android.location.BDUnknownException: sendAccessVersionInfoCmdBDV40()",
					e);
		}
	}

	
	public void sendAccessSerialInfoCmdBDV40(int type, String serialNum)
			throws BDUnknownException, BDParameterException {
		if("S500".equals(DEVICE_MODEL)){
			try {
				mBDRDSSManager.sendAccessSerialInfoCmdBDV40(type, serialNum);
			} catch (android.location.BDUnknownException e) {
				e.printStackTrace();
			} catch (android.location.BDParameterException e) {
				e.printStackTrace();
			}
		}else{
			try {
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(type);
				stringBuffer.append(",");
				stringBuffer.append(serialNum);
				write(bd2SendPackage("$BDXHM",stringBuffer.toString()));
			} catch (Exception e) {
				Log.e("BDRDSS",
						"android.location.BDUnknownException: sendAccessSerialInfoCmdBDV40()",
						e);
				throw new BDUnknownException(
						"android.location.BDUnknownException: sendAccessSerialInfoCmdBDV40()",
						e);
			}
		}
	}

	private void sendTimeFreqCmdBDV40(int timeFreq) throws BDUnknownException {
		try {
		} catch (Exception e) {
			/* 496 */Log
					.e("BDRDSS",
							"android.location.BDUnknownException:sendTimeFreqCmdBDV40()",
							e);
			/* 497 */throw new BDUnknownException(
					"android.location.BDUnknownException:sendTimeFreqCmdBDV40()",
					e);
		}
	}

	/**
	 * 发送北斗2.1定位
	 * 
	 * @param isImmediate
	 * @param heightType
	 * @param heightFlag
	 * @param heightValue
	 * @param txValue
	 * @param timeFreq
	 * @throws BDParameterException
	 * @throws BDUnknownException
	 */
	public void sendLocationInfoReqCmdBDV21(boolean isImmediate,
			int heightType, String heightFlag, double heightValue,
			double txValue, int timeFreq) throws BDParameterException,
			BDUnknownException {
		if("S500".equals(DEVICE_MODEL)){
			try {
				mBDRDSSManager.sendLocationInfoReqCmdBDV21(isImmediate,heightType,heightFlag,heightValue,txValue,timeFreq);
			} catch (android.location.BDParameterException e) {
				e.printStackTrace();
			} catch (android.location.BDUnknownException e) {
				e.printStackTrace();
			}
		}else{
			try {
				boolean isShowHeight = false;
				if (heightFlag.equals("H"))
					isShowHeight = true;
				else if (heightFlag.equals("L"))
					isShowHeight = false;
				else {
					throw new BDParameterException(
							"android.location.BDParameterException: sendLocationInfoReqCmdBDV21() 'String heightFlag' exception");
				}
				double atmosphericPressure = 0d; // 气压数据
				double temperatureValue = 0d;// 温度
				StringBuffer sb = new StringBuffer();
				/* 定位地址 */
				String locUserAddress = "0000000";
				/* 是否是紧急定位 */
				String immediate = "";
				if (isImmediate) {
					immediate = "A"; // A字符
				} else {
					immediate = "V"; // V字符
				}
				sb = sb.append(locUserAddress + ",").append(immediate + ",")
						.append(heightType + ",").append(heightFlag + ",");
				/* 0-有高程 */
				if (heightType == 0) {
					if (isShowHeight) {// 高空
						/* 高程数据位空 */
						heightValue = 0d;
					} else {// 普通
						/* 天线数据为空 */
						txValue = 0d;
					}
				}/* 1-无高程 */
				else if (heightType == 1) {
					heightValue = 0d;
				}/* 2-测高1 */
				else if (heightType == 2) {
					heightValue = 0d;
				}/* 3-测高2 */
				else if (heightType == 3) {
					if (isShowHeight) {// 高空
						/* 高程数据位空 */
						heightValue = 0d;
					}
				}
				sb.append((heightValue == 0) ? "0" : heightValue)
						.append(",")
						.append((txValue == 0) ? "0" : txValue)
						.append(",")
						.append((atmosphericPressure == 0) ? "0"
								: atmosphericPressure).append(",")
						.append((temperatureValue == 0) ? "0" : temperatureValue)
						.append(",").append(timeFreq + "");
				write(bd2SendPackage("$CCDWA", sb.toString()));
			} catch (Exception e) {
				Log.e("BDRDSS",
						"android.location.BDUnknownException:sendBD21LocationInfo()",
						e);
				throw new BDUnknownException(
						"android.location.BDUnknownException:sendBD21LocationInfo",
						e);
			}
		}
	}

	public void sendLocationReport2CmdBDV21(String userAddress,
			String heightFlag, double antennaHeight, int timeFreq)
			throws BDUnknownException, BDParameterException {
		if("S500".equals(DEVICE_MODEL)){
			try {
				mBDRDSSManager.sendLocationReport2CmdBDV21(userAddress, heightFlag, antennaHeight, timeFreq);
			} catch (android.location.BDUnknownException e) {
				e.printStackTrace();
			} catch (android.location.BDParameterException e) {
				e.printStackTrace();
			}
		}else{
			if ((userAddress == null) || (userAddress.equals(""))) {
				throw new BDParameterException(
						"android.location.BDParameterException: sendLocationReport2CmdBDV21() 'String userAddress' parameter exception");
			}
	
			if ((heightFlag == null) || (heightFlag.equals("")))
				throw new BDParameterException(
						"android.location.BDParameterException: sendLocationReport2CmdBDV21() 'String heightFlag' parameter exception");
	
			try {

				//String tianxian2 = String.format("%.1f", antennaHeight);
				//antennaHeight = Float.parseFloat(tianxian2);

				StringBuffer sb = new StringBuffer();
				sb.append(userAddress).append(",").
						append(heightFlag).append(",").
						append(antennaHeight).append(",").
						append(timeFreq);
				write(bd2SendPackage("$CCWBA", sb.toString()));



			} catch (Exception e) {
				Log.e("BDRDSS",
						"android.location.BDUnknownException:sendLocationReport2CmdBDV21()",
						e);
				throw new BDUnknownException(
						"android.location.BDUnknownException:sendLocationReport2CmdBDV21()",
						e);
			}
		}
	}

	/**
	 * 发送位置报告1
	 * 
	 * @param report
	 * @throws BDUnknownException
	 * @throws BDParameterException
	 */
	public void sendLocationReport1CmdBDV21(BDLocationReport report)
			throws BDUnknownException, BDParameterException {
		if("S500".equals(DEVICE_MODEL)){
			try {
				mBDRDSSManager.sendLocationReport1CmdBDV21(report);
			} catch (android.location.BDUnknownException e) {
				e.printStackTrace();
			} catch (android.location.BDParameterException e) {
				e.printStackTrace();
			}
		}else{
			if (report == null)
				throw new BDParameterException(
						"android.location.BDParameterException: sendLocationReport1CmdBDV21() 'BDLocationReport report' parameter exception");
			try {
				StringBuffer sb = new StringBuffer();
				sb.append(report.getMsgType() + ",")
						.append(report.getReportFeq() + ",")
						.append(report.getUserAddress() + ",")
						.append(report.getReportTime() + ",")
						.append(report.getLatitude() + ",")
						.append(report.getLatitudeDir() + ",")
						.append(report.getLongitude() + ",")
						.append(report.getLongitudeDir() + ",")
						.append(report.getHeight() + ",")
						.append(report.getHeightUnit());
				write(bd2SendPackage("$CCWAA", sb.toString()));
			} catch (Exception e) {
				Log.e("BDRDSS",
						"android.location.BDUnknownException:sendLocationReport1CmdBDV21()",
						e);
				throw new BDUnknownException(
						"android.location.BDUnknownException:sendLocationReport1CmdBDV21()",
						e);
			}
		}
	}

	
	/**
	 * 发送北斗短报文
	 * @param receiveAddress
	 * @param commType
	 * @param encodeMode
	 * @param responsemode
	 * @param message
	 * @throws BDUnknownException
	 * @throws BDParameterException
	 */
	public void sendSMSCmdBDV21(String receiveAddress, int commType,
			int encodeMode, String responsemode, String message)
			throws BDUnknownException, BDParameterException {
		if("S500".equals(DEVICE_MODEL)){
			try {
				mBDRDSSManager.sendSMSCmdBDV21(receiveAddress, commType, encodeMode,responsemode,message);
				Log.i(TAG, "Send S500 SMS");
			} catch (android.location.BDUnknownException e) {
				e.printStackTrace();
			} catch (android.location.BDParameterException e) {
				e.printStackTrace();
			}
		}else{
			if ((receiveAddress == null) || (receiveAddress.equals(""))) {
				throw new BDParameterException(
						"com.novsky.communication.protocal.BDParameterException: sendSMSCmdBDV21() 'String receiveAddress' parameter exception");
			}
	
			if ((responsemode == null) || (responsemode.equals(""))) {
				throw new BDParameterException(
						"com.novsky.communication.protocal.BDParameterException: sendSMSCmdBDV21() ' String responsemode' parameter exception");
			}
	
			if ((message == null) || (message.equals("")))
				throw new BDParameterException(
						"com.novsky.communication.protocal.BDParameterException: sendSMSCmdBDV21() 'String message' parameter exception");
			/*
			 * 如果当前北斗卡号是六位,则给北斗卡号前面补零
			 */
			if (receiveAddress.length() == 6) {
				receiveAddress = "0" + receiveAddress;
			}
			try {
				StringBuffer cmd = new StringBuffer();
				cmd.append("$CCTXA,").append(receiveAddress).append(",")
						.append(commType).append(",").append(encodeMode)
						.append(",");
				byte[] msgBuffer = null;
				/* 汉字或代码 */
				if (encodeMode == 0 || encodeMode == 1) {
					msgBuffer = message.getBytes("GBK");
				}/* 混合传输 */
				else if (encodeMode == 2) {
					byte[] array = message.getBytes("GBK");
					byte[] temp = new byte[array.length + 1];
					temp[0] = (byte) 0xA4;
					System.arraycopy(array, 0, temp, 1, array.length);
					String hexString = bytesToHexString2(temp);
					msgBuffer = hexString.getBytes("GBK");
				}
				bd2SendSMSPackage(cmd.toString(), msgBuffer);
				write(bd2SendSMSPackage(cmd.toString(), msgBuffer));
				Log.i(TAG, "Send Other Device SMS");
			} catch (Exception e) {
				Log.e("BDRDSS",
						"com.novsky.communication.protocal.BDUnknownException:sendSMSCmdBDV21()",
						e);
				throw new BDUnknownException(
						"com.novsky.communication.protocal.BDUnknownException:sendSMSCmdBDV21()",
						e);
			}
		}
	}
	
	
	/**
	 * 设置/读取管信
	 */
	public void sendManagerInfoCmdBDV21(int managerMode, String manageInfo)
			throws BDUnknownException, BDParameterException {
       if("S500".equals(DEVICE_MODEL)){
    	   try {
			mBDRDSSManager.sendManagerInfoCmdBDV21(managerMode,manageInfo);
		} catch (android.location.BDUnknownException e) {
			e.printStackTrace();
		} catch (android.location.BDParameterException e) {
			e.printStackTrace();
		}
       }else{
		if ((managerMode != 2) && (managerMode != 1) && (managerMode != 3)) {
			throw new BDParameterException(
					"android.location.BDParameterException: sendManagerInfoCmdBDV21() 'int managerMode' parameter exception");
		}
		if (manageInfo == null) {
			throw new BDParameterException(
					"android.location.BDParameterException: sendManagerInfoCmdBDV21() 'String manageInfo' parameter exception");
		}
		try {
			String cmd = managerMode + "," + manageInfo;	
			if(managerMode==2){
				cmd= managerMode + ",";
			}
			write(bd2SendPackage("$CCGXM", cmd));
		} catch (Exception e) {
			Log.e("BDRDSS",
					"android.location.BDUnknownException:sendManagerInfoCmdBDV21()",
					e);
			throw new BDUnknownException(
					"android.location.BDUnknownException:sendManagerInfoCmdBDV21()",
					e);
		}
       }
	}

	/**
	 *  设置RNSS定位模式
	 * @param strategy
	 */
		public void setLocationStrategy(int strategy) {
			if (1 == strategy) // 单北斗
			{
				write("$CFGSYS,H10\r\n".getBytes());
				Log.e("BDRNSSManager", "setLocationStrategy:strategy:1 ");
			} else if (2 == strategy) {
				write("$CFGSYS,H01\r\n".getBytes());
				Log.e("BDRNSSManager", "setLocationStrategy:strategy:2 ");
			} else if (0 == strategy) // 混合
			{
				write("$CFGSYS,H11\r\n".getBytes());
				Log.e("BDRNSSManager", "setLocationStrategy:strategy: 0");
			} else { // 混合
				write("$CFGSYS,H11\r\n".getBytes());
				Log.e("BDRNSSManager", "setLocationStrategy:strategy:else ");
			}
			write("$CFGSAVE,\r\n".getBytes());
		}
	
	public void sendRMOCmdBDV21(String cmd, int switchMode, int timeFreq)
			throws BDUnknownException, BDParameterException {
		if("S500".equals(DEVICE_MODEL)){
			try {
				mBDRDSSManager.sendRMOCmdBDV21(cmd, switchMode, timeFreq);
			} catch (android.location.BDUnknownException e) {
				e.printStackTrace();
			} catch (android.location.BDParameterException e) {
				e.printStackTrace();
			}
		}else{
			if ((cmd == null) || ("".equals(cmd))) {
				throw new BDParameterException(
						"com.novsky.communication.protocal.BDParameterException: sendRMOCmdBDV21() 'String cmd' is null or empty parameter exception！");
			}
			if ((switchMode != 1) && (switchMode != 2) && (switchMode != 4)
					&& (switchMode != 3)) {
				throw new BDParameterException(
						"com.novsky.communication.protocal.BDParameterException: sendRMOCmdBDV21() 'int switchMode' value is error!");
			}
			try {
				write(bd2SendPackage("$CCRMO", cmd + "," + switchMode + ","
						+ timeFreq));
			} catch (Exception e) {
				Log.e("BDRDSS",
						"com.novsky.communication.protocal.BDUnknownException:sendRMOCmdBDV21()",
						e);
				throw new BDUnknownException(
						"com.novsky.communication.protocal.BDUnknownException:sendRMOCmdBDV21()",
						e);
			}
			
		}
	}
	
	
	
	private static String bytesToHexString2(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}
	
	/**
	 * 封包
	 * @param cmd
	 * @param message
	 * @return
	 */
	private byte[] bd2SendSMSPackage(String cmd, byte[] message) {
		for (int i = 0; i < message.length; i++) {
			Log.i(TAG, "message[" + i + "]=" + message[i]);
		}
		
		/* 2.生成和校验 */
		byte[] temp = cmd.toString().getBytes();
		int tempLen = temp.length;
		int messageLen = message.length;
		byte[] buffer = new byte[tempLen + messageLen + 5];
		System.arraycopy(temp, 0, buffer, 0, tempLen);
		System.arraycopy(message, 0, buffer, tempLen, messageLen);
		buffer[tempLen + messageLen] = (byte) (0x2A);
		// 计算CRC
		// 校验和占用2个字节,结束标识符占用2个字节
		byte[] crcArray = new byte[tempLen + messageLen + 1];
		System.arraycopy(buffer, 0, crcArray, 0, tempLen + messageLen + 1);
		byte[] crc = getCrc(crcArray);
		buffer[tempLen + messageLen + 1] = crc[0];
		buffer[tempLen + messageLen + 2] = crc[1];
		/* 3.增加结束标识 */
		buffer[tempLen + messageLen + 3] = 0x0D;
		buffer[tempLen + messageLen + 4] = 0x0A;

		for (int i = 0; i < buffer.length; i++) {
			Log.i(TAG, "totalBuffer[" + i + "]=" + buffer[i]);
		}
		return buffer;
	}

	
	/**
	 * 得到和校验
	 * 
	 * @param buffer
	 * @return
	 */
	private byte[] getCrc(byte[] buffer) {
		int size = buffer.length;
		byte bCrc = buffer[1];
		for (int i = 2; i < size - 1; i++) {
			bCrc = (byte) (bCrc ^ buffer[i]);
		}

		byte tmpHigh = (byte) ((bCrc & 0xF0) >> 4);
		byte tmpLow = (byte) ((bCrc & 0x0F));

		if (tmpHigh <= 9) {
			tmpHigh = (byte) (tmpHigh + 48);
		} else {
			tmpHigh = (byte) (tmpHigh + 55);
		}
		if (tmpLow <= 9) {
			tmpLow = (byte) (tmpLow + 48);
		} else {
			tmpLow = (byte) (tmpLow + 55);
		}
		byte[] temp = new byte[2];
		temp[0] = tmpHigh;
		temp[1] = tmpLow;
		return temp;
	}

	
	/**
	 * 读取线程
	 * @author Administrator
	 *
	 */
	public class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			byte[] buffer = new byte[512];
			byte[] cmd = new byte[512];
			int bytes = 0;
			int index = 0;
			while (!isInterrupted()){
				int size;
				try {
					if (mInputStream == null){
						return;
					}
					/*
					 * 这里的read要注意，它会一直等待数据,直到天荒地老。如果要判断是否接受完成，只有设置结束标识，或作其他特殊的处理
					 */
					size = mInputStream.read(buffer);
					if (size > 0){
						boolean isStop = false;
						for(int i =0;i<size; i++){
							//如果是以$符号开始
							if(buffer[i] == (byte)'$') {
								index = 0;
								Log.i(TAG, "start flag $");
							}
							if (i>0 &&buffer[i] == (byte) 0x0A
									&&buffer[i - 1] == (byte) 0x0D) {
								isStop = true;
								Log.i(TAG, "end flag \n");
							}
							//如果长度大于512则进行重新创建数组对象，丢弃原先的命令。
							if (index>= 512){
								cmd = new byte[512];
								index=0;
							}else{
								cmd[index] = buffer[i];
								if (isStop){ 
									// 发送命令，并清除已发送的命令.
									if (cmd[0] == 36) {// $
										parseReceiveData(cmd, index);
									} else {
										Log.i(TAG, "the cmd is wrong");
									}
									isStop = false;
									index = 0;
								}else{
									index++;	
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	/**
	 * 数据解析
	 * @param buffer
	 * @param size
	 */
	public synchronized void parseReceiveData(byte[] buffer, int size) {
		try {
			String cmd = new String(buffer, 0, size - 2, "GBK");
			Log.i(TAG, "parseReceiveData size=" + size +"," +cmd);

			//1313 , 2016-12-11 14:27:37.427 , $GNZDA,062512.760,00,00,0000,00,00*4B
			//                                         时间      日  月  年 本地时区 本时区分钟差
			//$GPZDA $BDZDA
			if (cmd.startsWith("$GNZDA")){
				if (mBDLocationZDAListeners!=null){
					ZDATime zdaTime = new ZDATime();
					String[] results = cmd.split(",");
					String utcTime = results[1];
					String day = results[2];
					String month = results[3];
					String year = results[4];
					String timezone = results[5];
					String minuteDeviation = "";
					if (results.length==7){
						 minuteDeviation = results[6].split("\\*")[0];
					}else {

					}
					zdaTime.setUtcTime(utcTime);
					zdaTime.setDay(day);
					zdaTime.setMinuteDeviation(month);
					zdaTime.setYear(year);
					zdaTime.setTimeZone(timezone);
					zdaTime.setMinuteDeviation(minuteDeviation);

					for(BDLocationZDAListener mBDLocationZDAListener:mBDLocationZDAListeners){
						mBDLocationZDAListener.onZDATime(zdaTime);
					}
					Log.i(TAG, "parseReceiveData size=" + size +"," +cmd);
				}
			}

			if (cmd.startsWith("$GPZDA")){
				if (mBDLocationZDAListeners!=null){
					ZDATime zdaTime = new ZDATime();
					String[] results = cmd.split(",");
					String utcTime = results[1];
					String day = results[2];
					String month = results[3];
					String year = results[4];
					String timezone = results[5];
					String minuteDeviation = "";
					if (results.length==6){
						 minuteDeviation = results[6].split("\\*")[0];
					}else {

					}
					zdaTime.setUtcTime(utcTime);
					zdaTime.setDay(day);
					zdaTime.setMinuteDeviation(month);
					zdaTime.setYear(year);
					zdaTime.setTimeZone(timezone);
					zdaTime.setMinuteDeviation(minuteDeviation);

					for(BDLocationZDAListener mBDLocationZDAListener:mBDLocationZDAListeners){
						mBDLocationZDAListener.onZDATime(zdaTime);
					}
					Log.i(TAG, "parseReceiveData size=" + size +"," +cmd);
				}
			}

			if (cmd.startsWith("$BDZDA")){
				if (mBDLocationZDAListeners!=null){
					ZDATime zdaTime = new ZDATime();
					String[] results = cmd.split(",");
					String utcTime = results[1];
					String day = results[2];
					String month = results[3];
					String year = results[4];
					String timezone = results[5];
					String minuteDeviation = "";
					if (results.length==6){
						 minuteDeviation = results[6].split("\\*")[0];
					}else {

					}
					zdaTime.setUtcTime(utcTime);
					zdaTime.setDay(day);
					zdaTime.setMinuteDeviation(month);
					zdaTime.setYear(year);
					zdaTime.setTimeZone(timezone);
					zdaTime.setMinuteDeviation(minuteDeviation);

					for(BDLocationZDAListener mBDLocationZDAListener:mBDLocationZDAListeners){
						mBDLocationZDAListener.onZDATime(zdaTime);
					}
					Log.i(TAG, "parseReceiveData size=" + size +"," +cmd);
				}
			}

			if (cmd.startsWith("$BDFKI")) {
				/* 反馈信息解析代码 */
				if (mBDFKIListeners != null) {
					Log.i(TAG, "mBDFKIListener is not null!");
					String[] results = cmd.split(",");
					String cmdName = results[1];
					String cmdStatus = results[2];
					String freqStatus = results[3];
					String sendPrompt = "";
					int waitTime = 0;
					if (results.length == 5) {
						waitTime = Integer
								.valueOf(results[4].split("\\*")[0]);
					} else {
						sendPrompt = results[4];
						waitTime = Integer
								.valueOf(results[5].split("\\*")[0]);
					}
					Log.i(TAG, "cmdName=" + cmdName + ", cmdStatus="
							+ cmdStatus + ", freqStatus=" + freqStatus
							+ ",sendPrompt=" + sendPrompt + ", waitTime="
							+ waitTime);
					for(BDFKIListener mBDFKIListener:mBDFKIListeners){
						if (("Y").equals(cmdStatus)) {
							mBDFKIListener.onCmd(cmdName, true);
						} else if (("N").equals(cmdStatus)) {
							if (waitTime > 0) {
								mBDFKIListener.onTime(waitTime);
							} else {
								if ("1".equals(sendPrompt)) {
									mBDFKIListener.onSystemLauncher();
								} else if ("2".equals(sendPrompt)) {
									mBDFKIListener.onPower();
								} else if ("3".equals(sendPrompt)) {
									mBDFKIListener.onSilence();
								} else {
									mBDFKIListener.onCmd(cmdName, false);
								}
							}
						}		
					}
				} else {
					Log.i(TAG, "FKIListeners is null!");
				}
			}
			/* RDSS定位信息解析代码 */
			else if (cmd.startsWith("$BDDWR")) {
				if (mBDLocationListeners != null) {
					BDLocation location = new BDLocation();
					// if(cmd.equals("$BDDWR")){
					String[] params = cmd.split(",");
					/*
					 * 1-本用户设备进行定位申请返回的定位信息
					 * 2-具备指挥功能的用户设备进行定位查询返回的下属用户位置信息
					 * 3-接收到位置报告的定位信息
					 */
					int locType = Integer.valueOf(params[1]);
					String userId = params[2], locTime = params[3];
					String lat = params[4], latDir = params[5], lon = params[6];// 经度
					String lonDir = params[7];// 经度方向
					String height = params[8];// 大地高
					String heightUnit = params[9];// 高单位
					String heightDev = params[10];// 高程异常
					String heightDevUnit = params[11];// 高程异常单位
					String precision = params[12];// 精度
					String immediate = params[13];// 紧急定位指示
					String mutriValue = params[14];// 多解值
					String heightType = params[15].split("\\*")[0];// 高空类型

					location.setUserAddress(userId);
					location.setMsgType(locType);
					location.mLocationTime = locTime;//增加 定位时间  add by llg 20161207
					location.setLongitude(Double.valueOf(lon));
					location.setLongitudeDir(lonDir);
					location.setLatitude(Double.valueOf(lat));
					location.setLatitudeDir(latDir);
					location.setEarthHeight(Double.valueOf(height));
					location.setEarthHeightUnit(heightUnit);
					location.setHeightExcepiton(Double.valueOf(heightDev));
					location.setHeightExceptionUnit(heightDevUnit);
					location.setImmediateFlag(immediate);
					location.setMultiValue(mutriValue);
					location.setAccuracy(Integer.valueOf(precision));
					location.setHeightTypePrompt(heightType);
					for(BDLocationListener mBDLocationListener:mBDLocationListeners){
					  mBDLocationListener.onLocationChange(location);
				    }
				}
			}
			else if (cmd.startsWith("$TXSQ")) {
				
				//cmd.replace(oldChar, newChar);
				//BDMessageInfo mBDMessageInfo = new BDMessageInfo();
				// $  T  X  S  Q    长度             发件用户地址      类别       收件用户地址           长度         应答                                                                                                                                                                        校验和
				// 24 54 58 53 51 ||00 1F ||1F FC 09 ||46 ||1F FC 09 ||00 68 ||00// F1 //01 //1E AB 91 72 14 0B 92 16 1E 1E 53 //75 
				
				
//				Intent intent = new Intent(BDCommManager.PROVIDERS_CHANGED_ACTION);
//                intent.putExtra(BDCommManager.BDRDSS_MESSAGE, mBDMessageInfo);
//                mContext.sendBroadcast(intent);
				
			}
			else if (cmd.startsWith("$TXXX")) {
				
				//cmd.replace(oldChar, newChar);
				//BDMessageInfo mBDMessageInfo = new BDMessageInfo();
				// $  T  X  S  Q    长度             发件用户地址      类别       收件用户地址           长度         应答                                                                                                                                                                        校验和
				// 24 54 58 53 51 ||00 1F ||1F FC 09 ||46 ||1F FC 09 ||00 68 ||00// F1 //01 //1E AB 91 72 14 0B 92 16 1E 1E 53 //75 
				
				
//				Intent intent = new Intent(BDCommManager.PROVIDERS_CHANGED_ACTION);
//				intent.putExtra(BDCommManager.BDRDSS_MESSAGE, mBDMessageInfo);
//				mContext.sendBroadcast(intent);
				
			}
			// 短报文内容解析代码
			//$BDTXR,3,0455998,2,,A4CDA8D0C5B2E2CAD420D2BBB6FEC8FDCBC4CEE5C1F9C6DFB0CBBEC5CAAE31323334353637383930*3
			else if (cmd.startsWith("$BDTXR")||cmd.startsWith("$PJTXR")) {
					Log.i(TAG, "mBDMessageListener is not null!");
					BDMessageInfo mBDMessageInfo = new BDMessageInfo();
					String message = "";
					String[] results = cmd.split(",");
					String messageType = results[1];
					String userAddress = results[2];
					String encodingMode = results[3];
					String sendTime = results[4];
					/* 由于短信内容有字符编码的问题，所有不直接使用上面截取的短信内容 */
					int index = 0, startCount = 0, flag = 0, count = 0;
					char mchar = (char) buffer[index];
					while (mchar != '*') {
						// 如果当前的字符是逗号，则flag加1
						if (mchar == ',') {
							flag++;
						}
						// 如果flag等于5
						if (flag == 5) {
							// 并且当前字符是逗号，
							if (mchar == ',') {
								startCount = index;
							} else {
								count++; // 统计短信长度
							}
						}
						mchar = (char) buffer[index];
						index++;
					}
					/* 当编码方式混码方式 */
					if (Integer.valueOf(encodingMode) == 2) {
						//A4CDA8D0C5B2E2CAD420D2BBB6FEC8FDCBC4CEE5C1F9C6DFB0CBBEC5CAAE31323334353637383930
						String msg = results[5].split("\\*")[0];
						//CDA8D0C5B2E2CAD420D2BBB6FEC8FDCBC4CEE5C1F9C6DFB0CBBEC5CAAE31323334353637383930
						String msgSRC = msg.substring(2);
						String msgHeader = msg.substring(0,2);
						message = new String(hexStringToBytes(msg.substring(2)), "GBK");
						
					} else {
						/* 当编码方式是代码或汉字方式 */
						byte[] messageArr = new byte[count];
						System.arraycopy(buffer, startCount, messageArr, 0,
								messageArr.length);
						message = new String(messageArr, "GBK");
					}
					mBDMessageInfo.setmUserAddress(userAddress);
					mBDMessageInfo.setMsgCharset(Integer
							.valueOf(encodingMode));
					mBDMessageInfo.setMsgType(Integer.valueOf(messageType));
					mBDMessageInfo.setmSendTime(sendTime);
					mBDMessageInfo.setMessage(message.getBytes("GBK"));
					Intent intent = new Intent(BDCommManager.PROVIDERS_CHANGED_ACTION);
                    intent.putExtra(BDCommManager.BDRDSS_MESSAGE, mBDMessageInfo);
                    mContext.sendBroadcast(intent);
             //加密解密模块
			} else if (cmd.startsWith("$BDICI")) {
				if (mLocalInfoListeners != null) {
					String[] ici = cmd.split(",");
					CardInfo cardInfo = new CardInfo();
					cardInfo.setCardAddress(ici[1]);
					cardInfo.setSerialNum(ici[2]);
					cardInfo.setBroadCastAddress(ici[3]);
					cardInfo.setCardType(ici[4]);
					cardInfo.setSericeFeq(Integer.valueOf(ici[5]));
					cardInfo.setCommLevel(Integer.valueOf(ici[6]));
					cardInfo.setCheckEncryption(ici[7]);
					cardInfo.setSubordinatesNum(Integer.valueOf(ici[8]
							.split("\\*")[0]));
					Log.i(TAG, ici[1] + "," + ici[2] + "," + ici[3] + ","
							+ ici[4] + "," + ici[5] + "," + ici[6] + ","
							+ ici[7] + "," + ici[8]);
					for(LocalInfoListener mLocalInfoListener :mLocalInfoListeners){
					   mLocalInfoListener.onCardInfo(cardInfo);
					}
				} else {
					Log.i(TAG, "mLocalInfoListeners is  null!");
				}
				//位置报告1 rn位置报告
			} else if (cmd.startsWith("$BDWAA")) {
				if (mBDLocReportListeners != null) {
					String[] reportArray = cmd.split(",");
					BDLocationReport report = new BDLocationReport();
					report.setMsgType(Integer.valueOf(reportArray[1]));
					//非空判断
					if (reportArray[2].isEmpty()){
						report.setReportFeq(0);
					}else {
						report.setReportFeq(Integer.valueOf(reportArray[2]));
					}
					report.setUserAddress(reportArray[3]);
					report.setReportTime(reportArray[4]);
					report.setLatitude(Double.valueOf(reportArray[5]));
					report.setLatitudeDir(reportArray[6]);
					report.setLongitude(Double.valueOf(reportArray[7]));
					report.setLongitudeDir(reportArray[8]);
					report.setHeight(Double.valueOf(reportArray[9]));
					report.setHeightUnit(reportArray[10]);
					for(BDLocReportListener mBDLocReportListener:mBDLocReportListeners){
					   mBDLocReportListener.onLocReport(report);
					}
				} else {
					Log.i(TAG, "mBDLocReportListeners is  null!");
				}
				//位置信息  rnss  混合
			} else if (cmd.startsWith("$GNGGA")) {
				if (mBDRNSSLocationListeners != null) {
					String[] bdRNSSLocationArray = cmd.split(",");
					boolean isCall = false;
					if (gnLocation == null) {
						gnLocation = new BDRNSSLocation();
						isCall = true;
					}
					gnLocation
							.setLatitude(changeLonLatMinuteToDegree(Double
									.valueOf(bdRNSSLocationArray[2])));
					gnLocation
							.setLongitude(changeLonLatMinuteToDegree(Double
									.valueOf(bdRNSSLocationArray[4])));
					gnLocation.setAltitude(Double
							.valueOf(bdRNSSLocationArray[9]));
					
					if (!isCall) {
						Log.i(TAG, gnLocation.getLongitude() + ","
								+ gnLocation.getLatitude() + ","
								+ gnLocation.getAltitude());
					 for(BDRNSSLocationListener mBDRNSSLocationListener:mBDRNSSLocationListeners)	
						 mBDRNSSLocationListener.onLocationChanged(gnLocation);
					 }
					 gnLocation = null;
				} else {
					Log.i(TAG, "mBDRNSSLocationListeners is null!");
				}
				//最简导航数据传输
			} else if (cmd.startsWith("$GNRMC")) {
				if (mBDRNSSLocationListeners != null) {
					String[] bdRNSSRMCArray = cmd.split(",");
					boolean isCall = false;
					if (gnLocation == null) {
						gnLocation = new BDRNSSLocation();
						isCall = true;
					}
					String time = bdRNSSRMCArray[1];
					String status = bdRNSSRMCArray[2];
					gnLocation.setAvailable(status.equals("A"));
					// 速度为  节 *1.852 为 km/h  /3.6为  m/s
					gnLocation.setSpeed((float) (Float.valueOf(bdRNSSRMCArray[7]) * 1.852/3.6));
					//gnLocation.setSpeed((float) (Float.valueOf(bdRNSSRMCArray[7]) * 1.852));
					gnLocation.setBearing(Float.valueOf(bdRNSSRMCArray[8]));
					String yearMonthDay = bdRNSSRMCArray[9];
			        String dateStr = yearMonthDay + " " + time;
			        SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyy HHmmss");
			        Date d = null;
			        try {
			              d = sdf2.parse(dateStr);
			        } catch (ParseException e2) {
			              e2.printStackTrace();
			        }
			        this.gnLocation.setTime(d.getTime());
					this.gnLocation.setmDate(d.getDate());
					if (!isCall) {
						Log.i(TAG, gnLocation.getLongitude() + ","
								+ gnLocation.getLatitude() + ","
								+ gnLocation.getAltitude());
						 for(BDRNSSLocationListener mBDRNSSLocationListener:mBDRNSSLocationListeners){	
							 mBDRNSSLocationListener.onLocationChanged(gnLocation);
						 }
						 gnLocation = null;
					}
				} else {
					Log.i(TAG, "mBDRNSSLocationListeners is null!");
				}
				// 北斗定位
			} else if (cmd.startsWith("$BDGGA")) {
				if (mBDRNSSLocationListeners != null) {
					String[] bdRNSSLocationArray = cmd.split(",");
					boolean isCall = false;
					if (bdLocation == null) {
						bdLocation = new BDRNSSLocation();
						isCall = true;
					}
					bdLocation.setLatitude(changeLonLatMinuteToDegree(Double
									.valueOf(bdRNSSLocationArray[2])));
					bdLocation.setLongitude(changeLonLatMinuteToDegree(Double
									.valueOf(bdRNSSLocationArray[4])));
					bdLocation.setAltitude(Double
							.valueOf(bdRNSSLocationArray[9]));
					if (!isCall) {
						Log.i(TAG, bdLocation.getLongitude() + ","
								+ bdLocation.getLatitude() + ","
								+ bdLocation.getAltitude());
						for(BDRNSSLocationListener mBDRNSSLocationListener :mBDRNSSLocationListeners){
							mBDRNSSLocationListener.onLocationChanged(bdLocation);
						}
						bdLocation = null;
					}
				} else {
					Log.i(TAG, "mBDRNSSLocationListeners is null!");
				}
			} else if (cmd.startsWith("$BDRMC")) {
				if (mBDRNSSLocationListeners != null) {
					String[] bdRNSSRMCArray = cmd.split(",");
					boolean isCall = false;
					if (bdLocation == null) {
						bdLocation = new BDRNSSLocation();
						isCall = true;
					}
					String time = bdRNSSRMCArray[1];
					String status = bdRNSSRMCArray[2];
					bdLocation.setAvailable(status.equals("A"));
					//bdLocation.setSpeed((float) (Float.valueOf(bdRNSSRMCArray[7]) * 1.852));
					bdLocation.setSpeed((float) (Float.valueOf(bdRNSSRMCArray[7]) * 1.852/3.6));
					bdLocation.setBearing(Float.valueOf(bdRNSSRMCArray[8]));
					String yearMonthDay = bdRNSSRMCArray[9];
		            String dateStr = yearMonthDay + " " + time;
		            SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyy HHmmss");
		            Date d = null;
		            try {
		              d = sdf2.parse(dateStr);
		            } catch (ParseException e2) {
		              e2.printStackTrace();
		            }
		            this.bdLocation.setTime(d.getTime());
					if (!isCall){
						  Log.i(TAG, bdLocation.getLongitude() + ","
								+ bdLocation.getLatitude() + ","
								+ bdLocation.getAltitude());
						  for(BDRNSSLocationListener mBDRNSSLocationListener:mBDRNSSLocationListeners){
							  mBDRNSSLocationListener.onLocationChanged(bdLocation);
						  }
						  bdLocation=null;
					}
				} else {
					Log.i(TAG, "mBDRNSSLocationListeners is null!");
				}
				//gps 定位
			} else if (cmd.startsWith("$GPGGA")) {
				if (mBDRNSSLocationListeners != null) {
					String[] bdRNSSLocationArray = cmd.split(",");
					boolean isCall = false;
					if (gpLocation == null) {
						gpLocation = new BDRNSSLocation();
						isCall = true;
					}
					gpLocation
							.setLatitude(changeLonLatMinuteToDegree(Double
									.valueOf(bdRNSSLocationArray[2])));
					gpLocation
							.setLongitude(changeLonLatMinuteToDegree(Double
									.valueOf(bdRNSSLocationArray[4])));
					gpLocation.setAltitude(Double
							.valueOf(bdRNSSLocationArray[9]));
					
					if (!isCall) {
						Log.i(TAG, gpLocation.getLongitude() + ","
								+ gpLocation.getLatitude() + ","
								+ gpLocation.getAltitude());
					  for(BDRNSSLocationListener mBDRNSSLocationListener:mBDRNSSLocationListeners){
						  mBDRNSSLocationListener.onLocationChanged(gpLocation);
					  }
					  gpLocation = null;
					}
				} else {
					Log.i(TAG, "mBDRNSSLocationListeners is null!");
				}
			} else if (cmd.startsWith("$GPRMC")) {
				if (mBDRNSSLocationListeners != null) {
					String[] bdRNSSRMCArray = cmd.split(",");
					boolean isCall = false;
					if (gpLocation == null) {
						gpLocation = new BDRNSSLocation();
						isCall = true;
					}
					String time = bdRNSSRMCArray[1];
					String status = bdRNSSRMCArray[2];
					gpLocation.setAvailable(status.equals("A"));
					//gpLocation.setSpeed((float) (Float.valueOf(bdRNSSRMCArray[7]) * 1.852));
					gpLocation.setSpeed((float) (Float.valueOf(bdRNSSRMCArray[7]) * 1.852/3.6));
					gpLocation.setBearing(Float.valueOf(bdRNSSRMCArray[8]));
					String yearMonthDay = bdRNSSRMCArray[9];
		            String dateStr = yearMonthDay + " " + time;
		            SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyy HHmmss");
		            Date d = null;
		            try {
		              d = sdf2.parse(dateStr);
		            } catch (ParseException e2) {
		              e2.printStackTrace();
		            }
			        this.gpLocation.setTime(d.getTime());
					if (!isCall) {
						Log.i(TAG, gpLocation.getLongitude() + ","
								+ gpLocation.getLatitude() + ","
								+ gpLocation.getAltitude());
					  for(BDRNSSLocationListener mBDRNSSLocationListener:mBDRNSSLocationListeners){	
						  mBDRNSSLocationListener.onLocationChanged(gpLocation);
					  }
						gpLocation = null;
				    }
				} else {
					Log.i(TAG, "mBDRNSSLocationListeners is null!");
				}
				//可视卫星状态
			} else if (cmd.startsWith("$BDGSV")){
				if (mBDSatelliteListeners != null) {
					String[] gsv = cmd.split(",");
					int cmdNum = Integer.valueOf(gsv[1]), currentNum = Integer
							.valueOf(gsv[2]), visualSatelliteNum = Integer
							.valueOf(gsv[3]), num = 4;
					if (currentNum == 1) {
						bdlist = new ArrayList<BDSatellite>();
						if(currentNum==cmdNum){
							num=visualSatelliteNum;
						}
					} else {
						if (currentNum == cmdNum) {
							num = (visualSatelliteNum % 4 == 0) ? 4
									: (visualSatelliteNum % 4);
						}
					}
					for (int i = 0; i < num; i++) {
						BDSatellite mBDSatellite = new BDSatellite();
						String prn = gsv[(i + 1) * 4];
						mBDSatellite.setPrn((!"".equals(prn)) ? Integer
								.valueOf(prn) : 0);
						String elevation = gsv[(i + 1) * 4 + 1];
						mBDSatellite
								.setElevation((!"".equals(elevation)) ? Float
										.valueOf(elevation) : 0);
						String azimuth = gsv[(i + 1) * 4 + 2];
						mBDSatellite
								.setAzimuth((!"".equals(azimuth)) ? Float
										.valueOf(azimuth) : 0);
						String snr = "".equals(gsv[(i + 1) * 4 + 3]) ? "0"
								: gsv[(i + 1) * 4 + 3];
						if (snr.indexOf("*") > -1) {
							snr = "".equals(snr.split("\\*")[0]) ? "0"
									: snr.split("\\*")[0];
						}
						mBDSatellite.setSnr(Float.valueOf(snr));
						
						//临时演示用修改  返回的bd卫星号数小于160
						//A310B用
//						if (mBDSatellite.getPrn()<160) {
//							mBDSatellite.setPrn(mBDSatellite.getPrn()+160);
//						}
						if(mBDSatellite.getPrn() < 160)
							mBDSatellite.setPrn(mBDSatellite.getPrn()+160);
						
						if (mBDFixNumberArray[mBDSatellite.getPrn()-160]==1) {
							mBDSatellite.setUsedInFix(true);
							mBDFixNumberArray[mBDSatellite.getPrn()-160]=0;
						}else{
							mBDSatellite.setUsedInFix(false);
						}
						Log.i("BDSNR", "mBDSatellite.getPrn()="+mBDSatellite.getPrn()+","+mBDSatellite.usedInFix());
						bdlist.add(mBDSatellite);
					}
					if (currentNum == cmdNum) {
						for(BDSatelliteListener mBDSatelliteListener:mBDSatelliteListeners){
						    mBDSatelliteListener.onBDGpsStatusChanged(bdlist);
						}
						bdlist = null;
					}
				} else {
					Log.i(TAG, "mBDSatelliteListeners is null!");
				}
			}else if (cmd.startsWith("$GPGSV")) {
				if (mGPSatelliteListeners != null) {
					String[] gsv = cmd.split(",");
					int cmdNum = Integer.valueOf(gsv[1]), currentNum = Integer
							.valueOf(gsv[2]), visualSatelliteNum = Integer
							.valueOf(gsv[3]), num = 4;
					if (currentNum == 1) {
						gplist = new ArrayList<GPSatellite>();
						if(currentNum==cmdNum){
							num=visualSatelliteNum;
						}
					} else {
						if (currentNum == cmdNum) {
							num = (visualSatelliteNum % 4 == 0) ? 4
									: (visualSatelliteNum % 4);
						}
					}
					for (int i = 0; i < num; i++) {
						GPSatellite mGPSatellite = new GPSatellite();
						String prn = gsv[(i + 1) * 4];
						mGPSatellite.setPrn((!"".equals(prn)) ? Integer
								.valueOf(prn) : 0);
						String elevation = gsv[(i + 1) * 4 + 1];
						mGPSatellite
								.setElevation((!"".equals(elevation)) ? Float
										.valueOf(elevation) : 0);
						String azimuth = gsv[(i + 1) * 4 + 2];
						mGPSatellite
								.setAzimuth((!"".equals(azimuth)) ? Float
										.valueOf(azimuth) : 0);
						String snr = "".equals(gsv[(i + 1) * 4 + 3]) ? "0"
								: gsv[(i + 1) * 4 + 3];
						if (snr.indexOf("*") > -1) {
							snr = "".equals(snr.split("\\*")[0]) ? "0"
									: snr.split("\\*")[0];
						}
						mGPSatellite.setSnr(Float.valueOf(snr));
						if (mGPSFixNumberArray[mGPSatellite.getPrn()]==1) {
							mGPSatellite.setUsedInFix(true);
							mGPSFixNumberArray[mGPSatellite.getPrn()]=0;
						}else{
							mGPSatellite.setUsedInFix(false);
						}
						gplist.add(mGPSatellite);
					}
					if (currentNum == cmdNum) {
						for(GPSatelliteListener mGPSatelliteListener:mGPSatelliteListeners){
						    mGPSatelliteListener.onGpsStatusChanged(gplist);
						}
						gplist = null;
					}
				} else {
					Log.i(TAG, "mGPSatelliteListeners is null!");
				}
			} else if (cmd.startsWith("$BDGSA")) {
				String[] gsa = cmd.split(",");
				for (int i = 3; i <= 14; i++) {
					Integer prn = Integer.valueOf("".equals(gsa[i]) ? "0"
							: gsa[i]);
					if(prn < 160)
						prn += 160;
					mBDFixNumberArray[prn-160]=1;
				}

			}else if (cmd.startsWith("$GPGSA")
					|| cmd.startsWith("$GNGSA")) {
				String[] gsa = cmd.split(",");
				for (int i = 3; i <= 14; i++) {
					Integer prn = Integer.valueOf("".equals(gsa[i]) ? "0"
							: gsa[i]);
					if(prn>=160){
						mBDFixNumberArray[prn-160]=1;
					}else{
						mGPSFixNumberArray[prn]=1;
					}
				}
				// 定位模式??
			}else if (cmd.startsWith("$CFGSYS")) {
				if (mBDLocationStrategyListeners != null) {
					String temp = cmd.replace("\r\n", "");
					int strategy = Integer.valueOf(temp.split(",")[1]);
					for(BDLocationStrategyListener mBDLocationStrategyListener:mBDLocationStrategyListeners){
					    mBDLocationStrategyListener
							.onLocationStrategy(strategy);
					}
				} else {
					Log.i(TAG, "mBDLocationStrategyListener is null!");
				}
				// 波束状态
			} else if (cmd.startsWith("$BDBSI")) {
				if (mBDBeamStatusListeners != null) {
					BDBeam mBDBeam = new BDBeam();
					String[] beams = cmd.split(",");
					int responseBeamId = ((beams[1] != null) && (!""
							.equals(beams[1]))) ? Integer.valueOf(beams[1])
							: 1;
					mBDBeam.setResponseBeamId(responseBeamId);
					int mTimeLagId = ((beams[2] != null) && (!""
							.equals(beams[2]))) ? Integer.valueOf(beams[2])
							: 1;
					mBDBeam.setTimeLagId(mTimeLagId);
					int[] beamWaves = new int[10];
					beamWaves[0] = (!"".equals(beams[3])) ? Integer
							.valueOf(beams[3]) : 0;
					beamWaves[1] = (!"".equals(beams[4])) ? Integer
							.valueOf(beams[4]) : 0;
					beamWaves[2] = (!"".equals(beams[5])) ? Integer
							.valueOf(beams[5]) : 0;
					beamWaves[3] = (!"".equals(beams[6])) ? Integer
							.valueOf(beams[6]) : 0;
					beamWaves[4] = (!"".equals(beams[7])) ? Integer
							.valueOf(beams[7]) : 0;
					beamWaves[5] = (!"".equals(beams[8])) ? Integer
							.valueOf(beams[8]) : 0;
					beamWaves[6] = (!"".equals(beams[9])) ? Integer
							.valueOf(beams[9]) : 0;
					beamWaves[7] = (!"".equals(beams[10])) ? Integer
							.valueOf(beams[10]) : 0;
					beamWaves[8] = (!"".equals(beams[11])) ? Integer
							.valueOf(beams[11]) : 0;
					String beam12 = beams[12].split("\\*")[0];
					beamWaves[9] = (!"".equals(beam12)) ? Integer
							.valueOf(beam12) : 0;
					mBDBeam.setBeamWaves(beamWaves);
					String str = "";
					for (int i = 0; i < beamWaves.length; i++) {
						str += (beamWaves[i] + ",");
					}
					Log.i(TAG, "beamWaves=" + str);
					for(BDBeamStatusListener mBDBeamStatusListener: mBDBeamStatusListeners){
					  mBDBeamStatusListener.onBeamStatus(mBDBeam);
					}
				} else {
					Log.i(TAG, "mBatteryStatusListeners is null!");
				}
				//管理信息
			} else if (cmd.startsWith("$BDGXM")) {
				if (mManagerInfoListeners != null) {
					String[] gxms = cmd.split(",");
					String manager = gxms[2].split("\\*")[0];
					for(ManagerInfoListener mManagerInfoListener:mManagerInfoListeners){
					  mManagerInfoListener.onManagerInfo(manager);
					}
				} else {
					Log.i(TAG, "mManagerInfoListeners is null!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 构造北斗2.1协议
	 * @param sendCommand
	 * @param subCmd
	 * @return
	 */
	private byte[] bd2SendPackage(String sendCommand, String subCmd) {
		StringBuffer sb = new StringBuffer();
		/* 1.构造类型标识+数据 */
		sb.append(sendCommand + ",");
		sb.append(subCmd);
		sb.append("*");
		Log.i(TAG, "sb==" + sb.toString());
		/* 2.生成和校验 */
		byte[] buffer = null;
		buffer = sb.toString().getBytes();
		int size = buffer.length;
		byte[] totalBuffer = new byte[size + 4]; // 校验和占用2个字节,结束标识符占用2个字节
		System.arraycopy(buffer, 0, totalBuffer, 0, size);
		byte[] crc = getCrc(buffer);
		totalBuffer[size] = crc[0];
		totalBuffer[size + 1] = crc[1];
		/* 3.增加结束标识 */
		totalBuffer[size + 2] = 0x0D;
		totalBuffer[size + 3] = 0x0A;
		for (int i = 0; i < totalBuffer.length; i++) {
			Log.i(TAG, "totalBuffer[" + i + "]=" + totalBuffer[i]);
		}
		return totalBuffer;
	}

	
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.replaceAll(" ", "").toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	

	/**
	 * 将char字符转换成byte
	 * 
	 * @param c
	 * @return
	 */
	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * 发送命令
	 * @param cmd
	 */
	public void write(byte[] cmd) {
		try {
			if(mOutputStream!=null){
				mOutputStream.write(cmd);
				mOutputStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 把经纬度从分为单位转换成以度为单位
	 * 
	 * @param num
	 * @return
	 */
	public static double changeLonLatMinuteToDegree(double num) {
		if (num == 0.0 || num < 100) {
			return 0.0;
		}
		String value = String.valueOf(num);
		int index = value.indexOf(".");
		return Double.valueOf(value.substring(0, index - 2)).doubleValue()
				+ (Double.valueOf(value.substring(index - 2)).doubleValue() / 60.0d);
	}
	
	
	
}
