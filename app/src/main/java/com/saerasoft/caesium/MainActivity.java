package com.saerasoft.caesium;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MainActivityInterface{

    private int bucketsCount = 0;
    private int imagesCount = 0;
    private long bucketsItemsSize = 0;
    private List<CBucket> bucketsList = new ArrayList<>();
    private final int[] indicatorColors = {
            R.color.chart_1,
            R.color.chart_2,
            R.color.chart_3,
            R.color.chart_4,
            R.color.chart_5,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Request runtime permissions
        requestPermissions();

        //Map all the images of the device
        CBuckets cBuckets = getAllImages();

        //Initialize the chart with all the data
        PieChart pieChart = setPieChartData();

//      Show the pie chart
        showPieChart(pieChart);

        CBucketAdapter adapter = new CBucketAdapter(this.bucketsList, this);

        RecyclerView listView = (RecyclerView) findViewById(R.id.listView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(linearLayoutManager);
        listView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.list_divider));
        listView.addItemDecoration(dividerItemDecoration);

//        Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
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
            CBucketAdapter adapter = (CBucketAdapter) ((RecyclerView) (this.getWindow().getDecorView().findViewById(
                    R.id.listView))).getAdapter();

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);

        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100:
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);

                }
            }
        }
    }

    public PieChart setPieChartData() {
        List<PieEntry> entries = new ArrayList<>();
        long topItemsSize = 0;

        if (this.bucketsCount > 4) {
            int j = 0;
            for (int i = 0; i < this.bucketsCount; i++) {
                CBucket cBucket = this.bucketsList.get(i);
                if (cBucket.isChecked()) {
                    long bucketSize = cBucket.getItemsSize();
                    entries.add(new PieEntry(bucketSize, ""));
                    topItemsSize += bucketSize;
                    j++;
                }
                if (j == 4) {
                    break;
                }
            }
        }
        entries.add(new PieEntry((this.bucketsItemsSize - topItemsSize), ""));


        PieDataSet set = new PieDataSet(entries, "Buckets");
        set.setColors(indicatorColors);
        set.setDrawValues(false);
        set.setDrawIcons(false);
        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
        PieData data = new PieData(set);
        pieChart.setData(data);

        return pieChart;
    }

    public void showPieChart(PieChart pieChart) {
        //Styling
        pieChart.setTransparentCircleRadius(75f);
        pieChart.setCenterText(String.valueOf(this.imagesCount) + "\n" +
                Formatter.formatFileSize(this, this.bucketsItemsSize));
        pieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.white));
        pieChart.setCenterTextSize(26f);
        pieChart.setCenterTextTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        pieChart.setDescription(null);
        pieChart.setTransparentCircleAlpha(100);
        pieChart.setHoleRadius(70f);
        pieChart.getLegend().setEnabled(false);
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.primary));

        pieChart.animateX(800);
        pieChart.animateY(800);
        pieChart.invalidate();

    }

    private CBuckets getAllImages() {
        ContentResolver cr = this.getContentResolver();

        String[] columns = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATE_MODIFIED,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};

        Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, null);
        CBuckets cBuckets = new CBuckets();
        int count = 0;
        if (cur != null) {
            cur.moveToFirst();
            while (cur.moveToNext()) {
                Integer bucketID = cur.getInt(5);
                CImage cImage = new CImage(cur.getInt(0),
                        cur.getString(1),
                        cur.getString(2),
                        cur.getLong(3),
                        cur.getLong(4));
                if (cBuckets.containsKey(bucketID)) {
                    CBucket cBucket = cBuckets.get(bucketID);
                    cBucket.getImagesList().add(cImage);
                } else {
                    CBucket cBucket = new CBucket(cur.getInt(5), cur.getString(6));
                    cBucket.getImagesList().add(cImage);
                    cBuckets.put(bucketID, cBucket);
                }
                count++;
            }
            cur.close();
        }

        this.bucketsList = cBuckets.sortList();
        this.bucketsCount = this.bucketsList.size();
        this.bucketsItemsSize = cBuckets.getTotalItemsSize();
        this.imagesCount = count;

        return cBuckets;
    }

    public void updateValues() {
        long itemsSize = 0;
        int count = 0;
        for (CBucket cBucket : this.bucketsList) {
            if (cBucket.isChecked()) {
                itemsSize += cBucket.getItemsSize();
                count += cBucket.getSize();
            }
        }
        this.imagesCount = count;
        this.bucketsItemsSize = itemsSize;
    }

    @Override
    public int[] getIndicatorColors() {
        return this.indicatorColors;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
