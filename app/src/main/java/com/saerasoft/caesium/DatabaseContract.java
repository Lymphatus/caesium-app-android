package com.saerasoft.caesium;

import android.provider.BaseColumns;

/*
 * Created by lymphatus on 18/07/16.
 */
public class DatabaseContract {
    public DatabaseContract() {

    }

    // Inner class that defines the table contents
    public static abstract class ImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "images";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_HEADER = "header_title";
        public static final String COLUMN_NAME_MIME = "mime_type";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_HIT_TIMESTAMP = "hit_timestamp";
    }
}
