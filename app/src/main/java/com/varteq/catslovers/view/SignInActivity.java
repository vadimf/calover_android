package com.varteq.catslovers.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Toaster;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInActivity extends BaseActivity {

    private String TAG = SignInActivity.class.getSimpleName();

    @BindView(R.id.usernameEditText)
    EditText usernameEditText;
    @BindView(R.id.passwordEditText)
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        Log.d(TAG, "onCreate");
        ButterKnife.bind(this);
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.layout_root);
    }

    @OnClick(R.id.btnSignIn)
    void btnSignIn() {
        Log.d(TAG, "btnSignIn");
        Toaster.shortToast(R.string.ComingSoon);
    }

    @OnClick(R.id.btnSignUp)
    void btnSignUp() {
        Log.d(TAG, "btnSignUp");
        runActivity(SignUpActivity.class);
    }


    @OnClick(R.id.btnForgotPassword)
    void btnForgotPassword() {
        Log.d(TAG, "btnForgotPassword");
        Toaster.shortToast(R.string.ComingSoon);
    }
}
