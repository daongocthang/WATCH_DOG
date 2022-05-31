package com.standalone.watchdog.adapter;

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

import com.standalone.watchdog.R;
import com.standalone.watchdog.models.StockInfo;

import java.util.ArrayList;
import java.util.List;

public class StockSearchAdapter extends ArrayAdapter<String> {
    private final List<StockInfo> items;
    private final Context context;
    private final int layoutResourceId;

    public StockSearchAdapter(@NonNull Context context, int resource, @NonNull List<StockInfo> items) {
        super(context, resource);
        List<String> suggestion = new ArrayList<>();
        for (StockInfo i : items) {
            suggestion.add(i.getSymbol());
        }
        super.addAll(suggestion);
        this.items = items;
        this.context = context;
        this.layoutResourceId = resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String symbol = getItem(position);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        final TextView tvSymbol = convertView.findViewById(R.id.acItemSymbol);
        final TextView tvShortName = convertView.findViewById(R.id.acShortName);

        tvSymbol.setText(symbol);
        StockInfo stockInfo = items.stream().filter(items -> symbol.equals(items.getSymbol())).findFirst().orElse(null);
        tvShortName.setText(stockInfo != null ? stockInfo.getShortName() : "");

        return convertView;
    }
}
