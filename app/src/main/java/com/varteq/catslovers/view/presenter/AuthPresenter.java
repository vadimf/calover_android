package com.varteq.catslovers.view.presenter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
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
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import com.amazonaws.services.cognitoidentityprovider.model.CodeMismatchException;
import com.amazonaws.services.cognitoidentityprovider.model.InvalidParameterException;
import com.amazonaws.services.cognitoidentityprovider.model.LimitExceededException;
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.AppController;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.AuthToken;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.Cat;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.utils.qb.CognitoAuthHelper;
import com.varteq.catslovers.utils.qb.ItemToDisplay;
import com.varteq.catslovers.view.ValidateNumberActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.varteq.catslovers.utils.NetworkUtils.isNetworkErr;

public class AuthPresenter {

    private String TAG = AuthPresenter.class.getSimpleName();
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private String password = Utils.getPass(16);
    private String username;
    private NewPasswordContinuation newPasswordContinuation;
    private ValidateNumberActivity view;
    private CodeDialogClickListener listener;
    private OneTimeOnClickListener errListener;
    private String lastCode;
    private long lastResendTime = System.currentTimeMillis();
    private long RESEND_INTERVAL = 90 * 1000;
    private boolean isPasswordReseted;

    public AuthPresenter(String username, ValidateNumberActivity view) {
        this.username = username;
        this.view = view;
    }

    public void resetPassword() {
        //startResetCall();
        //fakeLogin();
        fakeLogin2();
    }

    private void startResetCall() {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                startResetCall();
            }
        };
        CognitoAuthHelper.getPool().getUser(username).forgotPasswordInBackground(forgotPasswordHandler);
    }

    private void fakeLogin() {
        String token = "OorXZH_4_RkQFjcSFXsBg4GQKqruhXEf";
        Profile.setAuthToken(view, token);
        ServiceGenerator.setToken(token);
        Profile.setUserPetCount(view, 0);
        view.onSuccessSignIn();
        Profile.setUserPhone(view, "+380935772102");
        Profile.saveUser(view, "RObert", "r@r.com");
        Profile.setUserLogin(view, true);
        Profile.setUserId(view, String.valueOf(6));
        Profile.setUserStation(view, "38");
    }

    private void fakeLogin2() {
        isPasswordReseted = true;
        password = "yTx6/Y1L9]45e79E";
        CognitoAuthHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
    }

    ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() {
        @Override
        public void onSuccess() {
            //closeWaitDialog();
            //view.showDialogMessage("Password successfully changed!","");
            errListener = new OneTimeOnClickListener() {
                @Override
                protected void onClick() {
                    CognitoAuthHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
                }
            };
            isPasswordReseted = true;
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
            Log.e(TAG, "forgotPasswordHandler", e);
            if (checkNetworkErrAndShowSnackbar(e))
                return;
            if (e instanceof UserNotFoundException) {
                signUp();
            } else if (e instanceof CodeMismatchException) {
                view.onCodeValidate(false);
                getForgotPasswordCode(forgotPasswordContinuation);
            }
            // Cannot reset password for the user as there is no registered/verified email or phone_number
            else if (e instanceof InvalidParameterException) {
                confirmSignUp();
            } else if (e instanceof LimitExceededException) {
                String m = e.getLocalizedMessage();
                view.showIndefiniteError(m.substring(0, m.indexOf("(")), null);
            }
        }
    };

    private void signUp() {
        // Read user data and register
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        userAttributes.addAttribute("name", Profile.getUserName(view));
        userAttributes.addAttribute("email", Profile.getEmail(view));
        userAttributes.addAttribute("phone_number", username);

        //showWaitDialog("Signing up...");

        CognitoAuthHelper.getPool().signUpInBackground(username, password, userAttributes, null, signUpHandler);
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                signUp();
            }
        };
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
            Log.e(TAG, "signUpHandler", exception);
            if (checkNetworkErrAndShowSnackbar(exception))
                return;
            if (exception instanceof InvalidParameterException)
                view.onInvalidPhoneFormat(exception.getMessage());
            view.showDialogMessage("Sign up failed", CognitoAuthHelper.formatException(exception));
        }
    };

    private void confirmSignUp() {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                confirmSignUp();
            }
        };
        listener = code -> CognitoAuthHelper.getPool().getUser(username).
                confirmSignUpInBackground(code, true, confHandler);
    }

    GenericHandler confHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            //view.showDialogMessage("Success!",userName+" has been confirmed!", true);
            errListener = new OneTimeOnClickListener() {
                @Override
                protected void onClick() {
                    CognitoAuthHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
                }
            };
            CognitoAuthHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
        }

        @Override
        public void onFailure(Exception exception) {
            Log.e(TAG, "confHandler", exception);
            if (checkNetworkErrAndShowSnackbar(exception))
                return;
            if (exception instanceof CodeMismatchException) {
                view.onCodeValidate(false);
                confirmSignUp();
            }
            view.showDialogMessage("Confirmation failed", CognitoAuthHelper.formatException(exception));
        }
    };

    private void updateUserAttributes() {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                updateUserAttributes();
            }
        };

        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute("name", Profile.getUserName(view));
        userAttributes.addAttribute("email", Profile.getEmail(view));
        CognitoAuthHelper.getPool().getUser(username).updateAttributesInBackground(userAttributes, new UpdateAttributesHandler() {
            @Override
            public void onSuccess(List<CognitoUserCodeDeliveryDetails> attributesVerificationList) {
                getAuthToken(CognitoAuthHelper.getCurrSession().getAccessToken().getJWTToken());
                Log.i(TAG, "updateUserAttributes success");
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e(TAG, "updateUserAttributes onFailure " + exception.getMessage());
            }
        });
    }

    private void getAuthToken(String token) {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                getAuthToken(token);
            }
        };
        view.showWaitDialog();
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
                                Profile.setAuthToken(view, data.getToken());
                                Profile.setUserId(view, data.getUserId());
                                ServiceGenerator.setToken(data.getToken());
                                loginToQB();
                            }
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null) {
                                Log.e(TAG, error.getMessage() + error.getCode());
                                view.showIndefiniteError(error.getMessage(), errListener);
                            }
                            view.hideWaitDialog();
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<AuthToken>> call, Throwable t) {
                /*if (t instanceof IOException) {
                    Toast.makeText(view, "this is an actual network failure :( inform the user and possibly retry", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                }*/
                checkNetworkErrAndShowSnackbar(t.toString());
                Log.e(TAG, "getApiService().auth onFailure " + t.getMessage());
                view.hideWaitDialog();
            }
        });
    }

    private void loginToQB() {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                view.showWaitDialog();
                loginToQB();
            }
        };
        String id = Profile.getUserId(view);
        if (id.isEmpty()) {
            getCats();
            return;
        }
        final QBUser qbUser = new QBUser(id, AppController.USER_PASS);
        qbUser.setFullName(Profile.getUserName(view));
        //qbUser.setExternalId(profile.getUserId());
        //qbUser.setWebsite(profile.getPicture());
        //qbUser.setFullName(profile.getFirstName() + " " + profile.getLastName());

        ChatHelper.getInstance().login(qbUser, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Log.i(TAG, "chat login success");
                getCats();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "loginToQB", e);
                if (checkNetworkErrAndShowSnackbar(e)) {
                    view.hideWaitDialog();
                    return;
                }
                if (e.getHttpStatusCode() == 401)
                    signUpToQB(qbUser);
                else {
                    view.showIndefiniteError(e.getLocalizedMessage(), errListener);
                    view.hideWaitDialog();
                }
            }
        });
    }

    private void signUpToQB(QBUser qbUser) {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                view.showWaitDialog();
                signUpToQB(qbUser);
            }
        };

        ChatHelper.getInstance().singUp(qbUser, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Log.i(TAG, "chat singUp success");
                if (bundle != null)
                    loginToQB();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "chat singUp error", e);
                if (checkNetworkErrAndShowSnackbar(e)) {
                    view.hideWaitDialog();
                    return;
                }
                getCats();
            }
        });
    }

    private void getCats() {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                view.showWaitDialog();
                getCats();
            }
        };

        Call<BaseResponse<List<Cat>>> call = ServiceGenerator.getApiServiceWithToken().getCats();
        call.enqueue(new Callback<BaseResponse<List<com.varteq.catslovers.api.entity.Cat>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<Cat>>> call, Response<BaseResponse<List<Cat>>> response) {
                Profile.setUserPetCount(view, 1);
                uploadAvatar();
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Cat>>> call, Throwable t) {
                Log.e(TAG, "getCats() onFailure " + t.getMessage());
                if (checkNetworkErrAndShowSnackbar(t.toString())) {
                    view.hideWaitDialog();
                    return;
                }
                view.onSuccessSignIn();
            }
        });
    }

    public void getPrivateFeedstation() {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                view.showWaitDialog();
                getPrivateFeedstation();
            }
        };

        Call<BaseResponse<List<RFeedstation>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstations();
        call.enqueue(new Callback<BaseResponse<List<RFeedstation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RFeedstation>>> call, Response<BaseResponse<List<RFeedstation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RFeedstation>>(response) {

                        @Override
                        protected void onSuccess(List<RFeedstation> data) {
                            for (RFeedstation station : data) {
                                if (station.getIsPublic() != null && !station.getIsPublic() &&
                                        Profile.getUserId(view).equals(station.getCreated())) {
                                    Profile.setUserStation(view, String.valueOf(station.getId()));
                                    break;
                                }
                            }
                            view.onSuccessSignIn();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                            view.onSuccessSignIn();
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RFeedstation>>> call, Throwable t) {
                Log.e(TAG, "getPrivateFeedstation onFailure " + t.getMessage());
                if (checkNetworkErrAndShowSnackbar(t.toString())) {
                    view.hideWaitDialog();
                    return;
                }
                view.onSuccessSignIn();
            }
        });
    }

    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            //Log.e(TAG, "Profile Success");
            CognitoAuthHelper.setCurrSession(cognitoUserSession);
            CognitoAuthHelper.newDevice(device);
            Log.i(TAG, "cognitoUserSession.getAccessToken " + cognitoUserSession.getAccessToken().getJWTToken());
            //closeWaitDialog();
            Profile.setUserPhone(view, username);
            if (isPasswordReseted)
                updateUserAttributes();
            else getAuthToken(CognitoAuthHelper.getCurrSession().getAccessToken().getJWTToken());
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
            if (checkNetworkErrAndShowSnackbar(e))
                return;
            if (e instanceof NotAuthorizedException) {
                resetPassword();
                Toast.makeText(view, "Verification code sent. Please confirm you number again", Toast.LENGTH_LONG).show();
            }
            view.showDialogMessage("Sign-in failed", CognitoAuthHelper.formatException(e));
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            /**
             * For Custom authentication challenge, implement your logic to present challenge to the
             * user and pass the user's responses to the continuation.
             */
            Log.i(TAG, "authenticationChallenge");
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
        Log.i(TAG, "continueWithFirstTimeSignIn");
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
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                getForgotPasswordCode(forgotPasswordContinuation);
                forgotPasswordContinuation.setPassword(password);
                forgotPasswordContinuation.setVerificationCode(lastCode);
                forgotPasswordContinuation.continueTask();
            }
        };
        this.forgotPasswordContinuation = continuation;

        listener = code -> {
            forgotPasswordContinuation.setPassword(password);
            forgotPasswordContinuation.setVerificationCode(code);
            forgotPasswordContinuation.continueTask();
        };
    }

    public void resendCode() {
        if (lastResendTime + RESEND_INTERVAL > System.currentTimeMillis()) return;
        lastResendTime = System.currentTimeMillis();

        CognitoAuthHelper.getPool().getUser(username).resendConfirmationCodeInBackground(new VerificationHandler() {
            @Override
            public void onSuccess(CognitoUserCodeDeliveryDetails verificationCodeDeliveryMedium) {

            }

            @Override
            public void onFailure(Exception exception) {

            }
        });
    }

    interface CodeDialogClickListener {
        void onCodeConfirmed(String code);
    }

    public void confirmCode(String code) {
        lastCode = code;
        if (listener != null) {
            listener.onCodeConfirmed(code);
            listener = null;
        }
    }

    private boolean checkNetworkErrAndShowSnackbar(Exception exception) {
        return checkNetworkErrAndShowSnackbar(exception.toString());
    }

    private boolean checkNetworkErrAndShowSnackbar(String exception) {
        if (isNetworkErr(exception)) {
            view.showIndefiniteNetworkError(errListener);
            return true;
        } else return false;
    }

    public abstract class OneTimeOnClickListener implements View.OnClickListener {

        private boolean isExecuted;

        @Override
        public void onClick(View view) {
            if (isExecuted) return;
            isExecuted = true;
            onClick();
        }

        protected abstract void onClick();
    }

    public void uploadAvatar() {
        errListener = new OneTimeOnClickListener() {
            @Override
            protected void onClick() {
                view.showWaitDialog();
                uploadAvatar();
            }
        };

        String path = Profile.getUserAvatar(view);
        if (path.isEmpty()) {

            return;
        }
        try {
            MultipartUploadRequest uploadCatRequest = new MultipartUploadRequest(view, ServiceGenerator.apiBaseUrl + "user")
                    .setMethod("PUT")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            Log.e("uploadAvatar ", exception.getMessage());
                            if (checkNetworkErrAndShowSnackbar(exception)) {
                                view.hideWaitDialog();
                                return;
                            }
                            getPrivateFeedstation();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            Log.d("uploadAvatar ", "onCompleted");
                            getPrivateFeedstation();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            Log.d("uploadAvatar ", "canceled");
                            getPrivateFeedstation();
                        }
                    })
                    .setNotificationConfig(null)
                    .addFileToUpload(path, "avatar");

            String uploadId = uploadCatRequest.startUpload();
        } catch (Exception exc) {
            Log.e("uploadAvatar", exc.getMessage(), exc);
        }
    }
}
