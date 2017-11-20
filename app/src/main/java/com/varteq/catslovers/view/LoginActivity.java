package com.varteq.catslovers.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.Auth;
import com.varteq.catslovers.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends PhotoPickerActivity {

    @BindView(R.id.avatar)
    RoundedImageView avatar;
    @BindView(R.id.continue_button)
    Button continueButton;
    @BindView(R.id.twitter_button)
    Button twitterButton;
    @BindView(R.id.facebook_button)
    Button facebookButton;
    @BindView(R.id.email_editText)
    EditText emailEditText;
    @BindView(R.id.name_editText)
    EditText nameEditText;
    @BindView(R.id.upload_photo_button)
    Button uploadPhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        nameEditText.requestFocus();

        Uri avatarUri = Auth.getUserAvatar(this);
        if (avatarUri != null && !avatarUri.toString().isEmpty())
            avatar.setImageURI(avatarUri);
        else {
            Bitmap image = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            image.eraseColor(getResources().getColor(R.color.transparent));
            avatar.setImageBitmap(image);
        }

        findViewById(R.id.continue_button).setOnClickListener(
                view -> {
                    if (isInputValid()) {
                        Auth.saveUser(this, nameEditText.getText().toString(),
                                emailEditText.getText().toString());
                        startActivity(new Intent(LoginActivity.this, ConfirmNumberActivity.class));
                    }
                });
    }

    @OnClick(R.id.upload_photo_button)
    void uploadPhoto() {
        pickPhotoWithPermission(getString(R.string.select_avatar));
    }

    private boolean isInputValid() {
        if (nameEditText.getText() == null ||
                nameEditText.getText().toString().isEmpty()) {
            nameEditText.setError("Name should not be empty");
            return false;
        } else if (emailEditText.getText() == null ||
                emailEditText.getText().toString().isEmpty()) {
            emailEditText.setError("Email should not be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
            emailEditText.setError("Email is incorrect");
            return false;
        }
        return true;
    }

    @Override
    protected void onImageSelected(Uri uri) {
        super.onImageSelected(uri);
        if (null != uri) {
            Auth.saveUserAvatar(this, uri);
            avatar.setImageURI(uri);
        }
    }
}
