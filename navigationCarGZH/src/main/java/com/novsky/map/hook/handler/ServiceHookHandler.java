package com.novsky.map.hook.handler;

import android.os.IBinder;
import android.util.Log;

import com.novsky.map.hook.HookLocationManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by lisheng on 2019/3/5.
 */
public class ServiceHookHandler implements InvocationHandler {

    private Object mOriginService;
    private Class transport = null;
    private HookLocationManager hookLocationManager;
    private Class statusTransport = null;

    public ServiceHookHandler(IBinder binder) {
        try {
            statusTransport = Class.forName("android.location.LocationManager$GpsStatusListenerTransport");
            hookLocationManager = HookLocationManager.getInstance();
            transport = Class.forName("android.location.LocationManager$ListenerTransport");
            Class ILocationManager$Stub = Class.forName("android.location.ILocationManager$Stub");
            Method asInterface = ILocationManager$Stub.getDeclaredMethod("asInterface", IBinder.class);
            this.mOriginService = asInterface.invoke(null, binder);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        switch(method.getName()) {

            case "getLastLocation":
                Log.i("TEST" ,"===============>invoke getLastKnownLocation");
                return hookLocationManager.getLastLocation();

            case "requestLocationUpdates" :
                if (objects != null) {
                    for (int i = 0; i <objects.length ; i++) {
                        Log.i("TEST" , "=================> requestLocationUpdates" + objects[i]);
                        Object obj = objects[i];
                        if(obj != null) {
                            if (obj.getClass().equals(transport)) {
                                hookLocationManager.addLocationListner(obj);
                            }
                        }
                    }
                }
                return method.invoke(this.mOriginService, objects);

            case "requestSingleUpdate" :
                if (objects != null) {
                    for (int i = 0; i <objects.length ; i++) {
                        Log.i("TEST" , "=================> requestLocationUpdates" + objects[i]);
                        Object obj = objects[i];
                        if(obj != null) {
                            if (obj.getClass().equals(transport)) {
                                hookLocationManager.addLocationListner(obj);
                            }
                        }
                    }
                }
                return method.invoke(this.mOriginService, objects);

            case "addGpsStatusListener":
                if (objects != null) {
                    for (int i = 0; i < objects.length; i++) {
                        Object obj = objects[i];
                        Log.i("TEST", "=================> addGpsStatusListener " + objects[i]);
                        if(obj != null) {
                            if (obj.getClass().equals(statusTransport)) {
                                hookLocationManager.addGpsStatusListener(obj);
                            }
                        }
                    }
                }
                return method.invoke(this.mOriginService, objects);

            case "removeGpsStatusListener":
                if (objects != null) {
                    for (int i = 0; i < objects.length; i++) {
                        Object obj = objects[i];
                        Log.i("TEST", "=================> removeGpsStatusListener " + objects[i]);
                        if(obj != null) {
                            if (obj.getClass().equals(statusTransport)) {
                                hookLocationManager.removeGpsStatusListener(obj);
                            }
                        }
                    }
                }
                return method.invoke(this.mOriginService, objects);
            case "removeUpdates" :
                if (objects != null) {
                    for (int i = 0; i <objects.length ; i++) {
                        Log.i("TEST", "=================> removeUpdates " + objects[i]);
                        Object obj = objects[i];
                        if(obj != null && obj.getClass().equals(transport)) {
                            hookLocationManager.removeLocationListener(obj);
                        }
                    }
                }
                return method.invoke(this.mOriginService, objects);
            default:
                return method.invoke(this.mOriginService, objects);
        }
    }
}
