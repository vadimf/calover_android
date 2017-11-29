package com.varteq.catslovers;

import android.content.Context;
import android.graphics.Bitmap;

public class Utils {

    public static Bitmap getBitmapWithColor(int color) {
        Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        image.eraseColor(color);
        return image;
    }

    public static int convertDpToPx(float dp, Context context){
        float factor = context.getResources().getDisplayMetrics().density;
        return (int) (dp * factor);
    }

}
