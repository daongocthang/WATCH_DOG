package com.example.watchdog.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.watchdog.models.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockCollection {
    private static final String TAG = StockCollection.class.getSimpleName();
    private static final String FINFO_API = "https://finfoapi-hn.vndirect.com.vn/stocks";

    private List<Stock> stockList;
    private String url;
    private final Context context;

    public StockCollection(Context context) {
        this.context = context;
    }

    public void collectAllStocks(InfoResponseListener infoResponseListener) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, FINFO_API, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray("data");
                    Map<String, String> map = new HashMap<>();

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject object = data.getJSONObject(i);
                        map.put(object.getString("symbol"),object.getString("shortName"));
                    }
                    Log.e(TAG,map.toString());
                    infoResponseListener.onResponse(map);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "JsonObjectRequest onErrorResponse: " + error.getMessage());
            }
        });

        HttpVolley.getInstance(context).getRequestQueue().add(request);
    }


    public void collectAdPrice(List<Stock> stocks, PriceResponseListener responseListener) {
        initialize(stocks);
        assert !url.isEmpty();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray("data");
                    Log.e(TAG, "JSONObject onResponse: " + data);

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject object = data.getJSONObject(i);
                        for (Stock s : stockList) {
                            if (s.getSymbol().equals(object.getString("symbol")))
                                s.setLastPrice(object.getDouble("close"));
                        }
                    }

                    responseListener.onResponse(stockList);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "JsonObjectRequest onErrorResponse: " + error.getMessage());
            }
        });

        HttpVolley.getInstance(context).getRequestQueue().add(request);
    }


    private void initialize(List<Stock> stocks) {
        List<String> symbols = new ArrayList<>();
        for (Stock s : stocks) {
            symbols.add(s.getSymbol());
        }
        this.stockList = stocks;
        this.url = FINFO_API + "/adPrice?symbols=" + TextUtils.join(",", symbols);
    }


    public interface PriceResponseListener {
        void onResponse(List<Stock> stocks);
    }
    public interface InfoResponseListener{
        void onResponse(Map<String,String> map);
    }
}
