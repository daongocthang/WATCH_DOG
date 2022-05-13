package com.example.watchdog.interfaces;

import com.example.watchdog.models.Stock;

import org.json.JSONObject;

import java.util.List;

public interface OnStockPricesListener {
    public void onResponse(List<Stock> stocks);
}
