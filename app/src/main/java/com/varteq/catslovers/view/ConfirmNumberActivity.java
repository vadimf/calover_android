package com.varteq.catslovers.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.varteq.catslovers.R;

public class ConfirmNumberActivity extends AppCompatActivity {

    private EditText numberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_number);

        numberEditText = (EditText) findViewById(R.id.mobile_number_editText);
        numberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE)
                confirmNumber();
            return false;
        });

        findViewById(R.id.confirm_number_button).setOnClickListener(view -> {
            confirmNumber();
        });
    }

    private void confirmNumber() {
        if (isNumberValid())
            startActivity(new Intent(ConfirmNumberActivity.this, ValidateNumberActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } catch (Exception e) {
        }
    }

    private boolean isNumberValid() {
        if (numberEditText.getText() == null ||
                numberEditText.getText().toString().isEmpty()) {
            numberEditText.setError("Number should not be empty");
            return false;
        } else return true;
    }
}
