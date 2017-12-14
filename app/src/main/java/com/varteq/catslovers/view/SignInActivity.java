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
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.qb.CognitoAuthHelper;
import com.varteq.catslovers.view.presenter.AuthPresenter;

import java.util.List;
import java.util.Locale;

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

    private String username;
    private String password;

    @OnClick(R.id.btnSignIn)
    void btnSignIn() {
        Log.d(TAG, "btnSignIn");
        username = usernameEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        if (username.isEmpty()) {
            Toaster.shortToast(R.string.UsernameIsEmpty);
            return;
        }
        if (password.isEmpty()) {
            Toaster.shortToast(R.string.PasswordIsEmpty);
            return;
        }
        showWaitDialog();
        CognitoAuthHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
    }

    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            Log.d(TAG, "AuthenticationHandler onSuccess");
            hideWaitDialog();
            CognitoAuthHelper.setCurrSession(cognitoUserSession);

            //TODO call getToken and run MainActivity
            Toaster.shortToast(R.string.ComingSoon);
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
            Log.d(TAG, "AuthenticationHandler getAuthenticationDetails");
            hideWaitDialog();
            if (username != null) {
                CognitoAuthHelper.setUser(username);
            }
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(username, password, null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            Log.d(TAG, "AuthenticationHandler getMFACode");
            hideWaitDialog();
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            Log.d(TAG, "AuthenticationHandler authenticationChallenge");
            hideWaitDialog();
        }

        @Override
        public void onFailure(Exception e) {
            Log.d(TAG, "AuthenticationHandler onFailure");
            hideWaitDialog();
            Toaster.shortToast(CognitoAuthHelper.formatException(e));
        }
    };

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
