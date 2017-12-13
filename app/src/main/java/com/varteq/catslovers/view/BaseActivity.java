package com.varteq.catslovers.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.varteq.catslovers.utils.Snackbarer;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.dialog.WaitDialog;

import java.io.Serializable;

public abstract class BaseActivity extends AppCompatActivity {

    public Context context;
    public Activity activity;
    public WaitDialog waitDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        activity = this;
    }

    // ----------------- Run Activity ------------------

    public void runActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        // ? intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void runActivity(Class<?> cls, String name, Serializable value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(name, value);
        // ? intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void runActivity(Class<?> cls, String name, Parcelable value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(name, value);
        // ? intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void runActivityForRes(Class<?> cls, int code, String name, Serializable value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(name, value);
        // ? intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, code);
    }

    public void runActivity(Class<?> cls, String name, Serializable value, String name2, Serializable value2) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(name, value);
        intent.putExtra(name2, value2);
        // ? intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    // ----------------- Wait Dialog logic ------------------

    private static int waitDialogCount = 0;

    public void showWaitDialog() {
        if (waitDialog == null) {
            waitDialog = new WaitDialog(activity);
        }
        if (waitDialogCount == 0) {
            if (waitDialog != null) {
                waitDialog.showDialog();
            }
        }
        waitDialogCount++;
    }

    public void hideWaitDialog() {
        if (waitDialogCount > 0) {
            waitDialogCount--;
        }
        if (waitDialogCount == 0) {
            if (waitDialog != null) {
                waitDialog.hideDialog();
            }
        }
    }

    // ----------------- Etc ------------------

    protected void openURLinBrowser(String url) {
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application can handle this request."
                    + " Please install a webbrowser", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void showKeyboard(boolean show, View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view == null || imm == null) {
            return;
        }
        if (show) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected abstract View getSnackbarAnchorView();

    public void showIndefiniteNetworkError(View.OnClickListener listener) {
        Snackbarer.showIndefiniteNetworkSnackbar(getSnackbarAnchorView(), listener);
    }

    public void showLongNetworkError(View.OnClickListener listener) {
        Snackbarer.showLongNetworkSnackbar(getSnackbarAnchorView(), listener);
    }

    public void showIndefiniteError(String message, View.OnClickListener listener) {
        Snackbarer.showIndefiniteSnackbar(getSnackbarAnchorView(), message, listener);
    }

    public void showLongError(String message, View.OnClickListener listener) {
        Snackbarer.showLongSnackbar(getSnackbarAnchorView(), message, listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
