package com.varteq.catslovers.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.StorageUtils;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.utils.qb.imagepick.ImagePickHelper;
import com.varteq.catslovers.utils.qb.imagepick.OnImagePickedListener;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends BaseActivity implements OnImagePickedListener {

    private String TAG = SignUpActivity.class.getSimpleName();
    @BindView(R.id.avatar)
    RoundedImageView avatarImageView;
    @BindView(R.id.continue_button)
    Button continueButton;
    @BindView(R.id.twitter_linearLayout)
    LinearLayout twitterButton;
    @BindView(R.id.facebook_linearLayout)
    LinearLayout facebookButton;
    @BindView(R.id.email_editText)
    EditText emailEditText;
    @BindView(R.id.name_editText)
    EditText nameEditText;
    @BindView(R.id.upload_photo_button)
    Button uploadPhotoButton;
    private String avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);
        nameEditText.requestFocus();

        avatar = Profile.getUserAvatar(this);
        updateAvatar();

        findViewById(R.id.continue_button).setOnClickListener(
                view -> {
                    if (isInputValid()) {
                        Log.d(TAG, "continue_button OnClick");
                        Profile.saveUser(this, nameEditText.getText().toString(),
                                emailEditText.getText().toString());
                        startActivity(new Intent(SignUpActivity.this, ConfirmNumberActivity.class));
                    }
                });
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    @OnClick(R.id.upload_photo_button)
    void uploadPhoto() {
        new ImagePickHelper().pickAnImage(this, 0);
    }
    @OnClick(R.id.facebook_linearLayout)
    void facebookSignUp() {
        Toaster.shortToast(R.string.coming_soon);
    }
    @OnClick(R.id.twitter_linearLayout)
    void twitterSignUp() {
        Toaster.shortToast(R.string.coming_soon);
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

    final int THUMBSIZE = 250;
    private void updateAvatar() {
        if (avatar != null && !avatar.isEmpty()) {
            File image = new File(StorageUtils.getImagePickerDirectoryFile() +
                    avatar.substring(avatar.lastIndexOf("/"), avatar.length()));
            if (image.exists()) {
                Glide.with(this)
                        .load(image)
                        .apply(new RequestOptions().override(THUMBSIZE, THUMBSIZE))
                        .into(avatarImageView);
                return;
            } else Profile.saveUserAvatar(this, "");
        }
        avatarImageView.setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        if (null != file) {
            avatar = file.getPath();
            Profile.saveUserAvatar(this, avatar);
            updateAvatar();
        }
    }

    @Override
    public void onImagesPicked(int requestCode, List<File> file) {

    }

    @Override
    public void onVideoPicked(int requestCode, File file, Bitmap preview) {

    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {

    }

    @Override
    public void onImagePickClosed(int requestCode) {

    }
}
