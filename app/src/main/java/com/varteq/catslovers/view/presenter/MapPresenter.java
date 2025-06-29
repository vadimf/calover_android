package com.varteq.catslovers.view.presenter;

import android.location.Location;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RBusiness;
import com.varteq.catslovers.api.entity.REvent;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RGeoSearch;
import com.varteq.catslovers.api.entity.RPhoto;
import com.varteq.catslovers.model.Business;
import com.varteq.catslovers.model.Event;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.model.PhotoWithPreview;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.view.fragments.MapFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapPresenter {

    public static final int EVENT_TYPE_WARNING_NEWBORN_KITTENS = 1;
    public static final int EVENT_TYPE_WARNING_MUNICIPALITY_INSPECTOR = 2;
    public static final int EVENT_TYPE_WARNING_CAT_IN_HEAT = 3;
    public static final int EVENT_TYPE_WARNING_STRAY_CAT = 4;
    public static final int EVENT_TYPE_EMERGENCY_POISON = 5;
    public static final int EVENT_TYPE_EMERGENCY_MISSING_CAT = 6;
    public static final int EVENT_TYPE_EMERGENCY_CARCASS = 7;

    private String TAG = MapPresenter.class.getSimpleName();

    private MapFragment view;

    private boolean isWaitingUpdateFeedstations;
    Runnable updateFeedstationsWithDelayRunnable;
    Handler updateFeedstationsWithDelayHandler;
    private boolean isTesting = false;

    public MapPresenter(MapFragment view) {
        this.view = view;

        updateFeedstationsWithDelayHandler = new Handler();
    }

    public void getFeedstationsMock(double lat, double lng, Integer distance) {
        Location userLocation = Profile.getLocation(view.getContext());
        List<Feedstation> feedstations = new ArrayList<>();
        List<Event> events = new ArrayList<>();
        List<Business> businesses = new ArrayList<>();

        Feedstation feedstation = new Feedstation();
        feedstation.setLocation(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
        feedstation.setId(73);
        feedstation.setIsPublic(false);
        feedstation.setUserRole(Feedstation.UserRole.ADMIN);
        feedstations.add(feedstation);

        Business business = new Business();
        business.setLocation(new LatLng(51.526197, 31.279616));
        business.setAddress("");
        business.setLink("google.com");
        businesses.add(business);

        view.feedstationsLoaded(feedstations, events, businesses);
    }

    public void getFeedstations(double lat, double lng, Integer distance) {
        if (!isTesting) {
            Call<BaseResponse<RGeoSearch>> call = ServiceGenerator.getApiServiceWithToken().getGeoFeedstations(lat, lng, distance);
            call.enqueue(new Callback<BaseResponse<RGeoSearch>>() {
                @Override
                public void onResponse(Call<BaseResponse<RGeoSearch>> call, Response<BaseResponse<RGeoSearch>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        new BaseParser<RGeoSearch>(response) {

                            @Override
                            protected void onSuccess(RGeoSearch data) {
                                view.feedstationsLoaded(from(data.getFeedstations()), fromEvents(data.getEvents()), fromBusiness(data.getBusinesses()));
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
                public void onFailure(Call<BaseResponse<RGeoSearch>> call, Throwable t) {
                    Log.e(TAG, "getFeedstations onFailure " + t.getMessage());
                }
            });
        } else
            getFeedstationsMock(lat, lng, distance);
    }

    public void onCameraMoved(double lat, double lng) {
        if (isWaitingUpdateFeedstations)
            stopUpdateFeedstationWithDelay();
        startUpdateFeedstationWithDelay(lat, lng, 20);
    }

    public void onInfoWindowClicked(Object markerTag) {
        if (markerTag != null && markerTag instanceof Business) {
            Business business = (Business) markerTag;
            view.showBusinessMarkerBottomSheet(business);
        }
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

    private Business getBusinessMock(LatLng location) {
        Business business = new Business();
        business.setAddress("my address");
        business.setName("my name");
        business.setDescription("my description");
        business.setLink("https://google.com");
        business.setOpenHours("12");
        business.setPhone("+380121234567");
        business.setLocation(location);
        return business;
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

    public void onCreateEventChoosed(int eventType, double latitude, double longitude, String name) {
        String address = Utils.getAddressByLocation(latitude, longitude, view.getContext());
        if (name == null)
            name = address;
        Call<BaseResponse> call = ServiceGenerator.getApiServiceWithToken().createEvent(name, address, latitude, longitude, eventType);
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        Toaster.shortToast("Event successful created");
                    } else {
                        Toaster.shortToast("Fail to create event");
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Toaster.shortToast("Fail to create event");
            }
        });
    }

    public void onMarkerClicked(Object markerTag) {
        if (markerTag == null) return;
        if (markerTag instanceof Feedstation) {
            if (!view.isFeedstationBottomSheetShowed())
                view.hideBottomSheets();
            Feedstation feedstation = (Feedstation) markerTag;
            view.showFeedstationMarkerBottomSheet(feedstation);
            view.setBottomSheetFeedstationTag(feedstation);
            view.initStationAction(feedstation);
            view.hideMarkerDialogs();
            view.initAvatarCatBackground(feedstation);
            view.releaseClickedLocation();
        } else if (markerTag instanceof Event) {
            view.hideBottomSheets();
            Event event = (Event) markerTag;
            view.showEventMarkerDialog(event);
        } else if (markerTag instanceof Business) {
            view.hideBottomSheets();
            Business business = (Business) markerTag;
            view.showBusinessMarkerDialog(business);
        }

    }

    public void getEventTypes() {
        Call<BaseResponse<REvent>> call = ServiceGenerator.getApiServiceWithToken().getEventsTypes();
        call.enqueue(new Callback<BaseResponse<REvent>>() {
            @Override
            public void onResponse(Call<BaseResponse<REvent>> call, Response<BaseResponse<REvent>> response) {
                if (response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<BaseResponse<REvent>> call, Throwable t) {

            }
        });
    }

    public static List<Event> fromEvents(List<REvent> data) {
        if (data == null || data.isEmpty()) return null;
        List<Event> eventList = new ArrayList<>();
        for (REvent rEvent : data) {
            Event event = new Event();
            event.setId(rEvent.getId());
            if (rEvent.getEventType() == null) continue;
            event.setTypeName(rEvent.getEventType().getName());
            switch (rEvent.getId()) {
                case EVENT_TYPE_WARNING_NEWBORN_KITTENS:
                    event.setEventType(Event.EventType.NEWBORN_KITTENS);
                    break;
                case EVENT_TYPE_WARNING_MUNICIPALITY_INSPECTOR:
                    event.setEventType(Event.EventType.MUNICIPALITY_INSPECTOR);
                    break;
                case EVENT_TYPE_WARNING_CAT_IN_HEAT:
                    event.setEventType(Event.EventType.CAT_IN_HEAT);
                    break;
                case EVENT_TYPE_WARNING_STRAY_CAT:
                    event.setEventType(Event.EventType.STRAY_CAT);
                    break;
                case EVENT_TYPE_EMERGENCY_POISON:
                    event.setEventType(Event.EventType.POISON);
                    break;
                case EVENT_TYPE_EMERGENCY_MISSING_CAT:
                    event.setEventType(Event.EventType.MISSING_CAT);
                    break;
                case EVENT_TYPE_EMERGENCY_CARCASS:
                    event.setEventType(Event.EventType.CARCASS);
                    break;
            }
            switch (rEvent.getEventType().getCategory()) {
                case "warning":
                    event.setType(Event.Type.WARNING);
                    break;
                case "emergency":
                    event.setType(Event.Type.EMERGENCY);
                    break;
            }

            try {
                event.setAddress(rEvent.getAddress());
                event.setDate(TimeUtils.getLocalDateFromUtc(rEvent.getCreated()));
                event.setDescription(rEvent.getDescription());
                event.setLatLng(new LatLng(rEvent.getLat(), rEvent.getLng()));
                event.setName(rEvent.getName());
            } catch (Exception error) {
                Log.d(MapPresenter.class.getSimpleName(), error.getMessage());
            }


            eventList.add(event);
        }
        return eventList;
    }

    public static List<Business> fromBusiness(List<RBusiness> data) {
        if (data == null || data.isEmpty()) return null;
        List<Business> businessList = new ArrayList<>();
        for (RBusiness rBusiness : data) {
            Business business = new Business();
            business.setId(rBusiness.getId());
            business.setAddress(rBusiness.getAddress());
            business.setDescription(rBusiness.getDescription());
            business.setLocation(new LatLng(rBusiness.getLat(), rBusiness.getLng()));
            business.setName(rBusiness.getName());
            business.setLink(rBusiness.getLink());
            business.setPhone(rBusiness.getPhone());
            business.setDistance(rBusiness.getDistance());
            business.setOpenHours(rBusiness.getOpenHours());
            switch (rBusiness.getCategory()) {
                case "food":
                    business.setCategory(Business.Category.FOOD);
                    break;
                case "veterinary":
                    business.setCategory(Business.Category.VETERINARY);
                    break;
            }

            businessList.add(business);
        }
        return businessList;
    }

    public static List<Feedstation> from(List<RFeedstation> data) {
        if (data == null || data.isEmpty()) return null;
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
            feedstation.setTimeToEat1(TimeUtils.getLocalTimeFromDayStartOffset(station.getTimeToFeedMorning()));
            feedstation.setTimeToEat2(TimeUtils.getLocalTimeFromDayStartOffset(station.getTimeToFeedEvening()));
            feedstation.setTimeToFeed(TimeUtils.getLocalDateFromUtc(station.getTimeToFeed()));
            feedstation.setLastFeeding(TimeUtils.getLocalDateFromUtc(station.getLastFeeding()));
            feedstation.setDescription(station.getDescription());
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
            if (station.getPhotos() != null && !station.getPhotos().isEmpty()) {
                List<PhotoWithPreview> photos = new ArrayList<>();
                for (RPhoto photo : station.getPhotos())
                    photos.add(new PhotoWithPreview(photo.getId(), photo.getPhoto(), photo.getThumbnail()));
                feedstation.setPhotos(photos);
            }

            if (station.getFeedStatus() != null) {
                switch (station.getFeedStatus()) {
                    case "normal":
                        feedstation.setFeedStatus(Feedstation.FeedStatus.NORMAL);
                        break;
                    case "hungry":
                        feedstation.setFeedStatus(Feedstation.FeedStatus.HUNGRY);
                        break;
                    case "starving":
                        feedstation.setFeedStatus(Feedstation.FeedStatus.STARVING);
                        break;
                }
            }

            list.add(feedstation);
        }
        return list;
    }
}