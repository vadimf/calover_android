package com.varteq.catslovers.view;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.varteq.catslovers.R;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.AuthToken;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.qb.CognitoAuthHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            getAPIToken(cognitoUserSession.getAccessToken().getJWTToken());
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

    private void getAPIToken(String token) {
        Call<BaseResponse<AuthToken>> call = ServiceGenerator.getApiService().auth(token);
        call.enqueue(new Callback<BaseResponse<AuthToken>>() {
            @Override
            public void onResponse(Call<BaseResponse<AuthToken>> call, Response<BaseResponse<AuthToken>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<AuthToken>(response) {
                        @Override
                        protected void onSuccess(AuthToken data) {
                            Log.d(TAG, "onSuccess");
                            hideWaitDialog();
                            // TODO data.getToken()
                            // TODO run MainActivity
                            Toaster.shortToast(R.string.ComingSoon);
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, "onFailure");
                            hideWaitDialog();
                            if (error != null)
                                Toaster.shortToast(error.getMessage());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<AuthToken>> call, Throwable t) {
                Log.d(TAG, "onFailure");
                hideWaitDialog();
                Toaster.shortToast(t.getMessage());
            }
        });
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
