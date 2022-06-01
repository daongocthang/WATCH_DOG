package com.standalone.watchdog.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.standalone.watchdog.App;
import com.standalone.watchdog.Constant;
import com.standalone.watchdog.R;
import com.standalone.watchdog.activities.SplashActivity;
import com.standalone.watchdog.models.Stock;
import com.standalone.watchdog.receivers.TrackingReceiver;
import com.standalone.watchdog.utils.DbHandler;
import com.standalone.watchdog.utils.StockCollection;
import com.standalone.watchdog.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
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
        sendNotification(Constant.NOTIFICATION_TITLE_NOTHING, null, null, true);

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

    public void sendNotification(String title, String content, String expContent, boolean silent) {
        Intent activityIntent = new Intent(this, SplashActivity.class);
        PendingIntent pIntentActivity = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent actionIntent = new Intent(this, TrackingReceiver.class);
        actionIntent.setAction(ACTION_SERVICE_STOP);
        PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setContentIntent(pIntentActivity)
                .setSmallIcon(R.drawable.small_logo)
                .addAction(R.drawable.ic_baseline_close, Constant.EXIT, pIntentAction)
                .setSilent(silent)
                // set high priority for Heads Up Notification
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (!TextUtils.isEmpty(expContent)) {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(expContent));
        }

        Notification notification = builder.build();
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
                        sendNotification(Constant.NETWORK_ERROR, null, null, false);
                        alertedNetworkError = true;
                    }
                    continue;
                } else {
                    if (alertedNetworkError) {
                        sendNotification(Constant.NOTIFICATION_TITLE_NOTHING, null, null, true);
                        alertedNetworkError = false;
                    }
                }

                calendar.setTime(new Date());
                int currentHours = calendar.get(Calendar.HOUR_OF_DAY);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                if (currentHours < OPEN || currentHours >= CLOSED || dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY)
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
        StringBuilder contentText = new StringBuilder();
        StringBuilder bigContentText = new StringBuilder();
        String strDate = simpleDateFormat.format(calendar.getTime());
        boolean notifiable = false;
        contentText.append(String.format("[ %s ]%5s", strDate, " "));
        bigContentText.append(String.format("%-10s\t%10s\t%15s %n",
                Constant.NOTIFICATION_COLS.get("symbol"),
                Constant.NOTIFICATION_COLS.get("alarm"),
                Constant.NOTIFICATION_COLS.get("market")
        ));
        for (Stock s : stocks) {
            if (s.getLastPrice() == 0) continue;

            boolean alert = s.getWarningPrice() <= s.getLastPrice();
            if (s.getType() == Stock.LESS)
                alert = s.getWarningPrice() >= s.getLastPrice();

            if (alert) {
                String level = new Formatter().format("%s %.2f", s.getType() == Stock.LESS ? "<" : ">", s.getWarningPrice()).toString();
                contentText.append(new Formatter().format("%s%-10s", s.getSymbol(), level.replace(" ", "")));
                bigContentText.append(new Formatter().format("%-10s\t%10s\t%18.2f %n", s.getSymbol(), level, s.getLastPrice()));
                if (!s.isAlerted()) {
                    s.setAlerted(true);
                    notifiable = true;
                }
            } else {
                s.setAlerted(false);
            }
        }

        if (notifiable)
            sendNotification(Constant.NOTIFICATION_TITLE_WARNING, contentText.toString(), bigContentText.toString(), false);
    }

    @Override
    public void onError() {
        //Nothing
    }
}
