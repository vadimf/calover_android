package com.varteq.catslovers.view.presenter;

import android.location.Location;

import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.Cat;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
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

        Call<BaseResponse<List<Cat>>> call;
        if (inRadius) {
            Location location = Profile.getLocation(view.getContext());
            if (location == null) return;
            call = ServiceGenerator.getApiServiceWithToken().getCatsInRadius(location.getLatitude(), location.getLongitude(), 20);
        } else
            call = ServiceGenerator.getApiServiceWithToken().getCats();
        call.enqueue(new Callback<BaseResponse<List<Cat>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Cat>>> call, Response<BaseResponse<List<Cat>>> response) {
                new BaseParser<List<Cat>>(response) {

                    @Override
                    protected void onSuccess(List<Cat> data) {
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
            public void onFailure(Call<BaseResponse<List<Cat>>> call, Throwable t) {
                Log.e(TAG, "getCats onFailure " + t.getMessage());
            }
        });
    }

    private List<CatProfile> getCatProfiles(List<Cat> data) {
        List<CatProfile> list = new ArrayList<>();
        for (Cat cat : data) {
            CatProfile catProfile = new CatProfile();
            catProfile.setId(cat.getId());
            catProfile.setPetName(cat.getName());
            catProfile.setNickname(cat.getNickname());
            catProfile.setBirthday(TimeUtils.getLocalDateFromUtc(cat.getAge()));
            catProfile.setSex(null);
            catProfile.setWeight(cat.getWeight());
            catProfile.setCastrated(cat.getCastrated());
            catProfile.setDescription(cat.getDescription());
            catProfile.setType(cat.getType().equals("pet") ? CatProfile.Status.PET : CatProfile.Status.STRAY);
            catProfile.setFleaTreatmentDate(TimeUtils.getLocalDateFromUtc(cat.getNextFleaTreatment()));

            if (cat.getPermissions() != null) {
                if (cat.getPermissions().getRole() != null) {
                    if (cat.getPermissions().getRole().equals("admin"))
                        catProfile.setUserRole(Feedstation.UserRole.ADMIN);
                    else if (cat.getPermissions().getRole().equals("user"))
                        catProfile.setUserRole(Feedstation.UserRole.USER);
                }
                if (cat.getPermissions().getStatus() != null) {
                    GroupPartner.Status status = null;
                    if (cat.getPermissions().getStatus().equals("joined"))
                        status = GroupPartner.Status.JOINED;
                    else if (cat.getPermissions().getStatus().equals("invited"))
                        status = GroupPartner.Status.INVITED;
                    else if (cat.getPermissions().getStatus().equals("requested"))
                        status = GroupPartner.Status.REQUESTED;
                    catProfile.setStatus(status);
                }
            }

            List<Integer> colors = new ArrayList<>();
            for (String s : cat.getColor().split(","))
                colors.add(Integer.parseInt(s));
            catProfile.setColorsList(colors);
            list.add(catProfile);
        }
        return list;
    }
}