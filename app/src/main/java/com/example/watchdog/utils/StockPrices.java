package com.example.watchdog.utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.watchdog.interfaces.OnStockPricesListener;
import com.example.watchdog.models.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StockPrices {
    private static final String TAG = StockPrices.class.getSimpleName();
    private static final String FINFO_API = "https://finfoapi-hn.vndirect.com.vn/stocks/adPrice?symbols=";

    private OnStockPricesListener onStockPricesListener;
    private List<Stock> stockList;
    private String url;


    public StockPrices() {
    }

    public void collect() {

        assert !url.isEmpty();
        String stringJson = HttpHandler.getInstance().makeServiceCall(url);
        if (stringJson != null) {
            try {
                JSONObject jsonObject = new JSONObject(stringJson);
                JSONArray data = jsonObject.getJSONArray("data");
                pushStockPrices(data);
                if (onStockPricesListener != null) {
                    onStockPricesListener.onResponse(stockList);
                }
                Log.e(TAG, "JSONObject_Response: " + data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnStockPricesListener(OnStockPricesListener listener) {
        this.onStockPricesListener = listener;
    }

    public void setStocks(List<Stock> stocks) {

        List<String> symbols = new ArrayList<>();
        for (Stock s : stocks) {
            symbols.add(s.getSymbol());
        }

        this.stockList = stocks;
        url = FINFO_API + TextUtils.join(",", symbols);
    }

    private void pushStockPrices(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                for (Stock s : getBySymbol(jsonObject.getString("symbol"))) {
                    s.setLastPrice(jsonObject.getDouble("close"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<Stock> getBySymbol(String symbol) {
        List<Stock> stocks = new ArrayList<>();
        for (Stock s : stockList) {
            if (s.getSymbol().equals(symbol))
                stocks.add(s);
        }
        return stocks;
    }
}
