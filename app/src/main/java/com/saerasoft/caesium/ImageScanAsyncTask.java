package com.saerasoft.caesium;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImageScanAsyncTask extends AsyncTask<Activity, Integer, List<CBucket>> {

    private Context mContext;

    private int imagesCount = 0;
    private long bucketsItemsSize = 0;
    private List<CBucket> bucketsList;

    //Get a list of CaesiumImages from the phone using a provider
    protected List<CBucket> doInBackground(Activity... activities) {
        //Set the context to a global variable
        mContext = activities[0];
        //Get the instance of the database
        SQLiteDatabase db = new DatabaseHelper(mContext).getWritableDatabase();
        //Header collection we will return
        ContentResolver cr = mContext.getContentResolver();

        String[] columns = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATE_MODIFIED,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};

        Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, null);
        CBuckets cBuckets = new CBuckets();
        int count = 0;
        db.beginTransaction();
        if (cur != null) {
            cur.moveToFirst();
            while (cur.moveToNext()) {
                Integer imageID = cur.getInt(0);
                String imageTitle = cur.getString(1);
                String imagePath = cur.getString(2);
                Long imageSize = cur.getLong(3);
                Long imageTimestamp = cur.getLong(4);
                Integer bucketID = cur.getInt(5);
                String bucketName = cur.getString(6);

                CImage cImage = new CImage(imageID,
                        imageTitle,
                        imagePath,
                        imageSize,
                        imageTimestamp,
                        bucketName);

                DatabaseHelper.DatabaseType status = DatabaseHelper.getDatabaseTypeOfImage(db, cImage);
                if (imageSize > 0 && status != DatabaseHelper.DatabaseType.EQUAL) {
                    if (status == DatabaseHelper.DatabaseType.NEW) {
                        DatabaseHelper.insertImage(db, cImage);
                    }

                    //Actual insert in the list we want to display
                    if (cBuckets.containsKey(bucketID)) {
                        CBucket cBucket = cBuckets.get(bucketID);
                        cBucket.getImagesList().add(cImage);
                    } else {
                        CBucket cBucket = new CBucket(cur.getInt(5), cur.getString(6));
                        cBucket.getImagesList().add(cImage);
                        cBuckets.put(bucketID, cBucket);
                    }
                    count++;
                }
            }
            cur.close();
            db.endTransaction();

            this.bucketsList = cBuckets.sortList();
            this.bucketsItemsSize = cBuckets.getTotalItemsSize();
            this.imagesCount = count;
        }

        return this.bucketsList;
    }

    protected void onPostExecute(List<CBucket> cBuckets) {
        Log.d("ImageScan", "Scan completed.");
        ((MainActivity) mContext).onPostScan(this.imagesCount, this.bucketsItemsSize, cBuckets);
    }
}
