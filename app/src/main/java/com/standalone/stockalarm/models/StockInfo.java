package com.standalone.stockalarm.models;

import java.io.Serializable;

public class StockInfo implements Serializable {
    private final static String LONG_PHRASES ="(?i)công (?i)ty (?i)cổ (?i)phần";
    private final static String SHORT_PHRASES ="CTCP";
    private String stockNo;
    private String symbol;
    private String shortName;


    public StockInfo() {
    }

    public String getStockNo() {
        return stockNo;
    }

    public void setStockNo(String stockNo) {
        this.stockNo = stockNo;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName.replaceAll(LONG_PHRASES,SHORT_PHRASES);
    }
}
