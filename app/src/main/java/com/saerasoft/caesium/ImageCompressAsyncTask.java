package com.saerasoft.caesium;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Objects;

/**
 * Created by lymphatus on 08/10/15.
 */
public class ImageCompressAsyncTask extends AsyncTask<Object, Integer, Long> {

    private Context mContext;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder nBuilder;
    private Intent killerIntent;
    public static int COMPRESS_NOTIFICATION_ID = 0;

    static {
        System.loadLibrary("caesium");
        System.loadLibrary("jpeg");
    }

    public ImageCompressAsyncTask(Context context) {
        this.mContext = context;
    }

    protected Long doInBackground(Object... objs) {
        //Parse passed objects
        CHeaderCollection mCollection = (CHeaderCollection) objs[0];
        SQLiteDatabase db = (SQLiteDatabase) objs[1];
        int max_count = (int) objs[2];

        //Initialize a global counter
        int n = 0;

        //Initialize the compressed size counter
        long size = 0;

        //Initialize the in files size
        long inFilesSize = 0;

        //Utility variable to keep track of headers index
        int currentHeaderIndex = 0;

        //Get the starting time; we will use as performance meter and for hitting images
        Long startTimestamp = System.currentTimeMillis();

        //Get quality and exif from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int quality = Integer.valueOf(prefs.getString(SettingsActivity.KEY_COMPRESSION_LEVEL, "65"));
        int exif = (prefs.getBoolean(SettingsActivity.KEY_COMPRESSION_EXIF, true)) ? 1 : 0;

        //Scan each header
        for (int i = 0; i < mCollection.getHeaders().size(); i++) {
            CHeader header = mCollection.getHeaders().get(i);
            if (header.isSelected()) {
                int header_count = header.getCount();
                //And each image
                for (int j = 0; j < header_count; j++) {
                    CImage image = header.getImages().get(j);
                    long inSize = image.getSize();
                    //How much size we've eaten until now
                    //TODO BUG This does not give you 0 at the end
                    inFilesSize += inSize;
                    //Check for possible memory leaks
                    //TODO Not necessary anymore
                    if (fitsInMemory(image.getPath())) {
                        //Keep trace of the input file size
                        //Start the actual compression process
                        Log.d("CompressTask", "PROCESSING: " + image.getPath());
                        Log.d("CompressTask", "In size: " + image.getSize());
                        //If it's a JPEG, go for the turbo lib
                        try {
                            switch (image.getMimeType()) {
                                case "image/jpeg":
                                    //TODO We get crashes using the lib with standard compression
                                    //Use the Android lib instead for now
                                    if (quality == 0) {
                                        CompressRoutine(image.getPath(), exif, quality);
                                    } else {
                                        Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
                                        FileOutputStream fos = new FileOutputStream(image.getPath(), false);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                                        bitmap.recycle();
                                    }
                                    //CompressRoutine(image.getPath(), exif, quality);
                                    break;
                                case "image/png":
                                    //PNG section
                                    Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
                                    FileOutputStream fos = new FileOutputStream(image.getPath(), false);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                    bitmap.recycle();
                                    break;
                                default:
                                    //TODO Webp?
                                    Log.e("CompressTask", "Cannot compress this kind of image.");
                                    break;
                            }
                        } catch (FileNotFoundException e) {
                            Log.e("CompressTask", "File not found.");
                        } catch (NullPointerException e) {
                            Log.e("CompressTask", "Null pointer");
                        } catch (OutOfMemoryError e) {
                            Log.e("CompressTask", "OutOfMemory");
                        }

                        //Get the out file for its stats
                        File outFile = new File(image.getPath());
                        if (outFile.length() != 0) {
                            size += outFile.length();
                        } else {
                            size += inSize;
                        }

                        Log.d("CompressTask", "Out size: " + new File(image.getPath()).length());

                        /*
                        *
                        * TODO Insert the image into the database, because we are compressing it
                        * At this stage, we already have a filtered list, but we need to check if we
                        * compressed an edited image, so, instead of adding, we need to update the entry
                        *
                        */
                        //DatabaseHelper.insertImageIntoDatabase(db, image);
                        //DatabaseHelper.hitImageRow(db, image.getPath(), startTimestamp);
                        DatabaseHelper.databaseRoutine(db, image, true);

                        publishProgress(n++, max_count, i);

                        // Escape early if cancel() is called
                        if (isCancelled()) {
                            return size;
                        }
                    }
                }
            }
        }
        return size;
    }

    @Override
    protected void onPreExecute() {
        //Build a killer service to destroy the notification if the app is swiped out
        killerIntent = new Intent(mContext, NotificationKillerService.class);
        mContext.startService(killerIntent);
        //Click intent
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        //Build up the notification
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nBuilder = new NotificationCompat.Builder(mContext);
        nBuilder.setContentTitle(mContext.getString(R.string.notification_compress_title))
                .setContentText(mContext.getString(R.string.notification_compress_description))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        //Change the button icon
        MainActivityFragment.onPreCompress();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        // Sets the progress indicator to a max value, the
        // current completion percentage, and "determinate"
        // state
        nBuilder.setProgress(progress[1], progress[0], false);
        // Displays the progress bar for the first time.
        notificationManager.notify(0, nBuilder.build());
        MainActivityFragment.onCompressProgress(
                progress[0], //Current progress
                progress[1], //Max value
                progress[2]); //If the header is still on progress
    }

    @Override
    protected void onPostExecute(Long result) {
        //Kill the service
        mContext.stopService(killerIntent);
        // When the loop is finished, updates the notification
        nBuilder.setContentText(mContext.getString(R.string.notification_compress_description_finished))
                // Removes the progress bar
                .setProgress(0, 0, false);
        notificationManager.notify(COMPRESS_NOTIFICATION_ID, nBuilder.build());

        //Update UI after compression
        MainActivityFragment.onPostCompress(result);

        //Kill the notification
        notificationManager.cancel(COMPRESS_NOTIFICATION_ID);
    }

    @Override
    protected void onCancelled(Long size) {
        //Kill the notification
        notificationManager.cancel(COMPRESS_NOTIFICATION_ID);
        MainActivityFragment.onPostCompress(size);
    }

    //JNI Methods

    public boolean fitsInMemory(String path) {
        //Setup the options for image reading
        BitmapFactory.Options options = new BitmapFactory.Options();
        //Do not decode the entire image, just what we need
        options.inJustDecodeBounds = true;

        //Try do decode the file
        BitmapFactory.decodeFile(path, options);
        //Set all the remaining attributes
        //TODO Build a method to know if the image is too big
        int width = options.outWidth;
        int height = options.outHeight;
        return true;
    }

    public native void CompressRoutine(String in, int exif, int quality);
}