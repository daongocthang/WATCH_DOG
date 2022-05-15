package com.example.watchdog.services;

import static com.example.watchdog.App.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.watchdog.Constant;
import com.example.watchdog.MainActivity;
import com.example.watchdog.R;
import com.example.watchdog.models.Stock;
import com.example.watchdog.receivers.TrackingReceiver;
import com.example.watchdog.utils.DbHandler;
import com.example.watchdog.utils.StockCollection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TrackingService extends Service implements Runnable, StockCollection.PriceResponseListener {

    public static final String ACTION_SERVICE_STOP = "action_service_stop";

    private static final String TAG = TrackingService.class.getSimpleName();
    private static final int PERIOD = 10000;

    private Thread worker;
    private DbHandler db;
    private boolean running;
    private List<Stock> stockList;
    private StockCollection stockCollection;
    private SimpleDateFormat simpleDateFormat;

    @Override
    public void onCreate() {
        super.onCreate();

//        Toast.makeText(this, "Start Tracking Service", Toast.LENGTH_SHORT).show();
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
        //TODO: add stockList
        stockList = db.getAllStock();

        // stop if no work
        if (stockList.size() == 0)
            stopSelf();
    }

    public void sendNotification(String title, String content, boolean silent) {
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pIntentActivity = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent actionIntent = new Intent(this, TrackingReceiver.class);
        actionIntent.setAction(ACTION_SERVICE_STOP);
        PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pIntentActivity)
                .setSmallIcon(R.drawable.logo)
                .addAction(R.drawable.ic_baseline_close, Constant.EXIT, pIntentAction)
                .setSilent(silent)
                // set high priority for Heads Up Notification
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!running) break;
                stockCollection.collectAdPrice(stockList, this);
                Thread.sleep(PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResponse(List<Stock> stocks) {
        StringBuilder stringBuilder = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
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
