package com.varteq.catslovers.utils.qb.imagepick;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import com.varteq.catslovers.AppController;
import com.varteq.catslovers.utils.ImageUtils;
import com.varteq.catslovers.utils.constant.SchemeType;
import com.varteq.catslovers.utils.qb.BaseAsyncTask;
import com.varteq.catslovers.view.qb.dialog.ProgressDialogFragment;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetFilepathFromUriTask extends BaseAsyncTask<Intent, Void, List<File>> {

    private WeakReference<FragmentManager> fmWeakReference;
    private OnImagePickedListener listener;
    private int requestCode;

    public GetFilepathFromUriTask(FragmentManager fm, OnImagePickedListener listener, int requestCode) {
        this.fmWeakReference = new WeakReference<>(fm);
        this.listener = listener;
        this.requestCode = requestCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showProgress();
    }

    @Override
    public List<File> performInBackground(Intent... params) throws Exception {
        Intent data = params[0];

        Uri uri = data.getData();
        if (uri != null)
            return Collections.singletonList(getFileFromUri(uri));

        if (data.getClipData() != null) {
            ClipData mClipData = data.getClipData();
            List<File> files = new ArrayList<>();
            for (int i = 0; i < mClipData.getItemCount() && i < 5; i++) {

                ClipData.Item item = mClipData.getItemAt(i);
                uri = item.getUri();
                files.add(getFileFromUri(uri));

            }
            return files;
        }
        return null;
    }

    private File getFileFromUri(Uri uri) throws Exception {
        String imageFilePath = null;
        String uriScheme = uri.getScheme();

        boolean isFromGoogleApp = uri.toString().startsWith(SchemeType.SCHEME_CONTENT_GOOGLE);
        boolean isKitKatAndUpper = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (SchemeType.SCHEME_CONTENT.equalsIgnoreCase(uriScheme) && !isFromGoogleApp && !isKitKatAndUpper) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = AppController.getInstance().getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageFilePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        } else if (SchemeType.SCHEME_FILE.equalsIgnoreCase(uriScheme)) {
            imageFilePath = uri.getPath();
        } else {
            imageFilePath = ImageUtils.saveUriToFile(uri);
        }

        if (TextUtils.isEmpty(imageFilePath)) {
            throw new IOException("Can't find a filepath for URI " + uri.toString());
        }

        return new File(imageFilePath);
    }

    @Override
    public void onResult(List<File> files) {
        hideProgress();
        Log.w(GetFilepathFromUriTask.class.getSimpleName(), "onResult listener = " + listener);
        if (listener != null) {
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(files.get(0).getPath(),
                    MediaStore.Images.Thumbnails.MINI_KIND);
            if (thumb != null)
                listener.onVideoPicked(requestCode, files.get(0), ThumbnailUtils.createVideoThumbnail(files.get(0).getPath(),
                        MediaStore.Images.Thumbnails.MINI_KIND));
            else if (files.size() > 1)
                listener.onImagesPicked(requestCode, files);
            else
                listener.onImagePicked(requestCode, files.get(0));
            /*if (file.getName().endsWith(ImageUtils.IMAGE_FILE_EXTENSION))
                listener.onImagePicked(requestCode, file);
            else if (file.getName().endsWith(ImageUtils.VIDEO_FILE_EXTENSION))
                listener.onVideoPicked(requestCode, file, ThumbnailUtils.createVideoThumbnail(file.getPath(),
                        MediaStore.Images.Thumbnails.MINI_KIND));*/
        }
    }

    @Override
    public void onException(Exception e) {
        hideProgress();
        Log.w(GetFilepathFromUriTask.class.getSimpleName(), "onException listener = " + listener);
        if (listener != null) {
            listener.onImagePickError(requestCode, e);
        }
    }

    private void showProgress() {
        FragmentManager fm = fmWeakReference.get();
        if (fm != null) {
            ProgressDialogFragment.show(fm);
        }
    }

    private void hideProgress() {
        FragmentManager fm = fmWeakReference.get();
        if (fm != null) {
            ProgressDialogFragment.hide(fm);
        }
    }
}
