package com.varteq.catslovers.view.presenter;

import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.varteq.catslovers.model.QBFeedPost;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.view.NewFeedPostActivity;

public class NewFeedPostPresenter {

    private String TAG = NewFeedPostPresenter.class.getSimpleName();

    private NewFeedPostActivity view;

    public NewFeedPostPresenter(NewFeedPostActivity view) {
        this.view = view;
    }

    public void createFeed(String message) {

        String id = Profile.getUserStation(view);
        if (id.isEmpty()) {
            return;
        }

        QBFeedPost object = new QBFeedPost(message, id);

        QBCustomObjects.createObject(object).performAsync(new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject createdObject, Bundle params) {
                Log.d(TAG, "createObject Feeds onSuccess ");
            }

            @Override
            public void onError(QBResponseException errors) {
                Log.e(TAG, "createObject Feeds onError " + errors.getMessage());
            }
        });
    }
}