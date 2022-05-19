package com.standalone.watchdog.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.standalone.watchdog.AlertForm;
import com.standalone.watchdog.Constant;
import com.standalone.watchdog.R;
import com.standalone.watchdog.activities.MainActivity;

import com.standalone.watchdog.models.Stock;
import com.standalone.watchdog.utils.DbHandler;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private List<Stock> stockList;
    private final MainActivity activity;
    private final DbHandler db;

    public AlertAdapter(DbHandler db, MainActivity activity) {
        this.activity = activity;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alert_item, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        db.openDb();

        final Stock item = stockList.get(position);

        holder.tvSymbol.setText(item.getSymbol());
        holder.tvWarning.setText(String.valueOf(item.getWarningPrice()));
        holder.tvLast.setText(String.valueOf(item.getLastPrice()));
        if (item.getType() == Stock.LESS) {
            holder.tvType.setText(Constant.LESS);
            holder.tvType.setTextColor(ContextCompat.getColor(activity, R.color.warning_dark));

            if (item.getWarningPrice() >= item.getLastPrice()) {
                holder.imIcon.setVisibility(View.VISIBLE);
            }else {
                holder.imIcon.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.tvType.setText(Constant.GREATER);
            holder.tvType.setTextColor(ContextCompat.getColor(activity, R.color.success_dark));

            if (item.getWarningPrice() <= item.getLastPrice()) {
                holder.imIcon.setVisibility(View.VISIBLE);
            }else{
                holder.imIcon.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public Context getContext() {
        return activity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<Stock> stockList) {
        this.stockList = stockList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        Stock item = stockList.get(position);
        db.deleteStock(item.getId());
        stockList.remove(position);
        notifyItemRemoved(position);

        activity.startTrackingService();
    }

    public void editItem(int position) {
        Stock item = stockList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("stock", item);
        bundle.putInt("id", item.getId());
        AlertForm fragment = new AlertForm(activity.getStockDex());
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AlertForm.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol;
        TextView tvWarning;
        TextView tvLast;
        TextView tvType;
        ImageView imIcon;

        ViewHolder(View view) {
            super(view);
            tvSymbol = view.findViewById(R.id.alItemSymbol);
            tvWarning = view.findViewById(R.id.alItemWarning);
            tvLast = view.findViewById(R.id.alItemLast);
            tvType = view.findViewById(R.id.alItemType);
            imIcon = view.findViewById(R.id.alItemIcon);
        }
    }
}
