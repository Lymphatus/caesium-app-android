package com.saerasoft.caesium;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

/*
 * Created by lymphatus on 10/07/16.
 */
public class ImageScannerAsyncTask extends AsyncTask<Context, Long, ArrayList<CHeader>> {

    private static final String TAG = "ImageScannerAsyncTask";

    private long startTime;

    @Override
    protected void onPreExecute() {
        startTime = SystemClock.elapsedRealtime();
    }

    @Override
    protected ArrayList<CHeader> doInBackground(Context... params) {

        Context context = params[0];

        // TODO: 19/07/16 Check if the SD Card is included (should be)
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE
        };

        Cursor cursor = context.getContentResolver()
                .query(uri,
                        projection,
                        null,
                        null,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " DESC");

        if (cursor != null) {
            // TODO: 19/07/16  We can also retrieve size here if we want. It can speed up things maybe
            int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            int headerIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);

            ArrayList<CHeader> cHeaders = new ArrayList<>();
            SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();

            /*
             * This keeps the current iteration step
             * Images are ordered by header name, so we can avoid to
             * scan the entire list to know if an header already exists
             */

            int currentHeaderIndex = -1;

            long count = 0;
            long totalImagesCount = (long) cursor.getCount();
            long totalSize = 0;
            while (cursor.moveToNext()) {
                //Get the absolute image path, header and mime
                String path = cursor.getString(dataIndex);
                String header = cursor.getString(headerIndex);
                String mimeType = cursor.getString(mimeIndex);

                CImage cImage = new CImage(path, header, mimeType);
                CHeader cHeader = new CHeader(header);

                Log.i("DatabaseHelper", cImage.getPath());

                //if (true) {
                if (DatabaseHelper.hasToBeCompressed(db, cImage)) {
                    //This adds to the collection!!
                    Log.i(TAG, "Has to be compressed: TRUE");
                    try {
                        String currentTitle = cHeaders.get(currentHeaderIndex).getTitle();
                        if (currentTitle.compareTo(cHeader.getTitle()) != 0) {
                            //Different header, add the new one
                            currentHeaderIndex++;
                            cHeader.setHeaderColorAndInitial();
                            cHeaders.add(cHeader);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        //First
                        currentHeaderIndex++;
                        cHeader.setHeaderColorAndInitial();
                        cHeaders.add(cHeader);
                    }
                    cHeaders.get(currentHeaderIndex).add(cImage);
                    totalSize += cImage.getSize();
                    publishProgress(++count, totalImagesCount, totalSize, (long) currentHeaderIndex + 1);
                } else {
                    // TODO: 19/07/16 Remove this 
                    Log.i(TAG, "Has to be compressed: FALSE");
                }
            }
            cursor.close();
            return cHeaders;
        }
        return null;
    }

    protected void onProgressUpdate(Long... progress) {
        MainActivity.onScanProgress(progress[0], progress[1], progress[2], progress[3]);
    }

    protected void onPostExecute(ArrayList<CHeader> cHeaders) {
        long endTime = SystemClock.elapsedRealtime();
        Log.d("Main", "Scan took: " + (endTime - startTime) + " ms");
        MainActivity.onScanFinished(cHeaders);
    }
}
