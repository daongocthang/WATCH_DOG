package com.example.watchdog.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchdog.AddNewTask;
import com.example.watchdog.MainActivity;
import com.example.watchdog.R;
import com.example.watchdog.models.Stock;
import com.example.watchdog.utils.DbHandler;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<Stock> stockList;
    private final MainActivity activity;
    private final DbHandler db;

    public StockAdapter(DbHandler db, MainActivity activity) {
        this.activity = activity;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        db.openDb();

        final Stock item = stockList.get(position);
        holder.tvWarning.setText(String.valueOf(item.getWarningPrice()));
        holder.tvLast.setText(String.valueOf(item.getLastPrice()));

        holder.checkBox.setText(item.getSymbol());
        holder.checkBox.setChecked(toBoolean(item.getStatus()));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                db.updateStatus(item.getId(), isChecked ? 1 : 0);
                activity.startTrackingService();
            }
        });
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public Context getContext() {
        return activity;
    }

    private boolean toBoolean(int n) {
        return n != 0;
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
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvWarning;
        TextView tvLast;

        ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.stackCheckBox);
            tvWarning = view.findViewById(R.id.stackWarning);
            tvLast = view.findViewById(R.id.stackLast);
        }
    }
}
