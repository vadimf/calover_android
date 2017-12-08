package com.varteq.catslovers.view.presenter;

import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.AppController;
import com.varteq.catslovers.Log;
import com.varteq.catslovers.Profile;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.view.MainActivity;


public class MainPresenter {

    private MainActivity view;

    private String TAG = MainPresenter.class.getSimpleName();

    public MainPresenter(MainActivity view) {
        this.view = view;
    }

    public void checkQBLogin() {
        //Map<String, String> settings = CognitoAuthHelper.getCurrUser().getAttributes().getAttributes();

        //if (CognitoAuthHelper.getCurrUser()==null) return;
        //if (!settings.containsKey("username")) return;
        if (ChatHelper.getCurrentUser() != null) {
            view.showChat();
            return;
        }
        Profile.setUserPhone(view, "+380935772101");
        //Profile.saveUser(view, "Nata", "n@t.com");
        if (Profile.getUserPhone(view).isEmpty()) {
            return;
            //Profile.setUserPhone(view, "+380935772102");
        }

        final QBUser qbUser = new QBUser(Profile.getUserPhone(view), AppController.USER_PASS);
        //qbUser.setExternalId(profile.getUserId());
        //qbUser.setWebsite(profile.getPicture());
        //qbUser.setFullName(Profile.getUserName(view));

        ChatHelper.getInstance().login(qbUser, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Log.i(TAG, "chat login success");
                view.showChat();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, e.getMessage());
                //Log.e(TAG, String.valueOf(e.getHttpStatusCode()));
                if (e.toString().contains("Bad timestamp")) {
                    view.showLongError(e.getLocalizedMessage(), null);
                } else if (e.getHttpStatusCode() == 401) {
                    ChatHelper.getInstance().singUp(qbUser, new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            Log.i(TAG, "chat singUp success");
                            view.showChat();
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Log.e(TAG, "chat singUp error");
                        }
                    });
                }
            }
        });
    }
}
