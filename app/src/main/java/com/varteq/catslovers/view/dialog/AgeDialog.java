package com.varteq.catslovers.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.Toaster;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AgeDialog {

    @BindView(R.id.title_TextView)
    TextView textView;
    @BindView(R.id.year_numberPicker)
    NumberPicker yearNumberPicker;
    @BindView(R.id.months_numberPicker)
    NumberPicker monthsNumberPicker;
    @BindView(R.id.dialog_ok_button)
    Button positiveButton;
    @BindView(R.id.dialog_cancel_button)
    Button negativeButton;

    AlertDialog.Builder builder;
    AlertDialog dialog;

    private int MAX_YEAR = 20;
    private int MAX_MONTH = 11;

    public AgeDialog(Context context, Date birthday, AgeDialog.OnClickListener onClickListener) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_age, null, false);
        ButterKnife.bind(this, dialogView);

        //setTitleText(titleText);
        positiveButton.setOnClickListener(view -> {
            if (yearNumberPicker.getValue() == 0 && monthsNumberPicker.getValue() == 0) {
                Toaster.shortToast("Min age is 1 month");
                monthsNumberPicker.setValue(1);
                return;
            }
            onClickListener.onAgeChanged(getBirthdayDate(yearNumberPicker.getValue(), monthsNumberPicker.getValue()));
            dialog.dismiss();
        });
        negativeButton.setOnClickListener(view -> dialog.dismiss());

        yearNumberPicker.setMinValue(0);
        yearNumberPicker.setMaxValue(MAX_YEAR);
        monthsNumberPicker.setMinValue(0);
        monthsNumberPicker.setMaxValue(MAX_MONTH);

        initPickers(birthday);

        builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        dialog = builder.show();
    }

    private void initPickers(Date date) {
        if (date == null || date.getTime() == 0) {
            yearNumberPicker.setValue(1);
            monthsNumberPicker.setValue(1);
            return;
        }
        Calendar currentDate = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        long timePassedMonths = TimeUtils.calculatePassedMonths(calendar, currentDate);

        int years = ((int) timePassedMonths) / 12;
        int month = ((int) timePassedMonths) - (years * 12);

        int year = Math.min(MAX_YEAR, Math.max(0, years));
        yearNumberPicker.setValue(year);
        int months = Math.min(MAX_MONTH, Math.max(0, month));
        monthsNumberPicker.setValue(months);
    }

    private Date getBirthdayDate(int years, int months) {
        Calendar currentDate = Calendar.getInstance();
        int month = currentDate.get(Calendar.MONTH) - months;
        int year = currentDate.get(Calendar.YEAR) - years;
        currentDate.set(Calendar.MONTH, month);
        currentDate.set(Calendar.YEAR, year);
        return currentDate.getTime();
    }

    public void setTitleText(String textViewText) {
        textView.setText(textViewText);
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void show() {
        dialog = builder.show();
    }

    public interface OnClickListener {

        void onAgeChanged(Date birthday);
    }

}
