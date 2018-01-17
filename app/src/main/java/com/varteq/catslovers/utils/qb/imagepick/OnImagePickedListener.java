package com.varteq.catslovers.utils.qb.imagepick;

import android.graphics.Bitmap;

import java.io.File;
import java.util.List;

public interface OnImagePickedListener {

    void onImagePicked(int requestCode, File file);

    void onImagesPicked(int requestCode, List<File> file);

    void onVideoPicked(int requestCode, File file, Bitmap preview);

    void onImagePickError(int requestCode, Exception e);

    void onImagePickClosed(int requestCode);
}
