package com.varteq.catslovers.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.Auth;
import com.varteq.catslovers.Log;
import com.varteq.catslovers.R;
import com.varteq.catslovers.Utils;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.Cat;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.view.adapters.CatPhotosAdapter;
import com.varteq.catslovers.view.adapters.GroupPartnersAdapter;
import com.varteq.catslovers.view.adapters.ViewColorsAdapter;
import com.varteq.catslovers.view.dialog.ColorPickerDialog;
import com.varteq.catslovers.view.dialog.WrappedDatePickerDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatProfileActivity extends PhotoPickerActivity implements View.OnClickListener {

    private String TAG = CatProfileActivity.class.getSimpleName();
    public static final String CAT_KEY = "cat_key";
    public static final String MODE_KEY = "mode_key";
    public static final String IS_EDIT_MODE_KEY = "is_mode_key";
    @BindView(R.id.cat_profile_avatar_roundedImageView)
    RoundedImageView avatarImageView;
    @BindView(R.id.pet_name_textView)
    TextView petNameTextView;
    @BindView(R.id.nickname_textView)
    TextView nicknameTextView;

    @BindView(R.id.information_textView)
    TextView informationTextView;

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

    @BindView(R.id.age_value_textView)
    TextView ageValueTextView;
    @BindView(R.id.weight_value_textView)
    TextView weightValueTextView;

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

    @BindView(R.id.info_LinearLayout)
    LinearLayout infoLinearLayout;
    @BindView(R.id.follow_button)
    Button followButton;
    @BindView(R.id.reply_button)
    Button replyButton;

    @BindView(R.id.view_colors_RecyclerView)
    RecyclerView viewColorsRecyclerView;
    @BindView(R.id.flea_treatment_value_textView)
    TextView fleaTreatmentValueTextView;

    @BindView(R.id.upload_image_LinearLayout)
    LinearLayout uploadImageLinearLayout;
    private Menu actionBarMenu;
    private long petBirthdayMillis;
    private long fleaTreatmentDateMilis;

    public enum CatProfileScreenMode {
        EDIT_MODE,
        VIEW_MODE
    }

    private CatProfileScreenMode currentMode = CatProfileScreenMode.EDIT_MODE;

    private int clickedRoundViewId;
    private List<RoundedImageView> colorPickers;
    private List<Uri> photoList;
    private CatPhotosAdapter photosAdapter;

    private List<GroupPartner> groupPartnersList;
    private GroupPartnersAdapter groupPartnersAdapter;

    private List<Integer> colorsList;
    private ViewColorsAdapter viewColorsAdapter;

    private CatProfile catProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_profile);

        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);

        if (getIntent() != null) {
            if (getIntent().hasExtra(CAT_KEY)) {
                catProfile = (CatProfile) getIntent().getSerializableExtra(CAT_KEY);
                petNameTextView.setText(catProfile.getPetName());
            }
            if (getIntent().hasExtra(IS_EDIT_MODE_KEY) && !getIntent().getBooleanExtra(IS_EDIT_MODE_KEY, true))
                currentMode = CatProfileScreenMode.VIEW_MODE;
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            Serializable mode = savedInstanceState.getSerializable(MODE_KEY);
            if (mode != null && mode instanceof CatProfileScreenMode)
                currentMode = (CatProfileScreenMode) mode;
        }

        Uri avatarUri = Auth.getUserAvatar(this);
        if (avatarUri != null && !avatarUri.toString().isEmpty())
            avatarImageView.setImageURI(avatarUri);
        else
            avatarImageView.setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));

        avatarImageView.setOnClickListener(view -> {
            if (currentMode.equals(CatProfileScreenMode.VIEW_MODE))
                currentMode = CatProfileScreenMode.EDIT_MODE;
            else currentMode = CatProfileScreenMode.VIEW_MODE;

            setupUIMode();
        });

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
        photosAdapter = new CatPhotosAdapter(photoList, this::showImage);
        photosRecyclerView.setAdapter(photosAdapter);
        photosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        groupPartnersList = new ArrayList<>();
        groupPartnersList.add(new GroupPartner(null, "Admin", true));
        groupPartnersList.add(new GroupPartner(null, "User1", false));
        groupPartnersAdapter = new GroupPartnersAdapter(groupPartnersList, currentMode.equals(CatProfileScreenMode.EDIT_MODE),
                new GroupPartnersAdapter.OnPersonClickListener() {

                    @Override
                    public void onPersonClicked(Uri imageUri) {
                        Log.d(TAG, "onPersonClicked " + imageUri);
                    }

                    @Override
                    public void onAddPerson() {
                        Log.d(TAG, "onAddPerson");
                        addGroupPartner();
                    }
                });
        groupPartnersRecyclerView.setAdapter(groupPartnersAdapter);
        groupPartnersRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        colorsList = new ArrayList<>();
        colorsList.add(Color.GREEN);
        colorsList.add(Color.RED);

        viewColorsAdapter = new ViewColorsAdapter(colorsList);
        viewColorsRecyclerView.setAdapter(viewColorsAdapter);
        viewColorsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        setupUIMode();
    }

    private void compressColorsList() {
        while (colorsList.contains(null))
            colorsList.remove(null);
    }

    private void resizeColorsListWithEmptyValues() {
        while (colorPickers.size() > colorsList.size())
            colorsList.add(null);
    }

    private void setupColorPickersColors() {
        for (int i = 0; i < colorPickers.size(); i++) {
            if (i < colorsList.size())
                colorPickers.get(i).setImageBitmap(Utils.getBitmapWithColor(colorsList.get(i)));
            else
                colorPickers.get(i).setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));
        }
    }

    private void setupUIMode() {
        if (currentMode.equals(CatProfileScreenMode.EDIT_MODE))
            setupEditMode();
        else
            setupViewMode();
    }

    private void setupViewMode() {
        Log.d(TAG, "setupViewMode");
        currentMode = CatProfileScreenMode.VIEW_MODE;

        infoLinearLayout.setVisibility(View.VISIBLE);

        // colorsList settings
        compressColorsList();
        viewColorsRecyclerView.setVisibility(View.VISIBLE);
        expandColorsButton.setVisibility(View.GONE);
        colorConstraintLayout.setVisibility(View.GONE);

        noCheckBox.setEnabled(false);

        // flea treatment settings
        fleaTreatmentPickerButton.setVisibility(View.GONE);

        uploadImageLinearLayout.setVisibility(View.GONE);

        descriptionEditText.setEnabled(false);

        groupPartnersAdapter.switchToViewMode();

        if (actionBarMenu != null && actionBarMenu.findItem(R.id.app_bar_save) != null)
            actionBarMenu.findItem(R.id.app_bar_save).setVisible(false);

        informationTextView.setVisibility(View.VISIBLE);

        fleaTreatmentValueTextView.setText("45 days");
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) fleaTreatmentValueTextView.getLayoutParams();
        layoutParams.rightMargin = Utils.convertDpToPx(0, this);

        fleaTreatmentValueTextView.setLayoutParams(layoutParams);

        ageValueTextView.setOnClickListener(null);
        weightValueTextView.setOnClickListener(null);
    }

    private void setupEditMode() {
        Log.d(TAG, "setupEditMode");
        currentMode = CatProfileScreenMode.EDIT_MODE;

        infoLinearLayout.setVisibility(View.GONE);

        // colorsList settings
        viewColorsRecyclerView.setVisibility(View.GONE);
        expandColorsButton.setVisibility(View.VISIBLE);
        colorConstraintLayout.setVisibility(View.GONE);
        expandCollapseColors();
        setupColorPickersColors();
        resizeColorsListWithEmptyValues();

        noCheckBox.setEnabled(true);

        // flea treatment settings
        fleaTreatmentPickerButton.setVisibility(View.VISIBLE);

        uploadImageLinearLayout.setVisibility(View.VISIBLE);

        descriptionEditText.setEnabled(true);

        groupPartnersAdapter.switchToEditMode();

        if (actionBarMenu != null && actionBarMenu.findItem(R.id.app_bar_save) != null)
            actionBarMenu.findItem(R.id.app_bar_save).setVisible(true);

        informationTextView.setVisibility(View.GONE);

        weightValueTextView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(CatProfileActivity.this);
            View dialogView = LayoutInflater.from(CatProfileActivity.this).inflate(R.layout.dialog_weight, null, false);
            builder.setView(dialogView);
            builder.setPositiveButton("OK", (dialogInterface, i) -> weightValueTextView.setText(((EditText) dialogView.findViewById(R.id.yearsEditText)).getText() + " kg"));
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                    }
            );
            builder.show();
        });

        fleaTreatmentValueTextView.setText("10.08.2017");
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) fleaTreatmentValueTextView.getLayoutParams();
        layoutParams.rightMargin = fleaTreatmentPickerButton.getLayoutParams().width + Utils.convertDpToPx(8, this);
        fleaTreatmentValueTextView.setLayoutParams(layoutParams);


        ageValueTextView.setOnClickListener(view -> new WrappedDatePickerDialog(CatProfileActivity.this, (datePicker, i, i1, i2) -> {
            Calendar now = Calendar.getInstance();
            Calendar birthday = Calendar.getInstance();
            birthday.set(Calendar.YEAR, i);
            birthday.set(Calendar.MONTH, i1);
            birthday.set(Calendar.DAY_OF_MONTH, i2);
            birthday.set(Calendar.MILLISECOND, 0);

            petBirthdayMillis = birthday.getTimeInMillis();
            long nowMillis = now.getTimeInMillis();
            long timePassedMonthes = (TimeUnit.MILLISECONDS.toDays(nowMillis - petBirthdayMillis)) / 30;

            int years = ((int) timePassedMonthes) / 12;
            int month = ((int) timePassedMonthes) - (years * 12);

            String age = null;
            if (years > 0) {
                if (month > 0) {
                    age = years + " years, " + String.valueOf(month) + " months";
                } else if (month == 0) {
                    age = years + " years";
                }
            } else if (years == 0) {
                if (month > 0) {
                    age = String.valueOf(month) + " months";
                } else if (month == 0) {
                    age = "newborn";

                }
            }
            if (age == null) {
                age = "incorrect date";
            }
            ageValueTextView.setText(age);

        }));
    }

    private void addGroupPartner() {
        groupPartnersList.add(1, new GroupPartner(null, "User" + ((int) (Math.random() * 999999) + 111111), false));
        groupPartnersAdapter.notifyItemInserted(1);
        groupPartnersRecyclerView.scrollToPosition(0);
    }

    private void showImage(Uri imageUri) {
        if (imageUri == null) return;
        Log.d(TAG, "showImage " + imageUri);

        Intent intent = new Intent();
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".media.fileprovider", new File(chatEntry.getAvatar().getFile()));

            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            //uri = Uri.fromFile(new File(chatEntry.getAvatar().getFile()));
        }

        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_cat_profile_activity, menu);
        if (currentMode.equals(CatProfileScreenMode.EDIT_MODE))
            menu.findItem(R.id.app_bar_save).setVisible(true);
        else
            menu.findItem(R.id.app_bar_save).setVisible(false);
        actionBarMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_save:
                Log.d(TAG, "app_bar_save");
                saveCat();
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
            Log.d(TAG, "onImageSelected " + uri);
            photoList.add(0, uri);
            photosAdapter.notifyItemInserted(0);
            photosRecyclerView.scrollToPosition(0);
        }
    }

    private void saveCat() {
        int feedstationId = 0;
        String name = petNameTextView.getText().toString();
        String nickname = nicknameTextView.getText().toString();

        compressColorsList();
        String colors = "";
        for (int color : colorsList)
            colors += String.valueOf(color) + ",";
        if (!colors.isEmpty())
            colors = colors.substring(0, colors.length() - 1);
        resizeColorsListWithEmptyValues();

        int age = (int) (petBirthdayMillis / 1000L);
        String sex = null;
        String w = weightValueTextView.getText().toString();
        float weight = Float.parseFloat(w.substring(0, w.length() - 3));
        boolean castrated = noCheckBox.isChecked();
        String description = descriptionEditText.getText().toString();
        String type = "pet";
        int nextFleaTreatment = (int) (fleaTreatmentDateMilis / 1000L);

        Call<BaseResponse<Cat>> call = ServiceGenerator.getApiServiceWithToken().createCat(feedstationId, name,
                nickname, colors, age, sex, weight, castrated, description, type, nextFleaTreatment);
        call.enqueue(new Callback<BaseResponse<Cat>>() {
            @Override
            public void onResponse(Call<BaseResponse<Cat>> call, Response<BaseResponse<Cat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<Cat>(response) {

                        @Override
                        protected void onSuccess(Cat data) {
                            /*if (data.getToken() != null) {
                                Log.i(TAG, "getApiService().auth success");
                                Log.i(TAG, data.getToken());

                            }*/
                            CatProfileActivity.this.finishAffinity();
                            startActivity(new Intent(CatProfileActivity.this, MainActivity.class));
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Cat>> call, Throwable t) {
                Log.e(TAG, "createCat onFailure " + t.getMessage());
            }
        });
    }

    @OnClick(R.id.flea_treatment_picker_button)
    void showFleaTreatmentPicker() {
        new WrappedDatePickerDialog(this, (datePicker, i, i1, i2) -> {
            i1++;
            Calendar calendar = Calendar.getInstance();
            calendar.set(i, i1, i2);
            fleaTreatmentDateMilis = calendar.getTimeInMillis();
            String month;
            String day;
            String year = String.valueOf(i);
            if (i2 < 10) {
                day = "0" + String.valueOf(i2);
            } else {
                day = String.valueOf(i2);
            }
            if (i1 < 10) {
                month = "0" + String.valueOf(i1);
            } else {
                month = String.valueOf(i1);
            }
            fleaTreatmentValueTextView.setText(day + "." + month + "." + year);
        });
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


    @OnCheckedChanged(R.id.no_checkBox)
    void onNoChecked(boolean checked) {
        if (checked) {
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof RoundedImageView) {
            Log.d(TAG, "onClick RoundedImageView");
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, Color.GREEN, color -> {
                for (int i = 0; i < colorPickers.size(); i++)
                    if (colorPickers.get(i).getId() == clickedRoundViewId) {
                        colorPickers.get(i).setImageBitmap(Utils.getBitmapWithColor(color));
                        colorsList.set(i, color);
                    }
            });
            colorPickerDialog.show();
            clickedRoundViewId = view.getId();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(MODE_KEY, currentMode);
    }
}
