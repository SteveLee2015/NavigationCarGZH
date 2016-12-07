package com.bd.comm.protocal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * 读取properties配置文件
 * 
 * @date 2014-1-15 10:06:38
 * 
 * 
 */
public class MyProperUtil {
	
	private static String TAG ="MyProperUtil";
	
	private static Properties urlProps;

	/**
	 * 读取properties文件
	 * 
	 * @param c
	 * @return
	 */
	public static Properties getProperties(Context c) {
		Properties props = new Properties();
		try {
			// 方法一：通过activity中的context攻取setting.properties的FileInputStream
			InputStream in = c.getAssets().open("config.properties");
			// 方法二：通过class获取setting.properties的FileInputStream
			// InputStream in =
			// PropertiesUtill.class.getResourceAsStream("/assets/map.properties"));
			props.load(in);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		urlProps = props;

		return urlProps;
	}

	/**
	 * 读取复制的properties文件
	 * 
	 * @param c
	 * @param path
	 *            为data/data/packageName/fileName
	 * @return
	 */
	public static Properties getPropertiesWithClass() {
		Properties props = new Properties();
		try {
			// 方法一：通过activity中的context攻取setting.properties的FileInputStream
			// InputStream in = c.getAssets().open("map.properties");
			// 方法二：通过class获取setting.properties的FileInputStream
			InputStream in = MyProperUtil.class.getResourceAsStream("/assets/map.properties");
			props.load(in);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		urlProps = props;

		// System.out.println(urlProps.getProperty("class"));
		return urlProps;
	}


	
	/**
	 * 复制文件 及文件夹 保存地址为data/data/packageName/文件名
	 * 
	 * @param path
	 */
	public static void copyFileOrDir(Context c, String oldPath) {
		AssetManager assetManager = c.getAssets();
		String assets[] = null;
		try {
			assets = assetManager.list(oldPath);
			if (assets.length == 0) {
				copyFile(c, oldPath);
			} else {
				String newFullPath = "/data/data/" + c.getPackageName() + "/"
						+ oldPath;
				File dir = new File(newFullPath);
				if (!dir.exists())
					dir.mkdir();
				for (int i = 0; i < assets.length; ++i) {
					copyFileOrDir(c, oldPath + "/" + assets[i]);
				}
			}
		} catch (IOException ex) {
			Log.e(TAG,"I/O Exception"+ ex);
		}
	}

	/**
	 * 复制文件
	 * 
	 * @param filename
	 */
	public static void copyFile(Context c, String filename) {
		AssetManager assetManager = c.getAssets();

		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(filename);
			String newFileName = "/data/data/" + c.getPackageName() + "/"
					+ filename;
			out = new FileOutputStream(newFileName);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch (Exception e) {
			Log.e(TAG,e.getMessage());
		}

	}
}