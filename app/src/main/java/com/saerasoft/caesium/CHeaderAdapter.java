package com.saerasoft.caesium;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.lzyzsd.circleprogress.ArcProgress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by lymphatus on 12/07/16.
 */
public class CHeaderAdapter extends RecyclerView.Adapter<CHeaderAdapter.ViewHolder> {

    private static Context mContext;
    private ArrayList<CHeader> mCHeaders;
    private int lastPosition = -1;

    public CHeaderAdapter() {
        mCHeaders = new ArrayList<>();
    }

    // TODO: 19/07/16 Should we animate the count up and down?
    public static void updateCount(int deltaProgress, long deltaSize, boolean sign) {
        //Ok, this is just a trick to decide which sign should the following operations have
        int dir = sign ? 1 : -1;

        ArcProgress arcProgress = ((ArcProgress) ((Activity) mContext).getWindow().getDecorView()
                .findViewById(R.id.imagesArcProgress));

        MainActivity.totalItems = MainActivity.totalItems + dir * deltaProgress;

        arcProgress.setMax(MainActivity.totalItems);
        arcProgress.setProgress(MainActivity.totalItems);

        long total = MainActivity.totalSelectedFileSize + dir * deltaSize;
        arcProgress.setBottomText(android.text.format.Formatter.formatShortFileSize(mContext,
                total));
        MainActivity.totalSelectedFileSize = total;

        MainActivity.totalSelectedItems += dir;
    }

    @Override
    public CHeaderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_main, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CHeaderAdapter.ViewHolder holder, int position) {
        if (!mCHeaders.isEmpty()) {
            final CHeader currentHeader = mCHeaders.get(position);
            holder.headerCheckBox.setChecked(currentHeader.isSelected());
            holder.headerTitleTextView.setText(currentHeader.getTitle());
            holder.headerSubtitleTextView.setText(
                    String.format("%s - %s",
                            android.text.format.Formatter.formatFileSize(mContext, currentHeader.size()),
                            Long.toString(currentHeader.length())));
            holder.headerLogoImageView.getBackground().
                    setColorFilter(currentHeader.getColor(), PorterDuff.Mode.DARKEN);
            holder.headerLogoImageView.invalidate();
            holder.headerLogoTextView.setText(currentHeader.getInitialLetter());

            // Set the view to fade in
            setFadeAnimation(holder.itemView, position);

            /* Actions */

            holder.headerCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentHeader.setSelected(!currentHeader.isSelected());
                    updateCount(currentHeader.length(), currentHeader.size(), currentHeader.isSelected());
                }
            });
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    private void setFadeAnimation(View view, int position) {
        if (position > lastPosition) {
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            view.startAnimation(anim);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mCHeaders.size();
    }

    public int addCollection(ArrayList<CHeader> cHeaders, HeaderListSortOrder order) {
        mCHeaders.clear();
        mCHeaders.addAll(cHeaders);
        this.sortList(order);
        this.notifyDataSetChanged();
        return cHeaders.size();
    }

    public void setAllSelected(boolean selected) {
        for (CHeader cHeader : mCHeaders) {
            boolean previous = cHeader.isSelected();
            cHeader.setSelected(selected);
            // TODO: 19/07/16 This might be slow
            if (previous != selected) {
                updateCount(cHeader.length(), cHeader.size(), selected);
            }
        }
        notifyDataSetChanged();
    }

    public void sortList(final HeaderListSortOrder order) {
        Collections.sort(mCHeaders, new Comparator<CHeader>() {
            @Override
            public int compare(CHeader lhs, CHeader rhs) {
                // TODO: 19/07/16 Sort by ascending and descending order option
                switch (order) {
                    case TITLE:
                        return lhs.getTitle().compareTo(rhs.getTitle());
                    case FILE_SIZES:
                        // TODO: 19/07/16 CRITICAL This may overflow!!
                        return (int) (rhs.size() - lhs.size());
                    case LENGTH:
                        // TODO: 19/07/16 CRITICAL This may overflow!!
                        return rhs.length() - lhs.length();
                }
                //We should never reach this
                return 0;
            }
        });
        notifyDataSetChanged();
    }

    public enum HeaderListSortOrder {
        FILE_SIZES(0),
        LENGTH(1),
        TITLE(2);

        private static Map<Integer, HeaderListSortOrder> map = new HashMap<>();

        static {
            for (HeaderListSortOrder legEnum : HeaderListSortOrder.values()) {
                map.put(legEnum.order, legEnum);
            }
        }

        private int order;

        HeaderListSortOrder(final int index) { order = index; }

        public static HeaderListSortOrder valueOf(int index) {
            return map.get(index);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CheckBox headerCheckBox;
        public TextView headerTitleTextView;
        public TextView headerSubtitleTextView;
        public ImageView headerLogoImageView;
        public TextView headerLogoTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            headerCheckBox = (CheckBox) itemView.findViewById(R.id.headerCheckBox);
            headerTitleTextView = (TextView) itemView.findViewById(R.id.headerTitleTextView);
            headerSubtitleTextView = (TextView) itemView.findViewById(R.id.headerSubtitleTextView);
            headerLogoTextView = (TextView) itemView.findViewById(R.id.headerLogoTextView);
            headerLogoImageView = (ImageView) itemView.findViewById(R.id.headerLogoImageView);
        }
    }
}
