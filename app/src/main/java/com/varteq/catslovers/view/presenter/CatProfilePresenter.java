package com.varteq.catslovers.view.presenter;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.Cat;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RUser;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.utils.GenericOf;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.CatProfileActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatProfilePresenter {

    private String TAG = CatProfilePresenter.class.getSimpleName();

    private CatProfileActivity view;
    private MultipartUploadRequest uploadCatRequest;
    private boolean isCatUploading;

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
        groupPartnersList.add(1, new GroupPartner(null, "User" + ((int) (Math.random() * 999999) + 111111), GroupPartner.Status.JOINED, false));
        groupPartnersAdapter.notifyItemInserted(1);
    }

    public void getGroupPartners(Integer catId) {
        if (catId == null) return;

        Call<BaseResponse<List<RUser>>> call = ServiceGenerator.getApiServiceWithToken().getCatUsers(catId);

        call.enqueue(new Callback<BaseResponse<List<RUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RUser>>> call, Response<BaseResponse<List<RUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RUser>>(response) {

                        @Override
                        protected void onSuccess(List<RUser> data) {
                            List<GroupPartner> partners = new ArrayList<>();
                            for (RUser user : data) {
                                partners.add(from(user));
                            }
                            view.refreshGroupPartners(partners);
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

    private GroupPartner from(RUser user) {
        GroupPartner.Status status = GroupPartner.Status.INVITED;
        if (user.getStatus() != null) {
            if (user.getStatus().equals("joined"))
                status = GroupPartner.Status.JOINED;
            else if (user.getStatus().equals("invited"))
                status = GroupPartner.Status.INVITED;
            else if (user.getStatus().equals("requested"))
                status = GroupPartner.Status.REQUESTED;
        } else return null;

        boolean isAdmin = false;
        if (user.getRole().equals("admin"))
            isAdmin = true;

        String name = user.getUserInfo().getName();
        if (user.getUserId().equals(Integer.parseInt(Profile.getUserId(view)))) {
            name = "You";
        }
        return new GroupPartner(null, user.getUserId(), name, user.getUserInfo().getPhone(), status, isAdmin);
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

    public void saveCat(CatProfile cat, Location lastLocation) {
        String colors = "";
        for (int color : cat.getColorsList())
            colors += String.valueOf(color) + ",";
        if (!colors.isEmpty())
            colors = colors.substring(0, colors.length() - 1);

        String type = cat.getType().equals(CatProfile.Status.PET) ? "pet" : "stray";

        int age = (int) (cat.getBirthday().getTime() / 1000L);
        int nextFleaTreatment = (int) (cat.getFleaTreatmentDate().getTime() / 1000L);

        // fake location
        /*lastLocation = new Location("dddd");
        lastLocation.setLatitude(50.4437);
        lastLocation.setLongitude(30.5008);*/

        Call<BaseResponse<Cat>> call;
        if (cat.getFeedstationId() != null)
            call = ServiceGenerator.getApiServiceWithToken().createCat(cat.getFeedstationId(), cat.getPetName(),
                    cat.getNickname(), colors, age, cat.getSex(), cat.getWeight(), cat.isCastrated(), cat.getDescription(), type, nextFleaTreatment);
        else {
            call = ServiceGenerator.getApiServiceWithToken().createPrivateCat(cat.getPetName(),
                    cat.getNickname(), colors, age, cat.getSex(), cat.getWeight(), cat.isCastrated(), cat.getDescription(), type, nextFleaTreatment,
                    lastLocation.getLatitude(), lastLocation.getLongitude());
            Profile.setLocation(view, lastLocation);
        }

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
                            if (error != null && error.getCode() == 422)
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

    public void uploadCatWithPhotos(CatProfile cat, Location lastLocation) {
        if (isCatUploading) return;
        isCatUploading = true;
        view.showWaitDialog();

        String colors = "";
        for (int color : cat.getColorsList())
            colors += String.valueOf(color) + ",";
        if (!colors.isEmpty())
            colors = colors.substring(0, colors.length() - 1);

        String type = cat.getType().equals(CatProfile.Status.PET) ? "pet" : "stray";

        int age = (int) (cat.getBirthday().getTime() / 1000L);
        int nextFleaTreatment = (int) (cat.getFleaTreatmentDate().getTime() / 1000L);

        try {
            UploadNotificationConfig config = new UploadNotificationConfig();
            config.setRingToneEnabled(false)
                    .setClearOnActionForAllStatuses(true)
                    .setTitleForAllStatuses("Uploading cat");

            // update cat
            if (cat.getId() != null) {
                uploadCatRequest = new MultipartUploadRequest(view, ServiceGenerator.apiBaseUrl + "cats/" + cat.getId())
                        .setMethod("PUT");
            }
            // create cat
            else
                uploadCatRequest = new MultipartUploadRequest(view, ServiceGenerator.apiBaseUrl + "cats");

            if (cat.getFeedstationId() != null)
                uploadCatRequest.addParameter("feedstation_id", String.valueOf(cat.getFeedstationId()));

            // fake location
        /*lastLocation = new Location("dddd");
        lastLocation.setLatitude(50.4437);
        lastLocation.setLongitude(30.5008);*/

            if (lastLocation != null) {
                uploadCatRequest.addParameter("lat", String.valueOf(lastLocation.getLatitude()))
                        .addParameter("lng", String.valueOf(lastLocation.getLongitude()));
                Profile.setLocation(view, lastLocation);
            }

            uploadCatRequest.addParameter("name", cat.getPetName())
                    .addParameter("nickname", cat.getNickname())
                    .addParameter("color", colors)
                    .addParameter("age", String.valueOf(age))
                    .addParameter("sex", cat.getSex())
                    .addParameter("weight", String.valueOf(cat.getWeight()))
                    .addParameter("castrated", String.valueOf(cat.isCastrated()))
                    .addParameter("description", cat.getDescription())
                    .addParameter("type", type)
                    .addParameter("next_flea_treatment", String.valueOf(nextFleaTreatment))
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(1)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {

                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            Toaster.longToast("An error occurred while saving");
                            view.hideWaitDialog();
                            isCatUploading = false;
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            Reader reader = null;
                            JsonReader jsonReader = null;
                            try {
                                Gson gson = new Gson();
                                InputStream instream = new ByteArrayInputStream(serverResponse.getBody());
                                reader = new InputStreamReader(instream);
                                jsonReader = new JsonReader(reader);

                                BaseResponse<Cat> cat = gson.fromJson(jsonReader, new GenericOf<>(BaseResponse.class, Cat.class));
                                if (cat != null && cat.getSuccess())
                                    view.savedSuccessfully();
                                else if (cat != null && cat.getData() != null && cat.getData().getCode() != null && cat.getData().getCode() == 422)
                                    //Toaster.longToast(cat.getData().getMessage());
                                    Toaster.longToast("You should fill in PetName and fields from age to description");
                                else Toaster.longToast("An error occurred while saving");
                            } catch (Exception e) {
                            } finally {
                                try {
                                    if (reader != null)
                                        reader.close();
                                    if (jsonReader != null)
                                        jsonReader.close();
                                } catch (IOException e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                            view.hideWaitDialog();
                            isCatUploading = false;
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            view.hideWaitDialog();
                            isCatUploading = false;
                        }
                    })
                    .setNotificationConfig(config);

            for (int i = 0; i < cat.getPhotos().size() && i < 5; i++) {
                if (cat.getPhotos().get(i).getId() == null)
                    uploadCatRequest.addFileToUpload(cat.getPhotos().get(i).getPhoto(), "images[" + i + "]");
            }

            String uploadId = uploadCatRequest.startUpload();
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
            view.hideWaitDialog();
            isCatUploading = false;
        }
    }

    public void updateCat(CatProfile cat) {
        view.setToolbarTitle(cat.getPetName());
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
                            if (error != null && error.getCode() == 422)
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

    public void getStrayFeedstations() {

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            view.feedstationsLoaded(MapPresenter.from(data));
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
}
