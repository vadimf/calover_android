package com.varteq.catslovers.view.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.varteq.catslovers.view.FeedstationActivity;
import com.varteq.catslovers.view.adapters.info_window_adapter.EventInfoWindowAdapter;
import com.varteq.catslovers.view.presenter.MapPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.varteq.catslovers.utils.SystemPermissionHelper.PERMISSIONS_ACCESS_LOCATION_REQUEST;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = MapFragment.class.getSimpleName();
    private final int DEFAULT_ZOOM = 5;
    private GoogleMap googleMap;

    final private float markerPositionX = 0.5f; // Anchors the marker on center vertical
    final private float markerPositionY = 1.0f; // Anchors the marker on the bottom

    @BindView(R.id.seekBar)
    SeekBar seekBar;
    final private int SEEKBAR_STEPS_COUNT = 4;

    @BindView(R.id.avatar_imageView)
    RoundedImageView avatarImageView;
    @BindView(R.id.bottom_sheet_feedstation)
    FrameLayout bottomSheetFeedstationFrameLayout;
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

    BottomSheetBehavior bottomSheetBehaviorFeedstation;
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

    private Marker clickedMarker;
    private LatLng clickedMarkerLocation;
    private EventInfoWindowAdapter eventInfoWindowAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new MapPresenter(this);

        permissionHelper = new SystemPermissionHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
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

        // Obtain the SupportMapFragment and get notified when the googleMap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Glide.with(this)
                .load(getResources().getDrawable(R.drawable.cat2))
                .into(avatarImageView);
    }

    public void zoomMapForRadius(int radiusKm) {
        if (userLocation != null && googleMap != null)
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(getZoomMapForRadius(radiusKm)), null);
    }

    public float getZoomMapForRadius(int radiusKm) {
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
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int radius = (i + 1) * (SEEKBAR_STEPS_COUNT + 1);
                zoomMapForRadius(radius);
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
        if (feedstation.getFeedStatus()!=null) {
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
        googleMap.setInfoWindowAdapter(eventInfoWindowAdapter);

        setUserPosition();

        this.googleMap.setOnMapClickListener(latLng -> {
            hideBottomSheets();
            releaseClickedLocation();
        });

        this.googleMap.setOnMarkerClickListener(marker -> {
            clickedMarker = marker;
            presenter.onMarkerClicked(marker.getTag());
            return false;
        });

        googleMap.setOnMapLongClickListener(latLng -> {
            /*Location selectedLocation = new Location("");
            selectedLocation.setLatitude(latLng.latitude);
            selectedLocation.setLongitude(latLng.longitude);*/
            final Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            RelativeLayout mapOptionsDialogLayout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.dialog_map_options, null);
            Button openFeedStationButton = mapOptionsDialogLayout.findViewById(R.id.button_open_feedstation);
            Button warningsButton = mapOptionsDialogLayout.findViewById(R.id.button_warnings);
            Button emergenciesButton = mapOptionsDialogLayout.findViewById(R.id.button_emergencies);

            openFeedStationButton.setOnClickListener(view -> {
                FeedstationActivity.startInCreateMode(getActivity(), latLng);
                dialog.dismiss();
            });
            selectedLocation = latLng;
            warningsButton.setOnClickListener(view -> {
                showCreateWarningsEventBottomSheet();
                dialog.dismiss();
            });
            emergenciesButton.setOnClickListener(view -> {
                showCreateEmergenciesEventBottomSheet();
                dialog.dismiss();
            });


            dialog.setContentView(mapOptionsDialogLayout);
            dialog.show();
            Log.d(TAG, "OnMapLongClick " + latLng.latitude + " / " + latLng.longitude + "]");
        });

        this.googleMap.setOnCameraMoveListener(() -> {
            LatLng centerCoords = googleMap.getCameraPosition().target;
            presenter.onCameraMoved(centerCoords.latitude, centerCoords.longitude);
        });

        if (!listUpdated && userLocation != null) {
            listUpdated = true;
            presenter.getFeedstations(userLocation.latitude, userLocation.longitude, 20);
        }
    }

    public void showEventMarkerDialog(Event event) {
        clickedMarkerLocation = event.getLatLng();
        if (event != null)
            showMarkerInfoWindow(event.getAddress(), TimeUtils.getDateAsddMMMyyyy(event.getDate()), event.getTypeName(), event.getType());
    }

    private void showMarkerInfoWindow(String address, String date, String eventTypeName, Event.Type type) {
        if (clickedMarker != null) {
            eventInfoWindowAdapter.setShowWindow(true);
            eventInfoWindowAdapter.setValues(address, date, eventTypeName, type);
            clickedMarker.showInfoWindow();
        }
    }

    public void releaseClickedLocation(){
        clickedMarkerLocation = null;
    }

    public void hideEventMarkerDialog() {
        eventInfoWindowAdapter.setShowWindow(false);
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
        if (feedstation.getFeedStatus()!=null && feedstation.getFeedStatus().equals(Feedstation.FeedStatus.STARVING)) {
            hungryRelativeLayout.setVisibility(View.VISIBLE);
        } else
            hungryRelativeLayout.setVisibility(View.INVISIBLE);
    }

    public void setBottomSheetFeedstationTag(Object tag) {
        bottomSheetFeedstationFrameLayout.setTag(tag);
    }

    public void hideBottomSheets() {
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
            location.setLatitude(50.4437);
            location.setLongitude(30.5008);
            Profile.setLocation(getContext(), location);*/
        }

        userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        userLocationMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_place_24dp))) // insert image from request
                //.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_star))) // insert image from request
                .anchor(markerPositionX, markerPositionY)
                .position(userLocation);
        addUserLocationMarker();

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
        if (stations != null)
            for (Feedstation feedstation : stations) {
                if (feedstation.getLocation() == null) continue;
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

                if (bottomSheetBehaviorFeedstation.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (feedstation.getId().equals(((Feedstation) bottomSheetFeedstationFrameLayout.getTag()).getId())) {
                        bottomSheetFeedstationFrameLayout.setTag(marker.getTag());
                        initStationAction(feedstation);
                        fillFeedstationBottomSheet(feedstation);
                        initAvatarCatBackground(feedstation);
                    }
                }
            }

        if (events != null)
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
                if (clickedMarkerLocation != null
                        && event.getLatLng().latitude == clickedMarkerLocation.latitude
                        && event.getLatLng().longitude == clickedMarkerLocation.longitude) {
                    clickedMarker = marker;
                    showEventMarkerDialog(event);
                }
            }

        if (businesses != null)
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
            }

        addUserLocationMarker();
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
        permissionHelper.checkLocationSettings(getActivity(), locationRequest, locationSettingsResponse -> {
            getLastLocation();
        });
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        if (mFusedLocationClient == null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        setUserPosition(getActivity(), location);
                    } else getLocationUpdates();
                });
    }

    private void setUserPosition(Activity activity, Location lastLocation) {
        if (lastLocation != null) {
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
        if (requestCode == PERMISSIONS_ACCESS_LOCATION_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAvailability();
            }
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
}
