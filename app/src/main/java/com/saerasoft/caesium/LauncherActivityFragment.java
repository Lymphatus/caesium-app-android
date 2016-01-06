package com.saerasoft.caesium;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LauncherActivityFragment extends Fragment {

    public LauncherActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Hide the ActionBar for the splash
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        View rootView = inflater.inflate(R.layout.fragment_launch, container, false);

        ImageView logoImageView = (ImageView) rootView.findViewById(R.id.launchLogoImageView);
        TextView brandTextView = (TextView) rootView.findViewById(R.id.launchTextView);
        ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.launchProgressBar);
        TextView betaTextView = (TextView) rootView.findViewById(R.id.launchBetaTextView);

        //Logo Animation
        Animation launcherIn = AnimationUtils.loadAnimation(getContext(), R.anim.launcher_logo_in);
        logoImageView.startAnimation(launcherIn);

        //Brand animation
        Animation brandIn = AnimationUtils.loadAnimation(getContext(), R.anim.list_item_up);
        brandTextView.startAnimation(brandIn);
        betaTextView.startAnimation(brandIn);

        //ProgressBar Color by applying a white filter
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                        PorterDuff.Mode.SRC_IN);

        Animation progressIn = AnimationUtils.loadAnimation(getContext(), R.anim.launcher_logo_in);
        progressIn.setStartOffset(800);
        progressBar.startAnimation(progressIn);

        return rootView;
    }
}
