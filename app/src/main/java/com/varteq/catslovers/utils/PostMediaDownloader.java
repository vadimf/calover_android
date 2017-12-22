package com.varteq.catslovers.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.customobjects.QBCustomObjectsFiles;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.model.QBFeedPost;

import java.io.File;
import java.io.InputStream;

public class PostMediaDownloader {
    private final File parentDir;
    private Performer<InputStream> task = null;
    private boolean isCanceled;
    private AsyncTask<Void, Void, File> asyncTask;
    private String mediaName;
    private FeedPost.FeedPostType mediaType;
    private OnMediaLoaded listener;

    public PostMediaDownloader(FeedPost post, OnMediaLoaded listener) {
        this.listener = listener;
        mediaType = post.getType();
        mediaName = post.getMediaName();
        String fieldName = null;
        if (mediaType.equals(FeedPost.FeedPostType.PICTURE))
            fieldName = QBFeedPost.PICTURE_FIELD;
        else
            fieldName = QBFeedPost.VIDEO_FIELD;

        parentDir = StorageUtils.getAppExternalDataDirectoryFile();
        File mediaFile = null;

        for (File file : parentDir.listFiles()) {
            if (file.getName().equals(mediaName)) {
                mediaFile = file;
                break;
            }
        }

        if (isCanceled) return;

        if (mediaType.equals(FeedPost.FeedPostType.PICTURE)) {
            setImage(mediaFile);
            return;
        } else if (mediaFile != null) {
            listener.onVideoLoaded(mediaFile);
            return;
        }

        task = QBCustomObjectsFiles.downloadFile(new QBFeedPost(post.getId()), fieldName);
        task.performAsync(new QBEntityCallback<InputStream>() {
            @Override
            public void onSuccess(InputStream inputStream, Bundle params) {
                saveAndShowImage(inputStream, mediaName);
            }

            @Override
            public void onError(QBResponseException errors) {
                int i = 0;
            }
        });
    }

    private void saveAndShowImage(InputStream inputStream, String fileName) {
        asyncTask = new AsyncTask<Void, Void, File>() {

            @Override
            protected File doInBackground(Void... voids) {
                try {
                    return ImageUtils.saveStreamToFile(inputStream, null, fileName);
                } catch (Exception e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(File file) {
                listener.onVideoLoaded(file);
            }
        };
        asyncTask.execute();
    }

    private boolean setImage(File imageFile) {
        if (imageFile != null && mediaType.equals(FeedPost.FeedPostType.PICTURE)) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
            listener.onImageLoaded(bitmap);
            return true;
        } else return false;
    }

    public void cancelLoading() {
        isCanceled = true;
        if (task != null)
            task.cancel();
        if (asyncTask != null && asyncTask.getStatus()
                .equals(AsyncTask.Status.RUNNING)) {
            asyncTask.cancel(true);

            if (parentDir == null) return;

            for (File file : parentDir.listFiles()) {
                if (file.getName().equals(mediaName)) {
                    file.delete();
                    break;
                }
            }
        }
    }

    public interface OnMediaLoaded {
        void onImageLoaded(Bitmap bitmap);

        void onVideoLoaded(File file);
    }
}
