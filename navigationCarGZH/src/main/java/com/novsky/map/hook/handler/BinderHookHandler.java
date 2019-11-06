package com.novsky.map.hook.handler;

import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by lisheng on 2019/3/5.
 */

public class BinderHookHandler implements InvocationHandler {

    private IBinder mOriginBinder;
    private Class ILocationManager;

    public BinderHookHandler(IBinder binder) {
        this.mOriginBinder = binder;
        try {
            this.ILocationManager = Class.forName("android.location.ILocationManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            // 使得返回自定义的Service
            case "queryLocalInterface":
                ClassLoader classLoader = mOriginBinder.getClass().getClassLoader();
                Class[] interfaces = new Class[] {IInterface.class, IBinder.class, ILocationManager};
                ServiceHookHandler handler = new ServiceHookHandler(this.mOriginBinder);
                return Proxy.newProxyInstance(classLoader, interfaces, handler);

            default:
                return method.invoke(mOriginBinder, args);
        }
    }
}
