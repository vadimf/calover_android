package com.varteq.catslovers.view.fragments;

import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.ImageUtils;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.view.FeedstationActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = MapFragment.class.getSimpleName();
    private GoogleMap googleMap;

    final private float markerPositionX = 0.5f; // Anchors the marker on center vertical
    final private float markerPositionY = 1.0f; // Anchors the marker on the bottom

    @BindView(R.id.seekBar)
    SeekBar seekBar;
    final private int SEEKBAR_STEPS_COUNT = 7;

    @BindView(R.id.avatar_imageView)
    RoundedImageView avatarImageView;
    @BindView(R.id.bottom_sheet_feedstation)
    FrameLayout bottomSheetFeedstationFrameLayout;
    BottomSheetBehavior bottomSheetBehaviorFeedstation;
    @BindView(R.id.bottom_sheet_other_view)
    View bottomSheetOtherView;

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

        bottomSheetBehaviorFeedstation.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        goToFeedStationActivity();
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        bottomSheetFeedstationFrameLayout.setOnClickListener(view1 -> goToFeedStationActivity());
        // Obtain the SupportMapFragment and get notified when the googleMap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        seekBar.setMax(SEEKBAR_STEPS_COUNT - 1);
        Glide.with(this)
                .load(getResources().getDrawable(R.drawable.cat2))
                .into(avatarImageView);
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (bottomSheetBehaviorFeedstation.getState()) {
            case BottomSheetBehavior.STATE_EXPANDED:
                bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        Location location = Profile.getLocation(getContext());
        if (location == null) {
            location = new Location("fused");
            location.setLatitude(50.4437);
            location.setLongitude(30.5008);
            Profile.setLocation(getContext(), location);
        }

        // Add a marker in Sydney and move the camera
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng sydney = new LatLng(-33.866915, 151.204631);
        LatLng sydney1 = new LatLng(-33.967, 151.996);

        this.googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_place_24dp))) // insert image from request
                //.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_star))) // insert image from request
                .anchor(markerPositionX, markerPositionY)
                .position(userLocation));
        /*this.googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_star))) // insert image from request
                .anchor(markerPositionX, markerPositionY)
                .position(sydney1));*/

        //this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userLocation)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        this.googleMap.setOnMarkerClickListener(marker -> {
            bottomSheetBehaviorFeedstation.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return false;
        });

        googleMap.setOnMapLongClickListener(latLng -> {
            /*Location selectedLocation = new Location("");
            selectedLocation.setLatitude(latLng.latitude);
            selectedLocation.setLongitude(latLng.longitude);*/
            FeedstationActivity.startInCreateMode(getActivity(), latLng);
            Log.d(TAG, "OnMapLongClick " + latLng.latitude + " / " + latLng.longitude + "]");
        });
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

    private void goToFeedStationActivity() {
        Intent intent = new Intent(getContext(), FeedstationActivity.class);
        startActivity(intent);
    }

}
