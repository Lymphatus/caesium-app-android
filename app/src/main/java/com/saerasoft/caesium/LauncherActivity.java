package com.saerasoft.caesium;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    public final static String EXTRA_HEADER_COLLECTION = "com.saerasoft.caesium.E_HEADER_COLLECTION";

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

        //Create the AsyncTask
        ImageScanAsyncTask scanTask = new ImageScanAsyncTask();

        //If it's not already running, start it
        if (!(scanTask.getStatus() == AsyncTask.Status.RUNNING)) {
            scanTask.execute(this);
        }
    }
}
