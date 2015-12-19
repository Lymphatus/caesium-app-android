package com.saerasoft.caesium;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by lymphatus on 13/12/15.
 */
public class ClearDatabaseDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_clear_database_message)
                .setTitle(R.string.dialog_clear_database_title)
                .setPositiveButton(R.string.dialog_clear_database_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Delete the database
                        getActivity().deleteDatabase(DatabaseHelper.DATABASE_NAME);

                        //Restart the application
                        Intent i = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                })
                .setNegativeButton(R.string.dialog_clear_database_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //User cancelled the dialog, do nothing
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}