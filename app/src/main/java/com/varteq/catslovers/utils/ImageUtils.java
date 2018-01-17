package com.varteq.catslovers.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.varteq.catslovers.AppController;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.constant.MimeType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageUtils {

    public static final int IMAGE_OR_VIDEO_REQUEST_CODE = 383;
    public static final int GALLERY_REQUEST_CODE = 183;
    public static final int CAMERA_REQUEST_CODE = 212;

    private static final String CAMERA_FILE_NAME_PREFIX = "CAMERA_";
    public static final String IMAGE_FILE_EXTENSION = ".jpg";
    public static final String VIDEO_FILE_EXTENSION = "mp4";

    private ImageUtils() {
    }

    public static String saveUriToFile(Uri uri) throws Exception {
        ParcelFileDescriptor parcelFileDescriptor = AppController.getInstance().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        InputStream inputStream = new FileInputStream(fileDescriptor);

        return saveStreamToFile(inputStream, parcelFileDescriptor, null).getAbsolutePath();
    }

    public static File saveStreamToFile(InputStream inputStream, ParcelFileDescriptor parcelFileDescriptor, String fileName) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(inputStream);

        File parentDir = StorageUtils.getAppExternalDataDirectoryFile();
        if (fileName == null)
            fileName = String.valueOf(System.currentTimeMillis());
        if (parcelFileDescriptor != null)
            fileName += IMAGE_FILE_EXTENSION;
        /*if (isImageFile(uri.getPath()))
            fileName += IMAGE_FILE_EXTENSION;
        if (isVideoFile(uri.getPath()))
            fileName += VIDEO_FILE_EXTENSION;*/
        File resultFile = new File(parentDir, fileName);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(resultFile));

        byte[] buf = new byte[2048];
        int length;

        try {
            while ((length = bis.read(buf)) > 0) {
                bos.write(buf, 0, length);
            }
        } catch (Exception e) {
            throw new IOException("Can\'t save Storage API bitmap to a file!", e);
        } finally {
            if (parcelFileDescriptor != null)
                parcelFileDescriptor.close();
            bis.close();
            bos.close();
        }

        return resultFile;
    }

    public static File saveBitmapToFile(Bitmap bitmap, String fileName) throws Exception {

        File parentDir = StorageUtils.getAppExternalDataDirectoryFile();
        if (fileName == null)
            fileName = String.valueOf(System.currentTimeMillis());

        File resultFile = new File(parentDir, fileName);

        FileOutputStream outputStream = new FileOutputStream(resultFile);

        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream);
        } catch (Exception e) {
        } finally {
            outputStream.flush();
            outputStream.close();
        }

        return resultFile;
    }

    public static void startImagePicker(Activity activity, boolean isMultiselect) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MimeType.IMAGE_MIME);
        if (isMultiselect)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.dlg_choose_image_from)), GALLERY_REQUEST_CODE);
    }

    public static void startImagePicker(Fragment fragment, boolean isMultiselect) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MimeType.IMAGE_MIME);
        if (isMultiselect)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        fragment.startActivityForResult(Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_image_from)), GALLERY_REQUEST_CODE);
    }

    public static void startImageAndVideoPicker(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(MimeType.IMAGE_MIME);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{MimeType.IMAGE_MIME, MimeType.VIDEO_MIME})
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        fragment.startActivityForResult(intent, IMAGE_OR_VIDEO_REQUEST_CODE);
    }

    public static void startCameraForResult(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) == null) {
            return;
        }

        File photoFile = getTemporaryCameraFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public static void startCameraForResult(Fragment fragment) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(AppController.getInstance().getPackageManager()) == null) {
            return;
        }

        File photoFile = getTemporaryCameraFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getValidUri(photoFile, fragment.getContext()));
        fragment.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public static File getTemporaryCameraFile() {
        File storageDir = StorageUtils.getAppExternalDataDirectoryFile();
        File file = new File(storageDir, getTemporaryCameraFileName());
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File getLastUsedCameraFile() {
        File dataDir = StorageUtils.getAppExternalDataDirectoryFile();
        File[] files = dataDir.listFiles();
        List<File> filteredFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(CAMERA_FILE_NAME_PREFIX)) {
                filteredFiles.add(file);
            }
        }

        Collections.sort(filteredFiles);
        if (!filteredFiles.isEmpty()) {
            return filteredFiles.get(filteredFiles.size() - 1);
        } else {
            return null;
        }
    }

    private static Uri getValidUri(File file, Context context) {
        Uri outputUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String authority = context.getPackageName() + ".provider";
            outputUri = FileProvider.getUriForFile(context, authority, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        return outputUri;
    }

    private static String getTemporaryCameraFileName() {
        return CAMERA_FILE_NAME_PREFIX + System.currentTimeMillis() + ".jpg";
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /*public static boolean isImageFile(File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

        //String mimeType = mimeTypesMap.getContentType(fileName);

        mimeType = mimeTypesMap.getContentType(file);
        return true;
    }*/

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }
}
