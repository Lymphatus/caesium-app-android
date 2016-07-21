package com.saerasoft.caesium;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 133;
    private static Context mContext;
    //We need to keep track of these because it's not as fast as others to compute
    public static long totalSelectedFileSize = 0;
    public static int totalSelectedItems = 0;
    public static int totalItems = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "------------- START -------------");
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_main);

        RecyclerView listRecyclerView  = (RecyclerView) findViewById(R.id.listRecyclerView);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listRecyclerView.setAdapter(new CHeaderAdapter());

        //Asking for permissions here
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            new ImageScannerAsyncTask().execute(this.getApplicationContext());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Start the preference activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_select_all) {
            CHeaderAdapter adapter = (CHeaderAdapter) ((RecyclerView) ((Activity) mContext)
                    .getWindow().getDecorView().findViewById(R.id.listRecyclerView))
                    .getAdapter();

            adapter.setAllSelected(totalSelectedItems != adapter.getItemCount());
        } else if (id == R.id.action_order) {
            DialogFragment dialogFragment = new ListOrderDialogFragment();
            dialogFragment.show(getFragmentManager(), "order");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "PERMISSION GRANTED");
                    new ImageScannerAsyncTask().execute(this.getApplicationContext());
                } else {
                    finish();
                }
            }
        }
    }

    public static void onScanFinished(ArrayList<CHeader> cHeaders) {
        // TODO: 19/07/16 Make it prettier
        Log.d("MainActivity", "SCAN FINISHED!!");
        View mainView = ((Activity) mContext).getWindow().getDecorView();

        CHeaderAdapter adapter = (CHeaderAdapter) ((RecyclerView) mainView.findViewById(R.id.listRecyclerView))
                .getAdapter();
        int order = ((Activity) mContext).getPreferences(Context.MODE_PRIVATE).getInt(
                mContext.getString(R.string.sort_order_key), 0);
        adapter.addCollection(cHeaders, CHeaderAdapter.HeaderListSortOrder.valueOf(order));
        mainView.findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    public static void onScanProgress(long n, long total, long size, long headers) {
        ArcProgress arcProgress = ((ArcProgress) ((Activity) mContext).getWindow().getDecorView()
                .findViewById(R.id.imagesArcProgress));

        //This is safe because the original is an int
        arcProgress.setProgress(((int) n));
        arcProgress.setMax(((int) total));
        totalSelectedItems = (int) headers;
        totalItems = (int) total;

        if (n == total) {
            arcProgress.setBottomText(Formatter.formatShortFileSize(mContext, size));
            totalSelectedFileSize = size;
        }
    }
}
