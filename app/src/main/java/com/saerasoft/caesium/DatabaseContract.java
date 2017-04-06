package com.saerasoft.caesium;

import android.provider.BaseColumns;

public class DatabaseContract {

    // Inner class that defines the table contents
    public static abstract class ImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "images";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_BUCKET = "header";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_HIT_TIMESTAMP = "hit_timestamp";
    }
}