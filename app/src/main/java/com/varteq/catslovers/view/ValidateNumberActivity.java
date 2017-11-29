package com.varteq.catslovers.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.varteq.catslovers.Auth;
import com.varteq.catslovers.AuthPresenter;
import com.varteq.catslovers.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ValidateNumberActivity extends AppCompatActivity implements TextWatcher {

    private String TAG = ValidateNumberActivity.class.getSimpleName();
    public static String PHONE_NUMBER_KEY = "phone_number";
    private final int MAX_CHARS_COUNT = 1;
    @BindView(R.id.editText1)
    EditText editText1;
    @BindView(R.id.editText2)
    EditText editText2;
    @BindView(R.id.editText3)
    EditText editText3;
    @BindView(R.id.editText4)
    EditText editText4;
    @BindView(R.id.approve_button)
    Button approveButton;
    @BindView(R.id.resend_button)
    Button resendButton;
    private AlertDialog userDialog;

    private List<EditText> editTextList = new ArrayList<>();
    private boolean borderColorApplied;
    private String SHOW_DIALOG_KEY = "show_dialog";
    private boolean isDialogVisible;
    private String username;
    private AuthPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_number);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SHOW_DIALOG_KEY) &&
                    savedInstanceState.getBoolean(SHOW_DIALOG_KEY))
                showSuccessDialog();
        }

        if (getIntent() != null && getIntent().hasExtra(PHONE_NUMBER_KEY))
            username = getIntent().getStringExtra(PHONE_NUMBER_KEY);

        presenter = new AuthPresenter(username, this);
        presenter.resetPassword();

        editText1.addTextChangedListener(this);
        editText2.addTextChangedListener(this);
        editText3.addTextChangedListener(this);
        editText4.addTextChangedListener(this);

        editText4.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE)
                validateCode();
            return false;
        });

        InputFilter[] maxCountFilter = new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS_COUNT)};
        editText1.setFilters(maxCountFilter);
        editText2.setFilters(maxCountFilter);
        editText3.setFilters(maxCountFilter);
        editText4.setFilters(maxCountFilter);

        editTextList.add(editText1);
        editTextList.add(editText2);
        editTextList.add(editText3);
        editTextList.add(editText4);
    }

    @OnClick(R.id.approve_button)
    void validateCode() {
        for (EditText item : editTextList) {
            if (item.getText().length() != MAX_CHARS_COUNT && item.getId() != editText4.getId()) {
                onCodeValidate(false);
                return;
            }
        }
        String code = editText1.getText().toString() + editText2.getText().toString() + editText3.getText().toString();
        presenter.confirmCode(code);
    }

    public void onSuccessSignIn() {
        onCodeValidate(true);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        } catch (Exception e) {
        }

        Auth.setUserLogin(this, true);

        showSuccessDialog();
    }

    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success_login);
        dialog.setCancelable(false);

        Button okButton = dialog.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            ValidateNumberActivity.this.finishAffinity();
            startActivity(new Intent(getApplicationContext(), CatProfileActivity.class));
        });

        Button laterButton = dialog.findViewById(R.id.later_button);
        laterButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
        isDialogVisible = true;
    }

    @OnClick(R.id.resend_button)
    void onResendClick() {
        onBackPressed();
    }

    private void onCodeValidate(boolean isCorrect) {
        int resId;
        if (isCorrect)
            resId = R.drawable.correct_code;
        else {
            resId = R.drawable.incorrect_code;
            resendButton.setVisibility(View.VISIBLE);
        }

        for (EditText item : editTextList)
            item.setBackgroundResource(resId);

        borderColorApplied = true;
    }

    private void clearBorderColor() {
        for (EditText item : editTextList)
            item.setBackgroundResource(R.drawable.default_code);

        borderColorApplied = false;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        int hash = charSequence.hashCode();
        EditText editText = null;
        if (hash == editText1.getText().hashCode())
            editText = editText1;
        else if (hash == editText2.getText().hashCode())
            editText = editText2;
        else if (hash == editText3.getText().hashCode())
            editText = editText3;
        else if (hash == editText4.getText().hashCode())
            editText = editText4;

        onTextChanged(editText, start, count, after);

        if (borderColorApplied)
            clearBorderColor();
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private void onTextChanged(EditText editText, int start, int count, int after) {
        if (editText == null) return;
        int nextViewId = 0;
        if (start + after == MAX_CHARS_COUNT)
            nextViewId = editText.getNextFocusForwardId();
        else if (start == 0 && count == 1)
            nextViewId = editText.getNextFocusLeftId();

        for (EditText item : editTextList) {
            if (item.getId() == nextViewId)
                item.requestFocus();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_DIALOG_KEY, isDialogVisible);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDialogVisible) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } catch (Exception e) {
        }
    }

    public void showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    //
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }
}
