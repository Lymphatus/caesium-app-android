package com.saerasoft.caesium;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class LauncherActivity extends AppCompatActivity {

    public final static String EXTRA_HEADER_COLLECTION = "com.saerasoft.caesium.E_HEADER_COLLECTION";
    public final static int APP_PERMISSION_READ_EXTERNAL_STORAGE = 110;

    public static void scanFinished(Context context, CHeaderCollection headerCollection) {
        //Launch the Main Activity passing the HeaderCollection
        Intent main = new Intent(context, MainActivity.class);
        main.putExtra(EXTRA_HEADER_COLLECTION, headerCollection);
        context.startActivity(main);
        ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        //Ask for permission for Android 6.0+
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        APP_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else {
            startScanTask();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PermissionResult", String.valueOf(requestCode));
        switch (requestCode) {
            case 110: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startScanTask();
                }
            }
        }
    }

    public void startScanTask() {
        //Create the AsyncTask
        ImageScanAsyncTask scanTask = new ImageScanAsyncTask();
        //If it's not already running, start it
        if (!(scanTask.getStatus() == AsyncTask.Status.RUNNING)) {
            scanTask.execute(this);
        }
    }
}
