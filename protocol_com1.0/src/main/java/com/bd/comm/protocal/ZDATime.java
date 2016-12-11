package com.bd.comm.protocal;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by llg on 2016/12/11.
 */

public class ZDATime implements Parcelable {

    //1313 , 2016-12-11 14:27:37.427 , $GNZDA,062512.760,00,00,0000,00,00*4B
    //                                         时间      日  月  年 本地时区 本时区分钟差
    private String utcTime;
    private String day;
    private String month;
    private String year;
    private String timeZone;
    private String minuteDeviation;
    

    public String getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(String utcTime) {
        this.utcTime = utcTime;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getMinuteDeviation() {
        return minuteDeviation;
    }

    public void setMinuteDeviation(String minuteDeviation) {
        this.minuteDeviation = minuteDeviation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ZDATime> CREATOR = new Creator() {
        public ZDATime createFromParcel(Parcel in) {
            ZDATime mZDATime = new ZDATime();
            mZDATime.utcTime = in.readString();
            mZDATime.day = in.readString();
            mZDATime.month = in.readString();
            mZDATime.year = in.readString();
            mZDATime.timeZone = in.readString();
            mZDATime.minuteDeviation = in.readString();
            return mZDATime;
        }

        public ZDATime[] newArray(int size) {
            return new ZDATime[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(this.utcTime);
        parcel.writeString(this.day);
        parcel.writeString(this.month);
        parcel.writeString(this.year);
        parcel.writeString(this.timeZone);
        parcel.writeString(this.minuteDeviation);

    }


    @Override
    public String toString() {
        return "ZDATime{" +
                "utcTime='" + utcTime + '\'' +
                ", day='" + day + '\'' +
                ", month='" + month + '\'' +
                ", year='" + year + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", minuteDeviation='" + minuteDeviation + '\'' +
                '}';
    }
}
