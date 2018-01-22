package com.varteq.catslovers.view.presenter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RCat;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RPhoto;
import com.varteq.catslovers.api.entity.RUser;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.model.PhotoWithPreview;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.GenericOf;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.FeedstationActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedstationPresenter {

    private String TAG = FeedstationPresenter.class.getSimpleName();
    private String CREATE_FEEDSTATION_TAG = "CREATE";

    private FeedstationActivity view;
    private boolean isCatUploading;
    private MultipartUploadRequest uploadCatRequest;

    public FeedstationPresenter(FeedstationActivity view) {
        this.view = view;
    }

    public void onPetImageSelected(Uri uri, List photoList, RecyclerView.Adapter photosAdapter) {
        if (uri != null) {
            Log.d(TAG, "onImageSelected " + uri);
            photoList.add(0, uri);
            photosAdapter.notifyItemInserted(0);
        }
    }

    public void uploadFeedstationWithPhotos(Feedstation feedstation) {
        if (isCatUploading) return;
        isCatUploading = true;
        view.showWaitDialog();

        try {
            UploadNotificationConfig config = new UploadNotificationConfig();
            config.setRingToneEnabled(false)
                    .setClearOnActionForAllStatuses(true)
                    .setTitleForAllStatuses("Uploading feedstation");

            String uploadId = UUID.randomUUID().toString();
            // update feedstation
            if (feedstation.getId() != null) {
                uploadCatRequest = new MultipartUploadRequest(view, uploadId, ServiceGenerator.apiBaseUrl + "feedstations/" + feedstation.getId())
                        .setMethod("PUT");
            }
            // create feedstation
            else
                uploadCatRequest = new MultipartUploadRequest(view, uploadId + CREATE_FEEDSTATION_TAG, ServiceGenerator.apiBaseUrl + "feedstations");

            if (uploadCatRequest != null)
                uploadCatRequest.setUtf8Charset();

            uploadCatRequest.addParameter("name", feedstation.getName())
                    .addParameter("address", feedstation.getAddress())
                    .addParameter("description", feedstation.getDescription())
                    .addParameter("time_to_feed", String.valueOf(0))
                    .addParameter("lat", String.valueOf(feedstation.getLocation().latitude))
                    .addParameter("lng", String.valueOf(feedstation.getLocation().longitude))
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
                            Log.e("uploadFeedstationWithPhotos ", exception.getMessage());
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            Log.d("uploadFeedstationWithPhotos ", "onCompleted");
                            try {
                                Gson gson = new Gson();

                                BaseResponse<RFeedstation> station = gson.fromJson(serverResponse.getBodyAsString(), new GenericOf<>(BaseResponse.class, RFeedstation.class));
                                if (station != null && station.getSuccess()) {
                                    view.savedSuccessfully();
                                    if (uploadInfo.getUploadId().contains(CREATE_FEEDSTATION_TAG))
                                        createChat(station.getData().getId(), station.getData().getName());
                                } else Toaster.longToast("An error occurred while saving");
                            } catch (Exception e) {
                            }
                            view.hideWaitDialog();
                            isCatUploading = false;
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            view.hideWaitDialog();
                            isCatUploading = false;
                            Log.d("uploadFeedstationWithPhotos ", "canceled");
                        }
                    })
                    .setNotificationConfig(config);

            if (feedstation.getTimeToEat1() != null)
                uploadCatRequest.addParameter("time_to_feed_morning",
                        String.valueOf(TimeUtils.getUtcDayStartOffset(feedstation.getTimeToEat1())));
            if (feedstation.getTimeToEat2() != null)
                uploadCatRequest.addParameter("time_to_feed_evening",
                        String.valueOf(TimeUtils.getUtcDayStartOffset(feedstation.getTimeToEat2())));

            if (feedstation.getLastFeeding() != null)
                uploadCatRequest.addParameter("last_feeding",
                        String.valueOf(TimeUtils.getUtcFromLocal(feedstation.getLastFeeding().getTime())));

            List<String> photosToDelete = new ArrayList<>();
            for (PhotoWithPreview photo : feedstation.getPhotos()) {
                if (photo.getExpectedAction() != null && photo.getExpectedAction().equals(PhotoWithPreview.Action.DELETE))
                    photosToDelete.add(String.valueOf(photo.getId()));
            }
            if (!photosToDelete.isEmpty()) {
                uploadCatRequest.addParameter("images_delete", Arrays.toString(photosToDelete.toArray(new String[0])));
            }

            int i = 0;
            int currIndex = 0;
            for (PhotoWithPreview photoWithPreview : feedstation.getPhotos()) {
                if (photoWithPreview.getExpectedAction() != null && photoWithPreview.getExpectedAction().equals(PhotoWithPreview.Action.ADD))
                    uploadCatRequest.addFileToUpload(photoWithPreview.getPhoto(), "images[" + i++ + "]");
                if (i > 4) break;
                //if (++currIndex > 4) break;
            }

            uploadCatRequest.startUpload();
        } catch (Exception exc) {
            Log.e("uploadFeedstationWithPhotos", exc.getMessage(), exc);
            view.hideWaitDialog();
            isCatUploading = false;
        }
    }

    public void getCatsImages(Integer feedstationId) {
        Call<BaseResponse<List<RCat>>> call;
        call = ServiceGenerator.getApiServiceWithToken().getFeedstatisonCats(feedstationId);
        call.enqueue(new Callback<BaseResponse<List<RCat>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RCat>>> call, Response<BaseResponse<List<RCat>>> response) {
                new BaseParser<List<RCat>>(response) {

                    @Override
                    protected void onSuccess(List<RCat> data) {
                        Log.i(TAG, String.valueOf(data.size()));
                        if (data.size() < 1) return;
                        List<CatProfile> catProfiles = getCatProfiles(data);
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

    public void addGroupPartner(Integer feedstationId, String phone) {
        phone = phone.replace(" ", "");
        phone = phone.replace("(", "");
        phone = phone.replace(")", "");

        Call<BaseResponse<RUser>> call = ServiceGenerator.getApiServiceWithToken().inviteUserToFeedstation(feedstationId, phone);

        call.enqueue(new Callback<BaseResponse<RUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUser>> call, Response<BaseResponse<RUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RUser>(response) {

                        @Override
                        protected void onSuccess(RUser data) {
                            getGroupPartners(feedstationId);
                            /*GroupPartner partner = from(data, feedstationId);
                            if (partner != null && partner.getStatus() != null &&
                                    partner.getStatus().equals(GroupPartner.Status.JOINED))
                                addUserToChat(partner.getUserId(), feedstationId);*/
                            //else addInvitedUserToChat(phone, feedstationId);
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
                Log.e(TAG, "inviteUser onFailure " + t.getMessage());
            }
        });
    }

    private void checkChat(Integer feedstationId, String name) {
        ChatHelper.getInstance().getDialogForFeedstation(feedstationId, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                if (qbChatDialogs.size() < 1)
                    createChat(feedstationId, name);
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }

    public static void addUserToChat(Integer userId, Integer feedstationId) {
        ChatHelper.getInstance().getDialogForFeedstation(feedstationId, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                if (qbChatDialogs.size() > 0)
                    ChatHelper.getInstance().addUserToDialog(qbChatDialogs.get(0), String.valueOf(userId), new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialogs, Bundle bundle) {
                            int i = 0;
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            int i = 0;
                        }
                    });
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }

    /*private void addInvitedUserToChat(String phone, Integer feedstationId) {
        ChatHelper.getInstance().getDialogForFeedstation(feedstationId, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                if (qbChatDialogs.size() > 0 && !QBChatInfo.containsInvitedUser(qbChatDialogs.get(0).getCustomData(), phone))
                    ChatHelper.getInstance().addInvitedUserToDialog(qbChatDialogs.get(0), phone, new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialogs, Bundle bundle) {
                            int i = 0;
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            int i = 0;
                        }
                    });
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }*/

    /*public void checkInvitedUsers(List<GroupPartner> users, Feedstation feedstationId){
        if (users==null || users.isEmpty()) return;

        ChatHelper.getInstance().getDialogForFeedstation(feedstationId, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                if (qbChatDialogs.size() < 1) return;

                List<String> invitedList = QBChatInfo.getInvitedUsers(qbChatDialogs.get(0).getCustomData());
                if (invitedList==null) return;

                List<GroupPartner> usersToAdd = new ArrayList<>();

                for (String phone : invitedList) {
                    for (int i = users.size()-1; i>-1; i--){
                        if (users.get(i).getPhone().equals(phone) && users.get(i).getStatus().equals(GroupPartner.Status.JOINED)){
                            usersToAdd.add(users.get(i));
                            break;
                        }
                    }
                }





                    ChatHelper.getInstance().addInvitedUserToDialog(qbChatDialogs.get(0), phone, new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialogs, Bundle bundle) {
                            int i = 0;
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            int i = 0;
                        }
                    });
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }*/

    private void createChat(Integer id, String name) {
        ChatHelper.getInstance().createEmptyPublicDialog(id, name, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                ChatHelper.getInstance().updateFeedstations();
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }

    public void getGroupPartners(Integer feedstationId) {
        if (feedstationId == null) return;

        Call<BaseResponse<List<RUser>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstationUsers(feedstationId);

        call.enqueue(new Callback<BaseResponse<List<RUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RUser>>> call, Response<BaseResponse<List<RUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RUser>>(response) {

                        @Override
                        protected void onSuccess(List<RUser> data) {
                            List<GroupPartner> partners = new ArrayList<>();
                            for (RUser user : data) {
                                partners.add(from(user, feedstationId));
                                /*if (!from(user, feedstationId).isAdmin() )//&& from(user, feedstationId).getUserId() == 6)
                                    deleteGroupPartner(feedstationId, from(user, feedstationId).getUserId());*/
                                /*GroupPartner partner = from(user, feedstationId);
                                if (partner.getStatus().equals(GroupPartner.Status.JOINED) && !partner.isAdmin())
                                    addUserToChat(partner.getUserId(), feedstationId);*/
                            }
                            view.refreshGroupPartners(partners);
                            /*if (feedstation.getIsPublic() && feedstation.getUserRole() != null &&
                                    feedstation.getUserRole().equals(Feedstation.UserRole.ADMIN))
                                checkInvitedUsers(partners, feedstation);*/
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

    private GroupPartner from(RUser user, Integer feedstationId) {
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
            /*if (status.equals(GroupPartner.Status.INVITED))
                joinFeedstation(feedstationId);*/
        } else if (name == null || name.isEmpty())
            name = user.getUserInfo().getPhone();
        return new GroupPartner(user.getUserInfo().getAvatarUrlThumbnail(), user.getUserId(), name, user.getUserInfo().getPhone(), status, isAdmin);
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
                Log.e(TAG, "getFeedstations onFailure " + t.getMessage());
            }
        });
    }

    public void deleteGroupPartner(Integer feedstationId, int userId) {

        if (feedstationId == null) return;

        Call<BaseResponse<ErrorData>> call = ServiceGenerator.getApiServiceWithToken().deleteUserFromFeedstation(feedstationId, userId);
        call.enqueue(new Callback<BaseResponse<ErrorData>>() {
            @Override
            public void onResponse(Call<BaseResponse<ErrorData>> call, Response<BaseResponse<ErrorData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<ErrorData>(response) {

                        @Override
                        protected void onSuccess(ErrorData data) {
                            getGroupPartners(feedstationId);
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
                Log.e(TAG, "joinFeedstation onFailure " + t.getMessage());
            }
        });
    }
}
