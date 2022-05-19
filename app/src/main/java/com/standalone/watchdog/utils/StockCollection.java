package com.standalone.watchdog.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.standalone.watchdog.models.Stock;
import com.standalone.watchdog.models.StockInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StockCollection {
    private static final String TAG = StockCollection.class.getSimpleName();
    private final Context context;

    public StockCollection(Context context) {
        this.context = context;
    }

    public void collectAllStocks(InfoResponseListener infoResponseListener) {
        String url = "https://iboard.ssi.com.vn/dchart/api/1.1/defaultAllStocks";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e(TAG, "AllStocks: " + response.toString());
                    JSONArray data = response.getJSONArray("data");
                    List<StockInfo> stockInfoList = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject object = data.getJSONObject(i);
                        if (object.getString("type").equals("s")) {
                            StockInfo stockInfo = new StockInfo();
                            stockInfo.setStockNo(object.getString("stockNo"));
                            stockInfo.setSymbol(object.getString("code"));
                            stockInfo.setShortName(object.getString("clientName"));

                            stockInfoList.add(stockInfo);
                        }
                    }
                    infoResponseListener.onResponse(stockInfoList);

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

    public void collectMatchedPrices(List<Stock> stocks, PriceResponseListener responseListener) {
        String url = "https://wgateway-iboard.ssi.com.vn/graphql";
        List<String> params = new ArrayList<>();
        List<String> closed = new ArrayList<>();
        for (int i = 0; i < stocks.size(); i++) {
            String stockNo = stocks.get(i).getStockNo();

            if (closed.contains(stockNo)) continue;
            params.add(String.format("'%s'", stockNo));

            closed.add(stockNo);
        }

        String body = "{" +
                "    'operationName': 'stockRealtimesByIds'," +
                "    'variables': {" +
                "        'ids': [" + TextUtils.join(",", params) + "]" +
                "    }," +
                "    'query': 'query stockRealtimesByIds($ids: [String!]) {\\n  stockRealtimesByIds(ids: $ids) {\\n    stockNo\\n    stockSymbol\\n    refPrice\\n    matchedPrice\\n  }\\n}\\n'" +
                "}";

        try {
            JSONObject jsonBody = new JSONObject(body.replace("'", "\""));
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONObject("data").getJSONArray("stockRealtimesByIds");
                        Log.e(TAG, "StockRealTIme: " + data.toString());

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.getJSONObject(i);
                            for (Stock s : stocks) {
                                double last;
                                if (s.getStockNo().equals(object.getString("stockNo"))){
                                    last = object.getDouble("matchedPrice");
                                    if (last == 0) {
                                        last = object.getDouble("refPrice");
                                    }
                                    s.setLastPrice(last / 1000);
                                }
                            }
                        }

                        responseListener.onResponse(stocks);


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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<String> getSymbols(List<Stock> stocks) {
        List<String> symbols = new ArrayList<>();
        for (Stock s : stocks) {
            symbols.add(s.getSymbol());
        }
        return symbols;
    }


    public interface PriceResponseListener {
        void onResponse(List<Stock> stocks);
    }

    public interface InfoResponseListener {
        void onResponse(List<StockInfo> stockDex);
    }
}
