package com.saerasoft.caesium;

import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;

import java.io.File;

/**
 * Created by lymphatus on 10/07/16.
 */

public class CImage {

    //Log tag
    private static final String LOG_TAG = "CIMAGE";

    private String mPath, mHeader, mMimeType;
    private long mSize, mTimestamp;

    public CImage(String path, String header, String mimeType) {

        /*
         * Get all the info we need for every image on the phone
         * We don't need to actually load the image but just reading
         * a few parameters
         * None of the parameters should be null and this has to be checked
         * before calling the constructor
         */

        mPath = path;
        mHeader = header;
        mMimeType = mimeType;
        mSize = 0;
        mTimestamp = 0;

        /*
         * Check if the image actually exists in path
         * but just pop an error, nothing more
         */

        File image = new File(mPath);
        if (image.exists()) {
            mSize = image.length();
            mTimestamp = image.lastModified();
        } else {
            Log.e(LOG_TAG, "[ERROR] File not found: " + mPath);
        }
    }

    public String getPath() {
        return mPath;
    }

    public String getHeaderName() {
        return mHeader;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public long getSize() {
        return mSize;
    }
}
