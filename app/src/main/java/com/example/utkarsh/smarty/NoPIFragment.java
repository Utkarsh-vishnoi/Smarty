package com.example.utkarsh.smarty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;

public class NoPIFragment extends AppCompatDialogFragment {

    public static String TAG = "No PI";

    public interface NoPIFragmentListener {
        void onNoPIPositiveClick(DialogFragment dialog);
        void onNoPINegativeClick(DialogFragment dialog);
    }

    NoPIFragmentListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (NoPIFragmentListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString() + " must implement NoPIFragmentListener");
        }
    }

    public boolean show(FragmentManager manager) {
        if(manager.isStateSaved()) {
            return false;
        }
        show(manager, TAG);
        return true;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_error_message);
        builder.setTitle("No Smart PI Found...");
        builder.setMessage("Please recheck the Smart PI Configuration and try again.");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onNoPIPositiveClick(NoPIFragment.this);
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onNoPINegativeClick(NoPIFragment.this);
            }
        });
        return builder.create();
    }
}
