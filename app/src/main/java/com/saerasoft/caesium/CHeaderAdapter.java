package com.saerasoft.caesium;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lymphatus on 06/10/15.
 */
public class CHeaderAdapter extends RecyclerView.Adapter<CHeaderAdapter.ViewHolder> {

    private CHeaderCollection mHeaders;
    private Context mContext;
    private int lastPosition = -1;
    private ViewHolder vh;

    public CHeaderAdapter(CHeaderCollection headers, Context context) {
        this.mHeaders = headers;
        this.mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Declare the widgets
        public ImageView logoImageView;
        public TextView headerNameTextView;
        public TextView sizeTextView;
        public TextView countTextView;
        public TextView logoInitialTextView;
        public CheckBox checkBox;

        //The container for the animation
        public RelativeLayout container;

        public ViewHolder(View v) {
            super(v);
            logoImageView = (ImageView) v.findViewById(R.id.listEntryLogoImageView);
            headerNameTextView = (TextView) v.findViewById(R.id.listEntryTitleTextView);
            sizeTextView = (TextView) v.findViewById(R.id.listEntrySizeTextView);
            countTextView = (TextView) v.findViewById(R.id.listEntryCountTextView);
            logoInitialTextView = (TextView) v.findViewById(R.id.listEntryLogoInitialTextView);
            checkBox = (CheckBox) v.findViewById(R.id.listEntryCheckBox);
            container = (RelativeLayout) itemView.findViewById(R.id.listRelativeLayout);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Set the global view holder
        this.vh = holder;

        //Get the header according to position
        final CHeader header = mHeaders.getHeaders().get(position);

        //Set the values according to the CHeader passed
        holder.headerNameTextView.setText(header.getName());
        holder.sizeTextView.setText(Formatter.formatFileSize(mContext, header.getSize()));
        holder.countTextView.setText("(" + String.valueOf(header.getCount()) + ")");

        //Set a random color for each row
        //TODO This should be more clever

        if (header.getColor() == Color.WHITE) {
            header.setColor(Color.parseColor(
                    mContext.getResources().getStringArray(
                            R.array.pastel_colors)[new Random().nextInt(
                            (mContext.getResources().getStringArray(R.array.pastel_colors)).length)]));


        }

        holder.logoImageView.getBackground().setColorFilter(header.getColor(), PorterDuff.Mode.DARKEN);
        holder.logoImageView.invalidate();

        //Set the logo initial according to the header title
        //Gets the first alphabetic char
        Pattern p = Pattern.compile("\\p{Alpha}");
        Matcher m = p.matcher(header.getName());
        //If there's an alpha char, use it, otherwise use just the first char
        if (m.find()) {
            holder.logoInitialTextView.setText(String.valueOf(header.getName().charAt(m.start())));
        } else {
            holder.logoInitialTextView.setText(String.valueOf(header.getName().charAt(0)));
        }

        //Set a checkbox listener
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Keep track of the old progress value for the animation
                int oldProgress = mHeaders.getSelectedItemsImageCount();
                //Set the header checked
                header.setSelected(isChecked);
                updateUICount(oldProgress);
            }
        });

        //Set the checkbox according to the CHeader
        holder.checkBox.setChecked(header.isSelected());
        //And set him visible
        if (header.isCheckBoxVisible()) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.INVISIBLE);
        }

        //Set an animation
        setAnimation(holder.container, position);
    }

    @Override
    public int getItemCount() {
        return mHeaders.getHeaders().size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_main, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        holder.container.clearAnimation();
    }

    private void setAnimation(View viewToAnimate, int position) {
        //If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.list_item_up);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void updateUICount(int oldProgress) {
        //Get the views
        ArcProgress imagesCountArcProgress = (ArcProgress) ((Activity) mContext).getWindow().getDecorView().findViewById(
                R.id.mainImagesCountArcProgress);
        TextView imagesSizeTextView = (TextView) ((Activity) mContext).getWindow().getDecorView().findViewById(
                R.id.mainImagesSizeTextView);

        //This holds the current image count
        int progress = mHeaders.getSelectedItemsImageCount();

        //Set an animator
        ObjectAnimator animation = ObjectAnimator.ofInt(imagesCountArcProgress, "progress", progress);
        animation.setInterpolator(new DecelerateInterpolator());

        //The only case in which the two progresses are the same is the first time we call
        //the animation
        if (progress == oldProgress) {
            animation.setDuration(1000);
            //TODO This delay is arbitrary, do some research about that
            //animation.setStartDelay(200);
        } else {
            animation.setDuration(500);
        }
        animation.start();

        //Set the selected images size to the main text view
        imagesSizeTextView.setText(Formatter.formatFileSize(mContext, mHeaders.getSelectedItemsImageSize()));
    }

    public CHeaderCollection getHeaderCollection() {
        return mHeaders;
    }

    public void removeAt(int position) {
        mHeaders.removeAt(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mHeaders.getHeaders().size());
    }

    public void checkItemAt(int position, boolean checked) {
        mHeaders.getHeaders().get(position).setSelected(checked);
        updateUICount(mHeaders.getSelectedItemsImageCount());
    }

    public void setCheckboxVisible(boolean visible, int position) {
        mHeaders.getHeaders().get(position).setCheckBoxVisible(visible);
    }
}
