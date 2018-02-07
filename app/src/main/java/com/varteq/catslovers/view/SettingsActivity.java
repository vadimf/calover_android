package com.varteq.catslovers.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.PhotoWithPreview;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.qb.imagepick.ImagePickHelper;
import com.varteq.catslovers.utils.qb.imagepick.OnImagePickedListener;
import com.varteq.catslovers.view.dialog.EditTextDialog;
import com.varteq.catslovers.view.presenter.SettingsPresenter;

import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity implements OnImagePickedListener {
    private final String DEFAULT_NAME = "username";
    private final String AVATAR_KEY = "avatar_key";
    private final String NAME_KEY = "name_key";
    private final int THUMBSIZE = 250;
    private String TAG = SettingsActivity.class.getSimpleName();
    private PhotoWithPreview avatar;
    @BindView(R.id.avatar)
    RoundedImageView avatarImageView;
    @BindView(R.id.main_layout)
    ConstraintLayout mainLayout;
    //@BindView(R.id.textView_email)
    TextView emailTextView;
    @BindView(R.id.textView_change_username)
    TextView usernameTextView;
    SettingsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        presenter = new SettingsPresenter(this);
        if (savedInstanceState != null) {
            setAvatar(savedInstanceState.getParcelable(AVATAR_KEY));
            setUsername(savedInstanceState.getString(NAME_KEY));
        } else presenter.loadUserInfo();
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
        Glide.with(this)
                .load(avatar != null ? avatar.getThumbnail() : R.drawable.user_avatar_plug_img)
                .apply(new RequestOptions().override(THUMBSIZE, THUMBSIZE).error(R.drawable.user_avatar_plug_img))
                .into(avatarImageView);
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        if (null != file) {
            if (avatar == null)
                avatar = new PhotoWithPreview();
            avatar.setPhoto(file.getPath());
            avatar.setThumbnail(file.getPath());
            avatar.setExpectedAction(PhotoWithPreview.Action.CHANGE);
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

    @OnClick(R.id.textView_change_username)
    void changeUsernameClicked(){
        EditTextDialog editTextDialog = new EditTextDialog(this, "Enter your name", "name",
                new EditTextDialog.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        String enteredValue = getEditTextDialog().getEditText().getText().toString();
                        if (enteredValue.length() > 1) {
                            usernameTextView.setText(enteredValue);
                            dismiss();
                        } else {
                            Toaster.shortToast("Enter at least 2 symbols");
                        }
                    }

                    @Override
                    public void onNegativeButtonClick() {
                        dismiss();
                    }
                }
        );
        String name = usernameTextView.getText().toString();
        editTextDialog.setEditTextText((!name.isEmpty() && !name.equals(DEFAULT_NAME)) ? name : null);
        editTextDialog.setEditTextInputType(InputType.TYPE_CLASS_TEXT);
        editTextDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_action_done:
                presenter.uploadUserSettings(usernameTextView.getText().toString(), avatar);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setEmail(String email){
        //emailTextView.setText(email);
    }

    public void setAvatar(PhotoWithPreview photoWithPreview) {
        avatar = photoWithPreview;
        updateAvatar();
    }

    public void setUsername(String username){
        if (username != null && !username.isEmpty())
            usernameTextView.setText(username);
        else usernameTextView.setText(DEFAULT_NAME);
    }

    public void registerUploadAvatarReceiver(UploadServiceBroadcastReceiver broadcastReceiver) {
        broadcastReceiver.register(this);
    }

    public void unregisterUploadAvatarReceiver(UploadServiceBroadcastReceiver broadcastReceiver) {
        broadcastReceiver.unregister(this);
    }

    public void onSavedSuccessfully() {
        Toaster.longToast("User settings saved");
        hideWaitDialog();
        onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NAME_KEY, !usernameTextView.getText().toString().equals(DEFAULT_NAME) ? usernameTextView.getText().toString() : "");
        outState.putParcelable(AVATAR_KEY, avatar);
    }
}
