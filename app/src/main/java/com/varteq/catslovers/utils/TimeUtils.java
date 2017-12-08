package com.varteq.catslovers.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    private TimeUtils() {
    }

    public static String getTime(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds));
    }

    public static String getDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds));
    }

    public static long getDateAsHeaderId(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        return Long.parseLong(dateFormat.format(new Date(milliseconds)));
    }

    public static String getDateAsDDMMYYYY(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds));
    }

    public static long getTimeInMillis(int year, int month, int monthDay) {
        Calendar birthday = Calendar.getInstance();
        birthday.set(Calendar.YEAR, year);
        birthday.set(Calendar.MONTH, month);
        birthday.set(Calendar.DAY_OF_MONTH, monthDay);
        birthday.set(Calendar.MILLISECOND, 0);
        return birthday.getTimeInMillis();
    }

    public static Date getDateFromUTC(int sec) {
        return new Date(sec * 1000L);
    }
}
