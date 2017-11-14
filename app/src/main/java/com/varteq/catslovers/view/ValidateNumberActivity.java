package com.varteq.catslovers.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.varteq.catslovers.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ValidateNumberActivity extends AppCompatActivity implements TextWatcher {

    private String TAG = ValidateNumberActivity.class.getSimpleName();
    private final int MAX_CHARS_COUNT = 2;
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

    private List<EditText> editTextList = new ArrayList<>();
    private boolean borderColorApplied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_number);

        ButterKnife.bind(this);

        editText1.addTextChangedListener(this);
        editText2.addTextChangedListener(this);
        editText3.addTextChangedListener(this);
        editText4.addTextChangedListener(this);

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
            if (item.getText().length() != MAX_CHARS_COUNT) {
                onCodeValidate(false);
                return;
            }
        }
        onCodeValidate(true);
        //TODO: validateRequest
    }

    private void onCodeValidate(boolean isCorrect) {
        int resId;
        if (isCorrect)
            resId = R.drawable.correct_code;
        else resId = R.drawable.incorrect_code;

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
}
