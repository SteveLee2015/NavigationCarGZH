package com.novsky.map.util;

/**
 * Created by llg on 2016/12/8.
 */


import android.content.Context;

/**
 * 常用的配置参数
 */
public class Config {

    //自定义位置报告
    public static final int MY_LOC_REPORT_NOTIFICATION = 2016;
    //短报文
    public static final int SMS_NOTIFICATION = 2017;
    //线路导航
    public static final int BDNAL_NOTIFICATION = 2018;
    //指令导航
    public static final int BDNAC_NOTIFICATION = 2019;
    //友邻位置
    public static final int FRIENDS_LOC_NOTIFICATION = 2020;

    //rd rn 位置报告时间
    public static final int RN_RD_WAA = 1001;
    //rd 定位申请时间
    public static final int RD_DWR = 1002;

    /**定位模式适配*/
    /**
     * 定义访问模式为私有模式
     */
    public static int MODE = Context.MODE_PRIVATE;

    /**
     * 设置保存时的文件的名称
     */
    public static final String PREFERENCE_NAME = "LOCATION_MODEL_ACTIVITY";
    public static final String LOCATION_MODEL = "LOCATION_MODEL";
}
