/**
 * Project Name: MyFrameWork
 *
 * @version 3.6
 */
package com.mapabc.android.activity.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.mapabc.android.activity.base.NaviStudioApplication;


/**
 * 储存类
 */
public class FMSharedPreferenceUtils {

    public static final String SHARED_PERFERENCES_NAME = "sharedperference";
    public static final String KEY_CREATE_SHORTCUT = "key_create_shortcut";
    public static final String KEY_ISFRISTGUIDE = "key_isFristGuide";
    public static final String KEY_ISRUNNING = "isRunning";
    public static final String KEY_UUID = "UUID";
    public static final String KEY_REGISTERSUCCESS = "registerSuccess";

    private static FMSharedPreferenceUtils mSharePreferenceUtils;

    private FMSharedPreferenceUtils() {

    }

    public synchronized static FMSharedPreferenceUtils getInstance() {
        if (mSharePreferenceUtils == null) {
            mSharePreferenceUtils = new FMSharedPreferenceUtils();
        }
        return mSharePreferenceUtils;
    }

    private SharedPreferences getsp() {
        NaviStudioApplication instance = NaviStudioApplication.getContext();
        SharedPreferences sharedPreferences = instance.getSharedPreferences(SHARED_PERFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public void putInt(String key, int value) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                Editor editor = sharedPreferences.edit();
                editor.putInt(key, value);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                defaultValue = sharedPreferences.getInt(key, defaultValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public void putLong(String key, long value) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                Editor editor = sharedPreferences.edit();
                editor.putLong(key, value);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public long getLong(String key, long defValue) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                defValue = sharedPreferences.getLong(key, defValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    public void putBoolean(String key, boolean flag) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                Editor editor = sharedPreferences.edit();
                editor.putBoolean(key, flag);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getBoolean(String key, boolean flag) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                flag = sharedPreferences.getBoolean(key, flag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public void putString(String key, String value) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                Editor editor = sharedPreferences.edit();
                editor.putString(key, value);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getString(String key, String defValue) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                defValue = sharedPreferences.getString(key, defValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    public void remove(String key) {
        try {
            SharedPreferences sharedPreferences = getsp();
            if (sharedPreferences != null) {
                Editor editor = sharedPreferences.edit();
                editor.remove(key);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
  
	