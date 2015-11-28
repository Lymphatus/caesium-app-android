package com.saerasoft.caesium;

import android.provider.BaseColumns;
/**
 * Created by lymphatus on 02/10/15.
 */
public class DatabaseContract {

    public DatabaseContract() {

    }

    // Inner class that defines the table contents
    public static abstract class ImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "images";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_HEADER = "header";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_WIDTH = "width";
        public static final String COLUMN_NAME_HEIGHT = "height";
        public static final String COLUMN_NAME_MIME = "mime_type";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_NEW = "new";
        public static final String COLUMN_NAME_HIT_TIMESTAMP = "hit_timestamp";
    }
}