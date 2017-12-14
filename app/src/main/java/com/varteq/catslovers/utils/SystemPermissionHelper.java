package com.varteq.catslovers.utils;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class SystemPermissionHelper {
    public static final int PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1;
    public static final int PERMISSIONS_FOR_TAKE_PHOTO_REQUEST = 2;
    public static final int PERMISSIONS_ACCESS_LOCATION_REQUEST = 3;
    public static final int REQUEST_CHECK_SETTINGS = 4;

    private Activity activity;
    private Fragment fragment;

    public SystemPermissionHelper(Activity activity) {
        this.activity = activity;
    }

    public SystemPermissionHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    public boolean isSaveImagePermissionGranted() {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public boolean isCameraPermissionGranted() {
        return isPermissionGranted(Manifest.permission.CAMERA);
    }

    public boolean isAccessLocationGranted() {
        return isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private boolean isPermissionGranted(String permission) {
        if (fragment != null) {
            return ContextCompat.checkSelfPermission(fragment.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void requestPermissionsForSaveFileImage() {
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestPermissionsTakePhoto() {
        checkAndRequestPermissions(PERMISSIONS_FOR_TAKE_PHOTO_REQUEST, Manifest.permission.CAMERA);
    }

    public void requestPermissionsAccessLocation() {
        checkAndRequestPermissions(PERMISSIONS_ACCESS_LOCATION_REQUEST, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void checkAndRequestPermissions(int requestCode, String... permissions) {
        if (collectDeniedPermissions(permissions).length > 0) {
            requestPermissions(requestCode, collectDeniedPermissions(permissions));
        }
    }

    private String[] collectDeniedPermissions(String... permissions) {
        ArrayList<String> deniedPermissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissionsList.add(permission);
            }
        }

        return deniedPermissionsList.toArray(new String[deniedPermissionsList.size()]);
    }

    private void requestPermissions(int requestCode, String... permissions) {
        if (fragment != null) {
            fragment.requestPermissions(permissions, requestCode);
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    public void checkLocationSettings(Activity activity, LocationRequest locationRequest, OnSuccessListener<LocationSettingsResponse> listener) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(activity, listener);

        task.addOnFailureListener(activity, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(activity,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }
}