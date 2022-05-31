package com.standalone.watchdog.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.standalone.watchdog.R;

import java.util.Objects;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        Objects.requireNonNull(getSupportActionBar()).hide();

        final Button btTryAgain = findViewById(R.id.btTryAgain);
        btTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ErrorActivity.this, SplashActivity.class));
                finish();
            }
        });
    }
}