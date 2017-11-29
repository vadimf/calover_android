package com.varteq.catslovers;

import android.graphics.Bitmap;

import java.util.Random;

public class Utils {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()_+-=[]|,./?><";

    public static Bitmap getBitmapWithColor(int color) {
        Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        image.eraseColor(color);
        return image;
    }

    public static String getPass(int length) {
        Random random = new Random(System.nanoTime());
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int position = random.nextInt(CHARS.length());
            password.append(CHARS.charAt(position));
        }
        return new String(password);
    }
}
