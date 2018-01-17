package com.varteq.catslovers.view;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.utils.qb.imagepick.ImagePickHelper;
import com.varteq.catslovers.utils.qb.imagepick.OnImagePickedListener;
import com.varteq.catslovers.view.presenter.SettingsPresenter;

import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity implements OnImagePickedListener {
    private String TAG = SettingsActivity.class.getSimpleName();
    private String avatar;
    @BindView(R.id.avatar)
    RoundedImageView avatarImageView;
    @BindView(R.id.main_layout)
    ConstraintLayout mainLayout;
    @BindView(R.id.textView_uploading_avatar_progress)
    TextView uploadingAvatarProgressTextView;
    @BindView(R.id.textView_email)
    TextView emailTextView;
    @BindView(R.id.textView_username)
    TextView usernameTextView;
    SettingsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        avatar = Profile.getUserAvatar(this);
        presenter = new SettingsPresenter(this);
        presenter.loadUserInfo();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return mainLayout;
    }

    @OnClick(R.id.avatar)
    public void onAvatarClicked() {
        new ImagePickHelper().pickAnImage(this, 0);
    }

    private void updateAvatar() {
        if (avatar != null)
            Glide.with(this)
                    .asBitmap()
                    .load(avatar)
                    .into(new SimpleTarget<Bitmap>() {
                        final int THUMBSIZE = 250;

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (resource.getWidth() > THUMBSIZE)
                                avatarImageView.setImageBitmap(ThumbnailUtils.extractThumbnail(resource,
                                        THUMBSIZE, THUMBSIZE));
                            else
                                avatarImageView.setImageBitmap(resource);
                        }
                    });
        else
            avatarImageView.setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));
    }

    public void updateAvatar(String url) {
        if (url != null)
            Glide.with(this)
                    .asBitmap()
                    .load(url)
                    .into(avatarImageView);
        else
            updateAvatar();
    }


    @Override
    public void onImagePicked(int requestCode, File file) {
        if (null != file) {
            avatar = file.getPath();
            Profile.saveUserAvatar(this, avatar);
            updateAvatar();
            presenter.uploadAvatar();
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

    public void setEmail(String email){
        emailTextView.setText(email);
    }

    public void setUsername(String username){
        usernameTextView.setText(username);
    }

    public void setAvatarUploadingProgress(String progress) {
        if (uploadingAvatarProgressTextView.getVisibility() == View.INVISIBLE)
            uploadingAvatarProgressTextView.setVisibility(View.VISIBLE);
        uploadingAvatarProgressTextView.setText(progress);
    }

    public void hideAvatarUploadingProgress() {
        uploadingAvatarProgressTextView.setVisibility(View.INVISIBLE);
    }

    public void registerUploadAvatarReceiver(UploadServiceBroadcastReceiver broadcastReceiver) {
        broadcastReceiver.register(this);
    }

    public void unregisterUploadAvatarReceiver(UploadServiceBroadcastReceiver broadcastReceiver) {
        broadcastReceiver.unregister(this);
    }

}
