package com.saerasoft.caesium;

import android.graphics.PorterDuff;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * A placeholder fragment containing a simple view.
 */
public class LauncherActivityFragment extends Fragment {

    public LauncherActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_launch, container, false);

        //ProgressBar Color by applying a white filter
        ((ProgressBar) rootView.findViewById(R.id.launchProgressBar)).getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                        PorterDuff.Mode.SRC_IN);

        return rootView;
    }
}
