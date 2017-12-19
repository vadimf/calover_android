package com.varteq.catslovers.view.presenter;

import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.model.QBFeedPost;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.view.fragments.FeedFragment;

import java.util.ArrayList;
import java.util.List;

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
        if (ids == null || ids.isEmpty()) return;
        QBRequestGetBuilder requestBuilder = QBFeedPost.getRequestBuilder(ids);

        QBCustomObjects.getObjects(QBFeedPost.CLASS_NAME, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {
                /*int skip = params.getInt(Consts.SKIP);
                int limit = params.getInt(Consts.LIMIT);*/
                view.feedsLoaded(from(customObjects));
            }

            @Override
            public void onError(QBResponseException errors) {
                Log.e(TAG, "get Feeds onError " + errors.getMessage());
            }
        });
    }

    private List<FeedPost> from(ArrayList<QBCustomObject> customObjects) {
        List<FeedPost> feeds = new ArrayList<>();
        for (QBCustomObject object : customObjects) {
            FeedPost feed = QBFeedPost.toFeedPost(object, null, null);
            feeds.add(feed);
        }
        return feeds;
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
                            List<String> ids = new ArrayList<>();
                            for (RFeedstation feedstation : data) {
                                if (!feedstation.getIsPublic())
                                    ids.add(String.valueOf(feedstation.getId()));
                            }
                            getFeeds(ids);
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RFeedstation>>> call, Throwable t) {
                Log.e(TAG, "getFeedstationsIds onFailure " + t.getMessage());
            }
        });
    }
}