package com.varteq.catslovers.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.LogLevel;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.AppController;
import com.varteq.catslovers.R;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.model.QBChatInfo;
import com.varteq.catslovers.utils.qb.QbDialogHolder;
import com.varteq.catslovers.utils.qb.QbDialogUtils;
import com.varteq.catslovers.utils.qb.QbUsersHolder;
import com.varteq.catslovers.utils.qb.callback.QbEntityCallbackTwoTypeWrapper;
import com.varteq.catslovers.utils.qb.callback.QbEntityCallbackWrapper;
import com.varteq.catslovers.view.presenter.MapPresenter;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();

    public static final int DIALOG_ITEMS_PER_PAGE = 100;
    public static final int CHAT_HISTORY_ITEMS_PER_PAGE = 50;
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

    private static ChatHelper instance;

    private QBChatService qbChatService;
    private List<Feedstation> myFeedstations;
    private long lastStationsUpdateTime;
    private long UPDATE_PERIOD = 5000;

    public static synchronized ChatHelper getInstance() {
        if (instance == null) {
            QBSettings.getInstance().setLogLevel(LogLevel.DEBUG);
            QBChatService.setDebugEnabled(true);
            QBChatService.setConfigurationBuilder(buildChatConfigs());
            instance = new ChatHelper();
        }
        return instance;
    }

    public boolean isLogged() {
        return QBChatService.getInstance().isLoggedIn();
    }

    public static QBUser getCurrentUser() {
        return QBChatService.getInstance().getUser();
    }

    private ChatHelper() {
        qbChatService = QBChatService.getInstance();
        qbChatService.setUseStreamManagement(true);
        updateMyFeedstations();
    }

    private static QBChatService.ConfigurationBuilder buildChatConfigs() {
        QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();

        configurationBuilder.setSocketTimeout(300);
        configurationBuilder.setUseTls(true);
        configurationBuilder.setKeepAlive(true);
        configurationBuilder.setAutojoinEnabled(false);
        configurationBuilder.setAutoMarkDelivered(true);
        configurationBuilder.setReconnectionAllowed(true);
        configurationBuilder.setAllowListenNetwork(true);

        return configurationBuilder;
    }

    public void addConnectionListener(ConnectionListener listener) {
        qbChatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        qbChatService.removeConnectionListener(listener);
    }

    public void singUp(final QBUser user, final QBEntityCallback<Void> callback) {
        // Create REST API session on QuickBlox
        QBUsers.signUp(user).performAsync(new QbEntityCallbackTwoTypeWrapper<QBUser, Void>(callback) {
            @Override
            public void onSuccess(QBUser qbUser, Bundle args) {
                user.setId(qbUser.getId());
                callback.onSuccess(null, null);
                loginToChat(user, new QbEntityCallbackWrapper<>(callback));
            }
        });
    }

    public void login(final QBUser user, final QBEntityCallback<Void> callback, QBEntityCallback<QBUser> callbackWithFileId) {
        // Create REST API session on QuickBlox
        QBUsers.signIn(user).performAsync(new QbEntityCallbackTwoTypeWrapper<QBUser, Void>(callback) {
            @Override
            public void onSuccess(QBUser qbUser, Bundle args) {
                user.setId(qbUser.getId());
                if (callbackWithFileId != null) {
                    user.setFileId(qbUser.getFileId());
                    user.setOldPassword(qbUser.getOldPassword());
                    callbackWithFileId.onSuccess(user, null);
                }
                loginToChat(user, new QbEntityCallbackWrapper<>(callback));
            }
        });
    }

    public void signOut() {
        qbChatService.destroy();
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
            }

            @Override
            public void onError(QBResponseException e) {
            }
        });
        QbUsersHolder.getInstance().destroy();
        QbDialogHolder.getInstance().clear();
    }

    public void loginToChat(final QBUser user, final QBEntityCallback<Void> callback) {
        if (qbChatService.isLoggedIn()) {
            callback.onSuccess(null, null);
            return;
        }

        qbChatService.login(user, callback);
    }

    private AsyncTask<Void, Void, Void> loginTask;

    public void loginToQuickBlox(Context context) {
        if (context == null || !Profile.isUserLoggedIn(context) ||
                (loginTask != null && loginTask.getStatus().equals(AsyncTask.Status.RUNNING)))
            return;
        int qbUserId = Profile.getQBUserId(context);
        final QBUser qbUser = new QBUser();
        qbUser.setId(qbUserId);
        qbUser.setPassword(AppController.USER_PASS);

        /*if (qbUserId<0){
            String id = Profile.getUserId(this);
            qbUser.setLogin(id);

            ChatHelper.getInstance().login(qbUser, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Profile.setQBUserId(getApplicationContext(), ChatHelper.getCurrentUser().getId());
                }

                @Override
                public void onError(QBResponseException e) {}
            }, null);
            return;
        }*/

        try {
            loginTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    loginToChat(qbUser, new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                        }

                        @Override
                        public void onError(QBResponseException e) {
                        }
                    });
                    return null;
                }
            };
            loginTask.execute();
        } catch (Exception e) {
        }
    }

    public void join(QBChatDialog chatDialog, final QBEntityCallback<Void> callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        chatDialog.join(history, callback);
    }

    public void leaveChatDialog(QBChatDialog chatDialog) throws XMPPException, SmackException.NotConnectedException {
        chatDialog.leave();
    }

    public void destroy() {
        qbChatService.destroy();
    }

    public void createDialogWithSelectedUsers(final List<QBUser> users,
                                              final QBEntityCallback<QBChatDialog> callback) {

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

    public void createPublicDialogWithUsers(int feedstationId, String name, final List<QBUser> users,
                                            final QBEntityCallback<QBChatDialog> callback) {

        QBChatDialog dialog = QbDialogUtils.createPublicDialog(name, users);
        dialog.setCustomData(new QBChatInfo(feedstationId));

        QBRestChatService.createChatDialog(dialog).performAsync(
                new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        QbDialogHolder.getInstance().addDialog(dialog);
                        QbUsersHolder.getInstance().putUsers(users);
                        super.onSuccess(dialog, args);
                    }
                });
    }

    public void createEmptyPublicDialog(int feedstationId, String name,
                                        final QBEntityCallback<QBChatDialog> callback) {

        QBChatDialog dialog = QbDialogUtils.createPublicDialog(name, new ArrayList<>());
        dialog.setCustomData(new QBChatInfo(feedstationId));

        QBRestChatService.createChatDialog(dialog).performAsync(
                new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        QbDialogHolder.getInstance().addDialog(dialog);
                        super.onSuccess(dialog, args);
                    }
                });
    }

    public void addUserToDialog(QBChatDialog qbDialog,
                                String userId,
                                QBEntityCallback<QBChatDialog> callback) {

        ArrayList<String> username = new ArrayList<>();
        username.add(userId);
        QBUsers.getUsersByLogins(username, null).performAsync(
                new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                        if (result == null || result.isEmpty()) {
                            callback.onError(new QBResponseException("User not found"));
                            return;
                        }

                        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
                        qbRequestBuilder.addUsers(result.get(0));

                        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(
                                new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                                    @Override
                                    public void onSuccess(QBChatDialog qbDialog, Bundle bundle) {
                                        QbUsersHolder.getInstance().putUsers(result);
                                        QbDialogUtils.logDialogUsers(qbDialog);
                                        super.onSuccess(qbDialog, bundle);
                                    }
                                });
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        callback.onError(e);
                    }
                });
    }

    public void addInvitedUserToDialog(QBChatDialog qbDialog,
                                       String userId,
                                       QBEntityCallback<QBChatDialog> callback) {

        QBDialogRequestBuilder qbRequestBuilder = QBChatInfo.getDialogRequestForAddInvitedUser(userId);

        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(callback);
    }

    public void removeInvitedUserFromDialog(QBChatDialog qbDialog,
                                            Integer userId,
                                            QBEntityCallback<QBChatDialog> callback) {

        QBDialogRequestBuilder qbRequestBuilder = QBChatInfo.getDialogRequestForRemoveInvitedUser(userId);

        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(callback);
    }

    public void getDialogForFeedstation(int feedstationId, final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        QBRequestGetBuilder builder = QBChatInfo.getRequestBuilder(feedstationId);
        getDialogs(builder, callback);
    }

    public void deleteDialogs(Collection<QBChatDialog> dialogs, final QBEntityCallback<ArrayList<String>> callback) {
        StringifyArrayList<String> dialogsIds = new StringifyArrayList<>();
        for (QBChatDialog dialog : dialogs) {
            dialogsIds.add(dialog.getDialogId());
        }

        QBRestChatService.deleteDialogs(dialogsIds, false, null).performAsync(callback);
    }

    public void deleteDialog(QBChatDialog qbDialog, QBEntityCallback<Void> callback) {
        if (qbDialog.getType() == QBDialogType.PUBLIC_GROUP) {
            Toaster.shortToast(R.string.public_group_chat_cannot_be_deleted);
        } else {
            QBRestChatService.deleteDialog(qbDialog.getDialogId(), false)
                    .performAsync(new QbEntityCallbackWrapper<Void>(callback));
        }
    }

    public void exitFromDialog(QBChatDialog qbDialog, QBEntityCallback<QBChatDialog> callback) {
        try {
            leaveChatDialog(qbDialog);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            callback.onError(new QBResponseException(e.getMessage()));
        }

        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
        qbRequestBuilder.removeUsers(QBChatService.getInstance().getUser().getId());

        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(callback);
    }

    public void updateDialogUsers(QBChatDialog qbDialog,
                                  final List<QBUser> newQbDialogUsersList,
                                  QBEntityCallback<QBChatDialog> callback) {
        List<QBUser> addedUsers = QbDialogUtils.getAddedUsers(qbDialog, newQbDialogUsersList);
        List<QBUser> removedUsers = QbDialogUtils.getRemovedUsers(qbDialog, newQbDialogUsersList);

        QbDialogUtils.logDialogUsers(qbDialog);
        QbDialogUtils.logUsers(addedUsers);
        Log.w(TAG, "=======================");
        QbDialogUtils.logUsers(removedUsers);

        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
        if (!addedUsers.isEmpty()) {
            qbRequestBuilder.addUsers(addedUsers.toArray(new QBUser[addedUsers.size()]));
        }
        if (!removedUsers.isEmpty()) {
            qbRequestBuilder.removeUsers(removedUsers.toArray(new QBUser[removedUsers.size()]));
        }
        qbDialog.setName(DialogUtils.createChatNameFromUserList(
                newQbDialogUsersList.toArray(new QBUser[newQbDialogUsersList.size()])));

        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(
                new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                    @Override
                    public void onSuccess(QBChatDialog qbDialog, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(newQbDialogUsersList);
                        QbDialogUtils.logDialogUsers(qbDialog);
                        super.onSuccess(qbDialog, bundle);
                    }
                });
    }

    public void loadChatHistory(QBChatDialog dialog, int skipPagination,
                                final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setSkip(skipPagination);
        customObjectRequestBuilder.setLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        customObjectRequestBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);

        QBRestChatService.getDialogMessages(dialog, customObjectRequestBuilder).performAsync(
                new QbEntityCallbackWrapper<ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {

                        Set<Integer> userIds = new HashSet<>();
                        for (QBChatMessage message : qbChatMessages) {
                            userIds.add(message.getSenderId());
                        }

                        if (!userIds.isEmpty()) {
                            getUsersFromMessages(qbChatMessages, userIds, callback);
                        } else {
                            callback.onSuccess(qbChatMessages, bundle);
                        }
                        // Not calling super.onSuccess() because
                        // we're want to load chat users before triggering the callback
                    }
                });
    }

    public void getDialogs(QBRequestGetBuilder customObjectRequestBuilder, final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        if (lastStationsUpdateTime + UPDATE_PERIOD < System.currentTimeMillis())
            updateMyFeedstations();
        customObjectRequestBuilder.setLimit(DIALOG_ITEMS_PER_PAGE);

        QBRestChatService.getChatDialogs(null, customObjectRequestBuilder).performAsync(
                new QbEntityCallbackWrapper<ArrayList<QBChatDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle args) {
                        Iterator<QBChatDialog> dialogIterator = dialogs.iterator();
                        while (dialogIterator.hasNext()) {
                            QBChatDialog dialog = dialogIterator.next();
                            if (dialog.getType() == QBDialogType.PUBLIC_GROUP) {
                                Integer feedstationId = QBChatInfo.getFeedstationId(dialog.getCustomData());
                                if (!containsId(feedstationId))
                                    dialogIterator.remove();
                            }
                        }

                        getUsersFromDialogs(dialogs, callback);
                        // Not calling super.onSuccess() because
                        // we want to load chat users before triggering callback
                    }
                });
    }

    private boolean containsId(Integer feedstationId) {
        if (myFeedstations != null && feedstationId != null) {
            for (Feedstation feedstation : myFeedstations) {
                if (feedstation.getStatus().equals(GroupPartner.Status.JOINED) && feedstation.getId().equals(feedstationId))
                    return true;
            }
        }
        return false;
    }

    public void getDialogById(String dialogId, final QBEntityCallback<QBChatDialog> callback) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(callback);
    }

    public void getUsersFromDialog(QBChatDialog dialog,
                                   final QBEntityCallback<ArrayList<QBUser>> callback) {
        List<Integer> userIds = dialog.getOccupants();

        final ArrayList<QBUser> users = new ArrayList<>(userIds.size());
        for (Integer id : userIds) {
            users.add(QbUsersHolder.getInstance().getUserById(id));
        }

        // If we already have all users in memory
        // there is no need to make REST requests to QB
        if (userIds.size() == users.size()) {
            callback.onSuccess(users, null);
            return;
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(qbUsers);
                        callback.onSuccess(qbUsers, bundle);
                    }
                });
    }

    public QBUser getInterlocutorUserFromDialog(QBChatDialog dialog) {
        if (ChatHelper.getCurrentUser().getId() == null) return null;
        for (Integer id : dialog.getOccupants()) {
            if (!ChatHelper.getCurrentUser().getId().equals(id))
                return QbUsersHolder.getInstance().getUserById(id);
        }
        return null;
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback) {
        loadFileAsAttachment(file, callback, null);
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback,
                                     QBProgressCallback progressCallback) {
        QBContent.uploadFileTask(file, true, null, progressCallback).performAsync(
                new QbEntityCallbackTwoTypeWrapper<QBFile, QBAttachment>(callback) {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
                        attachment.setId(qbFile.getId().toString());
                        attachment.setUrl(qbFile.getPublicUrl());
                        callback.onSuccess(attachment, bundle);
                    }
                });
    }

    private void getUsersFromDialogs(final ArrayList<QBChatDialog> dialogs,
                                     final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        List<Integer> userIds = new ArrayList<>();
        for (QBChatDialog dialog : dialogs) {
            userIds.addAll(dialog.getOccupants());
            userIds.add(dialog.getLastMessageUserId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        callback.onSuccess(dialogs, params);
                    }
                });
    }

    private void getUsersFromMessages(final ArrayList<QBChatMessage> messages,
                                      final Set<Integer> userIds,
                                      final QBEntityCallback<ArrayList<QBChatMessage>> callback) {

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        callback.onSuccess(messages, params);
                    }
                });
    }

    public void updateFeedstations() {
        updateMyFeedstations();
    }

    private void updateMyFeedstations() {
        lastStationsUpdateTime = System.currentTimeMillis();

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            myFeedstations = MapPresenter.from(data);
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
            public void onFailure(Call<BaseResponse<List<RFeedstation>>> call, Throwable t) {
                com.varteq.catslovers.utils.Log.e(TAG, "updateMyFeedstations onFailure " + t.getMessage());
            }
        });
    }
}