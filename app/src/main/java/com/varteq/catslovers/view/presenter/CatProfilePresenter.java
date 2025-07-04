package com.varteq.catslovers.view.presenter;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.R;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RCat;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RUser;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.model.PhotoWithPreview;
import com.varteq.catslovers.utils.GenericOf;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.NetworkUtils;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.utils.qb.QbDialogHolder;
import com.varteq.catslovers.utils.qb.QbDialogUtils;
import com.varteq.catslovers.utils.qb.QbUsersHolder;
import com.varteq.catslovers.utils.qb.callback.QbEntityCallbackWrapper;
import com.varteq.catslovers.view.CatProfileActivity;
import com.varteq.catslovers.view.qb.ChatActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.ArrayList;
import java.util.Calendar;
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

    public void onDeleteCatClicked(CatProfile catProfile) {
        if (null != catProfile) {
            if (catProfile.getUserRole() != null && catProfile.getUserRole().equals(Feedstation.UserRole.ADMIN))
                deleteCat(catProfile.getId());
            else {
                Toaster.longToast(R.string.only_admins_can_delete_cats);
            }
        } else {
            view.forceClose();
        }
    }

    private void deleteCat(Integer id) {
        view.showWaitDialog();
        if (id != null) {
            Call<BaseResponse<Object>> call = ServiceGenerator.getApiServiceWithToken().deleteCat(id);
            call.enqueue(new Callback<BaseResponse<Object>>() {
                @Override
                public void onResponse(Call<BaseResponse<Object>> call, Response<BaseResponse<Object>> response) {
                    view.hideWaitDialog();
                    if (response.isSuccessful()) {
                        new BaseParser<Object>(response) {
                            @Override
                            protected void onSuccess(Object data) {
                                Toaster.shortToast(R.string.cat_deleted);
                                view.forceClose();
                            }

                            @Override
                            protected void onFail(ErrorResponse error) {
                                if (error != null)
                                    Log.d(TAG, error.getMessage() + error.getCode());
                                Toaster.shortToast(R.string.server_error);
                            }
                        };
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<Object>> call, Throwable t) {
                    Log.e(TAG, "deleteCat onFailure " + t.getMessage());
                    view.hideWaitDialog();
                    if (NetworkUtils.isNetworkErr(t.getMessage()))
                        Toaster.shortToast(R.string.network_error);
                    else
                        Toaster.shortToast(R.string.server_error);
                }
            });
        }
    }

    public void onGroupPartnerClicked(GroupPartner groupPartner) {
        String login = String.valueOf(groupPartner.getUserId());
        if (login.equals(Profile.getUserId(view)))
            return;
        if (groupPartner.getStatus().equals(GroupPartner.Status.JOINED)) {
            createChatIfUserAllowed(login);
        } else {
            Toaster.shortToast(R.string.user_should_be_joined_the_feedstation);
        }
    }

    private void createChatIfUserAllowed(String login) {
        view.showWaitDialog();
        Call<BaseResponse<List<RUser>>> call = ServiceGenerator.getApiServiceWithToken().getAllowedUsers();
        call.enqueue(new Callback<BaseResponse<List<RUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RUser>>> call, Response<BaseResponse<List<RUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RUser>>(response) {
                        @Override
                        protected void onSuccess(List<RUser> data) {
                            boolean userAllowed = false;
                            for (RUser user : data) {
                                if (login.equals(String.valueOf(user.getUserId()))) {
                                    userAllowed = true;
                                    createChat(login);
                                }
                            }
                            if (!userAllowed) {
                                view.hideWaitDialog();
                                Toaster.shortToast(R.string.user_should_be_in_same_feedstation_with_you);
                            }
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                com.varteq.catslovers.utils.Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RUser>>> call, Throwable t) {
                com.varteq.catslovers.utils.Log.e(TAG, "createChatIfUserAllowed onFailure " + t.getMessage());
            }
        });
    }

    private void createChat(String login) {
        QBUsers.getUserByLogin(login).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                createChatDialog(qbUser, new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        ChatActivity.startActivity(view, qbChatDialog);
                        view.hideWaitDialog();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toaster.shortToast("Error creating dialog");
                        view.hideWaitDialog();
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                view.hideWaitDialog();
            }
        });
    }

    private void createChatDialog(final QBUser user,
                                  final QBEntityCallback<QBChatDialog> callback) {
        List<QBUser> users = new ArrayList<>();
        users.add(user);
        QBRestChatService.createChatDialog(QbDialogUtils.createDialog(users)).performAsync(
                new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        QbDialogHolder.getInstance().addDialog(dialog);
                        QbUsersHolder.getInstance().putUsers(users);
                        super.onSuccess(dialog, args);
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
        } else if (name == null || name.isEmpty())
            name = user.getUserInfo().getPhone();
        return new GroupPartner(user.getUserInfo().getAvatarUrlThumbnail(), user.getUserId(), name, user.getUserInfo().getPhone(), status, isAdmin);
    }

    public String getAgeInString(long petBirthdayMillis) {
        Calendar birthdayCalendar = Calendar.getInstance();
        birthdayCalendar.setTimeInMillis(petBirthdayMillis);
        Calendar nowCalendar = Calendar.getInstance();

        long nowMillis = System.currentTimeMillis();
        long timePassedDays = (TimeUnit.MILLISECONDS.toDays(nowMillis - petBirthdayMillis));
        long timePassedMonths =  TimeUtils.calculatePassedMonths(birthdayCalendar, nowCalendar);

        int years = ((int) timePassedMonths) / 12;
        int month = ((int) timePassedMonths) - (years * 12);

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
                age = String.valueOf(timePassedDays) + " days";

            }
        }
        if (age == null) {
            age = "incorrect date";
        }
        return age;
    }

    public void onFollowCatClicked(CatProfile catProfile) {
        String status = catProfile.getFeedStationStatus();
        Integer feedstationId = catProfile.getFeedstationId();
        if (feedstationId != null) {
            if (status != null && status.equals("joined"))
                joinCatsFeedstation(catProfile);
            else
                followCatsFeedstation(catProfile);
        } else
            view.showNoFeedstationMessage();
    }

    public void followCatsFeedstation(CatProfile catProfile) {
        Integer feedstationId = catProfile.getFeedstationId();
        Call<BaseResponse<ErrorData>> call1 = ServiceGenerator.getApiServiceWithToken().followFeedstation(feedstationId);
        call1.enqueue(new Callback<BaseResponse<ErrorData>>() {
            @Override
            public void onResponse(Call<BaseResponse<ErrorData>> call, Response<BaseResponse<ErrorData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<ErrorData>(response) {

                        @Override
                        protected void onSuccess(ErrorData data) {
                            view.showSuccessFollowMessage();
                            view.setStatusFollowed();
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
                com.varteq.catslovers.utils.Log.e(TAG, "followFeedstation onFailure " + t.getMessage());
            }
        });

    }


    public void joinCatsFeedstation(CatProfile catProfile) {
        Integer feedstationId = catProfile.getFeedstationId();
        Call<BaseResponse<RUser>> call1 = ServiceGenerator.getApiServiceWithToken().joinFeedstation(feedstationId);
        call1.enqueue(new Callback<BaseResponse<RUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUser>> call, Response<BaseResponse<RUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RUser>(response) {

                        @Override
                        protected void onSuccess(RUser data) {
                            view.showSuccessFollowMessage();
                            view.setStatusJoined();
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
                com.varteq.catslovers.utils.Log.e(TAG, "joinFeedstation onFailure " + t.getMessage());
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

        int age = TimeUtils.getUtcFromLocal(cat.getBirthday().getTime());
        int nextFleaTreatment = TimeUtils.getUtcFromLocal(cat.getFleaTreatmentDate().getTime());

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

            if (uploadCatRequest != null)
                uploadCatRequest.setUtf8Charset();
            // fake location
        /*lastLocation = new Location("dddd");
        lastLocation.setLatitude(50.4437);
        lastLocation.setLongitude(30.5008);*/

            if (lastLocation != null) {
                uploadCatRequest.addParameter("lat", String.valueOf(lastLocation.getLatitude()))
                        .addParameter("lng", String.valueOf(lastLocation.getLongitude()));
                String address = Utils.getAddressByLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), view);
                if (address != null)
                    uploadCatRequest.addParameter("address", address);
                else {
                    Toaster.longToast("Google play services error. Please, reboot your phone!");
                    Log.e("uploadCatWithPhotos", "Address is null");
                    view.hideWaitDialog();
                    isCatUploading = false;
                    return;
                }
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

                            String exceptionMessage = "";
                            if (exception != null && exception.getMessage() != null) {
                                exceptionMessage = exception.getMessage();
                            }
                            Log.e("uploadCatWithPhotos ", exceptionMessage);
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            Log.d("uploadCatWithPhotos ", "onCompleted");
                            try {
                                Gson gson = new Gson();

                                BaseResponse<RCat> cat = gson.fromJson(serverResponse.getBodyAsString(), new GenericOf<>(BaseResponse.class, RCat.class));
                                if (cat != null && cat.getSuccess())
                                    view.savedSuccessfully();
                                else Toaster.longToast("An error occurred while saving");
                            } catch (Exception e) {
                            }

                            view.hideWaitDialog();
                            isCatUploading = false;
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            view.hideWaitDialog();
                            isCatUploading = false;
                            Log.d("uploadCatWithPhotos ", "canceled");
                        }
                    })
                    .setNotificationConfig(config);

            int j = 0;
            if (cat.getPhotosToRemove() != null)
                for (PhotoWithPreview photo : cat.getPhotosToRemove()) {
                    if (photo.getExpectedAction() != null && photo.getExpectedAction().equals(PhotoWithPreview.Action.DELETE))
                        uploadCatRequest.addParameter("images_delete[" + j++ + "]", String.valueOf(photo.getId()));
                }

            int i = 0;
            int currIndex = 0;
            for (PhotoWithPreview photoWithPreview : cat.getPhotos()) {
                if (photoWithPreview.getExpectedAction() != null && photoWithPreview.getExpectedAction().equals(PhotoWithPreview.Action.ADD))
                    uploadCatRequest.addFileToUpload(photoWithPreview.getPhoto(), "images[" + i++ + "]");
                if (i > 4) break;
                //if (++currIndex > 4) break;
            }

            if (cat.getAvatar() != null && cat.getAvatar().getExpectedAction() != null &&
                    cat.getAvatar().getExpectedAction().equals(PhotoWithPreview.Action.CHANGE))
                uploadCatRequest.addFileToUpload(cat.getAvatar().getPhoto(), "avatar");

            String uploadId = uploadCatRequest.startUpload();
        } catch (Exception exc) {
            Log.e("uploadCatWithPhotos", exc.getMessage(), exc);
            view.hideWaitDialog();
            isCatUploading = false;
        }
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
