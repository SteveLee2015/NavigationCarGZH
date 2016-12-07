package com.novsky.map.main;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import android.os.SystemClock;

public class SystemDateTime {

	static final String TAG = "SystemDateTime";

	public static void setDateTime(int year, int month, int day, int hour,
			int minute) throws InterruptedException, IOException {
		requestPermission();
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		long when = c.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			SystemClock.setCurrentTimeMillis(when);
		}
		long now = Calendar.getInstance().getTimeInMillis();
		if (now - when > 1000) {
			throw new IOException("failed to set Date.");
		}
	}
	public static void setDateTime(long when) throws InterruptedException, IOException {
		requestPermission();
		if (when / 1000 < Integer.MAX_VALUE) {
			SystemClock.setCurrentTimeMillis(when);
		}
		long now = Calendar.getInstance().getTimeInMillis();
		if (now - when > 1000) {
			throw new IOException("failed to set Date.");
		}
	}

	public static void requestPermission() throws InterruptedException,
			IOException {
		createSuProcess("chmod 666 /dev/alarm").waitFor();
	}

	public static Process createSuProcess() throws IOException {
		File rootUser = new File("/system/xbin/su");
		if(rootUser.exists()) {
			return Runtime.getRuntime().exec(rootUser.getAbsolutePath());
		} else {
			return Runtime.getRuntime().exec("su");
		}
	}

	public static Process createSuProcess(String cmd) throws IOException {
		//DataOutputStream os = null;
		Process process = createSuProcess();
		try {
			//os = new DataOutputStream(process.getOutputStream());
			OutputStream stream=process.getOutputStream();
			stream.write(cmd.getBytes());
			stream.write("\n".getBytes());
			stream.write("exit \n".getBytes());
			stream.flush();
			//os.writeBytes(cmd);
			//os.writeBytes("exit $? \n");
			//os.writeBytes("exit");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
//			if (os != null) {
//				os.close();
//			}
		}
		return process;
   }

}
