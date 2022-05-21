package com.standalone.stockalarm.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.standalone.stockalarm.R;
import com.standalone.stockalarm.models.StockInfo;
import com.standalone.stockalarm.utils.StockCollection;
import com.standalone.stockalarm.utils.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Objects.requireNonNull(getSupportActionBar()).hide();

        if (!Utils.isNetworkConnecting(this)) {
            startActivity(new Intent(this, ErrorActivity.class));
            finish();
        } else {
            StockCollection stockCollection = new StockCollection(this);
            stockCollection.collectAllStocks(new StockCollection.InfoResponseListener() {
                @Override
                public void onResponse(List<StockInfo> list) {
                    final Intent newIntent = new Intent(SplashActivity.this, MainActivity.class);
                    newIntent.putExtra(MainActivity.EXTRA_STOCK_INFO, (Serializable) list);
                    startActivity(newIntent);
                    finish();
                }
            });
        }

    }
}