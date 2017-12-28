package com.varteq.catslovers.view.presenter;

import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RUser;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.view.MainActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter {

    private String TAG = MainPresenter.class.getSimpleName();

    private MainActivity view;

    public MainPresenter(MainActivity view) {
        this.view = view;
    }

    public void checkMyPrivateFeedstation() {
        String id = Profile.getUserStation(view);
        if (!id.isEmpty()) {
            view.showNewFeedPostActivity();
            return;
        }

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            for (RFeedstation feedstation : data) {
                                if (!feedstation.getIsPublic() && feedstation.getPermissions() != null && feedstation.getPermissions().getRole().equals("admin")) {
                                    Profile.setUserStation(view, String.valueOf(feedstation.getId()));
                                    view.showNewFeedPostActivity();
                                    return;
                                }
                            }
                            view.onPrivateFeedstationNotFound();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RFeedstation>>> call, Throwable t) {
            }
        });
    }

    public void checkInvitations() {

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getInvitations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            view.invitationsLoaded(MapPresenter.from(data));
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RFeedstation>>> call, Throwable t) {
            }
        });
    }

    public void joinFeedstation(Integer feedstationId) {

        if (feedstationId == null) return;

        Call<BaseResponse<RUser>> call = ServiceGenerator.getApiServiceWithToken().joinFeedstation(feedstationId);
        call.enqueue(new Callback<BaseResponse<RUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUser>> call, Response<BaseResponse<RUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RUser>(response) {

                        @Override
                        protected void onSuccess(RUser data) {
                            view.onSuccessJoin();
                            checkInvitations();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RUser>> call, Throwable t) {
                Log.e(TAG, "joinFeedstation onFailure " + t.getMessage());
            }
        });
    }

    public void leaveFeedstation(Integer feedstationId) {

        if (feedstationId == null) return;

        Call<BaseResponse<ErrorData>> call = ServiceGenerator.getApiServiceWithToken().leaveFeedstation(feedstationId);
        call.enqueue(new Callback<BaseResponse<ErrorData>>() {
            @Override
            public void onResponse(Call<BaseResponse<ErrorData>> call, Response<BaseResponse<ErrorData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<ErrorData>(response) {

                        @Override
                        protected void onSuccess(ErrorData data) {
                            checkInvitations();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<ErrorData>> call, Throwable t) {
                Log.e(TAG, "leaveFeedstation onFailure " + t.getMessage());
            }
        });
    }
}