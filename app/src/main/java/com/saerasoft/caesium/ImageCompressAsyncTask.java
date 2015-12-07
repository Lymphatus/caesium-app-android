package com.saerasoft.caesium;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by lymphatus on 08/10/15.
 */
public class ImageCompressAsyncTask extends AsyncTask<Object, Object, Long> {

    static {
        System.loadLibrary("caesium");
        System.loadLibrary("jpeg");
    }

    Context mContext;

    protected Long doInBackground(Object... objs) {

        //Parse passed objects
        mContext = (Context) objs[0];
        CHeaderCollection mCollection = (CHeaderCollection) objs[1];
        SQLiteDatabase db = (SQLiteDatabase) objs[2];
        int max_count = (int) objs[3];

        //Initialize a global counter
        int n = 0;

        //Initialize the compressed size counter
        long size = 0;

        //Initialize the in files size
        long inFilesSize = 0;

        //Get the starting time; we will use as performance meter and for hitting images
        Long startTimestamp = System.currentTimeMillis();

        //Get quality and exif from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int quality = Integer.valueOf(prefs.getString(SettingsActivity.KEY_COMPRESSION_LEVEL, "65"));
        int exif = (prefs.getBoolean(SettingsActivity.KEY_COMPRESSION_EXIF, true)) ? 1 : 0;

        //Scan each header
        for (CHeader header : mCollection.getHeaders()) {
            if (header.isSelected()) {
                //And each image
                for (CImage image : header.getImages()) {
                    long inSize = image.getSize();
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
                                        CompressRoutine(image.getPath(), exif, 0);
                                    } else {
                                        Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
                                        FileOutputStream fos = new FileOutputStream(image.getPath(), false);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                                        bitmap.recycle();
                                        bitmap = null;
                                    }
                                    break;
                                case "image/png":
                                    //PNG section
                                    Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
                                    FileOutputStream fos = new FileOutputStream(image.getPath(), false);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                    bitmap.recycle();
                                    bitmap = null;
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

                        //How much size we've eaten until now
                        //TODO BUG This does not give you 0 at the end
                        inFilesSize += inSize;

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

                        publishProgress(n++, max_count, inFilesSize);
                    }
                }
            }
        }
        return size;
    }

    protected void onProgressUpdate(Object... progress) {
        MainActivityFragment.onCompressProgress(mContext,
                (int) progress[0],
                (int) progress[1],
                (long) progress[2]);
    }

    protected void onPostExecute(Long result) {
        Log.d("CompressTask", "COMPRESSION FINISHED");
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