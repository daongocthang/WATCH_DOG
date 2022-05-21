package com.standalone.stockalarm.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;

public class Utils {
    public static boolean isServiceRunning(Context context, Class<?> cls) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (cls.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkConnecting(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            return cm.getActiveNetworkInfo().isConnected();
        }catch (Exception e){
            return false;
        }
    }
}
