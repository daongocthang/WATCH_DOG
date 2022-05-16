package com.example.watchdog.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.watchdog.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StockSearchAdapter extends ArrayAdapter<String> {
    private final Map<String,String> map;
    private final Context context;
    private final int layoutResourceId;

    public StockSearchAdapter(@NonNull Context context, int resource, @NonNull Map<String, String> map) {
        super(context,resource);
        super.addAll(map.keySet());
        this.map =map;
        this.context=context;
        this.layoutResourceId=resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String symbol=getItem(position);

        LayoutInflater inflater=((Activity) context).getLayoutInflater();
        convertView=inflater.inflate(layoutResourceId,parent,false);

        final TextView tvSymbol= convertView.findViewById(R.id.acItemSymbol);
        final TextView tvShortName=convertView.findViewById(R.id.acShortName);

        tvSymbol.setText(symbol);
        tvShortName.setText(map.get(symbol));

        return convertView;
    }
}
