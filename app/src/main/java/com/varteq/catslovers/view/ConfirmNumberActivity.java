package com.varteq.catslovers.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hbb20.CountryCodePicker;
import com.varteq.catslovers.Log;
import com.varteq.catslovers.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.varteq.catslovers.view.ValidateNumberActivity.PHONE_NUMBER_KEY;

public class ConfirmNumberActivity extends AppCompatActivity {

    private String TAG = ConfirmNumberActivity.class.getSimpleName();
    @BindView(R.id.mobile_number_editText)
    EditText numberEditText;
    @BindView(R.id.countryCodePicker)
    CountryCodePicker countryCodePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_number);
        ButterKnife.bind(this);

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
        countryCodePicker.setCountryForNameCode("ua");
    }

    private void confirmNumber() {
        if (isNumberValid()) {
            Log.d(TAG, "confirmNumber");
            Intent intent = new Intent(ConfirmNumberActivity.this, ValidateNumberActivity.class);
            String number = countryCodePicker.getSelectedCountryCodeWithPlus() + numberEditText.getText().toString();
            intent.putExtra(PHONE_NUMBER_KEY, number);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestFocus(numberEditText);
    }

    private boolean isNumberValid() {
        String number = countryCodePicker.getSelectedCountryCodeWithPlus() + numberEditText.getText().toString();
        if (numberEditText.getText() == null ||
                number.isEmpty()) {
            numberEditText.setError("Number should not be empty");
            return false;
        } else if (number.length() < 11 ||
                !Patterns.PHONE.matcher(number).matches()) {
            numberEditText.setError("Incorrect number");
            return false;
        } else return true;
    }


    private void requestFocus(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
