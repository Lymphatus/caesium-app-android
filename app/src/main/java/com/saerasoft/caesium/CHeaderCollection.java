package com.saerasoft.caesium;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by lymphatus on 05/10/15.
 */

public class CHeaderCollection implements Parcelable {

    public static final Parcelable.Creator<CHeaderCollection> CREATOR
            = new Parcelable.Creator<CHeaderCollection>() {
        public CHeaderCollection createFromParcel(Parcel in) {
            return new CHeaderCollection(in);
        }

        public CHeaderCollection[] newArray(int size) {
            return new CHeaderCollection[size];
        }
    };
    private ArrayList<CHeader> headers;
    private long size;
    private int count;


    public CHeaderCollection() {
        //Empty constructor
        this.headers = new ArrayList<>();
        this.size = 0;
        this.count = 0;
    }

    private CHeaderCollection(Parcel in) {
        this();
        this.headers = in.readArrayList(CHeaderCollection.class.getClassLoader());
        this.size = in.readLong();
        this.count = in.readInt();
    }

    public int getCount() {
        //Initial value should be 0 in case of empty list
        int n = 0;
        if (!this.headers.isEmpty()) {
            for (CHeader h :
                    this.headers) {
                n += h.getCount();
            }
        }
        return n;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<CHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(ArrayList<CHeader> headers) {
        this.headers = headers;
    }

    public long getSize() {
        //Initial value should be 0 in case of empty list
        int s = 0;
        //Check if there's a file list
        if (!this.headers.isEmpty()) {
            //Get file size for each element in the list
            for (CHeader h : this.headers) {
                s += h.getSize();
            }
        }
        return s;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getSelectedItemsImageCount() {
        return getSelectedHeaders().getCount();
    }

    public long getSelectedItemsImageSize() {
        return getSelectedHeaders().getSize();
    }

    public CHeaderCollection getSelectedHeaders() {
        CHeaderCollection selectedCollection = new CHeaderCollection();
        for (CHeader h : this.headers) {
            //Add to count only if selected
            if (h.isSelected()) {
                selectedCollection.add(h);
            }
        }
        return selectedCollection;
    }

    /* Parcelable methods */

    public int indexOfHeader(String headerName) {
        int n = 0;
        //Go through the list to find the header with that name
        for (CHeader h : this.headers) {
            //If the name matches, return the index
            if (h.getName().equals(headerName)) {
                return n;
            }
            n++;
        }
        //Not found, -1 is an invalid index, so it's safe to use
        return -1;
    }

    public void add(CHeader header) {
        this.headers.add(header);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeList(this.headers);
        out.writeLong(this.size);
        out.writeInt(this.count);
    }
}
