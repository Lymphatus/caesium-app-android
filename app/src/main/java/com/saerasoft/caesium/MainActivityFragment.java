package com.saerasoft.caesium;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

public class MainActivityFragment extends Fragment {

    public final static String BUNDLE_HEADER_COLLECTION = "com.saerasoft.caesium.B_HEADER_COLLECTION";

    private static CHeaderCollection collection;
    private RecyclerView.LayoutManager mLayoutManager;

    public MainActivityFragment() {

    }

    public static void onCompressProgress(Context context, int progress, int max, long size) {
        //Super ugly 1 row method to update progress bar count
        ((ArcProgress) ((Activity) context).getWindow().getDecorView().findViewById(
                R.id.mainImagesCountArcProgress)).setProgress(max - progress - 1);

        //Super ugly 1 row method to update the uncompressed count
        ((TextView) ((Activity) context).getWindow().getDecorView().findViewById(
                R.id.mainImagesSizeTextView)).setText(Formatter.formatFileSize(context,
                collection.getSelectedItemsImageSize() - size));
    }

    public static void onPostCompress(long size) {
        Log.d("MainFragment", "Compressed size: " + size);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            collection = savedInstanceState.getParcelable(BUNDLE_HEADER_COLLECTION);
        }
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

        if (getActivity().getIntent()
                .getParcelableExtra(LauncherActivity.EXTRA_HEADER_COLLECTION) != null) {
            collection = getActivity().getIntent()
                    .getParcelableExtra(LauncherActivity.EXTRA_HEADER_COLLECTION);
        }

        //Create an instance of the adapter for the list
        CHeaderAdapter adapter = new CHeaderAdapter(collection, getContext());

        //Set the total size of scanned images to the text view
        imagesSizeTextView.setText(Formatter.formatFileSize(getContext(), collection.getSize()));

        //List settings
        listView.setHasFixedSize(true);

        //Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(mLayoutManager);

        //Add the header and the adapter to the list
        listView.setAdapter(adapter);

        //Set the counter and progress bar total according to the collection with an animation
        final int maxImagesCount = collection.getCount();
        imagesCountArcProgress.setMax(maxImagesCount);


        /* -- Listeners -- */

        //Compress button action
        compressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageCompressAsyncTask compressor = new ImageCompressAsyncTask();
                compressor.execute(getActivity(),
                        collection,
                        new DatabaseHelper(getContext()).getWritableDatabase(),
                        collection.getSelectedItemsImageCount(),
                        collection.getSelectedItemsImageSize());
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
}
