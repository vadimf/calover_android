package com.varteq.catslovers.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.view.adapters.CatPhotosAdapter;
import com.varteq.catslovers.view.adapters.GroupPartnersAdapter;
import com.varteq.catslovers.view.adapters.ViewColorsAdapter;
import com.varteq.catslovers.view.dialog.ColorPickerDialog;
import com.varteq.catslovers.view.dialog.EditTextDialog;
import com.varteq.catslovers.view.dialog.WrappedDatePickerDialog;
import com.varteq.catslovers.view.presenter.CatProfilePresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CatProfileActivity extends PhotoPickerActivity implements View.OnClickListener {

    private String TAG = CatProfileActivity.class.getSimpleName();
    public static final String CAT_KEY = "cat_key";
    public static final String MODE_KEY = "mode_key";

    private String DEFAULT_VALUE = "setup";
    private String PET_DEFAULT_NAME = "Pet Name";

    @BindView(R.id.main_layout)
    ConstraintLayout mainLayout;
    @BindView(R.id.main_scrollView)
    ScrollView scrollView;
    @BindView(R.id.petLayout)
    FrameLayout petLayout;
    @BindView(R.id.strayLayout)
    FrameLayout strayLayout;
    @BindView(R.id.animal_type_pet_textView)
    TextView animalTypePetTextView;
    @BindView(R.id.animal_type_stray_textView)
    TextView animalTypeStrayTextView;

    @BindView(R.id.cat_profile_avatar_roundedImageView)
    RoundedImageView avatarImageView;
    @BindView(R.id.addPhotoButton)
    ImageButton addPhotoButton;
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
    CheckBox castratedCheckBox;

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
    private long petBirthdayMillis = 0;
    private long fleaTreatmentDateMilis = 0;
    private float weight = 0;
    private CatProfile.Status catType = CatProfile.Status.PET;
    private CatProfilePresenter presenter;
    private MenuItem saveMenu;
    private MenuItem editMenu;

    public enum CatProfileScreenMode {
        EDIT_MODE,
        VIEW_MODE,
        CREATE_MODE
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

    public static void startInViewMode(Activity activity, CatProfile catProfile) {
        Intent intent = new Intent(activity, CatProfileActivity.class);
        intent.putExtra(MODE_KEY, CatProfileScreenMode.VIEW_MODE);
        intent.putExtra(CatProfileActivity.CAT_KEY, catProfile);
        activity.startActivity(intent);
    }

    public static void startInEditMode(Activity activity, CatProfile catProfile) {
        Intent intent = new Intent(activity, CatProfileActivity.class);
        intent.putExtra(MODE_KEY, CatProfileScreenMode.EDIT_MODE);
        intent.putExtra(CatProfileActivity.CAT_KEY, catProfile);
        activity.startActivity(intent);
    }

    public static void startInCreateMode(Activity activity) {
        Intent intent = new Intent(activity, CatProfileActivity.class);
        intent.putExtra(MODE_KEY, CatProfileScreenMode.CREATE_MODE);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_profile);

        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);

        presenter = new CatProfilePresenter(this);

        if (savedInstanceState != null) {
            currentMode = (CatProfileScreenMode) savedInstanceState.getSerializable(MODE_KEY);
            catProfile = (CatProfile) savedInstanceState.getSerializable(CAT_KEY);
        } else if (getIntent() != null) {
            if (getIntent().hasExtra(CAT_KEY))
                catProfile = (CatProfile) getIntent().getSerializableExtra(CAT_KEY);

            currentMode = (CatProfileScreenMode) getIntent().getSerializableExtra(MODE_KEY);
        }

        if (!currentMode.equals(CatProfileScreenMode.CREATE_MODE))
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nicknameTextView.setText(Profile.getUserName(this));

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

        fillUI();
        setupUIMode();
    }

    private void fillUI() {
        if (catProfile != null) {
            if (catProfile.getType() != null && catProfile.getType().equals(CatProfile.Status.STRAY))
                selectAnimalTypeStray();
            else selectAnimalTypePet();
            petNameTextView.setText(catProfile.getPetName());
            nicknameTextView.setText(catProfile.getNickname());

            colorsList = catProfile.getColorsList() != null ? catProfile.getColorsList() : new ArrayList<>();
            //colorsList.add(Color.GRAY);

            if (catProfile.getBirthday() != null && catProfile.getBirthday().getTime() > 0) {
                petBirthdayMillis = catProfile.getBirthday().getTime();
                ageValueTextView.setText(presenter.getAgeInString(petBirthdayMillis));
            }
            setWeight(String.valueOf(catProfile.getWeight()));
            castratedCheckBox.setChecked(catProfile.isCastrated());
            if (catProfile.getFleaTreatmentDate() != null && catProfile.getFleaTreatmentDate().getTime() > 0) {
                fleaTreatmentDateMilis = catProfile.getFleaTreatmentDate().getTime();
                fleaTreatmentValueTextView.setText(TimeUtils.getDateAsDDMMYYYY(fleaTreatmentDateMilis));
            }
            descriptionEditText.setText(catProfile.getDescription());
        } else {
            colorsList = new ArrayList<>();
        }
        scrollView.setOnTouchListener((v, event) -> {
            if (mainLayout.getDescendantFocusability() != ViewGroup.FOCUS_BLOCK_DESCENDANTS)
                mainLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            else if (descriptionEditText.isFocused()) {
                descriptionEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    imm.hideSoftInputFromWindow(descriptionEditText.getWindowToken(), 0);
                } catch (Exception e) {
                }
            }
            return false;
        });

        Uri avatarUri = Profile.getUserAvatar(this);
        if (avatarUri != null && !avatarUri.toString().isEmpty())
            avatarImageView.setImageURI(avatarUri);
        else
            avatarImageView.setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));

        photoList = new ArrayList<>();
        photosAdapter = new CatPhotosAdapter(photoList, this::showImage);
        photosRecyclerView.setAdapter(photosAdapter);
        photosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        groupPartnersList = new ArrayList<>();
        groupPartnersList.add(new GroupPartner(null, "Admin", true));
        //groupPartnersList.add(new GroupPartner(null, "User1", false));
        groupPartnersAdapter = new GroupPartnersAdapter(groupPartnersList, !currentMode.equals(CatProfileScreenMode.VIEW_MODE),
                new GroupPartnersAdapter.OnPersonClickListener() {

                    @Override
                    public void onPersonClicked(Uri imageUri) {
                        Log.d(TAG, "onPersonClicked " + imageUri);
                    }

                    @Override
                    public void onAddPerson() {
                        Log.d(TAG, "onAddPerson");
                        presenter.addGroupPartner(groupPartnersList, groupPartnersAdapter);
                        groupPartnersRecyclerView.scrollToPosition(0);
                    }
                });
        groupPartnersRecyclerView.setAdapter(groupPartnersAdapter);
        groupPartnersRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        viewColorsAdapter = new ViewColorsAdapter(colorsList);
        viewColorsRecyclerView.setAdapter(viewColorsAdapter);
        viewColorsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupColorPickersColors() {
        for (int i = 0; i < colorPickers.size(); i++) {
            if (i < colorsList.size()) {
                colorPickers.get(i).setImageBitmap(Utils.getBitmapWithColor(colorsList.get(i)));
                if (colorsList.get(i) == getResources().getColor(R.color.white))
                    colorPickers.get(i).setBorderWidth((float) Utils.convertDpToPx(2, this));
            } else {
                colorPickers.get(i).setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));
                colorPickers.get(i).setBorderWidth((float) Utils.convertDpToPx(2, this));
            }
        }
    }

    private void setupUIMode() {
        if (currentMode.equals(CatProfileScreenMode.VIEW_MODE))
            setupViewMode();
        else
            setupEditMode();
    }

    private void setupViewMode() {
        Log.d(TAG, "setupViewMode");

        if (petNameTextView.getText().toString().isEmpty())
            petNameTextView.setText(PET_DEFAULT_NAME);
        if (ageValueTextView.getText().toString().equals(DEFAULT_VALUE))
            ageValueTextView.setText("");
        if (weightValueTextView.getText().toString().equals(DEFAULT_VALUE))
            weightValueTextView.setText("");

        viewColorsAdapter.notifyDataSetChanged();
        currentMode = CatProfileScreenMode.VIEW_MODE;

        infoLinearLayout.setVisibility(View.VISIBLE);
        addPhotoButton.setVisibility(View.INVISIBLE);

        petLayout.setEnabled(false);
        strayLayout.setEnabled(false);

        petNameTextView.setEnabled(false);

        // colorsList settings
        presenter.compressColorsList(colorsList);
        viewColorsRecyclerView.setVisibility(View.VISIBLE);
        expandColorsButton.setVisibility(View.GONE);
        colorConstraintLayout.setVisibility(View.GONE);

        castratedCheckBox.setEnabled(false);

        // flea treatment settings
        fleaTreatmentPickerButton.setVisibility(View.GONE);

        uploadImageLinearLayout.setVisibility(View.GONE);

        descriptionEditText.setEnabled(false);

        groupPartnersAdapter.switchToViewMode();

        if (saveMenu != null)
            saveMenu.setVisible(false);
        if (editMenu != null)
            editMenu.setVisible(true);

        informationTextView.setVisibility(View.VISIBLE);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) fleaTreatmentValueTextView.getLayoutParams();
        layoutParams.rightMargin = Utils.convertDpToPx(0, this);

        fleaTreatmentValueTextView.setLayoutParams(layoutParams);

        ageValueTextView.setEnabled(false);
        weightValueTextView.setEnabled(false);
    }

    private void setupEditMode() {
        Log.d(TAG, "setupEditMode");

        if (petNameTextView.getText().toString().isEmpty())
            petNameTextView.setText(PET_DEFAULT_NAME);
        if (ageValueTextView.getText().toString().isEmpty())
            ageValueTextView.setText(DEFAULT_VALUE);
        if (weightValueTextView.getText().toString().isEmpty())
            weightValueTextView.setText(DEFAULT_VALUE);

        infoLinearLayout.setVisibility(View.GONE);
        addPhotoButton.setVisibility(View.VISIBLE);

        petLayout.setEnabled(true);
        strayLayout.setEnabled(true);

        petNameTextView.setEnabled(true);

        // colorsList settings
        viewColorsRecyclerView.setVisibility(View.GONE);
        expandColorsButton.setVisibility(View.VISIBLE);
        colorConstraintLayout.setVisibility(View.GONE);
        expandCollapseColors();
        setupColorPickersColors();
        presenter.resizeColorsListWithEmptyValues(colorsList, colorPickers.size());

        castratedCheckBox.setEnabled(true);

        // flea treatment settings
        fleaTreatmentPickerButton.setVisibility(View.VISIBLE);

        uploadImageLinearLayout.setVisibility(View.VISIBLE);

        descriptionEditText.setEnabled(true);

        groupPartnersAdapter.switchToEditMode();

        if (saveMenu != null)
            saveMenu.setVisible(true);
        if (editMenu != null)
            editMenu.setVisible(false);

        informationTextView.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) fleaTreatmentValueTextView.getLayoutParams();
        layoutParams.rightMargin = fleaTreatmentPickerButton.getLayoutParams().width + Utils.convertDpToPx(8, this);
        fleaTreatmentValueTextView.setLayoutParams(layoutParams);

        ageValueTextView.setEnabled(true);
        weightValueTextView.setEnabled(true);
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

        saveMenu = menu.findItem(R.id.app_bar_save);
        editMenu = menu.findItem(R.id.app_bar_edit);

        if (saveMenu != null && editMenu != null) {
            if (currentMode.equals(CatProfileScreenMode.VIEW_MODE)) {
                saveMenu.setVisible(false);
                editMenu.setVisible(true);
            } else {
                saveMenu.setVisible(true);
                editMenu.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_save:
                Log.d(TAG, "app_bar_save");
                if (currentMode.equals(CatProfileScreenMode.CREATE_MODE))
                    presenter.saveCat(fillCatProfile());
                else if (currentMode.equals(CatProfileScreenMode.EDIT_MODE))
                    presenter.updateCat(fillCatProfile());
                return true;
            case R.id.app_bar_edit:
                Log.d(TAG, "app_bar_edit");
                currentMode = CatProfileScreenMode.EDIT_MODE;
                setupUIMode();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setWeight(String weight) {
        try {
            float w = Float.parseFloat(weight);
            if (w > 0) {
                this.weight = w;
                weightValueTextView.setText(weight + " kg");
            }
        } catch (Exception e) {
        }
    }

    @OnClick(R.id.description_editText)
    void onDescriptionClick() {
        mainLayout.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        descriptionEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            //if (!imm.isAcceptingText())
            imm.showSoftInput(descriptionEditText, 0);
        } catch (Exception e) {
        }
    }

    @OnClick(R.id.age_value_textView)
    void onAgeClick() {
        new WrappedDatePickerDialog(CatProfileActivity.this, petBirthdayMillis, (datePicker, i, i1, i2) -> {
            petBirthdayMillis = TimeUtils.getTimeInMillis(i, i1, i2);
            ageValueTextView.setText(presenter.getAgeInString(petBirthdayMillis));
        });
    }

    @OnClick(R.id.weight_value_textView)
    void onWeightClick() {
        EditTextDialog editTextDialog = new EditTextDialog(this, "Enter cat's weight", "kg", "OK", "Cancel",
                new EditTextDialog.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        String enteredValue = getEditTextDialog().getEditText().getText().toString();
                        if (Utils.isStringNumericPositive(enteredValue)) {
                            setWeight(enteredValue);
                            dismiss();
                        } else {
                            Toaster.shortToast("Invalid weight");
                        }
                    }

                    @Override
                    public void onNegativeButtonClick() {
                        dismiss();
                    }
                }
        );
        editTextDialog.setEditTextText(weight > 0 ? String.valueOf(weight) : null);
        editTextDialog.setEditTextInputType(InputType.TYPE_CLASS_PHONE);
        editTextDialog.show();
    }

    @OnClick(R.id.petLayout)
    void selectAnimalTypePet() {
        petLayout.setBackgroundResource(R.drawable.cat_profile_animal_type_selected_shape);
        strayLayout.setBackgroundResource(R.drawable.cat_profile_animal_type_unselected_shape);
        animalTypePetTextView.setTextAppearance(this, R.style.PrimaryTextView);
        animalTypeStrayTextView.setTextAppearance(this, R.style.SecondaryTextView);
        catType = CatProfile.Status.PET;
    }

    @OnClick(R.id.strayLayout)
    void selectAnimalTypeStray() {
        petLayout.setBackgroundResource(R.drawable.cat_profile_animal_type_unselected_shape);
        strayLayout.setBackgroundResource(R.drawable.cat_profile_animal_type_selected_shape);
        animalTypePetTextView.setTextAppearance(this, R.style.SecondaryTextView);
        animalTypeStrayTextView.setTextAppearance(this, R.style.PrimaryTextView);
        catType = CatProfile.Status.STRAY;
    }

    @OnClick(R.id.upload_image_button)
    void uploadCatImage() {
        pickPhotoWithPermission(getString(R.string.select_cat_photo));
    }

    @Override
    protected void onImageSelected(Uri uri) {
        super.onImageSelected(uri);
        presenter.onPetImageSelected(uri, photoList, photosAdapter);
        photosRecyclerView.scrollToPosition(0);
    }

    private CatProfile fillCatProfile() {
        if (catProfile == null)
            catProfile = new CatProfile();

        presenter.compressColorsList(colorsList);

        List c = new ArrayList<>();
        c.addAll(colorsList);
        catProfile.setColorsList(c);

        presenter.resizeColorsListWithEmptyValues(colorsList, colorPickers.size());

        String petName = petNameTextView.getText().toString();
        if (petNameTextView.getText().toString().equals(PET_DEFAULT_NAME))
            petName = "";

        String nickname = nicknameTextView.getText().toString();
        Date age = new Date(petBirthdayMillis);
        boolean castrated = castratedCheckBox.isChecked();
        String description = descriptionEditText.getText().toString();
        Date nextFleaTreatment = new Date(fleaTreatmentDateMilis);

        catProfile.setPetName(petName);
        catProfile.setNickname(nickname);
        catProfile.setBirthday(age);
        catProfile.setSex(null);
        catProfile.setWeight(weight);
        catProfile.setCastrated(castrated);
        catProfile.setDescription(description);
        catProfile.setType(catType);
        catProfile.setFleaTreatmentDate(nextFleaTreatment);

        return catProfile;
    }

    @OnClick(R.id.flea_treatment_picker_button)
    void showFleaTreatmentPicker() {
        new WrappedDatePickerDialog(this, fleaTreatmentDateMilis, (datePicker, i, i1, i2) -> {
            fleaTreatmentDateMilis = TimeUtils.getTimeInMillis(i, i1, i2);
            fleaTreatmentValueTextView.setText(TimeUtils.getDateAsDDMMYYYY(fleaTreatmentDateMilis));
        });
    }

    @OnClick(R.id.pet_name_textView)
    void changeCatName() {
        EditTextDialog editTextDialog = new EditTextDialog(this, "Enter cat's name", "name", "OK", "Cancel",
                new EditTextDialog.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        String enteredValue = getEditTextDialog().getEditText().getText().toString();
                        if (enteredValue.length() > 1) {
                            petNameTextView.setText(enteredValue);
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
        String name = petNameTextView.getText().toString();
        editTextDialog.setEditTextText(!name.isEmpty() ? name : null);
        editTextDialog.setEditTextInputType(InputType.TYPE_CLASS_TEXT);
        editTextDialog.show();
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

    @Override
    public void onClick(View view) {
        if (view instanceof RoundedImageView) {
            Log.d(TAG, "onClick RoundedImageView");

            Integer initColor = null;
            for (int i = 0; i < colorPickers.size(); i++)
                if (colorPickers.get(i).getId() == view.getId()) {
                    initColor = colorsList.get(i);
                    break;
                }

            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, initColor, color -> {
                for (int i = 0; i < colorPickers.size(); i++)
                    if (colorPickers.get(i).getId() == clickedRoundViewId) {
                        colorPickers.get(i).setImageBitmap(Utils.getBitmapWithColor(color));
                        colorsList.set(i, color);
                        if (color == 0)
                            colorPickers.get(i).setBorderWidth((float) Utils.convertDpToPx(2, this));
                        else
                            colorPickers.get(i).setBorderWidth((float) Utils.convertDpToPx(0, this));
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
        outState.putSerializable(CAT_KEY, fillCatProfile());
    }

    public void savedSuccessfully() {
        if (currentMode.equals(CatProfileScreenMode.CREATE_MODE)) {
            CatProfileActivity.this.finishAffinity();
            startActivity(new Intent(CatProfileActivity.this, MainActivity.class));
        } else {
            currentMode = CatProfileScreenMode.VIEW_MODE;
            setupUIMode();
        }
    }
}
