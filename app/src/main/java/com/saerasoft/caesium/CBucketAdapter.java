package com.saerasoft.caesium;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.List;


public class CBucketAdapter extends RecyclerView.Adapter<CBucketAdapter.ViewHolder> {
    private List<CBucket> cBuckets;
    private Context context;
    private ViewHolder vh;
    private int lastPosition = 0;

    public CBucketAdapter(List<CBucket> sortedList, Context context) {
        cBuckets = sortedList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Declare the widgets
        TextView nameTextView;
        TextView sizeTextView;
        TextView itemsSizeTextView;
        CheckBox checkBox;
        ImageView colorIndicator;

        //The container for the animation
        public RelativeLayout container;

        public ViewHolder(View convertView) {
            super(convertView);
            nameTextView = (TextView) convertView.findViewById(R.id.listBucketName);
            sizeTextView = (TextView) convertView.findViewById(R.id.listBucketSize);
            itemsSizeTextView = (TextView) convertView.findViewById(R.id.listBucketItemsSize);
            checkBox = (CheckBox) convertView.findViewById(R.id.listCheckBox);
            colorIndicator = (ImageView) convertView.findViewById(R.id.listColorIndicator);
            container = (RelativeLayout) itemView.findViewById(R.id.listRelativeLayout);
        }
    }


    @Override
    public void onBindViewHolder(CBucketAdapter.ViewHolder holder, int position) {
        //Set the global view holder
        this.vh = holder;

        //Get the header according to position
        final CBucket cBucket = cBuckets.get(position);

        //Set the values according to the CHeader passed
        holder.nameTextView.setText(cBucket.getName());
        holder.sizeTextView.setText(String.valueOf(cBucket.getSize()));
        holder.itemsSizeTextView.setText(Formatter.formatShortFileSize(context, cBucket.getItemsSize()));

        int[] indicatorColors = ((MainActivity)context).getIndicatorColors();

        switch (position) {
            case 0:
            case 1:
            case 2:
            case 3:
                holder.colorIndicator.setBackgroundColor(ContextCompat.getColor(context, indicatorColors[position]));
                break;
            default:
                holder.colorIndicator.setBackgroundColor(ContextCompat.getColor(context, indicatorColors[4]));
                break;
        }

        //Set a checkbox listener
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cBucket.isChecked() != isChecked) {
                    cBucket.setChecked(isChecked);
                    //Set the header checked
                    updateMainUI();
                }
            }
        });

        //Set the checkbox according to the CHeader
        holder.checkBox.setChecked(cBucket.isChecked());


        //Set an animation
        setAnimation(holder.container, position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return cBuckets.size();
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        holder.container.clearAnimation();
    }

    private void setAnimation(View viewToAnimate, int position) {
        //If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.list_item_up);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void updateMainUI() {
        MainActivity mainActivity = ((MainActivity) context);
        mainActivity.updateValues();
        PieChart pieChart = mainActivity.setPieChartData();
        mainActivity.showPieChart(pieChart);

//        //This holds the current image count
//        int progress = mHeaders.getSelectedItemsImageCount();
//
//        //Set an animator
//        ObjectAnimator animation = ObjectAnimator.ofInt(imagesCountArcProgress, "progress", progress);
//        animation.setInterpolator(new DecelerateInterpolator());
//
//        //The only case in which the two progresses are the same is the first time we call
//        //the animation
//        if (progress == oldProgress) {
//            animation.setDuration(1000);
//            //TODO This delay is arbitrary, do some research about that
//            //animation.setStartDelay(200);
//        } else {
//            animation.setDuration(500);
//        }
//        animation.start();
//
//        //Set the selected images size to the main text view
//        imagesSizeTextView.setText(Formatter.formatFileSize(mContext, mHeaders.getSelectedItemsImageSize()));
    }


    public void checkItemAt(int position, boolean checked) {
        cBuckets.get(position).setChecked(checked);
        updateMainUI();
    }
}
