package com.varteq.catslovers.view.presenter;

import android.content.Context;
import android.view.View;

import com.google.gson.Gson;
import com.varteq.catslovers.R;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RUser;
import com.varteq.catslovers.api.entity.RUserInfo;
import com.varteq.catslovers.api.entity.RUserSimple;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.GenericOf;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.MainActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.varteq.catslovers.utils.NetworkUtils.isNetworkErr;

public class MainPresenter {

    private String TAG = MainPresenter.class.getSimpleName();

    private MainActivity view;
    private MainPresenter.OneTimeOnClickListener errListener;

    public MainPresenter(MainActivity view) {
        this.view = view;
    }

    public void checkMyPrivateFeedstation() {
        String id = Profile.getUserStation(view);
        if (!id.isEmpty()) {
            view.showNewFeedPostActivity();
            return;
        }

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            for (RFeedstation feedstation : data) {
                                if (!feedstation.getIsPublic() && feedstation.getPermissions() != null && feedstation.getPermissions().getRole().equals("admin")) {
                                    Profile.setUserStation(view, String.valueOf(feedstation.getId()));
                                    view.showNewFeedPostActivity();
                                    return;
                                }
                            }
                            view.onPrivateFeedstationNotFound();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RFeedstation>>> call, Throwable t) {
            }
        });
    }

    public void checkInvitations() {

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getInvitations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            view.invitationsLoaded(MapPresenter.from(data));
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RFeedstation>>> call, Throwable t) {
            }
        });
    }

    public void joinFeedstation(Integer feedstationId) {

        if (feedstationId == null) return;

        Call<BaseResponse<RUser>> call = ServiceGenerator.getApiServiceWithToken().joinFeedstation(feedstationId);
        call.enqueue(new Callback<BaseResponse<RUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUser>> call, Response<BaseResponse<RUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RUser>(response) {

                        @Override
                        protected void onSuccess(RUser data) {
                            view.onSuccessJoin();
                            //FeedstationPresenter.addUserToChat(data.getUserId(), feedstationId);
                            ChatHelper.getInstance().updateFeedstations();
                            checkInvitations();
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
                Log.e(TAG, "joinFeedstation onFailure " + t.getMessage());
            }
        });
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
                            checkInvitations();
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
                Log.e(TAG, "leaveFeedstation onFailure " + t.getMessage());
            }
        });
    }

    public void loadUserInfo() {
        Call<BaseResponse<RUserSimple>> call = ServiceGenerator.getApiServiceWithToken().getUserInfo();
        call.enqueue(new Callback<BaseResponse<RUserSimple>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUserSimple>> call, Response<BaseResponse<RUserSimple>> response) {
                if (response.isSuccessful()) {
                    RUserSimple user = response.body().getData();
                    if (user != null) {
                        if (user.getName() != null)
                            view.setNavigationUsername(user.getName());
                        if (user.getAvatarUrlThumbnail() != null)
                            view.updateNavigationAvatar(user.getAvatarUrlThumbnail());
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RUserSimple>> call, Throwable t) {

            }
        });
    }


    public void uploadAvatar() {
        String path = Profile.getUserAvatar(view);
        if (path.isEmpty()) {
            //  updateQBUser(qbUser);

            return;
        }


        try {
            MultipartUploadRequest uploadCatRequest = new MultipartUploadRequest(view, ServiceGenerator.apiBaseUrl + "user")
                    .setMethod("PUT")
                    .addFileToUpload(path, "avatar");

            uploadCatRequest.setDelegate(new UploadStatusDelegate() {
                @Override
                public void onProgress(Context context, UploadInfo uploadInfo) {
                    //   view.setAvatarUploadingProgress(String.valueOf(uploadInfo.getProgressPercent()));
                }

                @Override
                public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                    checkNetworkErrAndShowToast(exception);
                    //   view.setAvatarUploadingProgress("Uploading error");
                }

                @Override
                public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                    try {
                        Gson gson = new Gson();

                        BaseResponse<RUserInfo> user = gson.fromJson(serverResponse.getBodyAsString(), new GenericOf<>(BaseResponse.class, RUserInfo.class));
                        if (user != null && user.getSuccess() && user.getData() != null && user.getData().getAvatarUrlThumbnail() != null) {
                            //   qbUser.setCustomData(user.getData().getAvatarUrlThumbnail());
                            Toaster.shortToast("Avatar uploading completed");
                        }
                    } catch (Exception e) {
                    }
                    // updateQBUser(qbUser);
                    // view.setAvatarUploadingProgress("Uploading completed");
                }

                @Override
                public void onCancelled(Context context, UploadInfo uploadInfo) {
                }
            });


            String uploadId = uploadCatRequest.startUpload();
        } catch (Exception exc) {
            Log.e("uploadAvatar", exc.getMessage(), exc);
        }
    }


    private boolean checkNetworkErrAndShowToast(Exception exception) {
        return checkNetworkErrAndShowToast(exception.toString());
    }

    private boolean checkNetworkErrAndShowToast(String exception) {
        if (isNetworkErr(exception)) {
            Toaster.shortToast(view.getString(R.string.network_error_message));
            return true;
        } else return false;
    }

    public abstract class OneTimeOnClickListener implements View.OnClickListener {

        private boolean isExecuted;

        @Override
        public void onClick(View view) {
            if (isExecuted) return;
            isExecuted = true;
            onClick();
        }

        protected abstract void onClick();
    }

}