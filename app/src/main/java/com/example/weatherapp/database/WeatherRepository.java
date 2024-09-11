package com.example.weatherapp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WeatherRepository {
    private final WeatherDatabaseHelper dbHelper;

    public WeatherRepository(Context context) {
        dbHelper = new WeatherDatabaseHelper(context);
    }

    public void saveWeatherData(String city, String data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WeatherDatabaseHelper.COLUMN_CITY, city);
        values.put(WeatherDatabaseHelper.COLUMN_DATA, data);
        values.put(WeatherDatabaseHelper.COLUMN_LAST_UPDATED, System.currentTimeMillis());
        db.insertWithOnConflict(WeatherDatabaseHelper.TABLE_WEATHER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getWeatherData(String city) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {WeatherDatabaseHelper.COLUMN_DATA};
        String selection = WeatherDatabaseHelper.COLUMN_CITY + " = ?";
        String[] selectionArgs = {city};

        Cursor cursor = db.query(WeatherDatabaseHelper.TABLE_WEATHER, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_DATA));
            cursor.close();
            return data;
        } else {
            return null;
        }
    }

    public long getLastUpdated(String city) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {WeatherDatabaseHelper.COLUMN_LAST_UPDATED};
        String selection = WeatherDatabaseHelper.COLUMN_CITY + " = ?";
        String[] selectionArgs = {city};

        Cursor cursor = db.query(WeatherDatabaseHelper.TABLE_WEATHER, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") long lastUpdated = cursor.getLong(cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_LAST_UPDATED));
            cursor.close();
            return lastUpdated;
        } else {
            return -1;
        }
    }
}
