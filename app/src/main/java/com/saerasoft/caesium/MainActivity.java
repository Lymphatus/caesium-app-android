package com.saerasoft.caesium;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Adapter;

import com.github.lzyzsd.circleprogress.ArcProgress;

//TODO If back button is pressed, stay on this activity

public class MainActivity extends AppCompatActivity {

    public final static int APP_PERMISSION_READ_EXTERNAL_STORAGE = 110;
    static final String FIRST_RUN = "firstRun";
    static boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (first) {
            Log.d("MainActivity", "savedInstance is null");
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
            first = false;
        } else {
            MainActivityFragment newFragment = new MainActivityFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.launchFragment, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Hide the options we don't want to be enabled during compression
        menu.findItem(R.id.action_settings).setEnabled(!MainActivityFragment.isCompressing);
        menu.findItem(R.id.action_select_all).setEnabled(!MainActivityFragment.isCompressing);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Start the preference activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_select_all) {
            //Select/deselect all the items in list
            CHeaderAdapter adapter = (CHeaderAdapter) ((RecyclerView) (this.getWindow().getDecorView().findViewById(
                    R.id.mainHeadersListView))).getAdapter();

            //Controller variable
            boolean checked = item.getTitle() == getString(R.string.action_select_all);

            //Loop through item and hit the checkbox
            for (int i = 0; i < adapter.getItemCount(); i++) {
                adapter.checkItemAt(i, checked);
                //Notify the RecyclerView something has changed
                adapter.notifyItemChanged(i);
            }
            //Change the setting title to (de)select
            if (checked) {
                item.setTitle(getString(R.string.action_deselect_all));
            } else {
                item.setTitle(getString(R.string.action_select_all));
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Disable back button
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("MainActivity", "savedInstance CALLED");
        // Save the user's current game state
        savedInstanceState.putBoolean(FIRST_RUN, false);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
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
