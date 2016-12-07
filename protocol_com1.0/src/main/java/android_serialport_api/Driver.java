package android_serialport_api;

import java.io.File;
import java.util.Vector;

import android.util.Log;
/**
 * 驱动器类
 * @author steve
 */
public class Driver {
	
	private static final String TAG="Driver";
	/**
	 * 驱动设备名称
	 */
	private String mDeviceName;
	/**
	 * 驱动设备根目录
	 */
	private String mDeviceRoot;
	
	Vector<File> mDevices=null;
	/**
	 * 初始化
	 * @param name
	 * @param root
	 */
	public Driver(String name,String root){
		mDeviceName=name;
		mDeviceRoot=root;
	}
	/**
	 * 所得设备根目录下的文件
	 * @return
	 */
	public Vector<File> getDevices(){
		if(mDevices==null){
			mDevices=new Vector<File>();
			File dev=new  File("/dev");
			File[] files=dev.listFiles();
			for(File file:files){
				if(file.getAbsolutePath().startsWith(mDeviceRoot)){
					Log.d(TAG, "Found new device:"+file);
					mDevices.add(file);
				}
			}
		}
		return mDevices;
	}
	/**
	 * 得到设备名称
	 * @return
	 */
	public String getName(){
		return mDeviceName;
	}
}
