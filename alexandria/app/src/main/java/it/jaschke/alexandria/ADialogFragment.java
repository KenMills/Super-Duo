package it.jaschke.alexandria;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by kenm on 8/2/2015.
 */
public class ADialogFragment extends DialogFragment {
    private String LOG_TAG = "ScannerDialogFragment";

    public static final String A_DIALOG_FRAGMENT = "ADialogFragment";
    public static final String DIALOG_TITLE   = "DIALOG_TITLE";
    public static final String DIALOG_MESSAGE = "DIALOG_MESSAGE";

    private YesNoListener listener;
    public interface YesNoListener {
        void onYes();
        void onNo();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.v(LOG_TAG, "onAttach");

        if (activity instanceof YesNoListener) {
            listener = (YesNoListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implemenet ADialogFragment.YesNoListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateDialog");

        Bundle bundle = getArguments();
        String title = bundle.getString(DIALOG_TITLE);
        String message = bundle.getString(DIALOG_MESSAGE);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onYes();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onNo();
                        }
                    }
                })
                .create();
    }

    @Override
    public void onPause() {
        Log.v(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.v(LOG_TAG, "onDismiss");
        super.onDismiss(dialog);
    }
}
