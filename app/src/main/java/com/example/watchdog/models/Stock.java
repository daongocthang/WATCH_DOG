package com.example.watchdog.models;

import java.io.Serializable;

public class Stock implements Serializable {
    private String symbol;
    private int id;
    private double warningPrice;
    private double lastPrice;
    private int status;

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
