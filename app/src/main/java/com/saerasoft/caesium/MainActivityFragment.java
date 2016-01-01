package com.saerasoft.caesium;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.concurrent.ExecutionException;

public class MainActivityFragment extends Fragment {

    public final static String BUNDLE_HEADER_COLLECTION = "com.saerasoft.caesium.B_HEADER_COLLECTION";

    private static CHeaderCollection collection;
    private RecyclerView.LayoutManager mLayoutManager;
    private static Context mContext;
    private static long inImagesSize;
    private static ImageCompressAsyncTask compressor;
    public static boolean isCompressing = false;

    public MainActivityFragment() {

    }

    public static void onPreCompress() {
        //Set the button icon to a stop
        //TODO Implement later
        //((FloatingActionButton) ((Activity) mContext).getWindow().getDecorView().findViewById(
                //R.id.mainCompressButton)).setImageResource(R.drawable.ic_stop_white_24dp);

        //Disable UI
        setCheckBoxesEnabled(((Activity) mContext).getWindow().getDecorView(), false);
        (((Activity) mContext).getWindow().getDecorView().findViewById(
                R.id.mainCompressButton)).setEnabled(false);
        //Tell everyone we are compressing
        isCompressing = true;
        //And invalidate the menu
        ((Activity) mContext).invalidateOptionsMenu();
    }

    public static void onCompressProgress(int progress, int max, int headerIndex) {
        //Super ugly 1 row method to update progress bar count
        ((ArcProgress) ((Activity) mContext).getWindow().getDecorView().findViewById(
                R.id.mainImagesCountArcProgress)).setProgress(max - progress - 1);

        //Super ugly 1 row method to update the size count
        ((TextView) ((Activity) mContext).getWindow().getDecorView().findViewById(
                R.id.mainImagesSizeTextView)).setText(mContext.getString(R.string.main_compressing));
    }

    public static void onPostCompress(long size) {
        Log.d("MainFragment", "Compressed size: " + size);

        //Set the compress icon to the action button
        //TODO Implement later
        //((FloatingActionButton) ((Activity) mContext).getWindow().getDecorView().findViewById(
                //R.id.mainCompressButton)).setImageResource(R.mipmap.logo2x);

        //Remove the selected items
        //Wow ugly methods!
        CHeaderAdapter mAdapter = (CHeaderAdapter) ((RecyclerView) ((Activity) mContext).getWindow().getDecorView().findViewById(
                R.id.mainHeadersListView)).getAdapter();

        Toast.makeText(mContext,
                "Saved " + Formatter.formatFileSize(mContext, inImagesSize - size),
                Toast.LENGTH_LONG).show();

        //TODO This will cycle a lot more than it should, but it works
        for (int i = 0; i < mAdapter.getHeaderCollection().getHeaders().size(); i++) {
            if (mAdapter.getHeaderCollection().getHeaders().get(i).isSelected()) {
                mAdapter.removeAt(i);
                i = 0;
            }
        }

        //Set the compressor to null, so we can start it again if needed
        compressor = null;

        //Enable UI
        setCheckBoxesEnabled(((Activity) mContext).getWindow().getDecorView(), true);
        (((Activity) mContext).getWindow().getDecorView().findViewById(
                R.id.mainCompressButton)).setEnabled(true);
        //Tell everyone we finished
        isCompressing = false;
        //And invalidate the menu
        ((Activity) mContext).invalidateOptionsMenu();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            collection = savedInstanceState.getParcelable(BUNDLE_HEADER_COLLECTION);
        }
        mContext = getContext();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d("MainFragment", "View created");

        //Get rootView for getting widgets
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Get the required elements of the layout
        final ArcProgress imagesCountArcProgress = (ArcProgress) rootView.findViewById(R.id.mainImagesCountArcProgress);
        RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.mainHeadersListView);
        FloatingActionButton compressButton = (FloatingActionButton) rootView.findViewById(R.id.mainCompressButton);
        TextView imagesSizeTextView = (TextView) rootView.findViewById(R.id.mainImagesSizeTextView);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.mainSwipeRefresh);

        if (getActivity().getIntent()
                .getParcelableExtra(LauncherActivity.EXTRA_HEADER_COLLECTION) != null) {
            collection = getActivity().getIntent()
                    .getParcelableExtra(LauncherActivity.EXTRA_HEADER_COLLECTION);
        }

        //Create an instance of the adapter for the list
        CHeaderAdapter adapter = new CHeaderAdapter(collection, getContext());

        //Set the total size of scanned images to the text view
        imagesSizeTextView.setText(Formatter.formatFileSize(getContext(), collection.getSelectedItemsImageSize()));

        //List settings
        //Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(mLayoutManager);

        //Add the adapter to the list
        listView.setAdapter(adapter);

        //Set the counter and progress bar total according to the collection with an animation
        final int maxImagesCount = collection.getCount();
        imagesCountArcProgress.setMax(maxImagesCount);
        imagesCountArcProgress.setProgress(collection.getSelectedItemsImageCount());

        compressor = null;

        /* -- Listeners -- */

        //Compress button action
        compressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create an instance of the compressor if it does not exists already
                if (compressor == null) {
                    compressor = new ImageCompressAsyncTask(getContext());
                }
                inImagesSize = collection.getSelectedItemsImageSize();
                if (compressor.getStatus() == AsyncTask.Status.RUNNING) {
                    Log.d("MainActivity", "Compressor is running");
                    compressor.cancel(false);
                } else {
                    //Set a global inImageSize for stats
                    inImagesSize = collection.getSelectedItemsImageSize();
                    Log.d("MainActivity", "IN SIZE: " + String.valueOf(collection.getSelectedItemsImageSize()));
                    Log.d("MainActivity", "Compressor is not running");
                    compressor.execute(collection,
                            new DatabaseHelper(getContext()).getWritableDatabase(),
                            collection.getSelectedItemsImageCount(),
                            collection.getSelectedItemsImageSize());
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ImageScanAsyncTask().execute((Activity) mContext);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("MainFragment", "Save called");
        outState.putParcelable(BUNDLE_HEADER_COLLECTION, collection);
        super.onSaveInstanceState(outState);
    }

    public static void setUIEnabled(View v, boolean enabled) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                if (vg.getChildAt(i).getId() == R.id.mainCompressButton) {
                    continue;
                }
                setUIEnabled(vg.getChildAt(i), enabled);
            }
        }
        v.setEnabled(enabled);
    }

    public static void setCheckBoxesEnabled(View v, boolean enabled) {
        ViewGroup vg = (ViewGroup) v;
        CHeaderAdapter adapter = (CHeaderAdapter) ((RecyclerView) vg.findViewById(R.id.mainHeadersListView)).getAdapter();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            adapter.setCheckboxVisible(enabled, i);
            //Notify the RecyclerView something has changed
            adapter.notifyItemChanged(i);
        }
    }
}
