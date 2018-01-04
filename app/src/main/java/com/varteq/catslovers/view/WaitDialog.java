package com.varteq.catslovers.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.varteq.catslovers.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class WaitDialog extends Dialog {

    private boolean forceHideEnabled;
    private boolean isShowing;
    private Button retryButton;
    private List<PropertyChangeListener> retryListeners = new ArrayList<>();
    public static final String RETRY_EVENT = "retry";
    private boolean enabledShowRetry = true;

    public WaitDialog(@NonNull Context context, boolean forceHideEnabled) {
        super(context);
        this.forceHideEnabled = forceHideEnabled;
    }

    public void setForceHideEnabled(boolean forceHideEnabled) {
        this.forceHideEnabled = forceHideEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait_dialog_layout);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        retryButton = findViewById(R.id.dialog_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enabledShowRetry = false;
                show();
                new android.os.Handler().postDelayed(() -> {
                    enabledShowRetry = true;
                    notifyListeners();
                }, 3000);
                notifyListeners();
            }
        });
    }

    @Override
    public void show() {
        if (!isShowing) {
            isShowing = true;
            super.show();
            findViewById(R.id.dialog_progress).setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void hide() {
        if (!forceHideEnabled)
            forceHide();
    }

    public void forceHide() {
        super.hide();
        isShowing = false;
    }

    public synchronized void showRetry(PropertyChangeListener newListener) {
        retryListeners.add(newListener);
        if (enabledShowRetry && isShowing) {
            isShowing = false;
            findViewById(R.id.dialog_progress).setVisibility(View.INVISIBLE);
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    private void notifyListeners() {
        for (PropertyChangeListener listener : retryListeners)
            listener.propertyChange(new PropertyChangeEvent(this, RETRY_EVENT, null, null));
        retryListeners.clear();
    }
}
