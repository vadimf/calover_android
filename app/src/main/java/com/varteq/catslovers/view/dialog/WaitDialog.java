package com.varteq.catslovers.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.ViewGroup.LayoutParams;

import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Log;


public class WaitDialog {

    private static String TAG = WaitDialog.class.getSimpleName();

    private Activity activity;
    private Dialog dialog;

    public WaitDialog(Activity _activity) {

        activity = _activity;

        dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setContentView(R.layout.dialog_wait);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        dialog.setCancelable(false);
    }

    /********************************
     * Controls
     ********************************/

    public void showDialog() {
        Log.d(TAG, "showDialog");
        // android bug in {Dialog},
        // android.view.windowmanager$badtokenexception
        if (dialog != null) {
            try {
                dialog.show();
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    public Dialog getDialog() {
        return dialog;
    }

    public boolean isShowing() {
        if (dialog != null) {
            dialog.isShowing();
        }
        return false;
    }

    public void hideDialog() {
        if (dialog != null) {
            dialog.hide();
        }
    }

    public void release() {
        activity = null;
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (activity != null) {
            Log.e(TAG, "finalize: bad release() or not call release()");
        }
    }
}
