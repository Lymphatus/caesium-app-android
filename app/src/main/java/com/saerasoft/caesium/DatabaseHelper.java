package com.saerasoft.caesium;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lymphatus on 18/07/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Caesium.db";

    /* Helper definitions */
    //Types
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    //Punctuation
    private static final String COMMA_SEP = ",";

    /* Helper queries */
    //Create the DB
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DatabaseContract.ImageEntry.TABLE_NAME + " (" +
                    DatabaseContract.ImageEntry._ID + INT_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_PATH + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_HEADER + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_MIME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_TIMESTAMP + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_HIT_TIMESTAMP + INT_TYPE +
                    ")";
    //Delete the DB
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " +
            DatabaseContract.ImageEntry.TABLE_NAME;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i("DatabaseHelper", "Calling constructor");
    }

    private static ContentValues populateAllWithDefaultValues(CImage cImage) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_PATH, cImage.getPath());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_HEADER, cImage.getHeaderName());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_MIME, cImage.getMimeType());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_TIMESTAMP, cImage.getTimestamp());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_HIT_TIMESTAMP, 0);

        return values;
    }

    public static long insertNewImage(SQLiteDatabase db, CImage cImage) {
        //Log.i("DatabaseHelper", "Inserting new image");
        //Create a whole new set of values
        ContentValues values = populateAllWithDefaultValues(cImage);

        //Insert the new image into the database
        //The returning value is the ID of the new row
        return db.insert(DatabaseContract.ImageEntry.TABLE_NAME,
                null,
                values);
    }

    public static int hitRow(SQLiteDatabase db, String path) {

        /* The return value should be 1 for success
         * 0 means we didn't find a match by path
         * and > 1 means we have multiple rows with the same path,
         * something we should track if happens
         */

        //Put the new timestamp on the content values
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_HIT_TIMESTAMP,
                System.currentTimeMillis());

        //We'll identify the row by the image path
        String selection = DatabaseContract.ImageEntry.COLUMN_NAME_PATH + " LIKE ?";
        //Path is the variable we need to pass to the selection
        String[] selectionArgs = {path};

        //Update the entry
        return db.update(DatabaseContract.ImageEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public static boolean rowExists(SQLiteDatabase db, String path) {
        return DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM "
                + DatabaseContract.ImageEntry.TABLE_NAME
                + " WHERE " + DatabaseContract.ImageEntry.COLUMN_NAME_PATH + "=? LIMIT 1",
                new String[] {path}) > 0;
    }

    public static boolean hasToBeCompressed(SQLiteDatabase db, CImage cImage) {
        boolean hasToBeCompressed;
        //Get path and timestamp
        String[] projection = {
                DatabaseContract.ImageEntry.COLUMN_NAME_PATH,
                DatabaseContract.ImageEntry.COLUMN_NAME_HIT_TIMESTAMP,
        };

        //Use path as where clause
        String[] where = {cImage.getPath()};

        //Execute the query to get path and timestamp
        Cursor cursor = db.query(
                DatabaseContract.ImageEntry.TABLE_NAME,                 // The table to query
                projection,                                             // The columns to return
                DatabaseContract.ImageEntry.COLUMN_NAME_PATH + " = ?",  // The columns for the WHERE clause
                where,                                                  // The values for the WHERE clause
                null,                                                   // don't group the rows
                null,                                                   // don't filter by row groups
                null                                                    // The sort order
        );

        if (cursor.moveToFirst()) {
            //It exists, check if has to be compressed or not
            //Log.i("DatabaseHelper", "Exists: TRUE");
            hasToBeCompressed = cursor.getLong(1) < cImage.getTimestamp();
            //Log.i("DatabaseHelper", "DB stamp:" + cursor.getLong(1)
            //+ " - Image stamp: " + cImage.getTimestamp());
        } else {
            //NOW exists and has to be compressed
            //Log.i("DatabaseHelper", "Exists: FALSE");
            hasToBeCompressed = insertNewImage(db, cImage) > 0;
        }
        cursor.close();
        return hasToBeCompressed;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i("DatabaseHelper", "Database does not exists, creating...");
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    public static void deleteDatabase(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }
}
