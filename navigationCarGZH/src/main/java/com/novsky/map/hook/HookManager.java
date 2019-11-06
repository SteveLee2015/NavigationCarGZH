package com.novsky.map.hook;

import android.content.Context;
import android.os.IBinder;


import com.novsky.map.hook.handler.BinderHookHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by lisheng on 2019/3/5.
 */
public class HookManager {

    public static boolean hookLocationManager() {
        try {
            Class ServiceManager = Class.forName("android.os.ServiceManager");
            Method getService = ServiceManager.getDeclaredMethod("getService", String.class);
            IBinder binder = (IBinder) getService.invoke(null, Context.LOCATION_SERVICE);

            ClassLoader classLoader = binder.getClass().getClassLoader();
            Class[] interfaces = {IBinder.class};
            BinderHookHandler handler = new BinderHookHandler(binder);
            IBinder customBinder = (IBinder) Proxy.newProxyInstance(classLoader, interfaces, handler);

            Field sCache = ServiceManager.getDeclaredField("sCache");
            sCache.setAccessible(true);
            Map<String, IBinder> cache = (Map<String, IBinder>) sCache.get(null);

            cache.put(Context.LOCATION_SERVICE, customBinder);
            sCache.setAccessible(false);
            return true;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } finally {
        }
        return false;
    }
}
