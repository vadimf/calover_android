package com.varteq.catslovers.view.presenter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.AppController;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.RUserInfo;
import com.varteq.catslovers.api.entity.RUserSimple;
import com.varteq.catslovers.model.PhotoWithPreview;
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
                    if (user != null && view != null) {
                        view.setEmail(user.getEmail());
                        view.setUsername(user.getName());
                        view.setAvatar(new PhotoWithPreview(null, user.getAvatarUrl(), user.getAvatarUrlThumbnail()));
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RUserSimple>> call, Throwable t) {

            }
        });
    }

    public void uploadUserSettings(String name, PhotoWithPreview avatar) {
        view.showWaitDialog();

        try {
            MultipartUploadRequest uploadCatRequest = new MultipartUploadRequest(view, ServiceGenerator.apiBaseUrl + "user")
                    .setMethod("PUT")
                    .setUtf8Charset()
                    .addParameter("name", name);

            if (avatar != null && avatar.getExpectedAction() != null && avatar.getExpectedAction().equals(PhotoWithPreview.Action.CHANGE))
                uploadCatRequest.addFileToUpload(avatar.getPhoto(), "avatar");

            uploadCatRequest.setDelegate(new UploadStatusDelegate() {
                @Override
                public void onProgress(Context context, UploadInfo uploadInfo) {
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
                    Toaster.longToast("An error occurred while saving");
                    view.hideWaitDialog();
                }

                @Override
                public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                    try {
                        Gson gson = new Gson();

                        BaseResponse<RUserInfo> user = gson.fromJson(serverResponse.getBodyAsString(), new GenericOf<>(BaseResponse.class, RUserInfo.class));
                        if (user != null && user.getSuccess()) {
                            if (user.getData() != null && user.getData().getAvatarUrlThumbnail() != null) {
                                qbUser.setCustomData(user.getData().getAvatarUrlThumbnail());
                            }
                            qbUser.setFullName(name);
                            updateQBUser(qbUser);
                            return;
                        }
                    } catch (Exception e) {
                    }
                    // updateQBUser(qbUser);
                    Toaster.longToast("An error occurred while saving");
                    view.hideWaitDialog();
                }

                @Override
                public void onCancelled(Context context, UploadInfo uploadInfo) {
                    view.hideWaitDialog();
                }
            });

            String uploadId = uploadCatRequest.startUpload();
        } catch (Exception exc) {
            Log.e("uploadUserSettings", exc.getMessage(), exc);
        }
    }

    private void updateQBUser(QBUser user) {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                view.showWaitDialog();
                updateQBUser(user);
            }
        };

        if (user.getOldPassword() == null)
            user.setOldPassword(AppController.USER_PASS);

        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {

            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.i(TAG, "updateQBUser success");
                Profile.saveUserAvatar(view, qbUser.getCustomData());
                view.onSavedSuccessfully();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "updateQBUser", e);
                if (checkNetworkErrAndShowSnackbar(e)) {
                    view.hideWaitDialog();
                    return;
                }
                view.showIndefiniteError(e.getLocalizedMessage(), errListener);
                view.hideWaitDialog();
            }
        });
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
