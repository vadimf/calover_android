package com.varteq.catslovers.view.presenter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;

import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.Cat;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.CatProfileActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatProfilePresenter {

    private String TAG = CatProfilePresenter.class.getSimpleName();

    private CatProfileActivity view;

    public CatProfilePresenter(CatProfileActivity view) {
        this.view = view;
    }

    public void compressColorsList(List colorsList) {
        while (colorsList.contains(null))
            colorsList.remove(null);
    }

    public void resizeColorsListWithEmptyValues(List colorsList, int cnt) {
        while (cnt > colorsList.size())
            colorsList.add(null);
    }

    public void addGroupPartner(List groupPartnersList, RecyclerView.Adapter groupPartnersAdapter) {
        groupPartnersList.add(1, new GroupPartner(null, "User" + ((int) (Math.random() * 999999) + 111111), false));
        groupPartnersAdapter.notifyItemInserted(1);
    }


    public String getAgeInString(long petBirthdayMillis) {
        long nowMillis = System.currentTimeMillis();
        long timePassedMonthes = (TimeUnit.MILLISECONDS.toDays(nowMillis - petBirthdayMillis)) / 30;

        int years = ((int) timePassedMonthes) / 12;
        int month = ((int) timePassedMonthes) - (years * 12);

        String age = null;
        if (years > 0) {
            if (month > 0) {
                age = years + " years, " + String.valueOf(month) + " months";
            } else if (month == 0) {
                age = years + " years";
            }
        } else if (years == 0) {
            if (month > 0) {
                age = String.valueOf(month) + " months";
            } else if (month == 0) {
                age = "newborn";

            }
        }
        if (age == null) {
            age = "incorrect date";
        }
        return age;
    }

    public void onPetImageSelected(Uri uri, List photoList, RecyclerView.Adapter photosAdapter) {
        if (uri != null) {
            Log.d(TAG, "onImageSelected " + uri);
            photoList.add(0, uri);
            photosAdapter.notifyItemInserted(0);
        }
    }

    public void saveCat(CatProfile cat) {
        String colors = "";
        for (int color : cat.getColorsList())
            colors += String.valueOf(color) + ",";
        if (!colors.isEmpty())
            colors = colors.substring(0, colors.length() - 1);

        String type = cat.getType().equals(CatProfile.Status.PET) ? "pet" : "stray";

        int age = (int) (cat.getBirthday().getTime() / 1000L);
        int nextFleaTreatment = (int) (cat.getFleaTreatmentDate().getTime() / 1000L);

        Call<BaseResponse<Cat>> call = ServiceGenerator.getApiServiceWithToken().createCat(cat.getId(), cat.getPetName(),
                cat.getNickname(), colors, age, cat.getSex(), cat.getWeight(), cat.isCastrated(), cat.getDescription(), type, nextFleaTreatment);
        call.enqueue(new Callback<BaseResponse<Cat>>() {
            @Override
            public void onResponse(Call<BaseResponse<Cat>> call, Response<BaseResponse<Cat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<Cat>(response) {

                        @Override
                        protected void onSuccess(Cat data) {
                            /*if (data.getToken() != null) {
                                Log.i(TAG, "getApiService().auth success");
                                Log.i(TAG, data.getToken());

                            }*/
                            view.savedSuccessfully();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                            if (error.getCode() == 422)
                                Toaster.longToast("You should fill in PetName and fields from age to description");
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Cat>> call, Throwable t) {
                Log.e(TAG, "createCat onFailure " + t.getMessage());
            }
        });
    }

    public void updateCat(CatProfile cat) {
        String colors = "";
        for (int color : cat.getColorsList())
            colors += String.valueOf(color) + ",";
        if (!colors.isEmpty())
            colors = colors.substring(0, colors.length() - 1);

        String type = cat.getType().equals(CatProfile.Status.PET) ? "pet" : "stray";

        int age = (int) (cat.getBirthday().getTime() / 1000L);
        int nextFleaTreatment = (int) (cat.getFleaTreatmentDate().getTime() / 1000L);

        Call<BaseResponse<Cat>> call = ServiceGenerator.getApiServiceWithToken().updateCat(cat.getId(), cat.getPetName(),
                cat.getNickname(), colors, age, cat.getSex(), cat.getWeight(), cat.isCastrated(), cat.getDescription(), type, nextFleaTreatment);
        call.enqueue(new Callback<BaseResponse<Cat>>() {
            @Override
            public void onResponse(Call<BaseResponse<Cat>> call, Response<BaseResponse<Cat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<Cat>(response) {

                        @Override
                        protected void onSuccess(Cat data) {
                            view.savedSuccessfully();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                            if (error.getCode() == 422)
                                Toaster.longToast("You should fill in PetName and fields from age to description");
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Cat>> call, Throwable t) {
                Log.e(TAG, "updateCat onFailure " + t.getMessage());
            }
        });
    }
}
