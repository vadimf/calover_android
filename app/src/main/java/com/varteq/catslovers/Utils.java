package com.varteq.catslovers;

import android.graphics.Bitmap;

public class Utils {

    public static Bitmap getBitmapWithColor(int color) {
        Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        image.eraseColor(color);
        return image;
    }
}
