package com.varteq.catslovers.view.presenter;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
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

    private boolean isWaitingUpdateFeedstations;
    Runnable updateFeedstationsWithDelayRunnable;
    Handler updateFeedstationsWithDelayHandler;

    public MapPresenter(MapFragment view) {
        this.view = view;

        updateFeedstationsWithDelayHandler = new Handler();
    }

    public void getFeedstations(double lat, double lng, Integer distance) {

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getGeoFeedstations(lat, lng, distance);
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
                            if (error != null)
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

    public void onCameraMoved(double lat, double lng) {
        if (isWaitingUpdateFeedstations)
            stopUpdateFeedstationWithDelay();
        startUpdateFeedstationWithDelay(lat, lng, 20);
    }

    public void onViewPaused() {
        stopUpdateFeedstationWithDelay();
    }

    private void startUpdateFeedstationWithDelay(double lat, double lng, Integer distance) {
        isWaitingUpdateFeedstations = true;
        updateFeedstationsWithDelayRunnable = () -> {
            isWaitingUpdateFeedstations = false;
            getFeedstations(lat, lng, distance);
        };
        updateFeedstationsWithDelayHandler.postDelayed(updateFeedstationsWithDelayRunnable, 500);
    }

    private void stopUpdateFeedstationWithDelay() {
        updateFeedstationsWithDelayHandler.removeCallbacks(updateFeedstationsWithDelayRunnable);
    }

    public void onGroupActionButtonClicked(Feedstation feedstation) {
        if (feedstation.getStatus() != null && feedstation.getStatus() == GroupPartner.Status.JOINED)
            leaveFeedstation(feedstation.getId());
        else
            followFeedstation(feedstation.getId());
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
                            view.onSuccessLeave();
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
                Log.e(TAG, "followFeedstation onFailure " + t.getMessage());
            }
        });
    }

    public void followFeedstation(Integer feedstationId) {

        if (feedstationId == null) return;

        Call<BaseResponse<ErrorData>> call = ServiceGenerator.getApiServiceWithToken().followFeedstation(feedstationId);
        call.enqueue(new Callback<BaseResponse<ErrorData>>() {
            @Override
            public void onResponse(Call<BaseResponse<ErrorData>> call, Response<BaseResponse<ErrorData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<ErrorData>(response) {

                        @Override
                        protected void onSuccess(ErrorData data) {
                            view.onSuccessFollow();
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
                Log.e(TAG, "followFeedstation onFailure " + t.getMessage());
            }
        });
    }

    public static List<Feedstation> from(List<RFeedstation> data) {
        List<Feedstation> list = new ArrayList<>();
        for (RFeedstation station : data) {
            if (station.getType() != null && !station.getType().equals("Feedstation"))
                continue;
            Feedstation feedstation = new Feedstation();
            feedstation.setId(station.getId());
            feedstation.setName(station.getName());
            feedstation.setCreatedUserId(station.getCreated());
            feedstation.setAddress(station.getAddress());
            feedstation.setDescription(station.getDescription());
            if (station.getIsPublic() != null && station.getIsPublic())
                feedstation.setTimeToFeed(TimeUtils.getLocalDateFromUtc(station.getTimeToFeed()));
            if (station.getLat() != null && station.getLng() != null)
                feedstation.setLocation(new LatLng(station.getLat(), station.getLng()));
            if (station.getIsPublic() != null)
                feedstation.setIsPublic(station.getIsPublic());
            else feedstation.setIsPublic(true);

            if (station.getPermissions() != null) {
                if (station.getPermissions().getRole() != null) {
                    if (station.getPermissions().getRole().equals("admin"))
                        feedstation.setUserRole(Feedstation.UserRole.ADMIN);
                    else if (station.getPermissions().getRole().equals("user"))
                        feedstation.setUserRole(Feedstation.UserRole.USER);
                }
                if (station.getPermissions().getStatus() != null) {
                    GroupPartner.Status status = null;
                    if (station.getPermissions().getStatus().equals("joined"))
                        status = GroupPartner.Status.JOINED;
                    else if (station.getPermissions().getStatus().equals("invited"))
                        status = GroupPartner.Status.INVITED;
                    else if (station.getPermissions().getStatus().equals("requested"))
                        status = GroupPartner.Status.REQUESTED;
                    feedstation.setStatus(status);
                }
            }
/*if (!station.getIsPublic() && Profile.getUserId(view.getContext()).equals(station.getCreated())){
    Profile.setUserStation(view.getContext(), String.valueOf(station.getId()));
}*/
            list.add(feedstation);
        }
        return list;
    }
}