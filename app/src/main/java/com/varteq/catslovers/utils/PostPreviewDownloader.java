package com.varteq.catslovers.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.customobjects.QBCustomObjectsFiles;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.model.QBFeedPost;

import java.io.File;
import java.io.InputStream;

public class PostPreviewDownloader {
    private Performer<InputStream> task = null;
    private ImageView view;
    private FeedPost post;
    private boolean isCanceled;
    private AsyncTask<Void, Void, Bitmap> asyncTask;

    public PostPreviewDownloader(ImageView view, FeedPost post) {
        this.view = view;
        this.post = post;

        String fileName;
        String fieldName = QBFeedPost.PICTURE_FIELD;
        if (post.getType().equals(FeedPost.FeedPostType.VIDEO)) {
            fileName = post.getPreviewName();
            fieldName = QBFeedPost.PREVIEW_FIELD;
        } else fileName = post.getMediaName();

        File parentDir = StorageUtils.getAppExternalDataDirectoryFile();
        File imageFile = post.getPreviewFile();

        if (setImage(imageFile)) return;

        for (File file : parentDir.listFiles()) {
            if (file.getName().equals(fileName)) {
                imageFile = file;
                post.setPreviewFile(file);
                break;
            }
        }

        if (isCanceled) return;

        if (setImage(imageFile)) return;

        task = QBCustomObjectsFiles.downloadFile(new QBFeedPost(post.getId()), fieldName);
        task.performAsync(new QBEntityCallback<InputStream>() {
            @Override
            public void onSuccess(InputStream inputStream, Bundle params) {
                saveAndShowImage(inputStream, fileName);
            }

            @Override
            public void onError(QBResponseException errors) {
                int i = 0;
            }
        });
    }

    private void saveAndShowImage(InputStream inputStream, String fileName) {
        asyncTask = new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                try {
                    post.setPreviewFile(ImageUtils.saveBitmapToFile(bitmap, fileName));
                } catch (Exception e) {
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                setImage(bitmap);
            }
        };
        asyncTask.execute();
    }

    private boolean setImage(File imageFile) {
        if (imageFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
            setImage(bitmap);
            return true;
        } else return false;
    }

    private void setImage(Bitmap bitmap) {
        if (!isCanceled)
            view.setImageBitmap(bitmap);
    }

    public void cancelLoading() {
        isCanceled = true;
        if (task != null)
            task.cancel();
        if (asyncTask != null)
            asyncTask.cancel(true);
    }
}
