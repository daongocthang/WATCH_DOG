package com.example.watchdog.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.watchdog.models.Stock;

import java.util.ArrayList;
import java.util.List;

public class DbHandler extends SQLiteOpenHelper {


    private static final int VERSION = 1;
    private static final String DB_NAME = "stockDatabase";
    private static final String STOCK_TABLE = "tbl_stock";
    private static final String ID = "id";
    private static final String STOCK_NO = "stock_no";
    private static final String SYMBOL = "symbol";
    private static final String SHORT_NAME = "shortName";
    private static final String TYPE = "type";
    private static final String WARNING = "warning";

    private static final String CREATE_STOCK_TABLE = "CREATE TABLE " + STOCK_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + STOCK_NO + " TEXT, "
            + SYMBOL + " TEXT, "
            + SHORT_NAME + " TEXT, "
            + WARNING + " INTEGER, "
            + TYPE + " INTEGER)";

    private SQLiteDatabase db;

    public DbHandler(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STOCK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + STOCK_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db,oldVersion,newVersion);
    }

    public void openDb() {
        db = this.getWritableDatabase();
    }

    @SuppressLint("Range")
    public List<Stock> getAllStock() {
        List<Stock> taskList = new ArrayList<>();
        Cursor curs = null;
        db.beginTransaction();
        try {
            curs = db.query(STOCK_TABLE, null, null, null, null, null, null);
            if (curs != null) {
                if (curs.moveToFirst()) {
                    do {
                        Stock s = new Stock();
                        s.setId(curs.getInt(curs.getColumnIndex(ID)));
                        s.setStockNo(curs.getString(curs.getColumnIndex(STOCK_NO)));
                        s.setSymbol(curs.getString(curs.getColumnIndex(SYMBOL)));
                        s.setShortName(curs.getString(curs.getColumnIndex(SHORT_NAME)));
                        s.setWarningPrice(curs.getDouble(curs.getColumnIndex(WARNING)));
                        s.setType(curs.getInt(curs.getColumnIndex(TYPE)));

                        taskList.add(s);

                    } while (curs.moveToNext());
                }
            }

        } finally {
            db.endTransaction();
            assert curs != null;
            curs.close();
        }
        return taskList;
    }

    public void insertStock(Stock stock) {
        ContentValues cv = new ContentValues();
        cv.put(STOCK_NO, stock.getStockNo());
        cv.put(SYMBOL, stock.getSymbol());
        cv.put(SHORT_NAME, stock.getShortName());
        cv.put(WARNING, stock.getWarningPrice());
        cv.put(TYPE, stock.getType());

        db.insert(STOCK_TABLE, null, cv);
    }

    public void updateStock(int id,String stockNo, String symbol, String shortName, Double warning, int type) {
        ContentValues cv = new ContentValues();
        cv.put(STOCK_NO, stockNo);
        cv.put(SYMBOL, symbol);
        cv.put(SHORT_NAME, shortName);
        cv.put(WARNING, warning);
        cv.put(TYPE, type);
        db.update(STOCK_TABLE, cv, ID + "= ?", new String[]{String.valueOf(id)});
    }

    public void deleteStock(int id) {
        db.delete(STOCK_TABLE, ID + "= ?", new String[]{String.valueOf(id)});
    }
}
