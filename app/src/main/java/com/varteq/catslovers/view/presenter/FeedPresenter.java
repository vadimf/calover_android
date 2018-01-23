package com.varteq.catslovers.view.presenter;

import android.os.Bundle;
import android.os.Handler;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.model.QBFeedPost;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.view.fragments.FeedFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedPresenter {

    private String TAG = FeedPresenter.class.getSimpleName();

    private FeedFragment view;

    public FeedPresenter(FeedFragment view) {
        this.view = view;
    }

    public void loadFeeds() {
        getFeedstationIds();
    }

    private void getFeeds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            view.onError();
            return;
        }

        if (!ChatHelper.getInstance().isLogged()) {
            ChatHelper.getInstance().loginToQuickBlox(view.getContext());
            new Handler().postDelayed(() -> {
                        if (ChatHelper.getInstance().isLogged())
                            getFeeds(ids);
                        else view.onError();
                    },
                    3500);
            return;
        }

        QBRequestGetBuilder requestBuilder = QBFeedPost.getRequestBuilder(ids);

        QBCustomObjects.getObjects(QBFeedPost.CLASS_NAME, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {
                Log.d(TAG, "QBCustomObjects.getObjects onSuccess count: " + (customObjects != null ? customObjects.size() : "null"));
                /*int skip = params.getInt(Consts.SKIP);
                int limit = params.getInt(Consts.LIMIT);*/
                loadUsers(customObjects);
            }

            @Override
            public void onError(QBResponseException errors) {
                view.onError();
                Log.e(TAG, "get Feeds onError " + errors.getMessage());
            }
        });
    }

    private List<FeedPost> from(ArrayList<QBCustomObject> customObjects, ArrayList<QBUser> qbUsers) {
        HashMap<Integer, QBUser> users = new HashMap<>();
        for (QBUser user : qbUsers)
            users.put(user.getId(), user);

        List<FeedPost> feeds = new ArrayList<>();
        for (QBCustomObject object : customObjects) {
            FeedPost feed = QBFeedPost.toFeedPost(object, null, users.get(object.getUserId()).getFullName());
            feeds.add(feed);
        }
        return feeds;
    }

    private void loadUsers(ArrayList<QBCustomObject> customObjects) {
        Set<Integer> userIds = new HashSet<>();
        for (QBCustomObject object : customObjects)
            userIds.add(object.getUserId());
        QBUsers.getUsersByIDs(userIds, null).performAsync(
                new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                        Log.d(TAG, "QBUsers.getUsersByIDs onSuccess count: " + (result != null ? result.size() : "null"));
                        view.feedsLoaded(from(customObjects, result));
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        view.onError();
                        Log.e(TAG, "getUsersByFilter onError " + e.getMessage());
                    }
                });
    }

    public void getFeedstationIds() {

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            //view.feedstationsLoaded(from(data));
                            Log.d(TAG, "getFeedstationIds onSuccess count: " + (data != null ? data.size() : "null"));
                            List<Feedstation> feedstationList = MapPresenter.from(data);
                            if (feedstationList == null) {
                                view.onError();
                                return;
                            }
                            List<String> ids = new ArrayList<>();
                            for (Feedstation feedstation : feedstationList) {
                                if (!feedstation.getIsPublic() && feedstation.getStatus() != null
                                        && feedstation.getStatus().equals(GroupPartner.Status.JOINED))
                                    ids.add(String.valueOf(feedstation.getId()));
                            }
                            Log.d(TAG, "getFeedstationIds onSuccess joined stations count: " + ids.size());
                            getFeeds(ids);
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            view.onError();
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RFeedstation>>> call, Throwable t) {
                view.onError();
                Log.e(TAG, "getFeedstationsIds onFailure " + t.getMessage());
            }
        });
    }

    public void updateFeedPost(FeedPost feedPost) {
        QBFeedPost object = new QBFeedPost(feedPost.getId(), feedPost.getLikes(), feedPost.getMessage(), String.valueOf(feedPost.getStationId()));

        QBCustomObjects.updateObject(object).performAsync(new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject createdObject, Bundle params) {
                int i = 0;
                Log.d(TAG, "updateFeedPost onSuccess ");
            }

            @Override
            public void onError(QBResponseException errors) {
                int i = 0;
                Log.e(TAG, "updateFeedPost onError " + errors.getMessage());
            }
        });
    }
}