package com.standalone.stockalarm.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class HttpVolley {
    private RequestQueue requestQueue;
    private static HttpVolley instance;

    private HttpVolley(Context context) {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized HttpVolley getInstance(Context context) {
        if (instance == null)
            instance = new HttpVolley(context);
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
