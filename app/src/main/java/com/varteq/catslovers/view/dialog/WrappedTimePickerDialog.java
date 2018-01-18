package com.varteq.catslovers.view.dialog;

import android.app.TimePickerDialog;
import android.content.Context;

import com.varteq.catslovers.R;

import java.util.Calendar;
import java.util.Date;

public class WrappedTimePickerDialog {

    public WrappedTimePickerDialog(Context context, Date initDate, TimePickerDialog.OnTimeSetListener listener) {
        // Use the current date as the default date in the picker
        Calendar c = Calendar.getInstance();
        if (initDate != null && initDate.getTime() > 0)
            c.setTime(initDate);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(context, R.style.PrimaryDialog, listener, hours, minutes, true);
        dialog.show();
    }
}
