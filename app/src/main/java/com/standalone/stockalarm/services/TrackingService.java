package com.standalone.stockalarm.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.standalone.stockalarm.App;
import com.standalone.stockalarm.Constant;
import com.standalone.stockalarm.R;
import com.standalone.stockalarm.activities.SplashActivity;
import com.standalone.stockalarm.models.Stock;
import com.standalone.stockalarm.receivers.TrackingReceiver;
import com.standalone.stockalarm.utils.DbHandler;
import com.standalone.stockalarm.utils.StockCollection;
import com.standalone.stockalarm.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TrackingService extends Service implements Runnable, StockCollection.PriceResponseListener {

    public static final String ACTION_SERVICE_STOP = "action_service_stop";
    public static final String ACTION_NOTIFICATION_SEND = "action_notification_send";
    private static final int OPEN = 9;
    private static final int CLOSED = 15;

    private static final String TAG = TrackingService.class.getSimpleName();
    private static final int PERIOD = 10000;

    private Thread worker;
    private DbHandler db;
    private boolean running;
    private List<Stock> stockList;
    private StockCollection stockCollection;
    private SimpleDateFormat simpleDateFormat;
    private Calendar calendar;
    private boolean alertedNetworkError;

    @Override
    public void onCreate() {
        super.onCreate();

//        Toast.makeText(this, "Start Tracking Service", Toast.LENGTH_SHORT).show();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("HH:mm");
        db = new DbHandler(this.getApplicationContext());
        db.openDb();
        stockCollection = new StockCollection(this);

        running = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pullStocks();
        sendNotification(Constant.NOTIFICATION_TITLE_NOTHING, null, true);

        if (worker == null) {
            worker = new Thread(this);
            worker.start();
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running = false;
//        Toast.makeText(this, "Force Stop Tracking Service", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public synchronized void pullStocks() {
        stockList = db.getAllStock();
        // stop if no work
        if (stockList.size() == 0)
            stopSelf();
    }

    public void sendNotification(String title, String content, boolean silent) {
        Intent activityIntent = new Intent(this, SplashActivity.class);
        PendingIntent pIntentActivity = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent actionIntent = new Intent(this, TrackingReceiver.class);
        actionIntent.setAction(ACTION_SERVICE_STOP);
        PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pIntentActivity)
                .setSmallIcon(R.drawable.small_logo)
                .addAction(R.drawable.ic_baseline_close, Constant.EXIT, pIntentAction)
                .setSilent(silent)
                // set high priority for Heads Up Notification
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(1, notification);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_NOTIFICATION_SEND);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!running) break;

                if (!Utils.isNetworkConnecting(this)) {
                    if (!alertedNetworkError) {
                        sendNotification(Constant.NETWORK_ERROR, null, false);
                        alertedNetworkError = true;
                    }
                    continue;
                } else {
                    if (alertedNetworkError) {
                        sendNotification(Constant.NOTIFICATION_TITLE_NOTHING, null, true);
                        alertedNetworkError = false;
                    }
                }

                calendar.setTime(new Date());
                int currentHours = calendar.get(Calendar.HOUR_OF_DAY);
                if (currentHours < OPEN || currentHours >= CLOSED)
                    continue;

                stockCollection.collectMatchedPrices(stockList, this);
                Thread.sleep(PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResponse(List<Stock> stocks) {
        StringBuilder stringBuilder = new StringBuilder();
        String strDate = simpleDateFormat.format(calendar.getTime());
        boolean notifiable = false;
        stringBuilder.append(String.format("[ %s ]", strDate));
        for (Stock s : stocks) {
            boolean alert = s.getWarningPrice() <= s.getLastPrice();
            if (s.getType() == Stock.LESS)
                alert = s.getWarningPrice() >= s.getLastPrice();

            if (alert) {
                stringBuilder.append(String.format(" %s%s%.2f ", s.getSymbol(), s.getType() == Stock.LESS ? "<" : ">", s.getWarningPrice()));
                if (!s.isAlerted()) {
                    s.setAlerted(true);
                    notifiable = true;
                }
            } else {
                s.setAlerted(false);
            }
        }

        if (notifiable)
            sendNotification(Constant.NOTIFICATION_TITLE_WARNING, stringBuilder.toString(), false);
    }
}