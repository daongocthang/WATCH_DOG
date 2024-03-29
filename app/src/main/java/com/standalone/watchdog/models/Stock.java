package com.standalone.watchdog.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Stock implements Serializable {
    public static final int LESS_THAN = 0;
    public static final int GREATER_THAN = 1;

    private int id;
    private String symbol;
    private String stockNo;
    private String shortName;
    private double warningPrice;
    private double lastPrice;
    private int type;
    private boolean alerted;

    public Stock() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getWarningPrice() {
        return warningPrice;
    }

    public void setWarningPrice(double warningPrice) {
        this.warningPrice = warningPrice;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isAlerted() {
        return alerted;
    }

    public void setAlerted(boolean alerted) {
        this.alerted = alerted;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getStockNo() {
        return stockNo;
    }

    public void setStockNo(String stockNo) {
        this.stockNo = stockNo;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    public String toString() {
        return String.format("id=%d;symbol=%s;warning=%,.2f;last=%,.2f", id, symbol, warningPrice, lastPrice);
    }
}
