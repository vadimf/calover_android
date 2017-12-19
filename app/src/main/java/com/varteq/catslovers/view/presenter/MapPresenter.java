package com.varteq.catslovers.view.presenter;

import com.google.android.gms.maps.model.LatLng;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.view.fragments.MapFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapPresenter {

    private String TAG = MapPresenter.class.getSimpleName();

    private MapFragment view;

    public MapPresenter(MapFragment view) {
        this.view = view;
    }

    public void getFeedstations() {

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            view.feedstationsLoaded(from(data));
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
                Log.e(TAG, "getFeedstations onFailure " + t.getMessage());
            }
        });
    }

    private List<Feedstation> from(List<RFeedstation> data) {
        List<Feedstation> list = new ArrayList<>();
        for (RFeedstation station : data) {
            Feedstation feedstation = new Feedstation();
            feedstation.setId(station.getId());
            feedstation.setName(station.getName());
            feedstation.setCreatedUserId(station.getCreated());
            feedstation.setAddress(station.getAddress());
            feedstation.setDescription(station.getDescription());
            if (station.getIsPublic())
                feedstation.setTimeToFeed(TimeUtils.getLocalDateFromUtc(station.getTimeToFeed()));
            if (station.getLat() != null && station.getLng() != null)
                feedstation.setLocation(new LatLng(station.getLat(), station.getLng()));
            feedstation.setIsPublic(station.getIsPublic());
/*if (!station.getIsPublic() && Profile.getUserId(view.getContext()).equals(station.getCreated())){
    Profile.setUserStation(view.getContext(), String.valueOf(station.getId()));
}*/
            list.add(feedstation);
        }
        return list;
    }
}