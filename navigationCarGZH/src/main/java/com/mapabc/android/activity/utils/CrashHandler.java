package com.mapabc.android.activity.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class CrashHandler implements UncaughtExceptionHandler {

    private Context context;
    private UncaughtExceptionHandler defUncaughtExcep;
    private static CrashHandler crashHandler;

    public final static String LOGPATH =   "/sdcard/navigation/crash.txt";

    private CrashHandler() {
    }

    public static CrashHandler newInstance() {
        if (crashHandler == null) {
            synchronized (CrashHandler.class) {
                if (crashHandler == null) {
                    crashHandler = new CrashHandler();
                }
            }
        }
        return crashHandler;
    }

    public void init(Context context) {
        this.context = context;
        defUncaughtExcep = Thread.currentThread().getDefaultUncaughtExceptionHandler();
        Thread.currentThread().setDefaultUncaughtExceptionHandler(this);
//		Thread.currentThread().setUncaughtExceptionHandler(this);
    }


    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);

		/*if(defUncaughtExcep != null){
            defUncaughtExcep.uncaughtException(thread, ex);
		}*/
        try {
            // 在throwable的参数里面保存的有程序的异常信息
            StringBuffer sb = new StringBuffer();
            // 1.得到手机的版本信息 硬件信息
            Field[] fields = Build.class.getDeclaredFields();
            for (Field filed : fields) {
                filed.setAccessible(true); // 暴力反射
                String name = filed.getName();
                String value = filed.get(null).toString();
                sb.append(name);
                sb.append(" =");
                sb.append(value);
                sb.append("\n");
            }
            // 2.得到当前程序的版本号
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            sb.append(info.versionName);
            sb.append("\n");
            // 3.得到当前程序的异常信息
            Writer writer = new StringWriter();
            PrintWriter printwriter = new PrintWriter(writer);

            ex.printStackTrace(printwriter);
            printwriter.flush();
            printwriter.close();

            sb.append(writer.toString());

            File ff = new File(LOGPATH);
            File fileParent = ff.getParentFile();
            if(!fileParent.exists()) {
                fileParent.mkdirs();
            }
            ff.createNewFile();

            FileWriter fw = new FileWriter(new File(LOGPATH));
            fw.write(sb.toString());
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        ActivityStack.newInstance().finishAll();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }



    private boolean handleException(Throwable ex) {
        boolean blHanlder = false;
        if (ex == null) {
            return blHanlder;
        }
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        int size = stackTraceElements.length;
//		Logger.e("Exception", ex.getMessage());
        for (int s = 0; s < size; s++) {
            StackTraceElement element = stackTraceElements[s];
            Log.e("Exception", element.toString());
        }

        return blHanlder;
    }

}
