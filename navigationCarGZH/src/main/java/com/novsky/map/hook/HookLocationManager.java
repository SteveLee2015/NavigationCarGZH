package com.novsky.map.hook;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.bd.comm.protocal.BDRNSSLocation;
import com.mapabc.naviapi.route.GpsInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lisheng on 2019/3/5.
 */

public class HookLocationManager {

    private static HookLocationManager instance;
    public List<Object> listeners = new ArrayList<>();
    private List<Object> gpsListeners = new ArrayList<>();

    private Class transport = null;
    private Class statusTransport = null;
    private Location lastLocation = null;

    public static HookLocationManager getInstance() {
        if (instance == null) {
            synchronized (HookLocationManager.class) {
                instance = new HookLocationManager();
            }
        }
        return instance;
    }

    private HookLocationManager() {
        try {
            lastLocation = new Location(LocationManager.GPS_PROVIDER);
            transport = Class.forName("android.location.LocationManager$ListenerTransport");
            statusTransport = Class.forName("android.location.LocationManager$GpsStatusListenerTransport");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addLocationListner(Object listener) {
        if (listeners != null) {
            listeners.add(listener);
        }
    }

    public void removeLocationListener(Object listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void addGpsStatusListener(Object gpsListener) {
        if (gpsListeners != null) {
            gpsListeners.add(gpsListener);
        }
    }

    public void removeGpsStatusListener(Object gpsListener) {
        if (gpsListeners != null) {
            gpsListeners.remove(gpsListener);
        }
    }

    public void notifyGpsStatusListener(int svCount, int[] prns, float[] snrs,
                                        float[] elevations, float[] azimuths, int ephemerisMask,
                                        int almanacMask, int usedInFixMask ) {

        for (Object  obj : gpsListeners) {
            try {
                Method[] methods = statusTransport.getDeclaredMethods();
                Method onSvStatusChanged = null;
                for (int i = 0; i < methods.length ; i++ ) {
                   Method method = methods[i];
                   if (method.getName().equals("onSvStatusChanged")) {
                       onSvStatusChanged = method;
                   }
                }
                if (onSvStatusChanged != null) {
                    onSvStatusChanged.invoke(obj, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyGpsStatusListener(int time) {

        for (Object  obj : gpsListeners) {
            try {
                Method[] methods = statusTransport.getDeclaredMethods();
                Method onFirstFix = null;
                for (int i = 0; i < methods.length ; i++ ) {
                    Method method = methods[i];
                    if (method.getName().equals("onFirstFix")) {
                        onFirstFix = method;
                    }
                }
                if (onFirstFix != null) {
                    onFirstFix.invoke(obj, time);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void notifyListener(BDRNSSLocation bdrnssLocation) {
        if (bdrnssLocation == null) {
            return ;
        }
        lastLocation.setLongitude(bdrnssLocation.getLongitude());
        lastLocation.setLatitude(bdrnssLocation.getLatitude());
        lastLocation.setBearing(bdrnssLocation.getBearing());
        lastLocation.setAccuracy(bdrnssLocation.getAccuracy());
        lastLocation.setAltitude(bdrnssLocation.getAltitude());
        lastLocation.setExtras(bdrnssLocation.getExtras());
        lastLocation.setProvider(bdrnssLocation.getProvider());
        lastLocation.setSpeed(bdrnssLocation.getSpeed());
        lastLocation.setTime(bdrnssLocation.getTime());
        notifyListener(lastLocation);
    }

    public void notifyListener(Location location) {
        for (Object obj : listeners) {
            try {
                Method onLocationChanged = transport.getDeclaredMethod("onLocationChanged", Location.class);
                onLocationChanged.invoke(obj, location);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public Location getLastLocation() {
        if (lastLocation == null) {
            lastLocation = new Location(LocationManager.GPS_PROVIDER);
            lastLocation.setLongitude(116.23);
            lastLocation.setLatitude(39.54);
        }
        return lastLocation;
    }
}
