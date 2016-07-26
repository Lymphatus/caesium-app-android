package com.saerasoft.caesium;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/*
 * Created by lymphatus on 20/07/16.
 */
public class ListOrderDialogFragment extends DialogFragment {
    // TODO: 20/07/16 Save it in preference
    private CHeaderAdapter.HeaderListSortOrder order;
    private int index = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int defaultValue = sharedPref.getInt(getString(R.string.sort_order_key), 0);
        // Set the dialog title
        builder.setTitle(R.string.title_dialog_order)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(R.array.entries_dialog_order, defaultValue,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                index = i;
                                order = CHeaderAdapter.HeaderListSortOrder.valueOf(index);
                                View mainView = getActivity().getWindow().getDecorView();

                                CHeaderAdapter adapter = (CHeaderAdapter) ((RecyclerView) mainView
                                        .findViewById(R.id.listRecyclerView))
                                        .getAdapter();

                                adapter.sortList(order);
                                saveToPreference(index);
                                dismiss();
                            }
                        });
        return builder.create();
    }

    public void saveToPreference(int index) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.sort_order_key), index);
        editor.apply();
    }
}
