package com.varteq.catslovers.view.presenter;

import android.content.Context;
import android.view.View;

import com.google.gson.Gson;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.RUserInfo;
import com.varteq.catslovers.api.entity.RUserSimple;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.GenericOf;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.SettingsActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadStatusDelegate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.varteq.catslovers.utils.NetworkUtils.isNetworkErr;

public class SettingsPresenter {
    private String TAG = SettingsPresenter.class.getSimpleName();

    SettingsActivity view;
    private OneTimeOnClickListener errListener;
    private QBUser qbUser;

    public SettingsPresenter(SettingsActivity view) {
        this.view = view;

        qbUser = ChatHelper.getCurrentUser();
    }

    public void loadUserInfo() {
        Call<BaseResponse<RUserSimple>> call = ServiceGenerator.getApiServiceWithToken().getUserInfo();
        call.enqueue(new Callback<BaseResponse<RUserSimple>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUserSimple>> call, Response<BaseResponse<RUserSimple>> response) {
                if (response.isSuccessful()) {
                    RUserSimple user = response.body().getData();
                    if (user != null) {
                        view.setEmail(user.getEmail());
                        view.setUsername(user.getName());
                        view.updateAvatar(user.getAvatarUrlThumbnail());
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
                    view.setAvatarUploadingProgress(String.valueOf(uploadInfo.getProgressPercent()));
                }

                @Override
                public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                    errListener = new OneTimeOnClickListener() {
                        @Override
                        protected void onClick() {
                            uploadCatRequest.startUpload();
                        }
                    };
                    checkNetworkErrAndShowSnackbar(exception);
                    view.setAvatarUploadingProgress("Uploading error");
                }

                @Override
                public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                    try {
                        Gson gson = new Gson();

                        BaseResponse<RUserInfo> user = gson.fromJson(serverResponse.getBodyAsString(), new GenericOf<>(BaseResponse.class, RUserInfo.class));
                        if (user != null && user.getSuccess() && user.getData() != null && user.getData().getAvatarUrlThumbnail() != null) {
                            qbUser.setCustomData(user.getData().getAvatarUrlThumbnail());
                            Toaster.shortToast("Avatar uploading completed");
                        }
                    } catch (Exception e) {
                    }
                    // updateQBUser(qbUser);
                    view.setAvatarUploadingProgress("Uploading completed");
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


    private boolean checkNetworkErrAndShowSnackbar(Exception exception) {
        return checkNetworkErrAndShowSnackbar(exception.toString());
    }

    private boolean checkNetworkErrAndShowSnackbar(String exception) {
        if (isNetworkErr(exception)) {
            view.showIndefiniteNetworkError(errListener);
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
