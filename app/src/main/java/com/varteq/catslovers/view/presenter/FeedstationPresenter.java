package com.varteq.catslovers.view.presenter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;

import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RUser;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.FeedstationActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedstationPresenter {

    private String TAG = FeedstationPresenter.class.getSimpleName();

    private FeedstationActivity view;

    public FeedstationPresenter(FeedstationActivity view) {
        this.view = view;
    }

    public void onPetImageSelected(Uri uri, List photoList, RecyclerView.Adapter photosAdapter) {
        if (uri != null) {
            Log.d(TAG, "onImageSelected " + uri);
            photoList.add(0, uri);
            photosAdapter.notifyItemInserted(0);
        }
    }

    public void saveFeedstation(Feedstation feedstation) {

        Call<BaseResponse<RFeedstation>> call = ServiceGenerator.getApiServiceWithToken().createFeedstation(feedstation.getName(),
                feedstation.getAddress(), feedstation.getDescription(), 0,
                feedstation.getLocation().latitude, feedstation.getLocation().longitude);


        call.enqueue(new Callback<BaseResponse<RFeedstation>>() {
            @Override
            public void onResponse(Call<BaseResponse<RFeedstation>> call, Response<BaseResponse<RFeedstation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RFeedstation>(response) {

                        @Override
                        protected void onSuccess(RFeedstation data) {
                            view.savedSuccessfully();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                            if (error != null && error.getCode() == 422)
                                Toaster.longToast("You should fill station data");
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RFeedstation>> call, Throwable t) {
                Log.e(TAG, "createFeedstation onFailure " + t.getMessage());
            }
        });
    }

    public void addGroupPartner(Integer feedstationId, String name, String phone, List groupPartnersList, RecyclerView.Adapter groupPartnersAdapter) {
        phone = phone.replace(" ", "");
        phone = phone.replace("(", "");
        phone = phone.replace(")", "");

        Call<BaseResponse<RUser>> call = ServiceGenerator.getApiServiceWithToken().inviteUserToFeedstation(feedstationId, phone);

        call.enqueue(new Callback<BaseResponse<RUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUser>> call, Response<BaseResponse<RUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RUser>(response) {

                        @Override
                        protected void onSuccess(RUser data) {
                            GroupPartner.Status status = GroupPartner.Status.DEFAULT;
                            if (data.getStatus() != null && data.getStatus().equals("invited"))
                                status = GroupPartner.Status.PENDING;

                            groupPartnersList.add(1, new GroupPartner(null, name, status, false));
                            groupPartnersAdapter.notifyItemInserted(1);
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
                Log.e(TAG, "inviteUser onFailure " + t.getMessage());
            }
        });
    }

    public void getGroupPartners(Integer feedstationId, List groupPartnersList, RecyclerView.Adapter groupPartnersAdapter) {

        Call<BaseResponse<List<RUser>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstationUsers(feedstationId);

        call.enqueue(new Callback<BaseResponse<List<RUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RUser>>> call, Response<BaseResponse<List<RUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RUser>>(response) {

                        @Override
                        protected void onSuccess(List<RUser> data) {
                            for (RUser user : data) {
                                GroupPartner.Status status = GroupPartner.Status.DEFAULT;
                                if (user.getStatus() != null && user.getStatus().equals("invited"))
                                    status = GroupPartner.Status.PENDING;
                                if (user.getPhone().equals(Profile.getUserPhone(view)))
                                    continue;

                                groupPartnersList.add(1, new GroupPartner(null, user.getPhone(), status, false));
                                groupPartnersAdapter.notifyItemInserted(1);
                            }
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
            public void onFailure(Call<BaseResponse<List<RUser>>> call, Throwable t) {
                Log.e(TAG, "getGroupPartners onFailure " + t.getMessage());
            }
        });
    }

    public void updateFeedstation(Feedstation feedstation) {

        Call<BaseResponse<RFeedstation>> call = ServiceGenerator.getApiServiceWithToken().updateFeedstation(feedstation.getId(), feedstation.getName(),
                feedstation.getAddress(), feedstation.getDescription(), 0,
                feedstation.getLocation().latitude, feedstation.getLocation().longitude);


        call.enqueue(new Callback<BaseResponse<RFeedstation>>() {
            @Override
            public void onResponse(Call<BaseResponse<RFeedstation>> call, Response<BaseResponse<RFeedstation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RFeedstation>(response) {

                        @Override
                        protected void onSuccess(RFeedstation data) {
                            view.savedSuccessfully();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                            if (error != null && error.getCode() == 422)
                                Toaster.longToast("You should fill station data");
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RFeedstation>> call, Throwable t) {
                Log.e(TAG, "updateFeedstation onFailure " + t.getMessage());
            }
        });
    }
}
