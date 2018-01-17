package com.varteq.catslovers.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.model.PhotoWithPreview;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.qb.imagepick.ImagePickHelper;
import com.varteq.catslovers.utils.qb.imagepick.OnImagePickedListener;
import com.varteq.catslovers.view.adapters.GroupPartnersAdapter;
import com.varteq.catslovers.view.adapters.PhotosAdapter;
import com.varteq.catslovers.view.dialog.EditTextDialog;
import com.varteq.catslovers.view.presenter.FeedstationPresenter;
import com.varteq.catslovers.view.qb.AttachmentImageActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedstationActivity extends BaseActivity implements OnImagePickedListener {

    private final int REQUEST_CODE_GET_IMAGE = 110;
    private final int RESULT_PICK_CONTACT = 11;
    private final String STATION_DEFAULT_NAME = "Station name";
    private final String ADDRESS_DEFAULT_VALUE = "station address";
    private String TAG = FeedstationActivity.class.getSimpleName();

    @BindView(R.id.avatar_imageView)
    ImageView avatarImageView;

    @BindView(R.id.expand_description_button)
    Button expandDescriptionButton;
    @BindView(R.id.description_editText)
    EditText descriptionEditText;
    @BindView(R.id.station_name_textView)
    TextView stationNameTextView;
    @BindView(R.id.address_textView)
    TextView addressTextView;
    @BindView(R.id.description_background)
    View descriptionBackground;
    @BindView(R.id.follow_button)
    Button followButton;
    @BindView(R.id.dialogTextView)
    TextView dialogTextView;
    @BindView(R.id.station_name_photos_textView)
    TextView stationNamePhotosTextView;

    @BindView(R.id.photos_RecyclerView)
    RecyclerView photosRecyclerView;
    @BindView(R.id.upload_image_LinearLayout)
    LinearLayout uploadImageLinearLayout;
    @BindView(R.id.photo_count_textView)
    TextView photoCountTextView;

    @BindView(R.id.expand_partners_button)
    Button expandPartnersButton;
    @BindView(R.id.group_partners_RecyclerView)
    RecyclerView groupPartnersRecyclerView;

    public static final String FEEDSTATION_KEY = "feed_key";
    public static final String LOCATION_KEY = "loc_key";
    public static final String MODE_KEY = "mode_key";
    private MenuItem saveMenu;
    private MenuItem editMenu;
    private int countOfSelectedPhotos = 0;

    public enum FeedstationScreenMode {
        EDIT_MODE,
        VIEW_MODE,
        CREATE_MODE
    }

    private FeedstationScreenMode currentMode = FeedstationScreenMode.EDIT_MODE;
    private List<PhotoWithPreview> photoList;
    private List<PhotoWithPreview> pagerPhotoList;
    private List<GroupPartner> groupPartnersList = new ArrayList<>();

    private PhotosAdapter photosAdapter;
    private GroupPartnersAdapter groupPartnersAdapter;
    private Feedstation feedstation;

    FeedstationPresenter presenter;
    ViewPager viewPager;
    HeaderPhotosViewPagerAdapter pagerAdapter;
    private int[] headerPhotos = {R.drawable.cat2, R.drawable.cat3, R.drawable.cat1};
    Geocoder geocoder;

    public static void startInViewMode(Activity activity, Feedstation feedstation) {
        Intent intent = new Intent(activity, FeedstationActivity.class);
        intent.putExtra(MODE_KEY, FeedstationScreenMode.VIEW_MODE);
        intent.putExtra(FEEDSTATION_KEY, feedstation);
        activity.startActivity(intent);
    }

    public static void startInCreateMode(Activity activity, LatLng location) {
        Intent intent = new Intent(activity, FeedstationActivity.class);
        intent.putExtra(MODE_KEY, FeedstationScreenMode.CREATE_MODE);
        intent.putExtra(LOCATION_KEY, location);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedstation);
        ButterKnife.bind(this);

        presenter = new FeedstationPresenter(this);

        if (savedInstanceState != null) {
            currentMode = (FeedstationScreenMode) savedInstanceState.getSerializable(MODE_KEY);
            feedstation = savedInstanceState.getParcelable(FEEDSTATION_KEY);
        } else if (getIntent() != null) {
            if (getIntent().hasExtra(FEEDSTATION_KEY))
                feedstation = getIntent().getExtras().getParcelable(FEEDSTATION_KEY);
            else if (getIntent().hasExtra(LOCATION_KEY)) {
                if (feedstation == null)
                    feedstation = new Feedstation();
                feedstation.setIsPublic(true);
                feedstation.setUserRole(Feedstation.UserRole.ADMIN);
                feedstation.setLocation(getIntent().getExtras().getParcelable(LOCATION_KEY));
                setAdress(feedstation.getLocation());
            }

            currentMode = (FeedstationScreenMode) getIntent().getSerializableExtra(MODE_KEY);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*Glide.with(this)
                .load(getResources().getDrawable(R.drawable.cat2))
                .into(avatarImageView);*/

        fillUI();
        setupUIMode();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    private void setAdress(LatLng location) {
        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    // In this sample, get just a single address.
                    1);
            //addressTextView.setText(addresses.get(0).getAddressLine(0));
            feedstation.setAddress(addresses.get(0).getAddressLine(0));

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            //errorMessage = getString(R.string.service_not_available);
            //Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            //errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, "Latitude = " + location.latitude +
                    ", Longitude = " +
                    location.longitude, illegalArgumentException);
        }

    }

    private void fillUI() {
        if (feedstation != null) {
            stationNameTextView.setText(feedstation.getName());
            addressTextView.setText(feedstation.getAddress());
            descriptionEditText.setText(feedstation.getDescription());
            photoList = feedstation.getPhotos();
            pagerPhotoList = new ArrayList<>();
            stationNamePhotosTextView.setText(feedstation.getName() != null ? feedstation.getName() : getString(R.string.new_cat_profile_screen_title));
            initStationAction();
        }
        /*scrollView.setOnTouchListener((v, event) -> {
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
        });*/

        if (photoList == null)
            photoList = new ArrayList<>();
        if (pagerPhotoList == null)
            pagerPhotoList = new ArrayList<>();
        pagerPhotoList.addAll(photoList);

        photoCountTextView.setText(String.valueOf(photoList.size()));

        photosAdapter = new PhotosAdapter(photoList, this::showImage);
        photosRecyclerView.setAdapter(photosAdapter);
        photosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        pagerAdapter = new HeaderPhotosViewPagerAdapter(this);
        viewPager = findViewById(R.id.header_photo_viewPager);
        viewPager.setAdapter(pagerAdapter);

        groupPartnersAdapter = new GroupPartnersAdapter(groupPartnersList, currentMode.equals(FeedstationScreenMode.EDIT_MODE),
                new GroupPartnersAdapter.OnPersonClickListener() {

                    @Override
                    public void onPersonClicked(GroupPartner groupPartner) {
                        if (feedstation.getUserRole() != null && feedstation.getUserRole().equals(Feedstation.UserRole.ADMIN)) {
                            Log.d(TAG, "onPersonClicked " + groupPartner.getName());
                            if (groupPartner.getStatus().equals(GroupPartner.Status.INVITED))
                                Toaster.shortToast("user invited");
                            else if (groupPartner.getStatus().equals(GroupPartner.Status.REQUESTED)) {
                                new AlertDialog.Builder(FeedstationActivity.this)
                                        .setTitle("Allow the user to become a member")
                                        .setNeutralButton(android.R.string.cancel, null)
                                        .setNegativeButton(R.string.no, (dialogInterface, i) ->
                                                presenter.deleteGroupPartner(feedstation.getId(), groupPartner.getUserId()))
                                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) ->
                                                presenter.addGroupPartner(feedstation.getId(), groupPartner.getPhone()))
                                        .create()
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onAddPerson() {
                        Log.d(TAG, "onAddPerson");
                        if (feedstation.getUserRole() != null && feedstation.getUserRole().equals(Feedstation.UserRole.ADMIN))
                            pickContact();
                    }
                });
        groupPartnersRecyclerView.setAdapter(groupPartnersAdapter);

        if (!currentMode.equals(FeedstationScreenMode.CREATE_MODE)) {
            presenter.getGroupPartners(feedstation.getId());
            presenter.getCatsImages(feedstation.getId());
        }

        groupPartnersRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    public void setStationActionName(String name) {
        dialogTextView.setText(name);
    }

    private void setupUIMode() {
        if (currentMode.equals(FeedstationScreenMode.VIEW_MODE))
            setupViewMode();
        else
            setupEditMode();
    }

    private void setupViewMode() {
        Log.d(TAG, "setupViewMode");

        if (stationNameTextView.getText().toString().isEmpty())
            stationNameTextView.setText(STATION_DEFAULT_NAME);
        if (addressTextView.getText().toString().equals(ADDRESS_DEFAULT_VALUE))
            addressTextView.setText("");
        setToolbarTitle(stationNameTextView.getText().toString());

        stationNameTextView.setEnabled(false);

        currentMode = FeedstationScreenMode.VIEW_MODE;

        /*addPhotoButton.setVisibility(View.INVISIBLE);
        addPhotoButton.setOnClickListener(null);*/

        uploadImageLinearLayout.setVisibility(View.GONE);

        descriptionEditText.setEnabled(false);

        groupPartnersAdapter.switchToViewMode();

        if (saveMenu != null)
            saveMenu.setVisible(false);
        if (editMenu != null)
            editMenu.setVisible(true);
    }

    private void setupEditMode() {
        Log.d(TAG, "setupEditMode");

        if (stationNameTextView.getText().toString().isEmpty())
            stationNameTextView.setText(STATION_DEFAULT_NAME);
        if (addressTextView.getText().toString().isEmpty())
            addressTextView.setText(ADDRESS_DEFAULT_VALUE);

        stationNameTextView.setEnabled(true);

        /*addPhotoButton.setVisibility(View.VISIBLE);
        addPhotoButton.setOnClickListener(view -> Toaster.shortToast("Coming soon"));*/

        uploadImageLinearLayout.setVisibility(View.VISIBLE);

        descriptionEditText.setEnabled(true);

        if (currentMode.equals(FeedstationScreenMode.EDIT_MODE))
            groupPartnersAdapter.switchToEditMode();

        if (saveMenu != null)
            saveMenu.setVisible(true);
        if (editMenu != null)
            editMenu.setVisible(false);
    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_cat_profile_activity, menu);

        saveMenu = menu.findItem(R.id.app_bar_save);
        editMenu = menu.findItem(R.id.app_bar_edit);

        if (saveMenu != null && editMenu != null) {
            if (currentMode.equals(FeedstationScreenMode.VIEW_MODE)) {
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
/*                if (!feedstation.getIsPublic()) {
                    savedSuccessfully();
                    return true;
                }*/
                if (!isProfileValid())
                    return true;
                presenter.uploadFeedstationWithPhotos(fillFeedstation());
                /*if (currentMode.equals(FeedstationScreenMode.CREATE_MODE))
                    presenter.saveFeedstation(fillFeedstation());
                else if (currentMode.equals(FeedstationScreenMode.EDIT_MODE))
                    presenter.updateFeedstation(fillFeedstation());*/
                return true;
            case R.id.app_bar_edit:
                Log.d(TAG, "app_bar_edit");
                //if (!feedstation.getCreatedUserId().equals(Profile.getUserId(this))) {
                if (feedstation.getUserRole() != null && feedstation.getUserRole().equals(Feedstation.UserRole.ADMIN)) {
                    currentMode = FeedstationScreenMode.EDIT_MODE;
                    setupUIMode();
                    return true;
                }
                Toaster.longToast("Only admins can modify feedstations");
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isProfileValid() {
        if (stationNameTextView.getText().toString().equals(STATION_DEFAULT_NAME)) {
            Toaster.longToast("You should fill in station name");
            return false;
        }
        if (addressTextView.getText().toString().equals(ADDRESS_DEFAULT_VALUE)) {
            Toaster.longToast("You should fill in station address");
            return false;
        }
        return true;
    }

    private Feedstation fillFeedstation() {
        if (feedstation == null)
            feedstation = new Feedstation();

        String stationName = stationNameTextView.getText().toString();
        String address = addressTextView.getText().toString();
        String description = descriptionEditText.getText().toString();

        feedstation.setName(stationName);
        feedstation.setAddress(address);
        feedstation.setDescription(description);
        feedstation.setPhotos(photoList);

        return feedstation;
    }

    private void initStationAction() {
        if (feedstation != null) {
            if (feedstation.getUserRole() != Feedstation.UserRole.ADMIN) {
                if (feedstation.getStatus() != null) {
                    switch (feedstation.getStatus()) {
                        case JOINED:
                            setStationActionName(getString(R.string.leave_group));
                            followButton.setBackground(getResources().getDrawable(R.drawable.ic_close_24dp));
                            followButton.setVisibility(View.VISIBLE);
                            break;
                        case REQUESTED:
                            setStationActionName(getString(R.string.group_join_request_sent));
                            followButton.setVisibility(View.GONE);
                            break;
                        case INVITED:
                            setStationActionName(getString(R.string.group_join_invited));
                            followButton.setVisibility(View.VISIBLE);
                            break;
                        default:
                            setStationActionName(getString(R.string.join_group));
                            followButton.setVisibility(View.VISIBLE);
                            break;
                    }
                } else {
                    setStationActionName(getString(R.string.join_group));
                    followButton.setVisibility(View.VISIBLE);
                }
            } else {
                setStationActionName(getString(R.string.group_admin));
                followButton.setVisibility(View.GONE);
            }
        }
    }


    private void showImage(String path) {
        if (path == null || path.isEmpty()) return;
        AttachmentImageActivity.start(this, path);
        Log.d(TAG, "showImage " + path);

        /*Intent intent = new Intent();
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
        intent.setDataAndType(imageUri, "image*//*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);*/
    }

    @OnClick(R.id.station_name_textView)
    void changeStationName() {
        String name = stationNameTextView.getText().toString();
        if (name.equals(STATION_DEFAULT_NAME))
            name = "";

        EditTextDialog editTextDialog = new EditTextDialog(this, "Enter station name", "station name",
                !name.isEmpty() ? name : null, InputType.TYPE_CLASS_TEXT,
                new EditTextDialog.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        String enteredValue = getEditTextDialog().getEditText().getText().toString();
                        if (enteredValue.length() > 1) {
                            stationNameTextView.setText(enteredValue);
                            dismiss();
                        } else {
                            Toaster.shortToast("Enter at least 2 symbols");
                        }
                    }
                }
        );

        editTextDialog.show();
    }

    @OnClick(R.id.address_textView)
    void changeStationAddress() {
        String address = addressTextView.getText().toString();

        EditTextDialog editTextDialog = new EditTextDialog(this, "Enter station address", "station address",
                !address.isEmpty() ? address : null, InputType.TYPE_CLASS_TEXT,
                new EditTextDialog.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        String enteredValue = getEditTextDialog().getEditText().getText().toString();
                        if (enteredValue.length() > 1) {
                            addressTextView.setText(enteredValue);
                            dismiss();
                        } else {
                            Toaster.shortToast("Enter at least 2 symbols");
                        }
                    }
                }
        );

        editTextDialog.show();
    }

    @OnClick(R.id.upload_image_button)
    void uploadCatImage() {
        if (countOfSelectedPhotos < 5)
            new ImagePickHelper().pickAnImages(this, REQUEST_CODE_GET_IMAGE);
        else Toaster.shortToast("You can add up to 5 photos per station update");
    }

    private class HeaderPhotosViewPagerAdapter extends PagerAdapter {
        private static final int THUMBSIZE = 500;
        Context context;

        public HeaderPhotosViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            if (pagerPhotoList == null)
                return 0;
            else return pagerPhotoList.size() < 4 ? pagerPhotoList.size() : 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            Glide.with(container)
                    .load(pagerPhotoList.get(position).getPhoto())
                    .apply(new RequestOptions().override(THUMBSIZE, THUMBSIZE))
                    .into(imageView);

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }

    @OnClick(R.id.expand_description_button)
    void expandCollapseDescription() {
        if (descriptionEditText.getVisibility() == View.VISIBLE) {
            expandDescriptionButton.setBackgroundResource(R.drawable.ic_expand_more_24dp);
            descriptionEditText.setVisibility(View.GONE);
            descriptionBackground.setVisibility(View.INVISIBLE);
        } else {
            expandDescriptionButton.setBackgroundResource(R.drawable.ic_expand_less_24dp);
            descriptionEditText.setVisibility(View.VISIBLE);
            descriptionBackground.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.expand_partners_button)
    void expandCollapseGroupPartners() {
        if (groupPartnersRecyclerView.getVisibility() == View.VISIBLE) {
            expandPartnersButton.setBackgroundResource(R.drawable.ic_expand_more_24dp);
            groupPartnersRecyclerView.setVisibility(View.GONE);
        } else {
            expandPartnersButton.setBackgroundResource(R.drawable.ic_expand_less_24dp);
            groupPartnersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void savedSuccessfully() {
        currentMode = FeedstationScreenMode.VIEW_MODE;
        setupUIMode();
        /*if (currentMode.equals(FeedstationScreenMode.CREATE_MODE)) {
            FeedstationActivity.this.finishAffinity();
            startActivity(new Intent(this, MainActivity.class));
        } else {

        }*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(MODE_KEY, currentMode);
        outState.putParcelable(FEEDSTATION_KEY, fillFeedstation());
    }

    // Pick contact logic
    public void pickContact() {
        if (feedstation != null && feedstation.getId() != null && feedstation.getId() > 0) {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
        } else Toaster.shortToast("First you should save this station");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    private String contactPicked(Intent data) {
        Cursor cursor;
        try {
            String phoneNo;
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);
            // Set the value to the textviews

            presenter.addGroupPartner(feedstation.getId(), phoneNo);

            return phoneNo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @OnClick(R.id.follow_button)
    void onFollowClick() {
        presenter.onGroupActionButtonClicked(feedstation);
    }

    public void onSuccessFollow() {
        Toaster.shortToast("Follow request sent");
        setStationActionName(getString(R.string.group_join_request_sent));
        followButton.setVisibility(View.GONE);
        feedstation.setStatus(GroupPartner.Status.REQUESTED);
    }

    public void onSuccessLeave() {
        Toaster.shortToast("You have been leaved the group");
        setStationActionName(getString(R.string.join_group));
        followButton.setVisibility(View.VISIBLE);
        feedstation.setStatus(null);
    }

    public void onSuccessJoin() {
        Toaster.shortToast("You have successfully joined");
    }

    public void addCatsImages(List<CatProfile> cats) {
        if (cats == null || cats.isEmpty()) return;
        for (CatProfile cat : cats) {

        }
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

    public void updateGroupPartnersItem(GroupPartner partner) {
        if (partner == null || groupPartnersRecyclerView == null) return;

        for (int i = 0; i < groupPartnersList.size(); i++) {
            if (groupPartnersList.get(i).getUserId().equals(partner.getUserId())) {
                groupPartnersList.set(i, partner);
                groupPartnersAdapter.notifyItemChanged(i);
                return;
            }
        }
        groupPartnersList.add(1, partner);
        groupPartnersAdapter.notifyItemInserted(1);
        groupPartnersRecyclerView.scrollToPosition(0);
    }

    public void catsLoaded(List<CatProfile> list) {
        if (null != list) {
            for (CatProfile item : list) {
                if (null != item.getAvatar())
                    photoList.add(item.getAvatar());
            }
            photosAdapter.notifyDataSetChanged();
            photoCountTextView.setText(String.valueOf(photoList.size()));
        }
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        if (REQUEST_CODE_GET_IMAGE == requestCode && file != null) {
            addImageToPhotosAndTopImagesList(file);
            pagerAdapter.notifyDataSetChanged();
            photosAdapter.notifyItemInserted(0);
            photosRecyclerView.scrollToPosition(0);
            photoCountTextView.setText(String.valueOf(photoList.size()));
        }
    }

    @Override
    public void onImagesPicked(int requestCode, List<File> files) {
        if (REQUEST_CODE_GET_IMAGE == requestCode && files != null) {
            for (File file : files)
                addImageToPhotosAndTopImagesList(file);
            pagerAdapter.notifyDataSetChanged();
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
        pagerPhotoList.add(photoList.get(0));
    }
}
