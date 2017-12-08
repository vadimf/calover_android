package com.varteq.catslovers.view;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.varteq.catslovers.utils.Snackbarer;

public abstract class BaseActivity extends AppCompatActivity {

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
