package com.saerasoft.caesium;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public final static String BUNDLE_HEADER_COLLECTION = "com.saerasoft.caesium.B_HEADER_COLLECTION";

    private static CHeaderCollection collection;

    public MainActivityFragment() {

    }

    public static void onCompressProgress(Context context, int progress) {
        //Super ugly 1 row method to update progress bar count
        ((ProgressBar) ((Activity) context).getWindow().getDecorView().findViewById(
                R.id.mainProgressBar)).setMax(progress);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d("MainFragment", "View created");
        //Get rootView for getting widgets
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Get the required elements of the layout
        TextView imagesCountTextView = (TextView) rootView.findViewById(R.id.mainImagesCountTextView);
        ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.mainProgressBar);
        ListView listView = (ListView) rootView.findViewById(R.id.mainHeadersListView);
        FloatingActionButton compressButton = (FloatingActionButton) rootView.findViewById(R.id.mainCompressButton);
        //Inflate the header layout to the list view
        ViewGroup listViewHeader = (ViewGroup) inflater.inflate(R.layout.list_header, listView, false);
        TextView headerImagesSize = (TextView) listViewHeader.findViewById(R.id.listHeaderImagesSizeTextView);

        if (getActivity().getIntent()
                .getParcelableExtra(LauncherActivity.EXTRA_HEADER_COLLECTION) != null) {
            collection = getActivity().getIntent()
                    .getParcelableExtra(LauncherActivity.EXTRA_HEADER_COLLECTION);
        }

        //Set the counter and progress bar total according to the collection
        int imagesCount = collection.getCount();
        imagesCountTextView.setText(String.valueOf(imagesCount));
        progressBar.setMax(imagesCount);

        //Create an instance of the adapter for the list
        CHeaderAdapter adapter = new CHeaderAdapter(getContext(), R.layout.list_main, collection.getHeaders());
        //Set the total size of scanned images to the list header
        headerImagesSize.setText(Formatter.formatFileSize(getContext(), collection.getSize()));

        //Add the header and the adapter to the list
        listView.addHeaderView(listViewHeader);
        listView.setAdapter(adapter);

        /* -- Listeners -- */

        //Compress button action
        compressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageCompressAsyncTask compressor = new ImageCompressAsyncTask();
                compressor.execute(getActivity(),
                        collection,
                        new DatabaseHelper(getContext()).getWritableDatabase());
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
