package com.varteq.catslovers.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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

    public static String getDateAsEEEMMddyyyyHHmmaa(long milis) {
        if (milis <= 0) return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MM-dd-yyyy HH:mmaa", Locale.getDefault());
        return dateFormat.format(new Date(milis));
    }

    public static String getDateAsddMMMyyyy(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getDateAsHHmm(Date date) {
        if (date == null) return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getDateAsHHmmInUTC(Date date) {
        if (date == null) return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

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

    public static Date getTimeInMillis(int hours, int minutes) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hours);
        date.set(Calendar.MINUTE, minutes);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private static long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    public static int getUtcDayStartOffset(Date date) {
        return (int) ((date.getTime() % MILLIS_IN_DAY) / 1000L);
    }

    public static Date getLocalTimeFromDayStartOffset(Integer seconds) {
        if (seconds == null)
            return null;
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, seconds);
        return date.getTime();
    }

    public static Date getLocalDateFromUtc(Integer seconds) {
        if (seconds == null)
            return null;
        long millis = ((long) seconds * 1000L);
        return new Date(millis + TimeZone.getDefault().getOffset(millis));
    }

    public static int getUtcFromLocal(long millis) {
        return (int) ((millis - TimeZone.getDefault().getOffset(millis)) / 1000L);
    }

    public static long calculatePassedMonths(Calendar fromDate, Calendar toDate) {
        long timePassedDays = (TimeUnit.MILLISECONDS.toDays(toDate.getTimeInMillis() - fromDate.getTimeInMillis()));
        long timePassedMonths = timePassedDays / 30;

        // maximum approximate formula to calculate passed months
        if (timePassedMonths > 1) {
            int birthdayMonth = fromDate.get(Calendar.MONTH);
            int currentMonth = toDate.get(Calendar.MONTH);
            int birthdayYear = fromDate.get(Calendar.YEAR);
            int currentYear = toDate.get(Calendar.YEAR);
            timePassedMonths = (currentYear - birthdayYear) * 12 + (currentMonth - birthdayMonth);
            if (toDate.get(Calendar.DAY_OF_MONTH) + (30 - fromDate.get(Calendar.DAY_OF_MONTH)) < 30)
                timePassedMonths--;
        }
        return timePassedMonths;
    }
}