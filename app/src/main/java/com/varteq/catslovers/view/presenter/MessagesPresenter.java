package com.varteq.catslovers.view.presenter;

import android.os.Handler;

import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.view.fragments.MessagesFragment;


public class MessagesPresenter {

    private MessagesFragment view;

    private String TAG = MessagesPresenter.class.getSimpleName();
    private Handler handler;

    public MessagesPresenter(MessagesFragment view) {
        this.view = view;
    }

    public void checkQBLogin() {
        if (handler != null) return;
        if (ChatHelper.getInstance().isLogged())
            view.registerQbChatListenersLoadDialogs();
        else if (handler == null) {
            ChatHelper.getInstance().loginToQuickBlox(view.getContext());
            handler = new Handler();
            handler.postDelayed(() -> {
                        if (view.getContext() == null)
                            return;
                        if (ChatHelper.getInstance().isLogged())
                            view.registerQbChatListenersLoadDialogs();
                        else view.stopRefreshing();
                        handler = null;
                    }
                    , 3500);
        }
    }
}