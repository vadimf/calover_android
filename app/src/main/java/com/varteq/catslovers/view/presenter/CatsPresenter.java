package com.varteq.catslovers.view.presenter;

import android.location.Location;

import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RCat;
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

    public void getCats(boolean inRadius) {

        Call<BaseResponse<List<RCat>>> call;
        if (inRadius) {
            Location location = Profile.getLocation(view.getContext());
            if (location == null) return;
            call = ServiceGenerator.getApiServiceWithToken().getCatsInRadius(location.getLatitude(), location.getLongitude(), 20);
        } else
            call = ServiceGenerator.getApiServiceWithToken().getCats();
        call.enqueue(new Callback<BaseResponse<List<RCat>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RCat>>> call, Response<BaseResponse<List<RCat>>> response) {
                new BaseParser<List<RCat>>(response) {

                    @Override
                    protected void onSuccess(List<RCat> data) {
                        Log.i(TAG, String.valueOf(data.size()));
                        if (data.size() < 1) return;
                        List<CatProfile> catProfiles = getCatProfiles(data);
                        Collections.sort(catProfiles, (catProfile, t1) -> catProfile.getPetName().toUpperCase().compareTo(t1.getPetName().toUpperCase()));
                        view.catsLoaded(catProfiles);
                    }

                    @Override
                    protected void onFail(ErrorResponse error) {
                        if (error != null)
                            Log.d(TAG, error.getMessage() + error.getCode());
                    }
                };
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RCat>>> call, Throwable t) {
                Log.e(TAG, "getCats onFailure " + t.getMessage());
            }
        });
    }

    private List<CatProfile> getCatProfiles(List<RCat> data) {
        List<CatProfile> list = new ArrayList<>();
        for (RCat RCat : data) {
            CatProfile catProfile = new CatProfile();
            catProfile.setId(RCat.getId());
            catProfile.setPetName(RCat.getName());
            catProfile.setNickname(RCat.getNickname());
            catProfile.setBirthday(TimeUtils.getLocalDateFromUtc(RCat.getAge()));
            catProfile.setSex(null);
            catProfile.setWeight(RCat.getWeight());
            catProfile.setCastrated(RCat.getCastrated());
            catProfile.setDescription(RCat.getDescription());
            catProfile.setType(RCat.getType().equals("pet") ? CatProfile.Status.PET : CatProfile.Status.STRAY);
            catProfile.setFleaTreatmentDate(TimeUtils.getLocalDateFromUtc(RCat.getNextFleaTreatment()));

            if (RCat.getPermissions() != null) {
                if (RCat.getPermissions().getRole() != null) {
                    if (RCat.getPermissions().getRole().equals("admin"))
                        catProfile.setUserRole(Feedstation.UserRole.ADMIN);
                    else if (RCat.getPermissions().getRole().equals("user"))
                        catProfile.setUserRole(Feedstation.UserRole.USER);
                }
                if (RCat.getPermissions().getStatus() != null) {
                    GroupPartner.Status status = null;
                    if (RCat.getPermissions().getStatus().equals("joined"))
                        status = GroupPartner.Status.JOINED;
                    else if (RCat.getPermissions().getStatus().equals("invited"))
                        status = GroupPartner.Status.INVITED;
                    else if (RCat.getPermissions().getStatus().equals("requested"))
                        status = GroupPartner.Status.REQUESTED;
                    catProfile.setStatus(status);
                }
            }

            List<Integer> colors = new ArrayList<>();
            for (String s : RCat.getColor().split(","))
                try {
                    colors.add(Integer.parseInt(s));
                } catch (Exception e) {
                }
            catProfile.setColorsList(colors);

            if (RCat.getPhotos() != null && !RCat.getPhotos().isEmpty()) {
                List<PhotoWithPreview> photos = new ArrayList<>();
                for (RPhoto photo : RCat.getPhotos())
                    photos.add(new PhotoWithPreview(photo.getId(), photo.getPhoto(), photo.getThumbnail()));
                catProfile.setPhotos(photos);
            }

            if (RCat.getFeedstation() != null)
                catProfile.setFeedstationId(RCat.getFeedstation().getId());

            if (RCat.getAvatarUrl() != null)
                catProfile.setAvatar(new PhotoWithPreview(null, RCat.getAvatarUrl(), RCat.getAvatarUrlThumbnail()));

            list.add(catProfile);
        }
        return list;
    }
}