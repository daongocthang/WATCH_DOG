package com.example.watchdog.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.watchdog.R;
import com.example.watchdog.models.StockInfo;
import com.example.watchdog.utils.StockCollection;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Objects.requireNonNull(getSupportActionBar()).hide();

        StockCollection stockCollection=new StockCollection(this);
        stockCollection.collectAllStocks(new StockCollection.InfoResponseListener() {
            @Override
            public void onResponse(List<StockInfo> list) {
                final Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_STOCK_INFO,(Serializable) list);
                startActivity(intent);
                finish();
            }
        });
    }
}