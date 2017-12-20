package com.varteq.catslovers.view.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.varteq.catslovers.model.QBFeedPost;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.view.MainActivity;
import com.varteq.catslovers.view.NewFeedPostActivity;

import java.util.List;

public class NewFeedPostPresenter {

    private String TAG = NewFeedPostPresenter.class.getSimpleName();

    private NewFeedPostActivity view;

    public NewFeedPostPresenter(NewFeedPostActivity view) {
        this.view = view;
    }

    public void createFeed(Context context, String message, Uri imageUri) {
        //TODO create logic of image
        String id = Profile.getUserStation(view);
        if (id.isEmpty()) {
            return;
        }

        QBFeedPost object = new QBFeedPost(message, id);

        QBCustomObjects.createObject(object).performAsync(new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject createdObject, Bundle params) {
                ((NewFeedPostActivity) context).finish();
                Log.d(TAG, "createObject Feeds onSuccess ");
            }

            @Override
            public void onError(QBResponseException errors) {
                Toast.makeText(context, "Error! New feed wasn't added", Toast.LENGTH_LONG);
                Log.e(TAG, "createObject Feeds onError " + errors.getMessage());
            }
        });
    }

    public void onPetImageSelected(Uri uri, List photoList, RecyclerView.Adapter photosAdapter) {
        if (uri != null) {
            Log.d(TAG, "onImageSelected " + uri);
            photoList.add(0, uri);
            photosAdapter.notifyItemInserted(0);
        }
    }
}