package com.varteq.catslovers;

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
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.varteq.catslovers.view.ValidateNumberActivity;

import java.util.Locale;
import java.util.Map;

public class AuthPresenter {

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
            }
        }
    };

    private void signUp() {
        // Read user data and register
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();


        //userAttributes.addAttribute(CognitoAuthHelper.getSignUpFieldsC2O().get("Given name").toString(), userInput);
        userAttributes.addAttribute(CognitoAuthHelper.getSignUpFieldsC2O().get("Phone number").toString(), username);

        /*userInput = email.getText().toString();
        if (userInput != null) {
            if (userInput.length() > 0) {
                userAttributes.addAttribute(CognitoAuthHelper.getSignUpFieldsC2O().get(email.getHint()).toString(), userInput);
            }
        }*/

        /*userInput = phone.getText().toString();
        if (userInput != null) {
            if (userInput.length() > 0) {
                userAttributes.addAttribute(CognitoAuthHelper.getSignUpFieldsC2O().get(phone.getHint()).toString(), userInput);
            }
        }*/

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
                confirmSignUp(cognitoUserCodeDeliveryDetails);
            }
        }

        @Override
        public void onFailure(Exception exception) {
            //closeWaitDialog();
            view.showDialogMessage("Sign up failed", CognitoAuthHelper.formatException(exception));
        }
    };

    private void confirmSignUp(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
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
            view.showDialogMessage("Confirmation failed", CognitoAuthHelper.formatException(exception));
        }
    };

    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            //Log.e(TAG, "Auth Success");
            CognitoAuthHelper.setCurrSession(cognitoUserSession);
            CognitoAuthHelper.newDevice(device);
            //Log.i("my", cognitoUserSession.getAccessToken().getJWTToken());
            //closeWaitDialog();
            view.onSuccessSignIn();
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
        // TODO: generate pass
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(username, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }

    private void getForgotPasswordCode(ForgotPasswordContinuation forgotPasswordContinuation) {
        this.forgotPasswordContinuation = forgotPasswordContinuation;

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

    /*private void showCodeDialog(CodeDialogClickListener listener) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_test_verification_code);
        dialog.setCancelable(false);

        EditText codeEditText = dialog.findViewById(R.id.code_EditText);
        Button okButton = dialog.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener!=null)
                listener.onCodeConfirmed(codeEditText.getText().toString());
        });

        dialog.show();
    }*/
}
