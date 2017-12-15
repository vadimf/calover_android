package com.varteq.catslovers.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import com.google.android.gms.maps.model.LatLng;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.adapters.CatPhotosAdapter;
import com.varteq.catslovers.view.adapters.GroupPartnersAdapter;
import com.varteq.catslovers.view.dialog.EditTextDialog;
import com.varteq.catslovers.view.presenter.FeedstationPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedstationActivity extends PhotoPickerActivity {

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

    @BindView(R.id.photos_RecyclerView)
    RecyclerView photosRecyclerView;
    @BindView(R.id.upload_image_LinearLayout)
    LinearLayout uploadImageLinearLayout;

    @BindView(R.id.expand_partners_button)
    Button expandPartnersButton;
    @BindView(R.id.group_partners_RecyclerView)
    RecyclerView groupPartnersRecyclerView;

    public static final String FEEDSTATION_KEY = "feed_key";
    public static final String LOCATION_KEY = "loc_key";
    public static final String MODE_KEY = "mode_key";
    private MenuItem saveMenu;
    private MenuItem editMenu;

    public enum FeedstationScreenMode {
        EDIT_MODE,
        VIEW_MODE,
        CREATE_MODE
    }

    private FeedstationScreenMode currentMode = FeedstationScreenMode.EDIT_MODE;
    private List<Uri> photoList;
    private List<GroupPartner> groupPartnersList;

    private CatPhotosAdapter photosAdapter;
    private GroupPartnersAdapter groupPartnersAdapter;
    private Feedstation feedstation;

    FeedstationPresenter presenter;
    ViewPager viewPager;
    HeaderPhotosViewPagerAdapter pagerAdapter;
    private int[] headerPhotos = {R.drawable.cat2, R.drawable.cat3, R.drawable.cat1};

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
                feedstation.setLocation(getIntent().getExtras().getParcelable(LOCATION_KEY));
            }

            currentMode = (FeedstationScreenMode) getIntent().getSerializableExtra(MODE_KEY);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Glide.with(this)
                .load(getResources().getDrawable(R.drawable.cat2))
                .into(avatarImageView);

        pagerAdapter = new HeaderPhotosViewPagerAdapter(this);
        viewPager = findViewById(R.id.header_photo_viewPager);
        viewPager.setAdapter(pagerAdapter);

        groupPartnersList = new ArrayList<>();
        groupPartnersList.add(new GroupPartner(null, "Admin", true));
        groupPartnersList.add(new GroupPartner(null, "User1", false));
        groupPartnersAdapter = new GroupPartnersAdapter(groupPartnersList, true,
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

        photoList = new ArrayList<>();
        photosAdapter = new CatPhotosAdapter(photoList, this::showImage);
        photosRecyclerView.setAdapter(photosAdapter);
        photosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        fillUI();
        setupUIMode();
    }

    private void fillUI() {
        if (feedstation != null) {
            stationNameTextView.setText(feedstation.getName());
            addressTextView.setText(feedstation.getAddress());
            descriptionEditText.setText(feedstation.getDescription());
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

        /*Uri avatarUri = Profile.getUserAvatar(this);
        if (avatarUri != null && !avatarUri.toString().isEmpty())
            avatarImageView.setImageURI(avatarUri);
        else
            avatarImageView.setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));*/

        photoList = new ArrayList<>();
        photosAdapter = new CatPhotosAdapter(photoList, this::showImage);
        photosRecyclerView.setAdapter(photosAdapter);
        photosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        groupPartnersList = new ArrayList<>();
        groupPartnersList.add(new GroupPartner(null, "Admin", true));
        //groupPartnersList.add(new GroupPartner(null, "User1", false));
        groupPartnersAdapter = new GroupPartnersAdapter(groupPartnersList, !currentMode.equals(FeedstationScreenMode.VIEW_MODE),
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

        /*addPhotoButton.setVisibility(View.VISIBLE);
        addPhotoButton.setOnClickListener(view -> Toaster.shortToast("Coming soon"));*/

        uploadImageLinearLayout.setVisibility(View.VISIBLE);

        descriptionEditText.setEnabled(true);

        groupPartnersAdapter.switchToEditMode();

        if (saveMenu != null)
            saveMenu.setVisible(true);
        if (editMenu != null)
            editMenu.setVisible(false);
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
                if (!isProfileValid())
                    return true;
                if (currentMode.equals(FeedstationScreenMode.CREATE_MODE))
                    presenter.saveFeedstation(fillFeedstation());
                else if (currentMode.equals(FeedstationScreenMode.EDIT_MODE))
                    presenter.updateFeedstation(fillFeedstation());
                return true;
            case R.id.app_bar_edit:
                Log.d(TAG, "app_bar_edit");
                currentMode = FeedstationScreenMode.EDIT_MODE;
                setupUIMode();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isProfileValid() {
        if (stationNameTextView.getText().toString().equals(STATION_DEFAULT_NAME))
            return false;
        if (addressTextView.getText().toString().equals(ADDRESS_DEFAULT_VALUE))
            return false;
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

        return feedstation;
    }

    @Override
    protected void onImageSelected(Uri uri) {
        super.onImageSelected(uri);
        presenter.onPetImageSelected(uri, photoList, photosAdapter);
        photosRecyclerView.scrollToPosition(0);
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

    @OnClick(R.id.station_name_textView)
    void changeStationName() {
        String name = stationNameTextView.getText().toString();

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
        pickPhotoWithPermission(getString(R.string.select_cat_photo));
    }


    private class HeaderPhotosViewPagerAdapter extends PagerAdapter {
        Context context;

        public HeaderPhotosViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return headerPhotos.length;
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
            imageView.setImageDrawable(getResources().getDrawable(headerPhotos[position]));
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
}
