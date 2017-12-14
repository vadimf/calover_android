package com.varteq.catslovers.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.varteq.catslovers.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditTextDialog {

    @BindView(R.id.dialogTextView)
    TextView textView;
    @BindView(R.id.dialogEditText)
    EditText editText;
    @BindView(R.id.dialog_ok_button)
    Button positiveButton;
    @BindView(R.id.dialog_cancel_button)
    Button negativeButton;

    AlertDialog.Builder builder;
    AlertDialog dialog;

    public EditTextDialog(Context context, String titleText, String inputHint, String inputText, Integer inputType, EditTextDialog.OnClickListener onClickListener) {

        onClickListener.setEditTextDialog(this);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, null, false);
        ButterKnife.bind(this, dialogView);


        setTitleText(titleText);
        setEditTextHint(inputHint);
        setEditTextText(inputText);
        setEditTextInputType(inputType);
        positiveButton.setOnClickListener(view -> onClickListener.onPositiveButtonClick());
        negativeButton.setOnClickListener(view -> onClickListener.onNegativeButtonClick());

        builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

    }

    public EditTextDialog(Context context, String titleText, String inputHint, EditTextDialog.OnClickListener onClickListener) {
        this(context, titleText, inputHint, null, null, onClickListener);
    }

    public void setEditTextInputType(Integer editTextInputType) {
        if (editTextInputType != null)
            editText.setInputType(editTextInputType);
    }

    public void setEditTextHint(String editTextHint) {
      editText.setHint(editTextHint);
    }

    public void setTitleText(String textViewText) {
        textView.setText(textViewText);
    }

    public void setEditTextText(String editTextText) {
       editText.setText(editTextText);
    }


    public void dismiss() {
        dialog.dismiss();
    }


    public void show() {
       dialog = builder.show();
    }

    public TextView getTextView() {
        return textView;
    }

    public EditText getEditText() {
        return editText;
    }

    public Button getPositiveButton() {
        return positiveButton;
    }

    public Button getNegativeButton() {
        return negativeButton;
    }


    public static abstract class OnClickListener {
        private EditTextDialog editTextDialog;

        public abstract void onPositiveButtonClick();

        public void onNegativeButtonClick() {
            dismiss();
        }

        public void dismiss() {
            editTextDialog.dismiss();
        }

        public void setEditTextDialog(EditTextDialog editTextDialog) {
            this.editTextDialog = editTextDialog;
        }

        public EditTextDialog getEditTextDialog(){
            return editTextDialog;
        }
    }


}
