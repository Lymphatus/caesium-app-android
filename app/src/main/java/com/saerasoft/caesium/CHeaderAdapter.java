package com.saerasoft.caesium;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lymphatus on 06/10/15.
 */
public class CHeaderAdapter extends ArrayAdapter<CHeader> {

    private ArrayList<CHeader> mHeaders;

    public CHeaderAdapter(Context context, int resource, ArrayList<CHeader> headers) {
        super(context, resource, headers);
        this.mHeaders = headers;
    }

    @Override
    public CHeader getItem(int position) {
        return this.mHeaders.get(position);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            //Inflate the View if it does not exist yet
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_main, parent, false);
        }

        //Get the header according to position
        CHeader header = mHeaders.get(position);

        //Check if it's not a null object
        if (header != null) {

            //Get the widgets within the view
            ImageView logoImageView = (ImageView) v.findViewById(R.id.listEntryLogoImageView);
            TextView headerNameTextView = (TextView) v.findViewById(R.id.listEntryTitleTextView);
            TextView sizeTextView = (TextView) v.findViewById(R.id.listEntrySizeTextView);
            TextView countTextView = (TextView) v.findViewById(R.id.listEntryCountTextView);
            TextView logoInitialTextView = (TextView) v.findViewById(R.id.listEntryLogoInitialTextView);

            //Set the values according to the CHeader passed
            headerNameTextView.setText(header.getName());
            sizeTextView.setText(Formatter.formatFileSize(getContext(), header.getSize()));
            countTextView.setText(String.valueOf(header.getCount()));

            //Set a random color for each row
            //TODO This should be more clever
            logoImageView.getBackground().setColorFilter(Color.parseColor(
                            getContext().getResources().getStringArray(
                                    R.array.pastel_colors)[new Random().nextInt(
                                    (getContext().getResources().getStringArray(R.array.pastel_colors)).length)]),
                    PorterDuff.Mode.DARKEN);
            logoImageView.invalidate();

            //Set the logo initial according to the header title
            //Gets the first alphabetic char
            Pattern p = Pattern.compile("\\p{Alpha}");
            Matcher m = p.matcher(header.getName());
            //If there's an alpha char, use it, otherwise use just the first char
            if (m.find()) {
                logoInitialTextView.setText(String.valueOf(header.getName().charAt(m.start())));
            } else {
                logoInitialTextView.setText(String.valueOf(header.getName().charAt(0)));
            }

        }

        return v;
    }
}
