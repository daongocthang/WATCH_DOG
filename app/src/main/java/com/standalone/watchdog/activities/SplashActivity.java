package com.standalone.watchdog.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.standalone.watchdog.R;
import com.standalone.watchdog.models.StockInfo;
import com.standalone.watchdog.utils.StockCollection;
import com.standalone.watchdog.utils.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @SuppressLint("SourceLockedOrientationActivity")
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