package com.example.watchdog.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.watchdog.MainActivity;
import com.example.watchdog.services.TrackingService;

public class TrackingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(TrackingService.ACTION_SERVICE_STOP)){
            Intent servIntent=new Intent(context, TrackingService.class);
            context.stopService(servIntent);

            Intent local=new Intent();
            local.setAction(MainActivity.ACTIVITY_FINISH);
            context.sendBroadcast(local);
        }
    }
}
