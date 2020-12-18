package com.jetcemetery.androidcalulus.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_calculus";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(OperationStats.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + OperationStats.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public void deleteOperationStats(OperationStats operation) {
        // Get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Long operationID = operation.getId();
        db.delete(OperationStats.TABLE_NAME, "id = ?", new String[] { String.valueOf(operationID) });
        db.close();
    }

    public long insertIndividualOperation(long elapsedTime, boolean success) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(OperationStats.COLUMN_TIME_ELAPSED, elapsedTime);
        values.put(OperationStats.COLUMN_SUCCESS, success);

        // insert row
        long id = db.insert(OperationStats.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public OperationStats getOperationStats(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(OperationStats.TABLE_NAME,
                new String[]{OperationStats.COLUMN_ID, OperationStats.COLUMN_TIME_ELAPSED, OperationStats.COLUMN_SUCCESS},
                OperationStats.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        OperationStats note = new OperationStats(
                cursor.getInt(cursor.getColumnIndex(OperationStats.COLUMN_ID)),
                cursor.getLong(cursor.getColumnIndex(OperationStats.COLUMN_TIME_ELAPSED)),
                cursor.getInt(cursor.getColumnIndex(OperationStats.COLUMN_SUCCESS)));

        // close the db connection
        cursor.close();

        return note;
    }

    public List<OperationStats> allOperations() {

        List<OperationStats> Operations = new LinkedList<OperationStats>();
        String query = "SELECT  * FROM " + OperationStats.TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        OperationStats operation;

        if (cursor.moveToFirst()) {
            do {
                long id = Long.parseLong(cursor.getString(0));
                long timeElapsed = Long.parseLong(cursor.getString(1));
                int tempSuccess = Integer.parseInt(cursor.getString(2));
                boolean success = false;
                if(tempSuccess != 0){
                    success = true;
                }
                operation = new OperationStats(id, timeElapsed, success);
                Operations.add(operation);
            } while (cursor.moveToNext());
        }

        return Operations;
    }

    public int updateOperation(OperationStats operation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OperationStats.COLUMN_ID, operation.getId());
        values.put(OperationStats.COLUMN_TIME_ELAPSED, operation.getTimeElapsed());
        values.put(OperationStats.COLUMN_SUCCESS, operation.getSuccess());

        int i = db.update(OperationStats.TABLE_NAME, // table
                values, // column/value
                "id = ?", // selections
                new String[] { String.valueOf(operation.getId()) });

        db.close();

        return i;
    }
}
