package com.saerasoft.caesium;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by lymphatus on 30/09/15.
 */
public class ImageScanAsyncTask extends AsyncTask<Activity, Integer, CHeaderCollection> {

    private Context mContext;

    //Get a list of CaesiumImages from the phone using a provider
    protected CHeaderCollection doInBackground(Activity... activities) {
        //Set the context to a global variable
        mContext = activities[0];
        //TODO error check for db?
        //Get the instance of the database
        SQLiteDatabase db = new DatabaseHelper(mContext).getWritableDatabase();
        //Header collection we will return
        CHeaderCollection headerCollection = new CHeaderCollection();

        //Gets the URI for the "primary" storage
        //TODO Check if the SD Card is included (should be)
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        //Set up the projection for the filepath, header and mime type
        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE
        };

        //Execute the query to get the cursor
        Cursor cursor = mContext.getContentResolver()
                .query(uri,
                        projection,
                        null,
                        null,
                        null);

        //Try to get indexes
        if (cursor != null) {
            int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            int headerIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);

            //Do stuff with the cursor you got
            while (cursor.moveToNext()) {
                //Get the absolute image path, header and mime
                String path = cursor.getString(dataIndex);
                String header = cursor.getString(headerIndex);
                String mimeType = cursor.getString(mimeIndex);

                /*
                 * We don't need to check the path here
                 * because the check is performed on the CImage constructor
                 */
                //Create the CImage we will use for the list and for the headers
                CImage image = new CImage(path, header, mimeType);

                //Skip empty files
                if (image.getSize() > 0 && DatabaseHelper.getDatabaseTypeOfImage(db, image) == DatabaseHelper.DatabaseType.NEW) {
                    //We need to know if an header with the same title exists and get the index
                    int headerPos = headerCollection.indexOfHeader(header);

                    //-1 means the header is new, otherwise is the index
                    if (headerPos == -1) {
                        //Create the new header
                        CHeader cHeader = new CHeader(header);
                        //Add the new file to the header internal list
                        cHeader.addFile(image);
                        //Add the header to the collection
                        headerCollection.add(cHeader);
                    } else {
                        //The header exists at defined index; Just add the new file to it
                        headerCollection.getHeaders().get(headerPos).addFile(image);
                    }
                }
            }

            //Close the cursor
            cursor.close();
        }

        for (CHeader h : headerCollection.getHeaders()) {
            if (h.getCount() == 0) {
                headerCollection.getHeaders().remove(headerCollection.getHeaders().indexOf(h));
            }
        }

        //Sort the collection by given parameter
        //TODO Make this a preference. Now is by image count
        Collections.sort(headerCollection.getHeaders(), new Comparator<CHeader>() {
            @Override
            public int compare(CHeader lhs, CHeader rhs) {
                return rhs.getCount() - lhs.getCount();
            }
        });

        return headerCollection;
    }

    protected void onPostExecute(CHeaderCollection cHeaders) {
        Log.d("ImageScan", "Scan completed.");
        LauncherActivity.scanFinished(mContext, cHeaders);
    }
}
