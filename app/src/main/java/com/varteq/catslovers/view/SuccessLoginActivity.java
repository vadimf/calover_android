package com.varteq.catslovers.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.qb.imagepick.ImagePickHelper;
import com.varteq.catslovers.utils.qb.imagepick.OnImagePickedListener;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SuccessLoginActivity extends AppCompatActivity implements OnImagePickedListener {

    private final int THUMBSIZE = 100;
    @BindView(R.id.cat_profile_avatar_roundedImageView)
    RoundedImageView avatarImageView;
    @BindView(R.id.cat_name_editText)
    EditText catNameEditText;
    private int REQUEST_CODE_GET_AVATAR = 111;
    private File avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.next_button)
    void onNextClick() {
        if (catNameEditText.getText() == null || catNameEditText.getText().toString().isEmpty()) {
            Toaster.shortToast("Cat's name should not be empty");
            return;
        }
        CatProfileActivity.startInCreateMode(this, catNameEditText.getText().toString(), avatar);
    }

    @OnClick(R.id.later_button)
    void onLaterClick() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finishAffinity();
    }

    @OnClick(R.id.dont_have_cat_button)
    void onDontHaveCatClick() {
        onLaterClick();
    }

    @OnClick(R.id.cameraImageView)
    void selectAvatar() {
        new ImagePickHelper().pickAnImage(this, REQUEST_CODE_GET_AVATAR);
    }

    private void updateAvatar() {
        Glide.with(this)
                .load(avatar)
                .apply(new RequestOptions().override(THUMBSIZE, THUMBSIZE).error(R.drawable.cat_cover_avatar))
                .into(avatarImageView);
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        if (file != null) {
            if (REQUEST_CODE_GET_AVATAR == requestCode) {
                avatar = file;
                updateAvatar();
            }
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
