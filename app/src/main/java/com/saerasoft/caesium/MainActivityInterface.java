package com.saerasoft.caesium;

/*
 * Created by lymphatus on 02/04/17.
 */


import java.util.ArrayList;
import java.util.List;

public interface MainActivityInterface {
    void updateValues();
    void onPostScan(int imagesCount, long bucketsItemsSize, List<CBucket> bucketsList);
}
