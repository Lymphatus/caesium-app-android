package com.saerasoft.caesium;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class SettingsActivity extends AppCompatActivity {

    //Options keys
    public static final String KEY_COMPRESSION_LEVEL = "pref_key_compression_level";
    public static final String KEY_COMPRESSION_EXIF = "pref_key_compression_exif";
    public static final String KEY_INFO_VERSION = "pref_key_info_version";
    public static final String KEY_INFO_USAGE = "pref_key_info_usage";
    public static final String KEY_MAINTENANCE_CLEAR_DATABASE = "pref_key_maintenance_clear_database";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemePreferences);
        super.onCreate(savedInstanceState);
        setContentView(com.saerasoft.caesium.R.layout.activity_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(com.saerasoft.caesium.R.id.container, new SettingsActivityFragment())
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.saerasoft.caesium.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
