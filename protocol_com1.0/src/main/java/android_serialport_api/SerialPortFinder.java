package android_serialport_api;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

import android.util.Log;
/**
 * 查找串口类
 * @author steve
 */
public class SerialPortFinder {
	
	private static final String TAG="SerialPort";
    /**
     * 所有驱动器
     */
	private Vector<Driver> mDrivers=null;
	/**
	 * 获得所有的驱动器
	 * @return
	 * @throws Exception
	 */
	public Vector<Driver> getDrivers() throws Exception{
		if(mDrivers==null){
			mDrivers=new Vector<Driver>();
		    LineNumberReader reader=new LineNumberReader(new FileReader("/proc/tty/drivers"));
		    String line=null;
		    while((line=reader.readLine())!=null){
		         //since driver namen may contain spaces ,we do not extract driver name with split()
		    	 String drivername=line.substring(0,0x15).trim();
		    	 String[] w = line.split(" +");
		    	 if((w.length>=5)&&(w[w.length-1].equals("serial"))){
		    		 Log.d(TAG, "Found new driver "+drivername+" on"+w[w.length-4]);
		    		 mDrivers.add(new Driver(drivername, w[w.length-4]));
		    	 }
		    }
		    reader.close();
		}
		return mDrivers;
	}
	/**
	 * 获得所有的设备名
	 * @return
	 */
	public String[] getAllDevices(){
		Vector<String> devices=new Vector<String>();
		//parse each driver
		Iterator<Driver> itdriv;
		try {
			itdriv=getDrivers().iterator();
			while(itdriv.hasNext()){
				Driver driver=itdriv.next();
				Iterator<File> itdev=driver.getDevices().iterator();
				while(itdev.hasNext()){
					String device=itdev.next().getName();
					String value=String.format("%s (%s)", device,driver.getName());
					devices.add(value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devices.toArray(new String[devices.size()]);
 	}
	
	/**
	 * 获得所有设备的路径
	 * @return
	 */
	public String[] getAllDevicesPath(){
		Vector<String> devices=new Vector<String>();
		//parse each driver;
		Iterator<Driver> itdriv;
		
		try {
			itdriv=getDrivers().iterator();
			while(itdriv.hasNext()){
			   Driver driver=itdriv.next();
			   Iterator<File> itdev=driver.getDevices().iterator();
			   while(itdev.hasNext()){
				   String device=itdev.next().getAbsolutePath();
				   devices.add(device);
			   }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devices.toArray(new String[devices.size()]);
	}
	
	/**
	 * 获得所有设备的描述
	 * @return
	 */
	public String[] getAllDevicesDesc(){
		Vector<String> devices=new Vector<String>();
		//parse each driver;
		Iterator<Driver> itdriv;
		int index=0;
		try {
			itdriv=getDrivers().iterator();
			while(itdriv.hasNext()){
			   Driver driver=itdriv.next();
			   Iterator<File> itdev=driver.getDevices().iterator();
			   while(itdev.hasNext()){
				   index++;
				   File file=itdev.next();
				   String path=file.getAbsolutePath();
				   String desc=String.format("%s (%s)","COM"+index,path);
				   devices.add(desc);
			   }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devices.toArray(new String[devices.size()]);
	}
}
