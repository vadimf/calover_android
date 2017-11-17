package com.varteq.catslovers.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.varteq.catslovers.ColorPickerDialog;
import com.varteq.catslovers.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CatProfileActivity extends AppCompatActivity implements View.OnClickListener {

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

    private int clickedRoundViewId;
    private List<RoundedImageView> colorPickers;

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

    @OnClick(R.id.expand_colors_button)
    void expandCollapseColors() {
        if (colorConstraintLayout.getVisibility() == View.VISIBLE) {
            expandColorsButton.setBackgroundResource(R.drawable.arrow_left);
            colorConstraintLayout.setVisibility(View.GONE);
        } else {
            expandColorsButton.setBackgroundResource(R.drawable.arrow_down);
            colorConstraintLayout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.expand_description_button)
    void expandCollapseDescription() {
        if (descriptionEditText.getVisibility() == View.VISIBLE) {
            expandDescriptionButton.setBackgroundResource(R.drawable.arrow_left);
            descriptionEditText.setVisibility(View.GONE);
        } else {
            expandDescriptionButton.setBackgroundResource(R.drawable.arrow_down);
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
