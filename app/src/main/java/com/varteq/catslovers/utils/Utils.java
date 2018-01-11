package com.varteq.catslovers.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    //private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()_+-=[]|,./?><";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String PUNCTUATION = "!@#$%&*()_+-=[]|,./?><";

    public static Bitmap getBitmapWithColor(int color) {
        Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        image.eraseColor(color);
        return image;
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
        String[] parts = fullAddress.split(",");
        StringBuilder splitAddress = new StringBuilder();
        
        if (parts.length > partsCount) {
            for (int i = 0; i < partsCount; i++) {
                splitAddress.append(parts[i]);
                if (i != partsCount - 1)
                    splitAddress.append(", ");
            }
        }
        return splitAddress.toString();
    }

}
