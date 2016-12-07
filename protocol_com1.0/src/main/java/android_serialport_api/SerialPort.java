package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

/**
 * 串口类
 * @author steve
 */
public class SerialPort {
     
	 private static final String TAG="SerialPort";
	 
	 private FileDescriptor mFd;
	 /**
	  * 读取文件流
	  */
	 private FileInputStream mFileInputStream;
	 /**
	  * 输出文件流
	  */
	 private FileOutputStream mFileOutputStream;
	 
	 public SerialPort(File device ,int baudrate ,int flag)throws SecurityException,IOException,Exception{
		 /**
		  * 检查当前权限是否正确 
		  */
		 if (!device.canRead() || !device.canWrite()) {
				try {
					/* Missing read/write permission, trying to chmod the file */
					Process su;
					su = Runtime.getRuntime().exec("/system/bin/su");
					String cmd3 = "chmod 777 " + device.getAbsolutePath() + "\n"
							+ "exit\n";
					su.getOutputStream().write(cmd3.getBytes());
					if ((su.waitFor() != 0) || !device.canRead()
							|| !device.canWrite()) {
						throw new SecurityException();
					}
				} catch (Exception e) {
					//e.printStackTrace();
					throw new Exception();
				}
	     }
		 
		 mFd=open(device.getAbsolutePath(),baudrate,flag);
		 if(mFd==null){
			 Log.i(TAG, "native open return null");
			 throw new IOException();
		 }
		 mFileInputStream=new FileInputStream(mFd);
		 mFileOutputStream=new FileOutputStream(mFd);
	 }
	 
	 public InputStream getInputStream(){
		 return mFileInputStream;
	 }
	 
	 public OutputStream getOutputStream(){
		 return mFileOutputStream;
	 }
	 
	 //JNI 方法
	 private native static FileDescriptor open(String path ,int baudrate,int flags);
	 
	 public native void close();
	 /**
	  * 加载库
	  */
	 static {
		 System.loadLibrary("serial_port");
	 }
}
