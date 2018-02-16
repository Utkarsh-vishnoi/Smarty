package com.example.utkarsh.smarty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;

public class ConnectionErrorFragment extends AppCompatDialogFragment {

    public interface ConnectionErrorFragmentListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    ConnectionErrorFragmentListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ConnectionErrorFragmentListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString() + " must implement ConnectionErrorListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_error_message);
        builder.setTitle("Error connecting to server...");
        builder.setMessage("It looks like you are not connected to the network. Check your internet connection and try again.");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDialogPositiveClick(ConnectionErrorFragment.this);
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDialogNegativeClick(ConnectionErrorFragment.this);
            }
        });
        return builder.create();
    }
}
