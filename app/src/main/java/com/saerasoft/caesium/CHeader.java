package com.saerasoft.caesium;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by lymphatus on 12/07/16.
 */
public class CHeader implements Parcelable {

    public enum HeaderSortOrder {
        SIZE,
        FILENAME,
        LAST_MODIFIED
    }
    
    /*
     * I don't really like hardcoding all the colors
     * but I'd also want to chose the header color at creation rather
     * than when adding it to the collection.
     * Parsing resource requires Context, which I don't wanna
     * pass as parameter here
     */

    private static final String[] pastelColors = {
            "#F06292",
            "#BA68C8",
            "#9575CD",
            "#7986CB",
            "#64B5F6",
            "#4DB6AC",
            "#4DD0E1",
            "#FFD54F",
            "#FF8A65",
            "#90A4AE"
    };
    
    private String mTitle;
    private long mTotalListElementsSize = 0;
    // TODO: 19/07/16 Should we use a different collection to speed things up?
    private ArrayList<CImage> mList;
    private boolean selected = true;
    private String mInitialLetter;
    private int mColor;

    public CHeader(String title) {
        mTitle = title;
        mList = new ArrayList<>();
    }

    public CHeader(String title, ArrayList<CImage> list) {
        mTitle = title;
        mList = new ArrayList<>(list);

        // TODO: 19/07/16 Can we make it faster?
        for (CImage ci : mList) {
            mTotalListElementsSize += ci.getSize();
        }
    }

    public CHeader(String title, ArrayList<CImage> list, HeaderSortOrder order) {
        mTitle = title;
        mList = new ArrayList<>(list);

        // TODO: 19/07/16 We should merge sorting and size computation to speed it up
        for (CImage ci : mList) {
            mTotalListElementsSize += ci.getSize();
        }

        this.sortList(order);
    }

    protected CHeader(Parcel in) {
        mTitle = in.readString();
        mTotalListElementsSize = in.readLong();
        selected = in.readByte() != 0;
    }

    public void setHeaderColorAndInitial() {
        mInitialLetter = parseInitialLetter();
        mColor = getRandomPastelColor();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public ArrayList<CImage> getList() {
        return mList;
    }

    public void setList(ArrayList<CImage> list) {
        mList = list;
    }

    public int length() {
        return mList.size();
    }

    public long size() {
        return mTotalListElementsSize;
    }

    public void add(CImage image) {
        mList.add(image);
        mTotalListElementsSize += image.getSize();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getInitialLetter() {
        return mInitialLetter;
    }

    public int getColor() {
        return mColor;
    }

    public void sortList(final HeaderSortOrder order) {
        Collections.sort(mList, new Comparator<CImage>() {
            @Override
            public int compare(CImage lhs, CImage rhs) {
                // TODO: 19/07/16 Sort by ascending and descending order option
                switch (order) {
                    case FILENAME:
                        return lhs.getPath().compareTo(rhs.getPath());
                    case SIZE:
                        // TODO: 19/07/16 CRITICAL This may overflow!!
                        return (int) (rhs.getSize() - lhs.getSize());
                    case LAST_MODIFIED:
                        // TODO: 19/07/16 CRITICAL This may overflow!!
                        return (int) (rhs.getTimestamp() - lhs.getTimestamp());
                }
                //We should never reach this
                return 0;
            }
        });
    }

    public String parseInitialLetter() {
        Pattern p = Pattern.compile("\\p{Alpha}");
        Matcher m = p.matcher(mTitle);
        //If there's an alpha char, use it, otherwise use just the first char
        if (m.find()) {
            return String.valueOf(mTitle.charAt(m.start()));
        } else {
            return String.valueOf(mTitle.charAt(0));
        }
    }

    public int getRandomPastelColor() {
        return Color.parseColor(
                pastelColors[new Random().nextInt(pastelColors.length)]);
    }

    /* Parcelable methods */

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeLong(mTotalListElementsSize);
        dest.writeByte((byte) (selected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CHeader> CREATOR = new Creator<CHeader>() {
        @Override
        public CHeader createFromParcel(Parcel in) {
            return new CHeader(in);
        }

        @Override
        public CHeader[] newArray(int size) {
            return new CHeader[size];
        }
    };
}
