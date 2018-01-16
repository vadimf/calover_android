package com.varteq.catslovers.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Log;

public class PopupDialog {

    private static String TAG = PopupDialog.class.getSimpleName();

    private Activity activity;
    private Dialog dialog;

    public enum Popup {
        THANK_YOU_FOR_SHARING, ANTI_FLEA_EXPIRES, ERROR, THANK_YOU_FOR_YOUR_UPDATE, FIDDING, SEE_YOU, FIXING
    }

    public PopupDialog(Activity activity, Popup popupType, boolean isCancelable, View.OnClickListener onButtonClickListener) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.dialog_popup);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(isCancelable);

        Button button = dialog.findViewById(R.id.button_ok);
        TextView messageTextView = dialog.findViewById(R.id.textView_message);
        TextView additionalMessageTextView = dialog.findViewById(R.id.textView_additional_message);
        ImageView imageView = dialog.findViewById(R.id.imageView);
        button.setOnClickListener(view -> onButtonClick(button, onButtonClickListener));
        switch (popupType) {
            case THANK_YOU_FOR_SHARING:
                messageTextView.setText(R.string.thank_you_for_sharing);
                additionalMessageTextView.setVisibility(View.GONE);
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.popup_sharing));
                break;
            case ANTI_FLEA_EXPIRES:
                messageTextView.setText(R.string.anti_flea_expires);
                additionalMessageTextView.setVisibility(View.GONE);
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.popup_flea));
                break;
            case ERROR:
                messageTextView.setText(R.string.small_error_occured);
                additionalMessageTextView.setVisibility(View.VISIBLE);
                additionalMessageTextView.setText(R.string.opssss);
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.popup_upps_try_again));
                break;
            case THANK_YOU_FOR_YOUR_UPDATE:
                messageTextView.setText(R.string.thank_you_for_update);
                additionalMessageTextView.setVisibility(View.GONE);
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.popup_thanks));
                break;
            case FIDDING:
                messageTextView.setText(R.string.tasty);
                additionalMessageTextView.setVisibility(View.GONE);
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.popup_food));
                break;
            case SEE_YOU:
                messageTextView.setText(R.string.see_you_again_soon);
                additionalMessageTextView.setVisibility(View.GONE);
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.popup_see_you));
                break;
            case FIXING:
                messageTextView.setText(R.string.we_are_fixing_it);
                additionalMessageTextView.setVisibility(View.GONE);
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.popup_fixing));
                break;
        }

    }

    /********************************
     * Controls
     ********************************/

    public void showDialog() {
        Log.d(TAG, "showDialog");
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

    public void onButtonClick(View view, View.OnClickListener onClickListener) {
        onClickListener.onClick(view);
        hideDialog();
        release();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (activity != null) {
            Log.e(TAG, "finalize: bad release() or not call release()");
        }
    }

}
