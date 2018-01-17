package com.varteq.catslovers.utils.qb.imagepick.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.ImageUtils;
import com.varteq.catslovers.utils.SystemPermissionHelper;

import static com.varteq.catslovers.utils.SystemPermissionHelper.PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST;
import static com.varteq.catslovers.utils.SystemPermissionHelper.PERMISSIONS_FOR_TAKE_PHOTO_REQUEST;

public class ImageSourcePickDialogFragment extends DialogFragment {

    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA = 1;

    private SystemPermissionHelper systemPermissionHelper;

    private OnImageSourcePickedListener onImageSourcePickedListener;
    private boolean permissionsResultReceived;
    private boolean showImageAndVideoPickerWithoutChoose = false;
    private Fragment fragment;
    private boolean isMultiselect;

    public ImageSourcePickDialogFragment() {
        systemPermissionHelper = new SystemPermissionHelper(this);
    }

    public static void show(FragmentManager fm, OnImageSourcePickedListener onImageSourcePickedListener, boolean isMultiselect) {
        ImageSourcePickDialogFragment fragment = new ImageSourcePickDialogFragment();
        fragment.setCancelable(false);
        fragment.setOnImageSourcePickedListener(onImageSourcePickedListener);
        fragment.setMultiselect(isMultiselect);
        fragment.show(fm, ImageSourcePickDialogFragment.class.getSimpleName());
    }

    public static void showImageAndVideoPicker(Fragment resultFragment, FragmentManager fm) {
        ImageSourcePickDialogFragment fragment = new ImageSourcePickDialogFragment();
        fragment.setShowImageAndVideoPickerWithoutChoose(true);
        fragment.setFragment(resultFragment);
        fragment.setCancelable(false);
        fragment.show(fm, ImageSourcePickDialogFragment.class.getSimpleName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!showImageAndVideoPickerWithoutChoose) {
            builder.setTitle(R.string.dlg_choose_image_from);
            builder.setItems(R.array.dlg_image_pick, null);
            AlertDialog alertDialog = builder.create();
            alertDialog.getListView().setOnItemClickListener((adapterView, view, i, l) -> {
                switch (i) {
                    case POSITION_GALLERY:
                        if (!systemPermissionHelper.isSaveImagePermissionGranted()) {
                            systemPermissionHelper.requestPermissionsForSaveFileImage();
                            return;
                        }
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.GALLERY, isMultiselect);
                        dismiss();
                        break;
                    case POSITION_CAMERA:
                        if (!systemPermissionHelper.isCameraPermissionGranted()) {
                            systemPermissionHelper.requestPermissionsTakePhoto();
                            return;
                        }
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA, isMultiselect);
                        dismiss();
                        break;
                }
            });
            return alertDialog;
        } else {
            if (!systemPermissionHelper.isSaveImagePermissionGranted()) {
                systemPermissionHelper.requestPermissionsForSaveFileImage();
            } else {
                ImageUtils.startImageAndVideoPicker(fragment);
                dismiss();
            }
            return builder.create();
        }
    }

    /*@Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (showImageAndVideoPickerWithoutChoose) {
            if (!systemPermissionHelper.isSaveImagePermissionGranted()) {
                systemPermissionHelper.requestPermissionsForSaveFileImage();
                return;
            }
            else ImageUtils.startImageAndVideoPicker(fragment);
        }
    }*/

    public void setShowImageAndVideoPickerWithoutChoose(boolean showImageAndVideoPickerWithoutChoose) {
        this.showImageAndVideoPickerWithoutChoose = showImageAndVideoPickerWithoutChoose;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (permissionsResultReceived)
            dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (showImageAndVideoPickerWithoutChoose) {
                    ImageUtils.startImageAndVideoPicker(fragment);
                } else
                    onImageSourcePickedListener.onImageSourcePicked(ImageSource.GALLERY, isMultiselect);
            }
        }
        if (requestCode == PERMISSIONS_FOR_TAKE_PHOTO_REQUEST) {
            if (permissions[0].equals(Manifest.permission.CAMERA)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA, isMultiselect);
            }
        }
        permissionsResultReceived = true;
    }

    public void setOnImageSourcePickedListener(OnImageSourcePickedListener onImageSourcePickedListener) {
        this.onImageSourcePickedListener = onImageSourcePickedListener;
    }

    public void setMultiselect(boolean multiselect) {
        isMultiselect = multiselect;
    }

    public interface OnImageSourcePickedListener {

        void onImageSourcePicked(ImageSource source, boolean isMultiselect);
    }

    public enum ImageSource {
        GALLERY,
        CAMERA
    }

    public static class LoggableActivityImageSourcePickedListener implements OnImageSourcePickedListener {

        private Activity activity;
        private Fragment fragment;

        public LoggableActivityImageSourcePickedListener(Activity activity) {
            this.activity = activity;
        }

        public LoggableActivityImageSourcePickedListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onImageSourcePicked(ImageSource source, boolean isMultiselect) {
            switch (source) {
                case GALLERY:
                    if (fragment != null) {
                        ImageUtils.startImagePicker(fragment, isMultiselect);
                    } else {
                        ImageUtils.startImagePicker(activity, isMultiselect);
                    }
                    break;
                case CAMERA:
                    if (fragment != null) {
                        ImageUtils.startCameraForResult(fragment);
                    } else {
                        ImageUtils.startCameraForResult(activity);
                    }
                    break;
            }
        }
    }
}