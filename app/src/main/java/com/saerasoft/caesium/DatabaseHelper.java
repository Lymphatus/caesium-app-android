package com.saerasoft.caesium;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lymphatus on 02/10/15.
 */

//TODO Merge the two update methods in something smaller, too much code in common

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
                    DatabaseContract.ImageEntry.COLUMN_NAME_SIZE + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_WIDTH + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_HEIGHT + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_MIME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_TIMESTAMP + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_NEW + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ImageEntry.COLUMN_NAME_HIT_TIMESTAMP + INT_TYPE +
                    ")";
    //Delete the DB
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " +
            DatabaseContract.ImageEntry.TABLE_NAME;

    public DatabaseHelper(Context context) {
        //Constructor
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static ContentValues populateAllEntries(CImage cImage) {
        //Create a whole set of values
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_PATH, cImage.getPath());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_HEADER, cImage.getHeader());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_SIZE, cImage.getSize());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_WIDTH, cImage.getWidth());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_HEIGHT, cImage.getHeight());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_MIME, cImage.getMimeType());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_TIMESTAMP, cImage.getTimestamp());
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_NEW, 1);
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_HIT_TIMESTAMP,
                System.currentTimeMillis());

        return values;
    }

    public static long insertImageIntoDatabase(SQLiteDatabase db, CImage cImage) {
        //Create a whole new set of values
        ContentValues values = populateAllEntries(cImage);

        //Insert the new image into the database
        //The returning value is the ID of the new row
        return db.insert(DatabaseContract.ImageEntry.TABLE_NAME,
                null,
                values);
    }

    public static int hitImageRow(SQLiteDatabase db, String path, long timestamp) {

        /* The return value should be 1 for success
         * 0 means we didn't find a match by path
         * and > 1 means we have multiple rows with the same path,
         * something we should track if happens
         */

        //Put the new timestamp on the content values
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_HIT_TIMESTAMP,
                timestamp);
        //Tell the DB the image is not new (means compressed)
        values.put(DatabaseContract.ImageEntry.COLUMN_NAME_NEW,
                0);

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

    public static int updateImageInfo(SQLiteDatabase db, CImage cImage) {

        /* The return value should be 1 for success
         * 0 means we didn't find a match by path
         * and > 1 means we have multiple rows with the same path,
         * something we should track if happens
         */

        //Create a whole new set of values
        ContentValues values = populateAllEntries(cImage);

        //We'll identify the row by the image path
        String selection = DatabaseContract.ImageEntry.COLUMN_NAME_PATH + " LIKE ?";
        //Path is the variable we need to pass to the selection
        String[] selectionArgs = {cImage.getPath()};

        //Update the entry
        return db.update(DatabaseContract.ImageEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

    }

    /* -- Start of the routine methods -- */

    public static int deleteUnhitImages(SQLiteDatabase db) {
        //Returns the number of deleted items
        //TODO Decide a proper timespan for this
        //Delete images that were not hit for 1 day
        String selection = DatabaseContract.ImageEntry.COLUMN_NAME_HIT_TIMESTAMP +
                "  <= date('now','-1 day')";

        return db.delete(DatabaseContract.ImageEntry.TABLE_NAME,
                selection,
                null);
    }

    public static DatabaseType getDatabaseTypeOfImage(SQLiteDatabase db, CImage cImage) {
        //Get path and timestamp
        String[] projection = {
                DatabaseContract.ImageEntry.COLUMN_NAME_PATH,
                DatabaseContract.ImageEntry.COLUMN_NAME_TIMESTAMP,
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

        //Check if the cursor is not empty, meaning we have and hit
        if (cursor.moveToFirst()) {
            //The images already exists, check if it has been modified
            if (cImage.getTimestamp() > cursor.getLong(1)) {
                //Cursor has done its job, we don't need to evaluate more
                //The image timestamp is higher, image MODIFIED
                Log.d("Database", "MODIFIED: " + cursor.getString(0));
                cursor.close();
                return DatabaseType.MODIFIED;
            } else {
                //Same (or less, but should not happen) timestamp, EQUAL image
                Log.d("Database", "EQUAL: " + cursor.getString(0));
                cursor.close();
                return DatabaseType.EQUAL;
            }
        } else {
            //The image does not exist in the database, it's NEW
            Log.d("Database", "NEW: " + cImage.getPath());
            cursor.close();
            return DatabaseType.NEW;
        }
    }

    public void onCreate(SQLiteDatabase db) {
        //Newly created database, exec the above query
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO Check the method and onDowngrade too
        //Delete and create again the database upon upgrading
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Since we are deleting and recreating the database, we can use
        //the onUpgrade method
        onUpgrade(db, oldVersion, newVersion);
    }

    public static void deleteDatabase(SQLiteDatabase db) {
        //Delete all entries
        db.execSQL(SQL_DELETE_ENTRIES);
    }

    /* -- End of the routine methods -- */


    /* -- Helper methods -- */

    public enum DatabaseType {
        NEW,
        MODIFIED,
        EQUAL
    }

    public static void databaseRoutine(SQLiteDatabase db, CImage image, Boolean compression) {
        //This methods updates the database
        //Cleaning is done while compressing

        //Update fill and/or update each entry of the database according to the image

        switch (getDatabaseTypeOfImage(db, image)) {
            case NEW:
                //Image is completely fresh, use the insert
                Log.d("ImageScan", "NEW: " + image.getPath());
                if (compression) {
                    DatabaseHelper.insertImageIntoDatabase(db, image);
                }
                break;
            case MODIFIED:
                //The file exists but was modified since last time
                DatabaseHelper.updateImageInfo(db, image);
                break;
            case EQUAL:
                //Same image, do nothing
                Log.d("ImageScan", "EQUAL: " + image.getPath());
                break;
            default:
                break;
        }
    }

    /* -- End of helper methods -- */

}
