package com.varteq.catslovers.view.presenter;

import android.location.Location;

import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RCat;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RFeedstationPermissions;
import com.varteq.catslovers.api.entity.RPhoto;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.model.PhotoWithPreview;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.view.fragments.CatsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatsPresenter {


    private String TAG = CatsPresenter.class.getSimpleName();

    private CatsFragment view;

    public CatsPresenter(CatsFragment view) {
        this.view = view;
    }

    public void getCats(int catsSection) {
        view.startRefreshing();
        Call<BaseResponse<List<RCat>>> call;
        Location location = Profile.getLocation(view.getContext());

        if (catsSection == CatsFragment.CATS_SECTION_EXPLORE) {
            if (location == null) return;
            call = ServiceGenerator.getApiServiceWithToken().getCatsInRadius(location.getLatitude(), location.getLongitude(), 20);
            call.enqueue(new Callback<BaseResponse<List<RCat>>>() {
                @Override
                public void onResponse(Call<BaseResponse<List<RCat>>> call, Response<BaseResponse<List<RCat>>> response) {
                    new BaseParser<List<RCat>>(response) {
                        @Override
                        protected void onSuccess(List<RCat> data) {
                            onCatsInRadiusLoaded(data, catsSection);
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                            view.stopRefreshing();
                        }
                    };
                }

                @Override
                public void onFailure(Call<BaseResponse<List<RCat>>> call, Throwable t) {
                    Log.e(TAG, "getCats onFailure " + t.getMessage());
                    view.stopRefreshing();
                }
            });


        } else {

            call = ServiceGenerator.getApiServiceWithToken().getCats();
            call.enqueue(new Callback<BaseResponse<List<RCat>>>() {
                @Override
                public void onResponse(Call<BaseResponse<List<RCat>>> call, Response<BaseResponse<List<RCat>>> response) {
                    new BaseParser<List<RCat>>(response) {
                        @Override
                        protected void onSuccess(List<RCat> data) {
                            onMyCatsLoaded(data, catsSection);
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                            view.stopRefreshing();
                        }
                    };
                }

                @Override
                public void onFailure(Call<BaseResponse<List<RCat>>> call, Throwable t) {
                    Log.e(TAG, "getCats onFailure " + t.getMessage());
                    view.stopRefreshing();
                }
            });
        }

    }

    private void onMyCatsLoaded(List<RCat> data, int catsSection) {
        Log.i(TAG, String.valueOf(data.size()));
        List<CatProfile> catProfiles = getCatProfiles(data);
        if (data.size() < 1) {
            view.catsLoaded(catProfiles, catsSection);
            return;
        }
        Collections.sort(catProfiles, (catProfile, t1) -> catProfile.getPetName().toUpperCase().compareTo(t1.getPetName().toUpperCase()));
        view.catsLoaded(catProfiles, catsSection);
    }

    private void onCatsInRadiusLoaded(List<RCat> data, int catsSection) {
        Log.i(TAG, String.valueOf(data.size()));
        List<CatProfile> catProfiles = getCatProfiles(data);
        if (data.size() < 1) {
            view.catsLoaded(catProfiles, catsSection);
            return;
        }
        Collections.sort(catProfiles, (catProfile, t1) -> catProfile.getPetName().toUpperCase().compareTo(t1.getPetName().toUpperCase()));
        view.catsLoaded(catProfiles, catsSection);
    }


    private List<CatProfile> getCatProfiles(List<RCat> data) {
        List<CatProfile> list = new ArrayList<>();
        for (RCat rCat : data) {
            CatProfile catProfile = new CatProfile();
            catProfile.setId(rCat.getId());
            catProfile.setPetName(rCat.getName());
            catProfile.setNickname(rCat.getNickname());
            catProfile.setBirthday(TimeUtils.getLocalDateFromUtc(rCat.getAge()));
            catProfile.setSex(null);
            catProfile.setWeight(rCat.getWeight());
            catProfile.setCastrated(rCat.getCastrated());
            catProfile.setDescription(rCat.getDescription());
            catProfile.setType(rCat.getType().equals("pet") ? CatProfile.Status.PET : CatProfile.Status.STRAY);
            catProfile.setFleaTreatmentDate(TimeUtils.getLocalDateFromUtc(rCat.getNextFleaTreatment()));

            if (rCat.getPermissions() != null) {
                Integer userId = null;
                if (rCat.getPermissions().getUserId() != null)
                    userId = rCat.getPermissions().getUserId();
                catProfile.setUserId(userId);

                if (rCat.getPermissions().getRole() != null) {
                    if (rCat.getPermissions().getRole().equals("admin"))
                        catProfile.setUserRole(Feedstation.UserRole.ADMIN);
                    else if (rCat.getPermissions().getRole().equals("user"))
                        catProfile.setUserRole(Feedstation.UserRole.USER);
                }
                if (rCat.getPermissions().getStatus() != null) {
                    GroupPartner.Status status = null;
                    if (rCat.getPermissions().getStatus().equals("joined"))
                        status = GroupPartner.Status.JOINED;
                    else if (rCat.getPermissions().getStatus().equals("invited"))
                        status = GroupPartner.Status.INVITED;
                    else if (rCat.getPermissions().getStatus().equals("requested"))
                        status = GroupPartner.Status.REQUESTED;
                    catProfile.setStatus(status);
                }
            }

            List<Integer> colors = new ArrayList<>();
            for (String s : rCat.getColor().split(","))
                try {
                    colors.add(Integer.parseInt(s));
                } catch (Exception e) {
                }
            catProfile.setColorsList(colors);

            if (rCat.getPhotos() != null && !rCat.getPhotos().isEmpty()) {
                List<PhotoWithPreview> photos = new ArrayList<>();
                for (RPhoto photo : rCat.getPhotos())
                    photos.add(new PhotoWithPreview(photo.getId(), photo.getPhoto(), photo.getThumbnail()));
                catProfile.setPhotos(photos);
            }

            if (rCat.getFeedstation() != null) {
                catProfile.setFeedstationId(rCat.getFeedstation().getId());
                RFeedstation rFeedstation = rCat.getFeedstation();
                RFeedstationPermissions rPermissions = rFeedstation.getPermissions();
                if (rPermissions != null) {
                    String status = null;
                    if (rPermissions.getStatus() != null)
                        status = rPermissions.getStatus();
                    catProfile.setFeedStationStatus(status);
                }

            }


            if (rCat.getAvatarUrl() != null)
                catProfile.setAvatar(new PhotoWithPreview(null, rCat.getAvatarUrl(), rCat.getAvatarUrlThumbnail()));

            list.add(catProfile);
        }
        return list;
    }
}