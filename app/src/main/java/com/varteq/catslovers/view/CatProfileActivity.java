package com.varteq.catslovers.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.SimpleColorDialog;

public class CatProfileActivity extends AppCompatActivity implements View.OnClickListener, SimpleDialog.OnDialogResultListener {

    @BindView(R.id.cat_profile_avatar_roundedImageView)
    RoundedImageView roundedImageView;
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

    private String COLOR_DIALOG = "color_dialog";
    private int clickedRoundViewId;
    private List<RoundedImageView> colorPickers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_profile);

        ButterKnife.bind(this);

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

    @Override
    public void onClick(View view) {
        if (view instanceof RoundedImageView) {
            SimpleColorDialog.build()
                    .title(R.string.pick_a_color)
                    .colorPreset(Color.RED)
                    .allowCustom(true)
                    .show(this, COLOR_DIALOG);
            clickedRoundViewId = view.getId();
        }
    }

    @Override
    public boolean onResult(String dialogTag, int which, Bundle extras) {

        if (COLOR_DIALOG.equals(dialogTag)) {
            switch (which) {
                case BUTTON_POSITIVE:
                    for (RoundedImageView imageView : colorPickers)
                        if (imageView.getId() == clickedRoundViewId) {
                            Bitmap image = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                            image.eraseColor(extras.getInt(SimpleColorDialog.COLOR));
                            imageView.setImageBitmap(image);
                        }
                    break;
            }
            return true;
        }

        return false;
    }
}
