package com.bd.comm.protocal;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android.app.Application;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

/**
 * Application用来对串口进行初始化和关闭串口
 * 
 * @author steve
 */
public class SerialApplication extends Application {

	public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
	private SerialPort mSerialPort = null;

	/**
	 * 获得串口
	 * @return
	 * @throws SecurityException
	 * @throws IOException
	 * @throws InvalidParameterException
	 */
	public SerialPort getSerialPort(String path,int baudrate) throws SecurityException, IOException,
			InvalidParameterException, Exception {
		if (mSerialPort == null) {
			/* check parameters */
			if ((path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}
			/* Open the serial port */
			try {
				mSerialPort = new SerialPort(new File(path), baudrate, 0);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception();
			}
		}
		return mSerialPort;
	}

	/**
	 * 关闭串口
	 */
	public void closeSerialPort() {
		 if(mSerialPort!=null){
		 mSerialPort.close();
		 mSerialPort=null;
		 }
	}
}
