package com.saerasoft.caesium;

import android.graphics.Color;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by lymphatus on 04/10/15.
 */

//TODO I feel count/size can be implemented a lot better
public class CHeader implements Serializable {

    private String name;
    private ArrayList<CImage> images;
    private long size;
    private int count;
    private int color;

    public CHeader(String headerName) {
        this.name = headerName;
        this.images = new ArrayList<>();
        this.size = 0;
        this.count = 0;
        /*
         * TODO Set white as default value
         * Set it while filling the list
         * We'd like to have a more "clever" color picker
         * By now, the color is set in the adapter creation
         */
        this.color = Color.parseColor("#FFFFFF");
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<CImage> getImages() {
        return images;
    }

    public void setImages(ArrayList<CImage> images) {
        this.images = images;
    }

    public long getSize() {
        //Initial value should be 0 in case of empty list
        int s = 0;
        //Check if there's a file list
        if (!this.images.isEmpty()) {
            //Get file size for each element in the list
            for (CImage image : this.images) {
                s += new File(image.getPath()).length();
            }
        }
        return s;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getCount() {
        //Initial value should be 0 in case of empty list
        int n = 0;
        if (!this.images.isEmpty()) {
            n = this.images.size();
        }
        return n;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isEqual(String header) {
        return this.name.equals(header);
    }

    public void addFile(CImage image) {
        this.images.add(image);
    }
}
