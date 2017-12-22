package com.varteq.catslovers.view.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.utils.ImageUtils;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.SystemPermissionHelper;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.view.FeedstationActivity;
import com.varteq.catslovers.view.presenter.MapPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
    BottomSheetBehavior bottomSheetBehaviorFeedstation;
    @BindView(R.id.bottom_sheet_other_view)
    View bottomSheetOtherView;

    MapPresenter presenter;
    private MarkerOptions userLocationMarkerOptions;
    private boolean listUpdated;
    private LatLng userLocation;

    private SystemPermissionHelper permissionHelper;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private LocationCallback locationCallback;

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
        bottomSheetBehaviorFeedstation = BottomSheetBehavior.from(bottomSheetFeedstationFrameLayout);
        bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_HIDDEN);
        setBottomSheetDimensions();

        initLocation();

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
        // Obtain the SupportMapFragment and get notified when the googleMap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        Glide.with(this)
                .load(getResources().getDrawable(R.drawable.cat2))
                .into(avatarImageView);
    }

    public void zoomMapForRadius(int radiusKm) {
        if (userLocation != null) {
            double radiusM = radiusKm * 1000;
            double zoomLevel;

            Circle circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(userLocation.latitude, userLocation.longitude))
                    .radius(radiusM)
                    .strokeWidth(0));

            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel = (16 - Math.log(scale) / Math.log(2));
            zoomLevel = zoomLevel + 0.5f;
            googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomLevel), null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (bottomSheetBehaviorFeedstation.getState()) {
            case BottomSheetBehavior.STATE_EXPANDED:
                bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
        }
        if (!listUpdated && googleMap != null && userLocation != null)
            presenter.getFeedstations(userLocation.latitude, userLocation.longitude, 20);
    }

    @Override
    public void onPause() {
        super.onPause();
        listUpdated = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        setUserPosition();

        this.googleMap.setOnMapClickListener(latLng -> bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_HIDDEN));

        this.googleMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() == null) return false;
            bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_COLLAPSED);
            stationNameTextView.setText(((Feedstation) marker.getTag()).getName());
            addressTextView.setText(((Feedstation) marker.getTag()).getAddress());
            bottomSheetFeedstationFrameLayout.setTag(marker.getTag());
            return false;
        });

        googleMap.setOnMapLongClickListener(latLng -> {
            /*Location selectedLocation = new Location("");
            selectedLocation.setLatitude(latLng.latitude);
            selectedLocation.setLongitude(latLng.longitude);*/
            FeedstationActivity.startInCreateMode(getActivity(), latLng);
            Log.d(TAG, "OnMapLongClick " + latLng.latitude + " / " + latLng.longitude + "]");
        });

        if (!listUpdated && userLocation != null)
            presenter.getFeedstations(userLocation.latitude, userLocation.longitude, 20);
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

        // Add a marker in Sydney and move the camera
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng sydney = new LatLng(-33.866915, 151.204631);
        LatLng sydney1 = new LatLng(-33.967, 151.996);

        userLocationMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_place_24dp))) // insert image from request
                //.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_star))) // insert image from request
                .anchor(markerPositionX, markerPositionY)
                .position(userLocation);
        addUserLocationMarker();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userLocation)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //method to integrate photo to marker
    private Bitmap getMarkerBitmapFromView(int resId) {
        android.view.LayoutInflater inflater = getLayoutInflater();
        View customMarkerView = inflater.inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
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

    private void setBottomSheetDimensions() {
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

    public void feedstationsLoaded(List<Feedstation> stations) {
        listUpdated = true;
        googleMap.clear();
        for (Feedstation feedstation : stations) {
            if (!feedstation.getIsPublic()) continue;
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .title(feedstation.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_blue)) // insert image from request
                    //.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_star))) // insert image from request
                    .anchor(markerPositionX, markerPositionY)
                    .position(feedstation.getLocation()));
            marker.setTag(feedstation);
        }
        addUserLocationMarker();
    }

    private void addUserLocationMarker() {
        if (googleMap != null && userLocationMarkerOptions != null)
            googleMap.addMarker(userLocationMarkerOptions);
        else setUserPosition();
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
                        lastLocation = location;
                        setUserPosition(getActivity(), lastLocation, DEFAULT_ZOOM);
                    } else getLocationUpdates();
                });
    }

    private void setUserPosition(Activity activity, Location lastLocation, int radius) {
        if (lastLocation != null) {
            Profile.setLocation(activity, lastLocation);
            setUserPosition();
            zoomMapForRadius(radius);
            userLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
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
                    lastLocation = locationResult.getLastLocation();
                    setUserPosition(getActivity(), lastLocation, DEFAULT_ZOOM);
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

}
