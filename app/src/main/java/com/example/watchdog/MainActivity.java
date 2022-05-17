package com.example.watchdog;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchdog.adapter.AlertAdapter;
import com.example.watchdog.interfaces.DialogCloseListener;
import com.example.watchdog.models.Stock;
import com.example.watchdog.services.TrackingService;
import com.example.watchdog.utils.DbHandler;
import com.example.watchdog.utils.StockCollection;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {
    private static final String TAG  = MainActivity.class.getSimpleName();
    public static final String ACTIVITY_FINISH = "main_activity_finish";
    private Map<String, String> stockDex;
    private DbHandler db;
    private AlertAdapter adapter;
    private StockCollection stockCollection;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTIVITY_FINISH)) {
                finish();
            }
            if(intent.getAction().equals(TrackingService.ACTION_NOTIFICATION_SEND)){
                Log.e(TAG,TrackingService.ACTION_NOTIFICATION_SEND);
                reloadAdapter();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTIVITY_FINISH);
        registerReceiver(receiver, filter);

        stockCollection = new StockCollection(this);
        stockDex = new HashMap<>();
        stockCollection.collectAllStocks(new StockCollection.InfoResponseListener() {
            @Override
            public void onResponse(Map<String, String> map) {
                stockDex.putAll(map);
            }
        });

        db = new DbHandler(this);
        db.openDb();

        RecyclerView taskRecyclerView = findViewById(R.id.tasksRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AlertAdapter(db, this);
        taskRecyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(adapter, this));
        itemTouchHelper.attachToRecyclerView(taskRecyclerView);

        List<Stock> stockList = db.getAllStock();
        Collections.reverse(stockList);
        adapter.setTasks(stockList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertForm(stockDex).show(getSupportFragmentManager(), AlertForm.TAG);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTrackingService();
        reloadAdapter();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        reloadAdapter();
        startTrackingService();
    }

    private void reloadAdapter() {
        List<Stock> dbAllStock = db.getAllStock();
        Collections.reverse(dbAllStock);
        stockCollection.collectAdPrice(dbAllStock, new StockCollection.PriceResponseListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(List<Stock> stocks) {
                adapter.setTasks(stocks);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void startTrackingService() {
        Intent serviceIntent = new Intent(this, TrackingService.class);
        startService(serviceIntent);
    }

    public Map<String, String> getStockDex() {
        return this.stockDex;
    }
}