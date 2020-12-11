package com.jetcemetery.androidcalulus.database;

public class OperationStats {
    public static final String TABLE_NAME = "OperationStats";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIME_ELAPSED = "timeEllapsedSinceStart";
    public static final String COLUMN_SUCCESS = "success";

    private long id;
    private long timeElapsed;
    private boolean success;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " LONG PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TIME_ELAPSED + " LONG,"
                    + COLUMN_SUCCESS + " BOOLEAN"
                    + ")";

    public OperationStats(long id, long timeElapsed, boolean success) {
        this.id = id;
        this.timeElapsed = timeElapsed;
        this.success = success;
    }

    public OperationStats(long id, long timeElapsed, int success) {
        boolean sucessOp = true;
        if(success == 0){
            sucessOp = false;
        }
        this.id = id;
        this.timeElapsed = timeElapsed;
        this.success = sucessOp;
    }

    public long getId() {
        return this.id;
    }

    public void setID(long parseLong) {
        this.id = parseLong;
    }

    public long getTimeElapsed() {
        return this.timeElapsed;
    }

    public boolean getSuccess() {
        return this.success;
    }
}