package com.varteq.catslovers.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;

import com.varteq.catslovers.AppController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Utils {

    //private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()_+-=[]|,./?><";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String PUNCTUATION = "!@#$%&*()_+-=[]|,./?><";

    public static Bitmap getBitmapWithColor(int color) {
        Bitmap bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(color);
        return bmp;
    }

    public static String getPass(int length) {
        List<String> charCategories = new ArrayList<>(4);
        charCategories.add(LOWER);
        charCategories.add(UPPER);
        charCategories.add(DIGITS);
        charCategories.add(PUNCTUATION);

        Random random = new Random(System.nanoTime());
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            String charCategory = charCategories.get(random.nextInt(charCategories.size()));
            int position = random.nextInt(charCategory.length());
            password.append(charCategory.charAt(position));
        }
        return new String(password);
    }

    public static int convertDpToPx(float dp, Context context) {
        float factor = context.getResources().getDisplayMetrics().density;
        return (int) (dp * factor);
    }

    public static boolean isStringNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isStringNumericPositive(String str) {
        if (isStringNumeric(str) && Float.parseFloat(str) > 0)
            return true;
        else
            return false;
    }

    public static String splitAddress(String fullAddress, int partsCount) {
        if (fullAddress == null)
            return "";
        String[] parts = fullAddress.split(",");
        StringBuilder splitAddress = new StringBuilder();

        if (parts.length > partsCount) {
            for (int i = 0; i < partsCount; i++) {
                splitAddress.append(parts[i]);
                if (i != partsCount - 1)
                    splitAddress.append(", ");
            }
            return splitAddress.toString();
        } else {
            return fullAddress;
        }
    }

    public static String getAddressByLocation(double latitute, double longitute, Context context) {
        Geocoder geocoder;
        List<Address> addresses = null;
        String address = null;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitute, longitute, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && !addresses.isEmpty())
            address = addresses.get(0).getAddressLine(0);
        else
            return null;

        return Utils.splitAddress(address, 3);
    }

    public static Bitmap drawTextOnBitmap(Bitmap originalBitmap, String text, int color, float textSize) {
        float scale = AppController.getInstance().getResources().getDisplayMetrics().density;
        Bitmap.Config bitmapConfig =
                originalBitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        originalBitmap = originalBitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(originalBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize((int) (textSize * scale));
        paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = ((originalBitmap.getWidth() - bounds.width()) / 2) - 3;
        int y = (originalBitmap.getHeight() + bounds.height()) / 2;

        canvas.drawText(text, x, y, paint);
        return originalBitmap;
    }

    public static double roundDouble(double d, int precise) {
        precise = 10 ^ precise;
        d = d * precise;
        int i = (int) Math.round(d);
        return (double) i / precise;
    }

}
