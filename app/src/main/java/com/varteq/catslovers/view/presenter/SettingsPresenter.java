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
import com.varteq.catslovers.utils.qb.QbUsersHolder;
import com.varteq.catslovers.view.SettingsActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.util.Collections;

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
        view.showWaitDialog();
        Call<BaseResponse<RUserSimple>> call = ServiceGenerator.getApiServiceWithToken().getUserInfo();
        call.enqueue(new Callback<BaseResponse<RUserSimple>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUserSimple>> call, Response<BaseResponse<RUserSimple>> response) {
                if (view == null) return;
                if (response.isSuccessful()) {
                    RUserSimple user = response.body().getData();
                    if (user != null) {
                        view.setEmail(user.getEmail());
                        view.setUsername(user.getName());
                        view.setAvatar(new PhotoWithPreview(null, user.getAvatarUrl(), user.getAvatarUrlThumbnail()));
                        view.hideWaitDialog();
                        return;
                    }
                }
                view.dataLoadingFailed();
            }

            @Override
            public void onFailure(Call<BaseResponse<RUserSimple>> call, Throwable t) {
                view.dataLoadingFailed();
            }
        });
    }

    public void uploadUserSettings(String name, PhotoWithPreview avatar) {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                uploadUserSettings(name, avatar);
            }
        };
        view.showWaitDialog();

        try {
            view.registerUploadAvatarReceiver(broadcastReceiver);
            MultipartUploadRequest uploadCatRequest = new MultipartUploadRequest(view, ServiceGenerator.apiBaseUrl + "user")
                    .setMethod("PUT")
                    .setUtf8Charset()
                    .addParameter("name", name);

            if (avatar != null && avatar.getExpectedAction() != null) {
                if (avatar.getExpectedAction().equals(PhotoWithPreview.Action.CHANGE))
                    uploadCatRequest.addFileToUpload(avatar.getPhoto(), "avatar");
                else if (avatar.getExpectedAction().equals(PhotoWithPreview.Action.DELETE))
                    uploadCatRequest.addParameter("avatar_delete", "true");
            }

            uploadCatRequest.setDelegate(broadcastReceiver);

            String uploadId = uploadCatRequest.startUpload();
        } catch (Exception exc) {
            Log.e("uploadUserSettings", exc.getMessage(), exc);
        }
    }

    private UploadServiceBroadcastReceiver broadcastReceiver = new UploadServiceBroadcastReceiver() {
        @Override
        public void onProgress(Context context, UploadInfo uploadInfo) {
        }

        @Override
        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {

            String exceptionMessage = "";
            if (exception != null && exception.getMessage() != null) {
                exceptionMessage = exception.getMessage();
            }
            Log.e("uploadUserSettings ", exceptionMessage);
            if (checkNetworkErrAndShowSnackbar(exception)) {
                view.hideWaitDialog();
                return;
            }
            Toaster.longToast("An error occurred while saving");
            view.hideWaitDialog();
            view.unregisterUploadAvatarReceiver(broadcastReceiver);
        }

        @Override
        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
            Log.d("uploadUserSettings ", "onCompleted");

            try {
                Gson gson = new Gson();

                BaseResponse<RUserInfo> user = gson.fromJson(serverResponse.getBodyAsString(), new GenericOf<>(BaseResponse.class, RUserInfo.class));
                if (user != null && user.getSuccess()) {
                    if (user.getData() != null && user.getData().getAvatarUrlThumbnail() != null) {
                        qbUser.setCustomData(user.getData().getAvatarUrlThumbnail());
                    } else {
                        qbUser.setCustomData("");
                    }
                    qbUser.setFullName(user.getData() != null ? user.getData().getName() : "");
                    QbUsersHolder.getInstance().putUsers(Collections.singletonList(qbUser));
                    updateQBUser(qbUser);
                    view.unregisterUploadAvatarReceiver(broadcastReceiver);
                    return;
                }
            } catch (Exception e) {
            }
            Toaster.longToast("An error occurred while saving");
            view.hideWaitDialog();
            view.unregisterUploadAvatarReceiver(broadcastReceiver);
        }

        @Override
        public void onCancelled(Context context, UploadInfo uploadInfo) {
            Log.d("uploadUserSettings ", "canceled");
            Toaster.longToast("An error occurred while saving");
            view.hideWaitDialog();
            view.unregisterUploadAvatarReceiver(broadcastReceiver);
        }
    };

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
        return exception != null && checkNetworkErrAndShowSnackbar(exception.toString());
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
