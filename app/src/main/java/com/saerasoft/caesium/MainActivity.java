package com.saerasoft.caesium;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;

import com.github.lzyzsd.circleprogress.ArcProgress;

//TODO If back button is pressed, stay on this activity

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
