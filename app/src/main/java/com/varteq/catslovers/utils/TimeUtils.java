package com.varteq.catslovers.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    private TimeUtils() {
    }

    public static String getTime(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds));
    }

    public static String getDateAsMMMMdd(long milliseconds) {
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

    public static String getDateAsMMMMddHHmm(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static long getTimeInMillis(int year, int month, int monthDay) {
        Calendar birthday = Calendar.getInstance();
        birthday.set(Calendar.YEAR, year);
        birthday.set(Calendar.MONTH, month);
        birthday.set(Calendar.DAY_OF_MONTH, monthDay);
        birthday.set(Calendar.MILLISECOND, 0);
        return birthday.getTimeInMillis();
    }

    public static Date getLocalDateFromUtc(Integer seconds) {
        if (seconds == null)
            return null;
        long millis = ((long) seconds * 1000);
        return new Date(millis + TimeZone.getDefault().getOffset(millis));
    }

    public static int getUtcFromLocal(long millis) {
        return (int) ((millis - TimeZone.getDefault().getOffset(millis)) / 1000L);
    }
}