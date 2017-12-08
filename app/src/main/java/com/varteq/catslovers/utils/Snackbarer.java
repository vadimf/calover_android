package com.varteq.catslovers.utils;


import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.varteq.catslovers.R;

public class Snackbarer {

    private static TSnackbar snackbar;

    public static void showSnackbar(View view, String message, String actionLabel, View.OnClickListener clickListener, int duration) {
        if (snackbar != null && snackbar.isShown())
            snackbar.dismiss();
        snackbar = TSnackbar.make(view, message, duration);
        if (clickListener != null) {
            snackbar.setAction(actionLabel != null ? actionLabel : view.getContext().getString(R.string.retry), clickListener);
        }
        snackbar.setActionTextColor(Color.WHITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(view.getContext().getResources().getColor(R.color.colorPrimaryLight));
        TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

    public static void showIndefiniteSnackbar(View view, String message, String actionLabel, View.OnClickListener clickListener) {
        showSnackbar(view, message, actionLabel, clickListener, TSnackbar.LENGTH_INDEFINITE);
    }

    public static void showIndefiniteSnackbar(View view, String message, View.OnClickListener clickListener) {
        showSnackbar(view, message, null, clickListener, TSnackbar.LENGTH_INDEFINITE);
    }

    public static void showIndefiniteNetworkSnackbar(View view, View.OnClickListener clickListener) {
        showIndefiniteSnackbar(view, view.getContext().getString(R.string.network_error_message), null, clickListener);
    }

    public static void showLongNetworkSnackbar(View view, View.OnClickListener clickListener) {
        showSnackbar(view, view.getContext().getString(R.string.network_error_message), null, clickListener, TSnackbar.LENGTH_LONG);
    }

    public static void showLongSnackbar(View view, String message, View.OnClickListener clickListener) {
        showSnackbar(view, message, null, clickListener, TSnackbar.LENGTH_LONG);
    }
}
