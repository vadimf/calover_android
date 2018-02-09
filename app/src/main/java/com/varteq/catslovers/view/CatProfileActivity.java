package com.varteq.catslovers.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.model.PhotoWithPreview;
import com.varteq.catslovers.utils.Consts;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.SystemPermissionHelper;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.utils.qb.imagepick.ImagePickHelper;
import com.varteq.catslovers.utils.qb.imagepick.OnImagePickedListener;
import com.varteq.catslovers.view.adapters.GroupPartnersAdapter;
import com.varteq.catslovers.view.adapters.PhotosAdapter;
import com.varteq.catslovers.view.adapters.ViewColorsAdapter;
import com.varteq.catslovers.view.dialog.ColorPickerDialog;
import com.varteq.catslovers.view.dialog.EditTextDialog;
import com.varteq.catslovers.view.dialog.WrappedDatePickerDialog;
import com.varteq.catslovers.view.presenter.CatProfilePresenter;
import com.varteq.catslovers.view.qb.AttachmentImageActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.varteq.catslovers.utils.SystemPermissionHelper.PERMISSIONS_ACCESS_LOCATION_REQUEST;
import static com.varteq.catslovers.utils.SystemPermissionHelper.REQUEST_CHECK_SETTINGS;

public class CatProfileActivity extends BaseActivity implements View.OnClickListener, OnImagePickedListener {

    private String TAG = CatProfileActivity.class.getSimpleName();
    public static final String CAT_KEY = "cat_key";
    public static final String MODE_KEY = "mode_key";
    public static final String CAT_NAME_KEY = "cat_name_key";
    public static final String CAT_AVATAR_KEY = "cat_avatar_key";

    private String DEFAULT_VALUE = "setup";
    private String PET_DEFAULT_NAME = "Pet Name";
    private final int REQUEST_CODE_GET_IMAGE = 1;
    private final int REQUEST_CODE_GET_AVATAR = 2;

    CharSequence[] fleaTreatmentPickerList;
    private final int FLEA_TREATMENT_THREE_MONTHS = 0;
    private final int FLEA_TREATMENT_SIX_MONTHS = 1;
    private final int FLEA_TREATMENT_TWELVE_MONTHS = 2;

    @BindView(R.id.main_layout)
    ConstraintLayout mainLayout;
    @BindView(R.id.main_scrollView)
    ScrollView scrollView;
    @BindView(R.id.catTypePet)
    ImageView catTypePetImageView;
    @BindView(R.id.catTypeStray)
    ImageView catTypeStrayImageView;

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
    @BindView(R.id.dialogTextView)
    TextView followTextView;

    @BindView(R.id.upload_image_LinearLayout)
    LinearLayout uploadImageLinearLayout;
    private long petBirthdayMillis = 0;
    private long fleaTreatmentDateMilis = 0;
    private float weight = 0;
    private CatProfile.Status catType = CatProfile.Status.PET;
    private CatProfilePresenter presenter;
    private MenuItem saveMenu;
    private MenuItem editMenu;
    private MenuItem deleteMenu;
    private Location lastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private SystemPermissionHelper permissionHelper;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private int countOfSelectedPhotos;
    private List<PhotoWithPreview> photosToRemove;

    public enum CatProfileScreenMode {
        EDIT_MODE,
        VIEW_MODE,
        CREATE_MODE
    }

    private CatProfileScreenMode currentMode = CatProfileScreenMode.EDIT_MODE;

    private int clickedRoundViewId;
    private List<RoundedImageView> colorPickers;
    private List<PhotoWithPreview> photoList;
    private PhotosAdapter photosAdapter;
    private PhotoWithPreview avatar;

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
        startInCreateMode(activity, null, null);
    }

    public static void startInCreateMode(Activity activity, String name, File avatar) {
        Intent intent = new Intent(activity, CatProfileActivity.class);
        intent.putExtra(MODE_KEY, CatProfileScreenMode.CREATE_MODE);
        if (name != null)
            intent.putExtra(CAT_NAME_KEY, name);
        if (avatar != null)
            intent.putExtra(CAT_AVATAR_KEY, avatar);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_profile);
        getSupportActionBar().setElevation(0);

        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);

        presenter = new CatProfilePresenter(this);

        if (savedInstanceState != null) {
            currentMode = (CatProfileScreenMode) savedInstanceState.getSerializable(MODE_KEY);
            catProfile = (CatProfile) savedInstanceState.getSerializable(CAT_KEY);
        } else if (getIntent() != null) {
            if (getIntent().hasExtra(CAT_KEY))
                catProfile = (CatProfile) getIntent().getSerializableExtra(CAT_KEY);
            if (getIntent().hasExtra(CAT_NAME_KEY))
                petNameTextView.setText(getIntent().getStringExtra(CAT_NAME_KEY));
            if (getIntent().hasExtra(CAT_AVATAR_KEY))
                onImagePicked(REQUEST_CODE_GET_AVATAR, (File) getIntent().getSerializableExtra(CAT_AVATAR_KEY));

            currentMode = (CatProfileScreenMode) getIntent().getSerializableExtra(MODE_KEY);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (currentMode.equals(CatProfileScreenMode.CREATE_MODE)) {
            permissionHelper = new SystemPermissionHelper(this);
            initLocation();
        }

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

        fleaTreatmentPickerList = new CharSequence[]{"3 " + getString(R.string.months),
                "6 " + getString(R.string.months), "12 " + getString(R.string.months)};

        fillUI();
        setupUIMode();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    private void initLocation() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(30 * 1000)
                .setFastestInterval(20 * 1000);
        checkLocationAvailability();
    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @SuppressLint("MissingPermission")
    public void checkLocationAvailability() {
        if (!permissionHelper.isAccessLocationGranted()) {
            permissionHelper.requestPermissionsAccessLocation();
            return;
        }
        permissionHelper.checkLocationSettings(this, locationRequest, locationSettingsResponse -> {
            getLastLocation();
        });
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        if (mFusedLocationClient == null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null)
                        lastLocation = location;
                    else getLocationUpdates();
                });
    }

    @SuppressLint("MissingPermission")
    public void getLocationUpdates() {
        if (locationCallback != null) return;
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    lastLocation = locationResult.getLastLocation();
                    stopLocationUpdates();
                }
            }
        };
        if (mFusedLocationClient != null)
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        if (locationCallback != null && mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        locationCallback = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLastLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        //Toast.makeText(getApplicationContext(), "impossible to add photoFiles without location", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_LOCATION_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAvailability();
            }
        }
    }

    private void fillUI() {
        if (catProfile != null) {
            fillFollowAction();

            if (catProfile.getType() != null && catProfile.getType().equals(CatProfile.Status.STRAY))
                setupAnimalType(CatProfile.Status.STRAY);
            else selectAnimalTypePet();
            if (catProfile.getPetName() != null && !catProfile.getPetName().isEmpty())
                setTitle(catProfile.getPetName());
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
            avatar = catProfile.getAvatar();
            photoList = catProfile.getPhotos();
            photosToRemove = catProfile.getPhotosToRemove();
            petNamePhotosTextView.setText(catProfile.getPetName());
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

        updateAvatar();

        if (photoList == null)
            photoList = new ArrayList<>();

        photoCountTextView.setText(String.valueOf(photoList.size()));

        photosAdapter = new PhotosAdapter(photoList, this::showImage, this::deleteImage);
        //photosAdapter = new PhotosAdapter(photoList, this::showImage);
        photosRecyclerView.setAdapter(photosAdapter);
        photosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        groupPartnersList = new ArrayList<>();
        //groupPartnersList.add(new GroupPartner(null, "User1", false));
        groupPartnersAdapter = new GroupPartnersAdapter(groupPartnersList, false,//currentMode.equals(CatProfileScreenMode.EDIT_MODE),
                new GroupPartnersAdapter.OnPersonClickListener() {

                    @Override
                    public void onPersonClicked(GroupPartner groupPartner) {
                        Log.d(TAG, "onPersonClicked " + groupPartner.getName());
                        presenter.onGroupPartnerClicked(groupPartner);
                    }

                    @Override
                    public void onAddPerson() {
                        Log.d(TAG, "onAddPerson");
                        presenter.addGroupPartner(groupPartnersList, groupPartnersAdapter);
                        groupPartnersRecyclerView.scrollToPosition(0);
                    }
                }, null);
        groupPartnersRecyclerView.setAdapter(groupPartnersAdapter);

        if (!currentMode.equals(CatProfileScreenMode.CREATE_MODE))
            presenter.getGroupPartners(catProfile.getId());

        groupPartnersRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        viewColorsAdapter = new ViewColorsAdapter(colorsList);
        viewColorsRecyclerView.setAdapter(viewColorsAdapter);
        viewColorsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    public void fillFollowAction() {
        String status = catProfile.getFeedStationStatus();
        followButton.setVisibility(View.VISIBLE);
        followTextView.setText("follow cat");
        if (status != null && status.equals("joined")) {
            followButton.setVisibility(View.GONE);
            followTextView.setText("you follow this cat");
        }
        if (status != null && status.equals("invited")) {
            followButton.setVisibility(View.VISIBLE);
            followTextView.setText("follow cat (you invited)");
        }
        if (status != null && status.equals("requested")) {
            followButton.setVisibility(View.GONE);
            followTextView.setText("follow request has been sent");
        }
    }

    public void setStatusFollowed() {
        catProfile.setFeedStationStatus("requested");
        fillFollowAction();
    }

    public void setStatusJoined() {
        catProfile.setFeedStationStatus("joined");
        fillFollowAction();
    }

    public void refreshGroupPartners(List<GroupPartner> partners) {
        if (partners == null || partners.isEmpty() || groupPartnersRecyclerView == null) return;
        groupPartnersList.clear();
        for (GroupPartner user : partners) {
            if (user.isAdmin())
                groupPartnersList.add(0, user);
            else
                groupPartnersList.add(user);
        }
        groupPartnersAdapter.notifyDataSetChanged();
        groupPartnersRecyclerView.scrollToPosition(0);
    }

    final int THUMBSIZE = 250;

    private void updateAvatar() {
        Glide.with(this)
                .load(avatar != null ? avatar.getThumbnail() : R.drawable.cat_cover_avatar)
                .apply(new RequestOptions().override(THUMBSIZE, THUMBSIZE))
                .into(avatarImageView);
    }

    private void setupColorPickersColors() {
        for (int i = 0; i < colorPickers.size(); i++) {
            if (i < colorsList.size())
                setCatColorPickerColor(i, colorsList.get(i));
            else
                setCatColorPickerColor(i, getResources().getColor(R.color.transparent));

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

        currentMode = CatProfileScreenMode.VIEW_MODE;

        infoLinearLayout.setVisibility(View.VISIBLE);
        addPhotoButton.setVisibility(View.INVISIBLE);

        catTypePetImageView.setEnabled(false);
        catTypeStrayImageView.setEnabled(false);

        petNameTextView.setEnabled(false);

        // colorsList settings
        presenter.compressColorsList(colorsList);
        viewColorsRecyclerView.setVisibility(View.VISIBLE);
        expandColorsButton.setVisibility(View.GONE);
        colorConstraintLayout.setVisibility(View.GONE);

        viewColorsAdapter.notifyDataSetChanged();

        castratedCheckBox.setEnabled(false);

        // flea treatment settings
        fleaTreatmentPickerButton.setVisibility(View.GONE);

        uploadImageLinearLayout.setVisibility(View.GONE);

        descriptionEditText.setEnabled(false);

        //groupPartnersAdapter.switchToViewMode();
        photosAdapter.switchToViewMode();

        if (saveMenu != null)
            saveMenu.setVisible(false);
        if (editMenu != null)
            editMenu.setVisible(true);
        if (deleteMenu != null)
            deleteMenu.setVisible(true);

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

        if (currentMode.equals(CatProfileScreenMode.CREATE_MODE)) {
            catTypePetImageView.setEnabled(true);
            catTypeStrayImageView.setEnabled(true);
        }

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

        //groupPartnersAdapter.switchToEditMode();
        photosAdapter.switchToEditMode();

        if (saveMenu != null)
            saveMenu.setVisible(true);
        if (editMenu != null)
            editMenu.setVisible(false);
        if (deleteMenu != null)
            deleteMenu.setVisible(false);

        informationTextView.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) fleaTreatmentValueTextView.getLayoutParams();
        layoutParams.rightMargin = fleaTreatmentPickerButton.getLayoutParams().width + Utils.convertDpToPx(8, this);
        fleaTreatmentValueTextView.setLayoutParams(layoutParams);

        ageValueTextView.setEnabled(true);
        weightValueTextView.setEnabled(true);
    }

    private void showImage(String path) {
        if (path == null || path.isEmpty()) return;
        AttachmentImageActivity.start(this, path);
        Log.d(TAG, "showImage " + path);
    }

    private void deleteImage(Integer position) {
        if (position == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete picture?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    if (photoList.get(position).getExpectedAction() != null && photoList.get(position).getExpectedAction().equals(PhotoWithPreview.Action.ADD))
                        countOfSelectedPhotos--;
                    else {
                        if (photosToRemove == null)
                            photosToRemove = new ArrayList<>();
                        photosToRemove.add(photoList.get(position));
                        photosToRemove.get(photosToRemove.size() - 1)
                                .setExpectedAction(PhotoWithPreview.Action.DELETE);
                    }

                    photoList.remove(position.intValue());
                    photosAdapter.notifyItemRemoved(position);
                    photoCountTextView.setText(String.valueOf(photoList.size()));
                    Log.d(TAG, "deleteImage " + position);
                })
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_cat_profile_activity, menu);

        saveMenu = menu.findItem(R.id.app_bar_save);
        editMenu = menu.findItem(R.id.app_bar_edit);
        deleteMenu = menu.findItem(R.id.app_bar_delete);

        if (saveMenu != null && editMenu != null) {
            if (currentMode.equals(CatProfileScreenMode.VIEW_MODE)) {
                saveMenu.setVisible(false);
                editMenu.setVisible(true);
                deleteMenu.setVisible(true);
            } else {
                saveMenu.setVisible(true);
                editMenu.setVisible(false);
                deleteMenu.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_save:
                Log.d(TAG, "app_bar_save");
                fillCatProfile();
                if (!isProfileValid())
                    return true;
                presenter.uploadCatWithPhotos(catProfile, lastLocation);
                /*if (currentMode.equals(CatProfileScreenMode.CREATE_MODE))
                    presenter.saveCat(fillCatProfile(), lastLocation);
                else if (currentMode.equals(CatProfileScreenMode.EDIT_MODE))
                    presenter.updateCat(fillCatProfile());*/
                return true;
            case R.id.app_bar_edit:
                Log.d(TAG, "app_bar_edit");
                if (catProfile.getUserRole() != null && catProfile.getUserRole().equals(Feedstation.UserRole.ADMIN)) {
                    currentMode = CatProfileScreenMode.EDIT_MODE;
                    setupUIMode();
                    return true;
                }
                Toaster.longToast("Only admins can modify cats");
                return true;
            case R.id.app_bar_delete:
                Log.d(TAG, "app_bar_delete");
                showDeleteCatDialog();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteCatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_cat_question);
        builder.setPositiveButton("Delete", (dialogInterface, i) -> presenter.onDeleteCatClicked(catProfile));
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }

    private boolean isProfileValid() {
        /*lastLocation = new Location("fused");
        lastLocation.setLatitude(51.5016225);
        lastLocation.setLongitude(31.2924888);*/
        if (currentMode.equals(CatProfileScreenMode.CREATE_MODE) && catType.equals(CatProfile.Status.PET) && lastLocation == null) {
            Toaster.longToast("We need location to display your cat on the map");
            checkLocationAvailability();
            return false;
        } else if (catType.equals(CatProfile.Status.STRAY) &&
                (catProfile == null || catProfile.getFeedstationId() == null)) {
            selectAnimalTypePet();
            Toaster.shortToast(R.string.stray_cat_no_feedstation_error);
            return false;
        } else if (catProfile.getPetName().isEmpty() || catProfile.getColorsList().isEmpty() || catProfile.getWeight() <= 0f) {
            Toaster.longToast("You should fill in PetName, color(s), weight");
            return false;
        } else return true;
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
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -Consts.CAT_MAX_YEARS_OLD);
        Long minDate = calendar.getTimeInMillis();
        new WrappedDatePickerDialog(CatProfileActivity.this, petBirthdayMillis, minDate, (datePicker, i, i1, i2) -> {
            petBirthdayMillis = TimeUtils.getTimeInMillis(i, i1, i2);
            ageValueTextView.setText(presenter.getAgeInString(petBirthdayMillis));
        });
    }

    @OnClick(R.id.weight_value_textView)
    void onWeightClick() {
        EditTextDialog editTextDialog = new EditTextDialog(this, "Enter cat's weight", "For example: 5.3",
                new EditTextDialog.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        String enteredValue = getEditTextDialog().getEditText().getText().toString();
                        if (Utils.isStringNumericPositive(enteredValue)) {
                            if (Float.parseFloat(enteredValue) <= 20) {
                                setWeight(String.valueOf(Float.parseFloat(enteredValue)));
                                dismiss();
                            } else {
                                Toaster.shortToast("Weight should be no more than 20");
                            }
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
        editTextDialog.setWeightFilter();
        editTextDialog.setEditTextText((weight > 0 && !weightValueTextView.getText().toString().equals(DEFAULT_VALUE)) ? String.valueOf(weight) : null);
        editTextDialog.setEditTextInputType(InputType.TYPE_CLASS_PHONE);
        editTextDialog.show();
    }

    @OnClick(R.id.catTypePet)
    void selectAnimalTypePet() {
        setupAnimalType(CatProfile.Status.PET);
    }

    @OnClick(R.id.catTypeStray)
    void selectAnimalTypeStray() {
        if (catType.equals(CatProfile.Status.PET) && currentMode.equals(CatProfileScreenMode.EDIT_MODE) && catProfile.getType().equals(CatProfile.Status.PET)) {
            Toaster.shortToast("Your cat can't be stray");
            return;
        }
        setupAnimalType(CatProfile.Status.STRAY);
        presenter.getStrayFeedstations();
    }

    private void setupAnimalType(CatProfile.Status catType) {
        switch (catType) {
            case PET:
                catTypePetImageView.setBackgroundResource(R.drawable.pet_selected);
                catTypeStrayImageView.setBackgroundResource(R.drawable.stray_non_selected);
                this.catType = CatProfile.Status.PET;
                break;
            case STRAY:
                catTypePetImageView.setBackgroundResource(R.drawable.pet_non_selected);
                catTypeStrayImageView.setBackgroundResource(R.drawable.stray_selected);
                this.catType = CatProfile.Status.STRAY;
                break;
        }
    }

    @OnClick(R.id.addPhotoButton)
    void selectAvatar() {
        //pickPhotoWithPermission(getString(R.string.select_cat_photo));
        new ImagePickHelper().pickAnImage(this, REQUEST_CODE_GET_AVATAR);
    }

    @OnClick(R.id.upload_image_button)
    void uploadCatImage() {
        //pickPhotoWithPermission(getString(R.string.select_cat_photo));
        if (countOfSelectedPhotos < 5)
            new ImagePickHelper().pickAnImages(this, REQUEST_CODE_GET_IMAGE);
        else Toaster.shortToast("You can add up to 5 photos per cat update");
    }

    private CatProfile fillCatProfile() {
        if (catProfile == null)
            catProfile = new CatProfile(true);

        presenter.compressColorsList(colorsList);

        List c = new ArrayList<>();
        c.addAll(colorsList);
        catProfile.setColorsList(c);

        if (!currentMode.equals(CatProfileScreenMode.VIEW_MODE))
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
        catProfile.setPhotosToRemove(photosToRemove);
        catProfile.setPhotos(photoList);
        catProfile.setAvatar(avatar);

        return catProfile;
    }

    public void forceClose() {
        finish();
    }

    @OnClick(R.id.flea_treatment_picker_button)
    void showFleaTreatmentPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(fleaTreatmentPickerList,
                (dialogInterface, i) -> {
                    int addMonthsCount = 0;
                    switch (i) {
                        case FLEA_TREATMENT_THREE_MONTHS:
                            addMonthsCount = 3;
                            break;
                        case FLEA_TREATMENT_SIX_MONTHS:
                            addMonthsCount = 6;
                            break;
                        case FLEA_TREATMENT_TWELVE_MONTHS:
                            addMonthsCount = 12;
                            break;
                    }
                    Calendar fleaThretmentCalendar = Calendar.getInstance();
                    fleaThretmentCalendar.add(Calendar.MONTH, addMonthsCount);
                    fleaTreatmentDateMilis = fleaThretmentCalendar.getTimeInMillis();
                    fleaTreatmentValueTextView.setText(TimeUtils.getDateAsDDMMYYYY(fleaTreatmentDateMilis));
                });
        builder.show();
    }

    @OnClick(R.id.pet_name_textView)
    void changeCatName() {
        EditTextDialog editTextDialog = new EditTextDialog(this, "Enter cat's name", "name",
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
        editTextDialog.setEditTextText((!name.isEmpty() && !name.equals(PET_DEFAULT_NAME)) ? name : null);
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
                Log.d(TAG, "Pet color picked: " + color);
                for (int i = 0; i < colorPickers.size(); i++)
                    if (colorPickers.get(i).getId() == clickedRoundViewId) {
                        setCatColorListColor(i, color);
                        setCatColorPickerColor(i, color);
                    }
            });
            colorPickerDialog.show();
            clickedRoundViewId = view.getId();
        }
    }

    private void setCatColorListColor(int colorListNumber, int color) {
        if (color == 0 || color == -1)
            color = getResources().getColor(R.color.white);
        colorsList.set(colorListNumber, color);
    }

    private void setCatColorPickerColor(int colorPickerNumber, int color) {
        colorPickers.get(colorPickerNumber).setBorderWidth((float) Utils.convertDpToPx(2, this));
        // set picker color
        if (color == 0 || color == -1)
            color = getResources().getColor(R.color.white);
        colorPickers.get(colorPickerNumber).setImageBitmap(Utils.getBitmapWithColor(color));

        // set picker border color
        int borderColor = getResources().getColor(R.color.cat_color_border);
        if (color != getResources().getColor(R.color.white) && color != getResources().getColor(R.color.transparent)) {
            colorPickers.get(colorPickerNumber).setBorderWidth((float) Utils.convertDpToPx(0, this));
            borderColor = color;
        }
        colorPickers.get(colorPickerNumber).setBorderColor(borderColor);
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
            photosToRemove = null;
            for (PhotoWithPreview photo : photoList)
                photo.setExpectedAction(null);
            setupUIMode();
        }
    }

    public void feedstationsLoaded(List<Feedstation> feedstations) {
        if (catType.equals(CatProfile.Status.PET) || feedstations == null) return;
        for (int i = 0; i < feedstations.size(); i++) {
            if (!feedstations.get(i).getIsPublic() || (feedstations.get(i).getStatus() != null && !feedstations.get(i).getStatus().equals(GroupPartner.Status.JOINED)))
                feedstations.remove(i);
        }
        if (feedstations.size() < 1) {
            selectAnimalTypePet();
            Toaster.shortToast("You must be a member of public feedstation");
            return;
        }
        String[] items = new String[feedstations.size()];
        for (int i = 0; i < feedstations.size(); i++)
            items[i] = feedstations.get(i).getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose feedstation for cat");
        builder.setItems(items, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.getListView().setOnItemClickListener((adapterView, view, i, l) -> {
            if (catProfile == null)
                catProfile = new CatProfile(true);
            catProfile.setFeedstationId(feedstations.get(i).getId());
            alertDialog.dismiss();
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                selectAnimalTypePet();
                Toaster.shortToast(R.string.stray_cat_no_feedstation_error);
            }
        });
        alertDialog.show();
    }

    @OnClick(R.id.follow_button)
    void onFollowClick() {
        presenter.onFollowCatClicked(catProfile);
    }

    public void showSuccessFollowMessage() {
        Toaster.shortToast(R.string.you_have_successfully_joined);
    }

    public void showNoFeedstationMessage() {
        Toaster.shortToast(R.string.no_feedstation);
    }


    @Override
    public void onImagePicked(int requestCode, File file) {
        if (file != null) {
            if (REQUEST_CODE_GET_IMAGE == requestCode) {
                addImageToPhotosAndTopImagesList(file);
                photosAdapter.notifyItemInserted(0);
                photosRecyclerView.scrollToPosition(0);
                photoCountTextView.setText(String.valueOf(photoList.size()));
            } else if (REQUEST_CODE_GET_AVATAR == requestCode) {
                if (avatar == null)
                    avatar = new PhotoWithPreview(null, file.getPath(), file.getPath());
                else {
                    avatar.setPhoto(file.getPath());
                    avatar.setThumbnail(file.getPath());
                }
                avatar.setExpectedAction(PhotoWithPreview.Action.CHANGE);
                updateAvatar();
            }
        }
    }

    @Override
    public void onImagesPicked(int requestCode, List<File> files) {
        if (REQUEST_CODE_GET_IMAGE == requestCode && files != null) {
            for (File file : files)
                addImageToPhotosAndTopImagesList(file);
            photosAdapter.notifyDataSetChanged();
            photosRecyclerView.scrollToPosition(0);
            photoCountTextView.setText(String.valueOf(photoList.size()));
        }
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

    private void addImageToPhotosAndTopImagesList(File file) {
        countOfSelectedPhotos++;
        photoList.add(0, new PhotoWithPreview(file.getPath(), file.getPath(), PhotoWithPreview.Action.ADD));
    }
}