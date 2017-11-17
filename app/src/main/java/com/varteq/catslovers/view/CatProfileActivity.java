package com.varteq.catslovers.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.Auth;
import com.varteq.catslovers.CatPhotosAdapter;
import com.varteq.catslovers.ColorPickerDialog;
import com.varteq.catslovers.DatePickerFragment;
import com.varteq.catslovers.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CatProfileActivity extends PhotoPickerActivity implements View.OnClickListener {

    @BindView(R.id.cat_profile_avatar_roundedImageView)
    RoundedImageView avatarImageView;
    @BindView(R.id.pet_name_textView)
    TextView petNameTextView;
    @BindView(R.id.nickname_textView)
    TextView nicknameTextView;

    @BindView(R.id.expand_colors_button)
    Button expandColorsButton;
    @BindView(R.id.color_constraintLayout)
    ConstraintLayout colorConstraintLayout;

    @BindView(R.id.color_one_roundedImageView)
    RoundedImageView colorOneRoundedImageView;
    @BindView(R.id.color_two_roundedImageView)
    RoundedImageView colorTwoRoundedImageView;
    @BindView(R.id.color_three_roundedImageView)
    RoundedImageView colorThreeRoundedImageView;
    @BindView(R.id.color_four_roundedImageView)
    RoundedImageView colorFourRoundedImageView;
    @BindView(R.id.color_five_roundedImageView)
    RoundedImageView colorFiveRoundedImageView;
    @BindView(R.id.color_six_roundedImageView)
    RoundedImageView colorSixRoundedImageView;

    @BindView(R.id.expand_description_button)
    Button expandDescriptionButton;
    @BindView(R.id.description_editText)
    EditText descriptionEditText;

    @BindView(R.id.yes_checkBox)
    CheckBox yesCheckBox;
    @BindView(R.id.no_checkBox)
    CheckBox noCheckBox;

    @BindView(R.id.flea_treatment_picker_button)
    Button fleaTreatmentPickerButton;

    @BindView(R.id.pet_name_photos_textView)
    TextView petNamePhotosTextView;
    @BindView(R.id.photo_count_textView)
    TextView photoCountTextView;

    @BindView(R.id.upload_image_button)
    Button uploadImageButton;
    @BindView(R.id.photos_RecyclerView)
    RecyclerView photosRecyclerView;

    @BindView(R.id.group_partners_RecyclerView)
    RecyclerView groupPartnersRecyclerView;

    private int clickedRoundViewId;
    private List<RoundedImageView> colorPickers;
    private List<Uri> photoList;
    private CatPhotosAdapter photosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_profile);

        ButterKnife.bind(this);

        Uri avatarUri = Auth.getUserAvatar(this);
        if (avatarUri != null && !avatarUri.toString().isEmpty())
            avatarImageView.setImageURI(avatarUri);
        else
            avatarImageView.setImageBitmap(getBitmapWithColor(getResources().getColor(R.color.transparent)));

        nicknameTextView.setText(Auth.getUserName(this));

        colorOneRoundedImageView.setOnClickListener(this);
        colorTwoRoundedImageView.setOnClickListener(this);
        colorThreeRoundedImageView.setOnClickListener(this);
        colorFourRoundedImageView.setOnClickListener(this);
        colorFiveRoundedImageView.setOnClickListener(this);
        colorSixRoundedImageView.setOnClickListener(this);

        colorPickers = new ArrayList<>(
                Arrays.asList(colorOneRoundedImageView,
                        colorTwoRoundedImageView,
                        colorThreeRoundedImageView,
                        colorFourRoundedImageView,
                        colorFiveRoundedImageView,
                        colorSixRoundedImageView));

        photoList = new ArrayList<>();
        photosAdapter = new CatPhotosAdapter(photoList);
        photosRecyclerView.setAdapter(photosAdapter);
        photosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_cat_profile_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_save:
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.upload_image_button)
    void uploadCatImage() {
        pickPhotoWithPermission(getString(R.string.select_cat_photo));
    }

    @Override
    protected void onImageSelected(Uri uri) {
        super.onImageSelected(uri);
        if (uri != null) {
            photoList.add(0, uri);
            photosAdapter.notifyItemInserted(0);
        }
    }

    @OnClick(R.id.flea_treatment_picker_button)
    void showFleaTreatmentPicker() {
        new DatePickerFragment(this, null);
    }

    @OnClick(R.id.expand_colors_button)
    void expandCollapseColors() {
        if (colorConstraintLayout.getVisibility() == View.VISIBLE) {
            expandColorsButton.setBackgroundResource(R.drawable.ic_expand_more_24dp);
            colorConstraintLayout.setVisibility(View.GONE);
        } else {
            expandColorsButton.setBackgroundResource(R.drawable.ic_expand_less_24dp);
            colorConstraintLayout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.expand_description_button)
    void expandCollapseDescription() {
        if (descriptionEditText.getVisibility() == View.VISIBLE) {
            expandDescriptionButton.setBackgroundResource(R.drawable.ic_expand_more_24dp);
            descriptionEditText.setVisibility(View.GONE);
        } else {
            expandDescriptionButton.setBackgroundResource(R.drawable.ic_expand_less_24dp);
            descriptionEditText.setVisibility(View.VISIBLE);
        }
    }

    @OnCheckedChanged(R.id.yes_checkBox)
    void onYesChecked(boolean checked) {
        if (checked) {
            noCheckBox.setChecked(false);
        }
    }

    @OnCheckedChanged(R.id.no_checkBox)
    void onNoChecked(boolean checked) {
        if (checked) {
            yesCheckBox.setChecked(false);
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof RoundedImageView) {
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, Color.GREEN, color -> {
                for (RoundedImageView imageView : colorPickers)
                    if (imageView.getId() == clickedRoundViewId) {
                        imageView.setImageBitmap(getBitmapWithColor(color));
                    }
            });
            colorPickerDialog.show();
            clickedRoundViewId = view.getId();
        }
    }

    private Bitmap getBitmapWithColor(int color) {
        Bitmap image = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        image.eraseColor(color);
        return image;
    }
}
