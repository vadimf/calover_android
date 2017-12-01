package com.varteq.catslovers.view.presenter;

import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.CodeMismatchException;
import com.amazonaws.services.cognitoidentityprovider.model.InvalidParameterException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.varteq.catslovers.Auth;
import com.varteq.catslovers.CognitoAuthHelper;
import com.varteq.catslovers.ItemToDisplay;
import com.varteq.catslovers.Log;
import com.varteq.catslovers.Utils;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.AuthToken;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.Cat;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.view.ValidateNumberActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthPresenter {

    private String TAG = AuthPresenter.class.getSimpleName();
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private String password = Utils.getPass(16);
    private String username;
    private NewPasswordContinuation newPasswordContinuation;
    private ValidateNumberActivity view;
    private CodeDialogClickListener listener;

    public AuthPresenter(String username, ValidateNumberActivity view) {
        this.username = username;
        this.view = view;
    }

    public void resetPassword() {
        CognitoAuthHelper.getPool().getUser(username).forgotPasswordInBackground(forgotPasswordHandler);
    }

    ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() {
        @Override
        public void onSuccess() {
            //closeWaitDialog();
            //view.showDialogMessage("Password successfully changed!","");
            CognitoAuthHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
        }

        @Override
        public void getResetCode(ForgotPasswordContinuation forgotPasswordContinuation) {
            //closeWaitDialog();
            getForgotPasswordCode(forgotPasswordContinuation);
        }

        @Override
        public void onFailure(Exception e) {
            //closeWaitDialog();
            view.showDialogMessage(null, CognitoAuthHelper.formatException(e));
            if (e instanceof UserNotFoundException) {
                signUp();
            } else if (e instanceof CodeMismatchException) {
                view.onCodeValidate(false);
                getForgotPasswordCode(forgotPasswordContinuation);
            }
            // Cannot reset password for the user as there is no registered/verified email or phone_number
            else if (e instanceof InvalidParameterException) {
                confirmSignUp();
            }
        }
    };

    private void signUp() {
        // Read user data and register
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        userAttributes.addAttribute("name", Auth.getUserName(view));
        userAttributes.addAttribute("email", Auth.getEmail(view));
        userAttributes.addAttribute("phone_number", username);

        //showWaitDialog("Signing up...");

        CognitoAuthHelper.getPool().signUpInBackground(username, password, userAttributes, null, signUpHandler);

    }

    SignUpHandler signUpHandler = new SignUpHandler() {
        @Override
        public void onSuccess(CognitoUser user, boolean signUpConfirmationState,
                              CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
            // Check signUpConfirmationState to see if the user is already confirmed
            //closeWaitDialog();
            Boolean regState = signUpConfirmationState;
            if (!signUpConfirmationState) {
                confirmSignUp();
            }
        }

        @Override
        public void onFailure(Exception exception) {
            //closeWaitDialog();
            if (exception instanceof InvalidParameterException)
                view.onInvalidPhoneFormat(exception.getMessage());
            view.showDialogMessage("Sign up failed", CognitoAuthHelper.formatException(exception));
        }
    };

    private void confirmSignUp() {
        listener = code -> CognitoAuthHelper.getPool().getUser(username).
                confirmSignUpInBackground(code, true, confHandler);
    }

    GenericHandler confHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            //view.showDialogMessage("Success!",userName+" has been confirmed!", true);
            CognitoAuthHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
        }

        @Override
        public void onFailure(Exception exception) {
            if (exception instanceof CodeMismatchException) {
                view.onCodeValidate(false);
                confirmSignUp();
            }
            view.showDialogMessage("Confirmation failed", CognitoAuthHelper.formatException(exception));
        }
    };

    private void getAuthToken(String token) {
        Call<BaseResponse<AuthToken>> call = ServiceGenerator.getApiService().auth(token);
        call.enqueue(new Callback<BaseResponse<AuthToken>>() {
            @Override
            public void onResponse(Call<BaseResponse<AuthToken>> call, Response<BaseResponse<AuthToken>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<AuthToken>(response) {

                        @Override
                        protected void onSuccess(AuthToken data) {
                            if (data.getToken() != null) {
                                Log.i(TAG, "getApiService().auth success");
                                Log.i(TAG, data.getToken());
                                Auth.setAuthToken(view, data.getToken());
                                ServiceGenerator.setToken(data.getToken());
                                getCats();
                            }
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<AuthToken>> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(view, "this is an actual network failure :( inform the user and possibly retry", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                }
                Log.e(TAG, "getApiService().auth onFailure " + t.getMessage());
            }
        });
    }

    private void getCats() {
        Call<BaseResponse<List<Cat>>> call = ServiceGenerator.getApiServiceWithToken().getCats();
        call.enqueue(new Callback<BaseResponse<List<com.varteq.catslovers.api.entity.Cat>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<Cat>>> call, Response<BaseResponse<List<Cat>>> response) {
                Auth.setUserPetCount(view, 1);
                view.onSuccessSignIn();
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Cat>>> call, Throwable t) {
                view.onSuccessSignIn();
            }
        });
    }

    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            //Log.e(TAG, "Auth Success");
            CognitoAuthHelper.setCurrSession(cognitoUserSession);
            CognitoAuthHelper.newDevice(device);
            Log.i(TAG, cognitoUserSession.getAccessToken().getJWTToken());
            //closeWaitDialog();

            getAuthToken(cognitoUserSession.getAccessToken().getJWTToken());
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
            //closeWaitDialog();
            Locale.setDefault(Locale.US);
            getUserAuthentication(authenticationContinuation, username);
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            //closeWaitDialog();
            view.showDialogMessage("getMFACode", "getMFACode");
            //mfaAuth(multiFactorAuthenticationContinuation);
        }

        @Override
        public void onFailure(Exception e) {
            //closeWaitDialog();

            view.showDialogMessage("Sign-in failed", CognitoAuthHelper.formatException(e));
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            /**
             * For Custom authentication challenge, implement your logic to present challenge to the
             * user and pass the user's responses to the continuation.
             */
            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                // This is the first sign-in attempt for an admin created user
                newPasswordContinuation = (NewPasswordContinuation) continuation;
                CognitoAuthHelper.setUserAttributeForDisplayFirstLogIn(newPasswordContinuation.getCurrentUserAttributes(),
                        newPasswordContinuation.getRequiredAttributes());
                //closeWaitDialog();
                CognitoAuthHelper.setPasswordForFirstTimeLogin(password);

                int count = CognitoAuthHelper.getFirstTimeLogInItemsCount();
                for (int i = 0; i < count; i++) {
                    ItemToDisplay item = CognitoAuthHelper.getUserAttributeForFirstLogInCheck(i);
                    CognitoAuthHelper.setUserAttributeForFirstTimeLogin(item.getLabelText(), item.getDataText());
                }

                continueWithFirstTimeSignIn();
            }
        }
    };

    private void continueWithFirstTimeSignIn() {
        newPasswordContinuation.setPassword(CognitoAuthHelper.getPasswordForFirstTimeLogin());
        Map<String, String> newAttributes = CognitoAuthHelper.getUserAttributesForFirstTimeLogin();
        if (newAttributes != null) {
            for (Map.Entry<String, String> attr : newAttributes.entrySet()) {
                //Log.e(TAG, String.format("Adding attribute: %s, %s", attr.getKey(), attr.getValue()));
                newPasswordContinuation.setUserAttribute(attr.getKey(), attr.getValue());
            }
        }
        try {
            newPasswordContinuation.continueTask();
        } catch (Exception e) {
            //closeWaitDialog();
            view.showDialogMessage("Sign-in failed", CognitoAuthHelper.formatException(e));
        }
    }

    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
        if (username != null) {
            CognitoAuthHelper.setUser(username);
        }
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(username, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }

    private void getForgotPasswordCode(ForgotPasswordContinuation continuation) {
        this.forgotPasswordContinuation = continuation;

        listener = code -> {
            forgotPasswordContinuation.setPassword(password);
            forgotPasswordContinuation.setVerificationCode(code);
            forgotPasswordContinuation.continueTask();
        };
    }

    interface CodeDialogClickListener {
        void onCodeConfirmed(String code);
    }

    public void confirmCode(String code) {
        if (listener != null) {
            listener.onCodeConfirmed(code);
            listener = null;
        }
    }
}
