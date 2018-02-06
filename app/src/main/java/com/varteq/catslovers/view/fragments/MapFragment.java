package com.varteq.catslovers.view.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.Business;
import com.varteq.catslovers.model.Event;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.utils.ImageUtils;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.SystemPermissionHelper;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.view.BusinessActivity;
import com.varteq.catslovers.view.FeedstationActivity;
import com.varteq.catslovers.view.MainActivity;
import com.varteq.catslovers.view.adapters.info_window_adapter.BusinessInfoWindowAdapter;
import com.varteq.catslovers.view.adapters.info_window_adapter.EventInfoWindowAdapter;
import com.varteq.catslovers.view.presenter.MapPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.varteq.catslovers.utils.SystemPermissionHelper.PERMISSIONS_ACCESS_LOCATION_REQUEST;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final int BUSINESS_PERMISSIONS_REQUEST_CALL_PHONE = 10;
    private final String TAG = MapFragment.class.getSimpleName();
    private final int DEFAULT_ZOOM = 5;
    private GoogleMap googleMap;

    private final String MY_LOCATION_BUTTON_TAG = "GoogleMapMyLocationButton";

    final private float markerPositionX = 0.5f; // Anchors the marker on center vertical
    final private float markerPositionY = 1.0f; // Anchors the marker on the bottom

    @BindView(R.id.seekBar)
    SeekBar seekBar;
    final private int SEEKBAR_STEPS_COUNT = 7;
    final private float SEEKBAR_MAX_VALUE = 21.0f;

    @BindView(R.id.avatar_imageView)
    RoundedImageView avatarImageView;
    @BindView(R.id.bottom_sheet_feedstation)
    FrameLayout bottomSheetFeedstationFrameLayout;
    @BindView(R.id.bottom_sheet_business)
    FrameLayout bottomSheetBusinessFrameLayout;
    @BindView(R.id.station_name_textView)
    TextView stationNameTextView;
    @BindView(R.id.address_textView)
    TextView addressTextView;

    @BindView(R.id.bottom_sheet_warnings)
    FrameLayout bottomSheetEventsWarningsFrameLayout;
    @BindView(R.id.radioGroup_warnings)
    RadioGroup warningsRadioGroup;

    @BindView(R.id.bottom_sheet_emergencies)
    FrameLayout bottomSheetEventsEmergenciesFrameLayout;
    @BindView(R.id.radioGroup_emergencies)
    RadioGroup emergenciesRadioGroup;
    @BindView(R.id.imageView_avatar_catBackground)
    ImageView avatarCatBackgroundImageView;
    @BindView(R.id.relativeLayout_hungry)
    RelativeLayout hungryRelativeLayout;

    @BindView(R.id.textView_business_name)
    TextView businessNameTextView;
    @BindView(R.id.textView_business_address)
    TextView businessAddressTextView;
    @BindView(R.id.textView_business_webLink)
    TextView businessWeblinkTextView;
    @BindView(R.id.textView_business_phone)
    TextView businessPhoneTextView;

    BottomSheetBehavior bottomSheetBehaviorFeedstation;
    BottomSheetBehavior bottomSheetBehaviorBusiness;
    BottomSheetBehavior bottomSheetBehaviorEventsWarnings;
    BottomSheetBehavior bottomSheetBehaviorEventsEmergencies;
    @BindView(R.id.bottom_sheet_other_view)
    View bottomSheetOtherView;
    Button followButton;
    TextView dialogTextView;

    MapPresenter presenter;
    private MarkerOptions userLocationMarkerOptions;
    private boolean listUpdated;
    private LatLng userLocation;
    private LatLng selectedLocation;

    private SystemPermissionHelper permissionHelper;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private Marker userLocationMarker;
    @BindView(R.id.new_action_ConstraintLayout)
    ConstraintLayout newActionLayout;

    private Marker clickedMarker;
    private LatLng clickedMarkerLocation;
    private Marker newActionMarker;
    private MarkerOptions newActionMarkerOptions;
    private Business clickedBusiness;
    private EventInfoWindowAdapter eventInfoWindowAdapter;
    private BusinessInfoWindowAdapter businessInfoWindowAdapter;
    private Point clickedCoordinate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new MapPresenter(this);

        permissionHelper = new SystemPermissionHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        applyCustomMyLocationButton(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        followButton = bottomSheetFeedstationFrameLayout.findViewById(R.id.follow_button);
        dialogTextView = bottomSheetFeedstationFrameLayout.findViewById(R.id.dialogTextView);
        setFeedstationBottomSheetDimensions();

        initBottomBehaviors();
        initLocation();
        initListeners();

        businessPhoneTextView.setPaintFlags(businessPhoneTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        businessWeblinkTextView.setPaintFlags(businessWeblinkTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Obtain the SupportMapFragment and get notified when the googleMap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Glide.with(this)
                .load(getResources().getDrawable(R.drawable.cat2))
                .into(avatarImageView);

        ImageView backgroundImageView = newActionLayout.findViewById(R.id.dialog_options_background);
        Button openFeedStationButton = newActionLayout.findViewById(R.id.button_open_feedstation);
        Button warningsButton = newActionLayout.findViewById(R.id.button_warnings);
        Button emergenciesButton = newActionLayout.findViewById(R.id.button_emergencies);

        openFeedStationButton.setOnClickListener(v -> {
            FeedstationActivity.startInCreateMode(getActivity(), selectedLocation);
            cancelNewActionDialog();
        });
        warningsButton.setOnClickListener(v -> {
            showCreateWarningsEventBottomSheet();
            cancelNewActionDialog();
        });
        emergenciesButton.setOnClickListener(v -> {
            showCreateEmergenciesEventBottomSheet();
            cancelNewActionDialog();
        });
        backgroundImageView.setOnClickListener(v -> cancelNewActionDialog());
    }

    private void zoomMapForLevel(float zoomLvl) {
        if (userLocation != null && googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLvl));
        }
    }

    public float getZoomMapForRadius(double radiusKm) {
        if (userLocation != null && googleMap != null) {
            double radiusM = radiusKm * 1000;
            double zoomLevel;

            Circle circle = googleMap.addCircle(new CircleOptions()
                    .center(userLocation)
                    .radius(radiusM)
                    .strokeWidth(0));

            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel = (16 - Math.log(scale) / Math.log(2));
            zoomLevel = zoomLevel + 0.5f;
            return (float) zoomLevel;
        }
        return 0;
    }

    private void initListeners() {
        bottomSheetBehaviorFeedstation.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        goToFeedStationActivity((Feedstation) bottomSheetFeedstationFrameLayout.getTag());
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        bottomSheetFeedstationFrameLayout.setOnClickListener(view1 ->
                goToFeedStationActivity((Feedstation) bottomSheetFeedstationFrameLayout.getTag()));
        followButton.setOnClickListener(view12 -> presenter.onGroupActionButtonClicked((Feedstation) bottomSheetFeedstationFrameLayout.getTag()));
        seekBar.setMax(SEEKBAR_STEPS_COUNT - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float zoomLvl = ((SEEKBAR_MAX_VALUE / SEEKBAR_STEPS_COUNT) * (progress + 1));
                    zoomMapForLevel(zoomLvl);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        warningsRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.radioButton_warnings_newborn_kittens:
                    presenter.onCreateEventChoosed(MapPresenter.EVENT_TYPE_WARNING_NEWBORN_KITTENS, selectedLocation.latitude, selectedLocation.longitude);
                    hideBottomSheets();
                    break;
                case R.id.radioButton_warnings_municipality_inspector:
                    presenter.onCreateEventChoosed(MapPresenter.EVENT_TYPE_WARNING_MUNICIPALITY_INSPECTOR, selectedLocation.latitude, selectedLocation.longitude);
                    hideBottomSheets();
                    break;
                case R.id.radioButton_warnings_cat_in_heat:
                    presenter.onCreateEventChoosed(MapPresenter.EVENT_TYPE_WARNING_CAT_IN_HEAT, selectedLocation.latitude, selectedLocation.longitude);
                    hideBottomSheets();
                    break;
                case R.id.radioButton_warnings_stray_cat:
                    presenter.onCreateEventChoosed(MapPresenter.EVENT_TYPE_WARNING_STRAY_CAT, selectedLocation.latitude, selectedLocation.longitude);
                    hideBottomSheets();
                    break;
            }
        });
        emergenciesRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.radioButton_emergencies_poison:
                    presenter.onCreateEventChoosed(MapPresenter.EVENT_TYPE_EMERGENCY_POISON, selectedLocation.latitude, selectedLocation.longitude);
                    hideBottomSheets();
                    break;
                case R.id.radioButton_emergencies_missing_cat:
                    presenter.onCreateEventChoosed(MapPresenter.EVENT_TYPE_EMERGENCY_MISSING_CAT, selectedLocation.latitude, selectedLocation.longitude);
                    hideBottomSheets();
                    break;
                case R.id.radioButton_emergencies_carcass:
                    presenter.onCreateEventChoosed(MapPresenter.EVENT_TYPE_EMERGENCY_CARCASS, selectedLocation.latitude, selectedLocation.longitude);
                    hideBottomSheets();
                    break;
            }

        });
    }

    private void initBottomBehaviors() {
        bottomSheetBehaviorFeedstation = BottomSheetBehavior.from(bottomSheetFeedstationFrameLayout);
        bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorEventsWarnings = BottomSheetBehavior.from(bottomSheetEventsWarningsFrameLayout);
        bottomSheetBehaviorEventsWarnings.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorEventsEmergencies = BottomSheetBehavior.from(bottomSheetEventsEmergenciesFrameLayout);
        bottomSheetBehaviorEventsEmergencies.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorBusiness = BottomSheetBehavior.from(bottomSheetBusinessFrameLayout);
        bottomSheetBehaviorBusiness.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (bottomSheetBehaviorFeedstation.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehaviorFeedstation.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            switch (bottomSheetBehaviorFeedstation.getState()) {
                case BottomSheetBehavior.STATE_EXPANDED:
                    bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
            }
            Feedstation feedstation = (Feedstation) bottomSheetFeedstationFrameLayout.getTag();
            fillFeedstationBottomSheet(feedstation);
            initStationAction(feedstation);
            initAvatarCatBackground(feedstation);
        }

    }

    public void initAvatarCatBackground(Feedstation feedstation) {
        int resourceId = R.drawable.location_blue;
        if (feedstation.getFeedStatus() != null) {
            if (feedstation.getFeedStatus().equals(Feedstation.FeedStatus.STARVING))
                resourceId = R.drawable.location_red;
            else if (feedstation.getFeedStatus().equals(Feedstation.FeedStatus.HUNGRY))
                resourceId = R.drawable.location_orange;
        }
        avatarCatBackgroundImageView.setImageDrawable(getResources().getDrawable(resourceId));
    }

    public void initStationAction(Feedstation feedstation) {
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

    @Override
    public void onStart() {
        super.onStart();
        if (!listUpdated && googleMap != null && userLocation != null) {
            listUpdated = true;
            presenter.getFeedstations(userLocation.latitude, userLocation.longitude, 20);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        listUpdated = false;
        presenter.onViewPaused();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        eventInfoWindowAdapter = new EventInfoWindowAdapter(getContext());
        businessInfoWindowAdapter = new BusinessInfoWindowAdapter(getContext());

        setUserPosition();

        this.googleMap.setOnMapClickListener(latLng -> {
            cancelNewActionDialog();
            hideBottomSheets();
            releaseClickedLocation();
        });

        this.googleMap.setOnMarkerClickListener(marker -> {
            clickedMarker = marker;
            presenter.onMarkerClicked(marker.getTag());
            return false;
        });

        this.googleMap.setOnInfoWindowClickListener(marker -> presenter.onInfoWindowClicked(marker.getTag()));

        this.googleMap.setOnMapLongClickListener(latLng -> {
            Log.d(TAG, "OnMapLongClick " + latLng.latitude + " / " + latLng.longitude + "]");
            initNewActionDialog(latLng);
        });

        this.googleMap.setOnCameraMoveListener(() -> {
            LatLng centerCoords = googleMap.getCameraPosition().target;
            presenter.onCameraMoved(centerCoords.latitude, centerCoords.longitude);

            // Update zoom seekbar when user changes zoom with gesture
            CameraPosition cameraPosition = googleMap.getCameraPosition();
            float zoomLvl = cameraPosition.zoom;
            int seekerZoomLvl = (int) (zoomLvl * (SEEKBAR_STEPS_COUNT / SEEKBAR_MAX_VALUE)) - 1;
            seekBar.setProgress(seekerZoomLvl);

            cancelNewActionDialog();
        });

        if (!listUpdated && userLocation != null) {
            listUpdated = true;
            presenter.getFeedstations(userLocation.latitude, userLocation.longitude, 20);
        }
    }

    public void cancelNewActionDialog() {
        deleteNewActionMarker();
        newActionLayout.setVisibility(View.GONE);
    }

    private void initNewActionDialog(LatLng latLng) {
        hideBottomSheets();
        selectedLocation = latLng;

        moveCameraToSelectAction(latLng);
        addNewActionMarker(latLng);

        newActionLayout.setVisibility(View.VISIBLE);
    }

    private void addNewActionMarker(LatLng latLng) {
        deleteNewActionMarker();
        newActionMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_purple))
                .anchor(markerPositionX, markerPositionY)
                .position(latLng);
        newActionMarker = googleMap.addMarker(newActionMarkerOptions);
    }

    /*private void moveDialogToMapPosition(Dialog dialog, LatLng latLng) {
        moveCameraToSelectAction(latLng);
        Point screenPosition = getScreenLocationByLatLng(latLng);

        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        wmlp.y = screenPosition.y + Utils.convertDpToPx(70, getContext());

        //Projection projection = googleMap.getProjection();

        int screenWidth = Utils.getScreenWidthPx(getActivity());
        int departure = (screenWidth / 2) - screenPosition.x;
        wmlp.x = wmlp.x - departure;
    }*/

    private Point getScreenLocationByLatLng(LatLng latLng) {
        Projection projection = googleMap.getProjection();
        return projection.toScreenLocation(latLng);
    }

    private LatLng getLatLngByScreenPosition(Point screenPosition) {
        screenPosition.y -= seekBar.getHeight();
        android.support.v7.widget.Toolbar toolbar = ((MainActivity) getActivity()).getToolbar();
        if (toolbar != null)
            screenPosition.y -= toolbar.getHeight();
        Projection projection = googleMap.getProjection();
        return projection.fromScreenLocation(screenPosition);
    }

    private void deleteNewActionMarker() {
        newActionMarkerOptions = null;
        if (newActionMarker != null)
            newActionMarker.remove();
    }

    public void showEventMarkerDialog(Event event) {
        if (event != null) {
            clickedMarkerLocation = event.getLatLng();
            showEventMarkerInfoWindow(event.getAddress(), TimeUtils.getDateAsddMMMyyyy(event.getDate()), event.getTypeName(), event.getType());
        }
    }

    public void showBusinessMarkerDialog(Business business) {
        if (business != null) {
            clickedMarkerLocation = business.getLocation();
            showBusinessMarkerInfoWindow(business.getName(), business.getAddress(), business.getDescription());
        }
    }

    private void showEventMarkerInfoWindow(String address, String date, String eventTypeName, Event.Type type) {
        if (clickedMarker != null) {
            googleMap.setInfoWindowAdapter(eventInfoWindowAdapter);
            eventInfoWindowAdapter.setShowWindow(true);
            eventInfoWindowAdapter.setValues(address, date, eventTypeName, type);
            clickedMarker.showInfoWindow();
        }
    }

    private void moveCameraToSelectAction(LatLng latLng) {
        LatLng selectedLocation = new LatLng(googleMap.getCameraPosition().target.latitude, latLng.longitude);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLocation));
    }

    private void showBusinessMarkerInfoWindow(String name, String address, String description) {
        if (clickedMarker != null) {
            googleMap.setInfoWindowAdapter(businessInfoWindowAdapter);
            businessInfoWindowAdapter.setShowWindow(true);
            businessInfoWindowAdapter.setValues(name, address, description);
            clickedMarker.showInfoWindow();
        }
    }

    public void releaseClickedLocation() {
        clickedMarkerLocation = null;
    }

    public void hideMarkerDialogs() {
        eventInfoWindowAdapter.setShowWindow(false);
        businessInfoWindowAdapter.setShowWindow(false);
        clickedMarker.showInfoWindow();
    }

    private void showCreateWarningsEventBottomSheet() {
        hideBottomSheets();
        warningsRadioGroup.clearCheck();
        bottomSheetBehaviorEventsWarnings.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void showCreateEmergenciesEventBottomSheet() {
        hideBottomSheets();
        emergenciesRadioGroup.clearCheck();
        bottomSheetBehaviorEventsEmergencies.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void showFeedstationMarkerBottomSheet(Feedstation feedstation) {
        bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_COLLAPSED);
        fillFeedstationBottomSheet(feedstation);
    }

    public void fillFeedstationBottomSheet(Feedstation feedstation) {
        stationNameTextView.setText(feedstation.getName());
        addressTextView.setText(feedstation.getAddress());
        if (feedstation.getFeedStatus() != null && feedstation.getFeedStatus().equals(Feedstation.FeedStatus.STARVING)) {
            hungryRelativeLayout.setVisibility(View.VISIBLE);
        } else
            hungryRelativeLayout.setVisibility(View.INVISIBLE);
    }

    public void showBusinessMarkerBottomSheet(Business business) {
        bottomSheetBehaviorBusiness.setState(BottomSheetBehavior.STATE_COLLAPSED);
        fillBusinessBottomSheet(business);
    }


    public void fillBusinessBottomSheet(Business business) {
        this.clickedBusiness = business;
        businessNameTextView.setText(business.getName());
        businessAddressTextView.setText(business.getAddress());
        businessWeblinkTextView.setText(business.getLink());
        businessPhoneTextView.setText(business.getPhone());
    }

    public void setBottomSheetFeedstationTag(Object tag) {
        bottomSheetFeedstationFrameLayout.setTag(tag);
    }

    public void hideBottomSheets() {
        bottomSheetBehaviorBusiness.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorEventsEmergencies.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorEventsWarnings.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_HIDDEN);
        emergenciesRadioGroup.clearCheck();
        warningsRadioGroup.clearCheck();
    }

    public boolean isFeedstationBottomSheetShowed() {
        return bottomSheetBehaviorFeedstation.getState() == BottomSheetBehavior.STATE_COLLAPSED
                || bottomSheetBehaviorFeedstation.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void setStationActionName(String name) {
        dialogTextView.setText(name);
    }


    private void setUserPosition() {
        Location location = Profile.getLocation(getContext());
        if (location == null) {
            return;
            /*location = new Location("fused");
            location.setLatitude(51.5016225);
            location.setLongitude(31.2924888);
            Profile.setLocation(getContext(), location);*/
        }

        userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        userLocationMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_place_24dp))) // insert image from request
                //.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_star))) // insert image from request
                .anchor(markerPositionX, markerPositionY)
                .position(userLocation);
        //addUserLocationMarker();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userLocation)      // Sets the center of the map to Mountain View
                .zoom(getZoomMapForRadius(DEFAULT_ZOOM))                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //method to integrate photo to marker
    private Bitmap getMarkerBitmapFromView(int resId) {
        android.view.LayoutInflater inflater = getLayoutInflater();
        View customMarkerView = inflater.inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setBackgroundResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private Bitmap resizeMarkerIcon(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        float scale = 0.6f;
        int inWidth = (int) (bitmap.getWidth() * scale);
        int inHeight = (int) (bitmap.getHeight() * scale);
        return Bitmap.createScaledBitmap(bitmap, inWidth, inHeight, false);
    }

    private void setFeedstationBottomSheetDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int toolbarHeight = Utils.convertDpToPx(56, getContext());
        int navigationBarHeight = Utils.convertDpToPx(56, getContext());
        int feedStationHeaderHeight = Utils.convertDpToPx(202, getContext());
        int bottomSheetHeight = Utils.convertDpToPx(190, getContext());
        int bottomSheetOtherViewHeight = screenHeight - (toolbarHeight + feedStationHeaderHeight + bottomSheetHeight + navigationBarHeight);
        ViewGroup.LayoutParams layoutParams = bottomSheetOtherView.getLayoutParams();
        layoutParams.height = bottomSheetOtherViewHeight;
        bottomSheetOtherView.setLayoutParams(layoutParams);
    }

    private void goToFeedStationActivity(Feedstation station) {
        FeedstationActivity.startInViewMode(getActivity(), station);
    }

    private int getFeedstationMarkerId(Feedstation feedstation) {
        if (feedstation.getUserRole() != null) {
            if (feedstation.getIsPublic()) {
                if (feedstation.getUserRole().equals(Feedstation.UserRole.ADMIN)) {
                    if (feedstation.getFeedStatus() != null) {
                        if (feedstation.getFeedStatus().equals(Feedstation.FeedStatus.HUNGRY))
                            return R.drawable.foodstation_copy_8;
                        else if (feedstation.getFeedStatus().equals(Feedstation.FeedStatus.STARVING))
                            return R.drawable.station_red_star_hollow;
                    }
                    return R.drawable.foodstation_copy_2;
                } else if (feedstation.getStatus() != null && feedstation.getStatus().equals(GroupPartner.Status.JOINED)) {
                    if (feedstation.getFeedStatus() != null) {
                        if (feedstation.getFeedStatus().equals(Feedstation.FeedStatus.HUNGRY))
                            return R.drawable.foodstation_copy_14;
                        else if (feedstation.getFeedStatus().equals(Feedstation.FeedStatus.STARVING))
                            return R.drawable.foodstation_copy_15;
                    }
                    return R.drawable.foodstation_copy_4;
                }
            } else if (feedstation.getUserRole().equals(Feedstation.UserRole.ADMIN)) {
                return R.drawable.foodstation_copy_3;
            } else if (feedstation.getStatus() != null && feedstation.getStatus().equals(GroupPartner.Status.JOINED)) {
                return R.drawable.foodstation_copy_4;
            }
        }
        if (feedstation.getFeedStatus() != null) {
            if (feedstation.getFeedStatus().equals(Feedstation.FeedStatus.HUNGRY))
                return R.drawable.location_icon_2;
            else if (feedstation.getFeedStatus().equals(Feedstation.FeedStatus.STARVING))
                return R.drawable.foodstation_copy_9;
        }
        return R.drawable.location_icon;
    }

    public void feedstationsLoaded(List<Feedstation> stations, List<Event> events, List<Business> businesses) {
        listUpdated = true;
        googleMap.clear();

        restoreNewActionMarker();

        if (stations != null)
            drawFeedstationsMarkers(stations);
        if (events != null)
            drawEventsMarkers(events);
        if (businesses != null)
            drawBusinessMarkers(businesses);

        //addUserLocationMarker();
    }

    private void drawFeedstationsMarkers(List<Feedstation> stations) {
        for (Feedstation feedstation : stations) {
            LatLng feedstationLocation = feedstation.getLocation();
            if (feedstationLocation == null)
                continue;
            improveUserMarkerPosition(feedstationLocation);

            int resourceId = getFeedstationMarkerId(feedstation);
            if (!isAdded()) return;
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .title(feedstation.getName())
                    .icon(BitmapDescriptorFactory.fromResource(resourceId)) // insert image from request
                    //.icon(BitmapDescriptorFactory.fromBitmap(resizeMarkerIcon(resourceId))) // insert image from request
                    //.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_star))) // insert image from request
                    .anchor(markerPositionX, markerPositionY)
                    .position(feedstation.getLocation()));
            marker.setTag(feedstation);

            restoreClickedFeedstationMarker(marker, feedstation);
        }
    }

    private void drawEventsMarkers(List<Event> events) {
        for (Event event : events) {
            if (event.getLatLng() == null) continue;
            int resourceId = R.drawable.event_orange;
            if (event.getType().equals(Event.Type.EMERGENCY))
                resourceId = R.drawable.event_red;

            if (!isAdded()) return;

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .title(event.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), resourceId)))
                    .anchor(markerPositionX, markerPositionY)
                    .position(event.getLatLng())
            );
            marker.setTag(event);
            restoreClickedEventMarker(marker, event);
        }
    }

    private void drawBusinessMarkers(List<Business> businesses) {
        for (Business business : businesses) {
            if (business.getLocation() == null) continue;
            int resourceId = R.drawable.food_business;
            if (business.getCategory() != null && business.getCategory().equals(Business.Category.VETERINARY))
                resourceId = R.drawable.veterinary_business;

            if (!isAdded()) return;

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .title(business.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), resourceId)))
                    .anchor(markerPositionX, markerPositionY)
                    .position(business.getLocation())
            );
            marker.setTag(business);
            if (clickedMarkerLocation != null
                    && business.getLocation().latitude == clickedMarkerLocation.latitude
                    && business.getLocation().longitude == clickedMarkerLocation.longitude) {
                clickedMarker = marker;
                showBusinessMarkerDialog(business);
            }
        }
    }

    private void improveUserMarkerPosition(LatLng feedstationLocation) {
        if (userLocation != null && isMarkersBeside(feedstationLocation, userLocation)) {
            LatLng newUserMarkerLocation = new LatLng(feedstationLocation.latitude + 0.0002, feedstationLocation.longitude);
            userLocationMarkerOptions.position(newUserMarkerLocation);
            //addUserLocationMarker();
        }
    }


    private void restoreClickedFeedstationMarker(Marker marker, Feedstation feedstation) {
        if (bottomSheetBehaviorFeedstation.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            if (feedstation.getId().equals(((Feedstation) bottomSheetFeedstationFrameLayout.getTag()).getId())) {
                bottomSheetFeedstationFrameLayout.setTag(marker.getTag());
                initStationAction(feedstation);
                fillFeedstationBottomSheet(feedstation);
                initAvatarCatBackground(feedstation);
            }
        }
    }

    private void restoreClickedEventMarker(Marker marker, Event event) {
        if (clickedMarkerLocation != null
                && event.getLatLng().latitude == clickedMarkerLocation.latitude
                && event.getLatLng().longitude == clickedMarkerLocation.longitude) {
            clickedMarker = marker;
            showEventMarkerDialog(event);
        }
    }

    private void restoreNewActionMarker() {
        if (newActionMarkerOptions != null) {
            newActionMarker = googleMap.addMarker(newActionMarkerOptions);
        }
    }

    private boolean isMarkersBeside(LatLng firstMarkerLocation, LatLng secondMarkerLocation) {
        double firstRoundedLat = Utils.roundDouble(firstMarkerLocation.latitude, 4);
        double firstRoundedLng = Utils.roundDouble(firstMarkerLocation.longitude, 4);
        double secondRoundedLat = Utils.roundDouble(secondMarkerLocation.latitude, 4);
        double secondRoundedLng = Utils.roundDouble(secondMarkerLocation.longitude, 4);
        return firstRoundedLat == secondRoundedLat && firstRoundedLng == secondRoundedLng;
    }

    private void addUserLocationMarker() {
        if (googleMap != null && userLocationMarkerOptions != null) {
            if (userLocationMarker != null && userLocationMarker.isVisible())
                userLocationMarker.remove();
            userLocationMarker = googleMap.addMarker(userLocationMarkerOptions);
        } else setUserPosition();
    }

    // Request current location
    private void initLocation() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(30 * 1000)
                .setFastestInterval(20 * 1000);
        checkLocationAvailability();
    }

    @SuppressLint("MissingPermission")
    public void checkLocationAvailability() {
        if (!permissionHelper.isAccessLocationGranted()) {
            permissionHelper.requestPermissionsAccessLocation();
            return;
        }

        if (getActivity() == null) return;
        permissionHelper.checkLocationSettings(getActivity(), locationRequest, locationSettingsResponse -> {
            getLastLocation();
            enableMyLocation();
        });
    }

    @SuppressLint("MissingPermission")
    public void enableMyLocation() {
        if (googleMap == null) return;
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        if (mFusedLocationClient == null && getActivity() != null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (getActivity() == null) return;
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        setUserPosition(getActivity(), location);
                    } else getLocationUpdates();
                });
    }

    private void setUserPosition(Activity activity, Location lastLocation) {
        if (lastLocation != null && activity != null) {
            Profile.setLocation(activity, lastLocation);
            setUserPosition();
            if (!listUpdated && userLocation != null) {
                listUpdated = true;
                presenter.getFeedstations(userLocation.latitude, userLocation.longitude, 20);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void getLocationUpdates() {
        if (locationCallback != null) return;
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    setUserPosition(getActivity(), locationResult.getLastLocation());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_LOCATION_REQUEST:
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationAvailability();
                }
                break;
            case BUSINESS_PERMISSIONS_REQUEST_CALL_PHONE:
                if (permissions[0].equals(Manifest.permission.CALL_PHONE)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPhone(businessPhoneTextView.getText().toString());
                }
                break;
        }
    }

    @OnClick(R.id.textView_business_webLink)
    public void onBusinessWeblinkClick(View view) {
        String url = ((TextView) view).getText().toString();
        if (!url.isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://" + url;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    @OnClick(R.id.button_business_more)
    public void onMoreButtonClicked() {
        Intent intent = new Intent(getActivity(), BusinessActivity.class);
        intent.putExtra("business", this.clickedBusiness);
        startActivity(intent);
    }

    @OnClick(R.id.textView_business_phone)
    public void onBusinessPhoneClick(View view) {
        String phone = ((TextView) view).getText().toString();
        if (!phone.isEmpty()) {

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, BUSINESS_PERMISSIONS_REQUEST_CALL_PHONE);
            } else {
                callPhone(phone);
            }
        }
    }


    private void callPhone(String phone) {
        if (phone != null && !phone.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            startActivity(intent);
        }
    }

    public void onLocationAvailable() {
        getLastLocation();
    }

    public void onSuccessFollow() {
        Toaster.shortToast("Follow request sent");
        setStationActionName(getString(R.string.group_join_request_sent));
        followButton.setVisibility(View.GONE);
        ((Feedstation) bottomSheetFeedstationFrameLayout.getTag()).setStatus(GroupPartner.Status.REQUESTED);
    }

    public void onSuccessLeave() {
        Toaster.shortToast("You have been leaved the group");
        setStationActionName(getString(R.string.join_group));
        followButton.setVisibility(View.VISIBLE);
        ((Feedstation) bottomSheetFeedstationFrameLayout.getTag()).setStatus(null);
        initAvatarCatBackground((Feedstation) bottomSheetFeedstationFrameLayout.getTag());
    }

    public void onSuccessJoin() {
        Toaster.shortToast("You have successfully joined");
    }

    private void applyCustomMyLocationButton(View mapView) {

        ImageView locationButton = mapView.findViewWithTag(MY_LOCATION_BUTTON_TAG);

        // Set custom icon
        locationButton.setImageDrawable(getResources().getDrawable(R.drawable.my_location_button));

        // Get device screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceScreenHeight = displayMetrics.heightPixels;
        int deviceScreenWidth = displayMetrics.widthPixels;

        // Set location button size
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        layoutParams.width = (int) Math.round(deviceScreenWidth * 0.1);
        layoutParams.height = (int) Math.round(deviceScreenWidth * 0.1);

        // Position location button on bottom right
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        locationButton.setLayoutParams(layoutParams);

        // Set margins
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) locationButton.getLayoutParams();
        marginLayoutParams.rightMargin = (int) Math.round(deviceScreenWidth * 0.03);
        marginLayoutParams.bottomMargin = (int) Math.round(deviceScreenHeight * 0.04);
        locationButton.setLayoutParams(marginLayoutParams);

        locationButton.requestLayout();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelNewActionDialog();
    }
}
