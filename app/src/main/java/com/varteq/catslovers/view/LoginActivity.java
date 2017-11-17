package com.varteq.catslovers.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.Auth;
import com.varteq.catslovers.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

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

    private final int RESULT_LOAD_IMAGE = 0;
    private final int REQUEST_CODE_IMAGE_PERMISSION = 100;

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
        pickPhotoWithPermission();
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

    private void performSelectAvatarIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_avatar)),
                RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (null != data) {
                    Uri avatarUri = data.getData();
                    Auth.saveUserAvatar(this, avatarUri);
                    avatar.setImageURI(avatarUri);
                }
                break;
        }
    }

    public void pickPhotoWithPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_IMAGE_PERMISSION);
        } else performSelectAvatarIntent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performSelectAvatarIntent();
            } else {
                //Permission denied
            }
        }
    }
}
