package com.varteq.catslovers.view.presenter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;

import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.utils.Log;
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

    public void addGroupPartner(List groupPartnersList, RecyclerView.Adapter groupPartnersAdapter) {
        groupPartnersList.add(1, new GroupPartner(null, "User" + ((int) (Math.random() * 999999) + 111111), false));
        groupPartnersAdapter.notifyItemInserted(1);
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
                            if (error.getCode() == 422)
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
                            if (error.getCode() == 422)
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
