package com.varteq.catslovers.view.presenter;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.QBCustomObjectsFiles;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.customobjects.model.QBCustomObjectFileField;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.model.QBFeedPost;
import com.varteq.catslovers.utils.ImageUtils;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.view.NewFeedPostActivity;

import java.io.File;

public class NewFeedPostPresenter {

    private String TAG = NewFeedPostPresenter.class.getSimpleName();

    private NewFeedPostActivity view;
    private boolean isPreviewUploaded;
    private QBCustomObject createdObject;
    private Bitmap preview;

    public NewFeedPostPresenter(NewFeedPostActivity view) {
        this.view = view;
    }

    public void createFeed(String message, File mediaFile, Bitmap preview, FeedPost.FeedPostType type) {
        String id = Profile.getUserStation(view);
        if (id.isEmpty()) {
            return;
        }

        this.preview = preview;
        QBFeedPost object = new QBFeedPost(message, id);

        QBCustomObjects.createObject(object).performAsync(new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject createdObject, Bundle params) {
                NewFeedPostPresenter.this.createdObject = createdObject;
                if (type.equals(FeedPost.FeedPostType.PICTURE))
                    attachFile(createdObject, mediaFile, QBFeedPost.PICTURE_FIELD, callback);
                else if (type.equals(FeedPost.FeedPostType.VIDEO)) {
                    attachFile(createdObject, mediaFile, QBFeedPost.VIDEO_FIELD, callback);
                }
                else view.createdSuccessfully();
                Log.d(TAG, "createObject Feeds onSuccess ");
            }

            @Override
            public void onError(QBResponseException errors) {
                Log.e(TAG, "createObject Feeds onError " + errors.getMessage());
            }
        });
    }

    private QBEntityCallback callback = new QBEntityCallback<QBCustomObjectFileField>() {

        @Override
        public void onSuccess(QBCustomObjectFileField uploadFileResult, Bundle params) {
            if (!isPreviewUploaded) {
                isPreviewUploaded = true;
                try {
                    attachFile(createdObject, ImageUtils.saveBitmapToFile(preview, null), QBFeedPost.PREVIEW_FIELD, callback);
                } catch (Exception e) {
                }
            } else view.createdSuccessfully();
            Log.d(TAG, "uploadFile Feeds onSuccess ");
        }

        @Override
        public void onError(QBResponseException errors) {
            Log.e(TAG, "uploadFile Feeds onError " + errors.getMessage());
        }
    };

    private void attachFile(QBCustomObject object, File mediaFile, String field, QBEntityCallback callback) {
        QBCustomObjectsFiles.uploadFile(mediaFile, object, field).performAsync(callback);
    }
}