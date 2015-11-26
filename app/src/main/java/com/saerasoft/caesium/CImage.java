package com.saerasoft.caesium;

import android.util.Log;

import java.io.File;
import java.io.Serializable;

/**
 * Created by lymphatus on 30/09/15.
 */
public class CImage implements Serializable {

    private String path;
    private String header;
    private int width;
    private int height;
    private long size;
    private String mimeType;
    private long timestamp;

    public CImage() {
        //Empty constructor
    }

    public CImage(String path, String header, String mimeType) {

        /*
         * Using this constructor you set up all the info about the file.
         * By passing readImageHeaders = true you also read the specific
         * image attributes, but it takes time.
         * Should be called with readImageHeaders = false for just checking
         * if the database already has this entry.
         */

        //Set the passed parameters
        this.path = path;
        this.header = header;
        this.mimeType = mimeType;

        //Try to get the file info
        try {
            File file = new File(path);

            //Check if the path is actually a file
            if (file.exists()) {
                //Get file size and timestamp
                this.size = file.length();
                this.timestamp = file.lastModified();
            } else {
                Log.e("CaesiumImage", "File not found: " + path);
            }
        } catch (NullPointerException e) {
            //Path is null, don't set anything
            Log.e("CaesiumImage", e.getLocalizedMessage());
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHeader() {
        return header;
    }

    public void setHeaderName(String header) {
        this.header = header;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
