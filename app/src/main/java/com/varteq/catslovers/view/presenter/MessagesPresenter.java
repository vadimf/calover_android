package com.varteq.catslovers.view.presenter;

import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.AppController;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.view.MainActivity;
import com.varteq.catslovers.view.fragments.MessagesFragment;


public class MessagesPresenter {

    private MessagesFragment view;

    private String TAG = MessagesPresenter.class.getSimpleName();

    public MessagesPresenter(MessagesFragment view) {
        this.view = view;
    }

    public void checkQBLogin() {
        //Map<String, String> settings = CognitoAuthHelper.getCurrUser().getAttributes().getAttributes();

        //if (CognitoAuthHelper.getCurrUser()==null) return;
        //if (!settings.containsKey("username")) return;
        if (ChatHelper.getInstance().isLogged()) {
            view.registerQbChatListenersLoadDialogs();
            return;
        }
        //Profile.setUserPhone(view.getContext(), "+380935772102");
        //Profile.saveUser(view, "Nata", "n@t.com");

        String id = Profile.getUserId(view.getContext());
        if (id.isEmpty()) {
            view.showError("Error. You should reSignIn to complete this action");
            return;
        }
        final QBUser qbUser = new QBUser(id, AppController.USER_PASS);
        //qbUser.setExternalId(profile.getUserId());
        //qbUser.setWebsite(profile.getPicture());
        //qbUser.setFullName(Profile.getUserName(view));

        ChatHelper.getInstance().login(qbUser, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Log.i(TAG, "chat login success");
                view.registerQbChatListenersLoadDialogs();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, e.getMessage());
                //Log.e(TAG, String.valueOf(e.getHttpStatusCode()));
                if (e.toString().contains("Bad timestamp")) {
                    ((MainActivity)view.getActivity()).showLongError(e.getLocalizedMessage(), null);
                } else if (e.getHttpStatusCode() == 401) {
                    qbUser.setFullName(Profile.getUserName(view.getContext()));
                    ChatHelper.getInstance().singUp(qbUser, new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            Log.i(TAG, "chat singUp success");
                            if (bundle != null) {
                                ChatHelper.getInstance().login(qbUser, new QBEntityCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid, Bundle bundle) {
                                        view.registerQbChatListenersLoadDialogs();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                }, null);
                            }
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Log.e(TAG, "chat singUp error");
                        }
                    });
                }
            }
        }, null);
    }
}
