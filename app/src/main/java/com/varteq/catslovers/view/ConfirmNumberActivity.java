package com.varteq.catslovers.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.varteq.catslovers.Log;
import com.varteq.catslovers.R;

import static com.varteq.catslovers.view.ValidateNumberActivity.PHONE_NUMBER_KEY;

public class ConfirmNumberActivity extends AppCompatActivity {

    private String TAG = ConfirmNumberActivity.class.getSimpleName();
    private EditText numberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_number);

        Log.d(TAG, "onCreate");

        numberEditText = findViewById(R.id.mobile_number_editText);
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
        if (isNumberValid()) {
            Log.d(TAG, "confirmNumber");
            Intent intent = new Intent(ConfirmNumberActivity.this, ValidateNumberActivity.class);
            intent.putExtra(PHONE_NUMBER_KEY, numberEditText.getText().toString());
            startActivity(intent);
        }
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
        } else if (numberEditText.getText().length() < 11 ||
                !Patterns.PHONE.matcher(numberEditText.getText()).matches()) {
            numberEditText.setError("Incorrect number");
            return false;
        } else return true;
    }
}
