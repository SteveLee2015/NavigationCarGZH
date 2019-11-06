package com.novsky.map.util;

/**
 * Created by lisheng on 2019/2/18.
 */

public class SCConstants {

    public static final int MULTI_POINT_NUM = 0x05;


    public static final long ONE_DAY_MILLSECOND =   10 * 60 * 1000;

    public static final int TIME_ZONE = 8;
    public static final int BD_CARD_FREQ_MILLIS = 61 * 1000;
    public static final int ALARM_MSG_MAX_LENGTH = 40;
    public static final int CHECK_TIME = 0x100001,
            UPDATE_DISTANCE = 0x100002,
            UPDATE_MULTI_POSITION = 0x100003,
            UPDATE_TIME_OUT = 0x100004 ,
            ALARM_STATUS = 0x100005 ,
            DO_NOTHIING = 0x100005,
            OUT_LINE_TYPE = 0x100006,
            GET_LISTENER = 0x100007,
            GET_4G_MESSAGE = 0x100008;

    public static int  mLaunchSequence = CHECK_TIME;
    public static int ALARM_MAX_NUM = 5;

    public static String QEURY_4G_SIGNAl = "AT+CSQ";
    public static String SEND_4G_CMD = "AT+CMGS";
    public static String QUERY_4G_MESSAGE = "AT+CMGL";

    private static boolean isLoaded = false;

    public static boolean isIsLoaded() {
        return isLoaded;
    }

    public static void setIsLoaded(boolean isLoaded) {
        SCConstants.isLoaded = isLoaded;
    }
}
