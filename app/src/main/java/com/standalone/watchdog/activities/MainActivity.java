package com.standalone.watchdog.activities;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.standalone.watchdog.AlertForm;
import com.standalone.watchdog.R;
import com.standalone.watchdog.RecyclerItemTouchHelper;
import com.standalone.watchdog.adapter.AlertAdapter;
import com.standalone.watchdog.interfaces.DialogCloseListener;
import com.standalone.watchdog.models.Stock;
import com.standalone.watchdog.models.StockInfo;
import com.standalone.watchdog.services.TrackingService;
import com.standalone.watchdog.utils.DbHandler;
import com.standalone.watchdog.utils.StockCollection;
import com.standalone.watchdog.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {
    public static final String EXTRA_STOCK_INFO = "extra_stock_info";

    public static final String ACTIVITY_FINISH = "main_activity_finish";
    public static final String TAG = MainActivity.class.getSimpleName();

    private List<StockInfo> stockDex;
    private DbHandler db;
    private AlertAdapter adapter;
    private StockCollection stockCollection;
    private Boolean hasError;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTIVITY_FINISH)) {
                finish();
            }
            if (intent.getAction().equals(TrackingService.ACTION_NOTIFICATION_SEND)) {
                Log.e(TAG, TrackingService.ACTION_NOTIFICATION_SEND);
                reloadAdapter();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        Intent intent = getIntent();
        stockDex = new ArrayList<>();
        List<StockInfo> extra = (List<StockInfo>) intent.getSerializableExtra(EXTRA_STOCK_INFO);
        if (extra != null)
            stockDex.addAll(extra);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTIVITY_FINISH);
        registerReceiver(receiver, filter);

        stockCollection = new StockCollection(this);

        db = new DbHandler(this);
        db.openDb();

        RecyclerView taskRecyclerView = findViewById(R.id.tasksRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AlertAdapter(db, this);
        taskRecyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(adapter));
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
        if (!Utils.isServiceRunning(this, TrackingService.class))
            startTrackingService();

        hasError = false;
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

    @SuppressLint("NotifyDataSetChanged")
    private void reloadAdapter() {
        if (!Utils.isNetworkConnecting(this)) {
            startActivity(new Intent(this, ErrorActivity.class));
            finish();
        }

        List<Stock> dbAllStock = db.getAllStock();
        Collections.reverse(dbAllStock);
        if (hasError) {
            adapter.setTasks(dbAllStock);
            adapter.notifyDataSetChanged();
            return;
        }
        stockCollection.collectMatchedPrices(dbAllStock, new StockCollection.PriceResponseListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(List<Stock> stocks) {
                adapter.setTasks(stocks);
                adapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onError() {
                hasError = true;
            }
        });
    }

    public void startTrackingService() {
        Intent serviceIntent = new Intent(this, TrackingService.class);
        startService(serviceIntent);
    }

    public List<StockInfo> getStockDex() {
        return this.stockDex;
    }
}