package com.varteq.catslovers;

import android.app.DatePickerDialog;
import android.content.Context;

import java.util.Calendar;

public class DatePickerFragment {

    public DatePickerFragment(Context context, DatePickerDialog.OnDateSetListener listener) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(context, listener, year, month, day);
        dialog.show();
    }
}
