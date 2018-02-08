package com.varteq.catslovers.view.dialog;

import android.app.DatePickerDialog;
import android.content.Context;

import com.varteq.catslovers.R;

import java.util.Calendar;

public class WrappedDatePickerDialog {

    public WrappedDatePickerDialog(Context context, Long initDate, Long minDate, DatePickerDialog.OnDateSetListener listener) {
        // Use the current date as the default date in the picker
        Calendar c = Calendar.getInstance();
        if (initDate != null && initDate > 0)
            c.setTimeInMillis(initDate);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        //AlertDialog.THEME_HOLO_LIGHT android.R.style.Theme_DeviceDefault_Light_Dialog
        DatePickerDialog dialog = new DatePickerDialog(context, R.style.PrimaryDialog, listener, year, month, day);
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        if (minDate != null && minDate >= 0)
            dialog.getDatePicker().setMinDate(minDate);
        dialog.show();
    }
}
