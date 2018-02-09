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
import android.view.View;

import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.ImageUtils;
import com.varteq.catslovers.utils.SystemPermissionHelper;

import static com.varteq.catslovers.utils.SystemPermissionHelper.PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST;
import static com.varteq.catslovers.utils.SystemPermissionHelper.PERMISSIONS_FOR_TAKE_PHOTO_REQUEST;

public class ImageSourcePickDialogFragment extends DialogFragment {

    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA = 1;
    private static final int POSITION_CANCEL_OR_REMOVE = 2;
    private static final int POSITION_CANCEL = 3;

    enum Type {
        DEFAULT,
        IMAGE_OR_VIDEO,
        DEFAULT_WITH_REMOVE
    }

    private SystemPermissionHelper systemPermissionHelper;

    private OnImageSourcePickedListener onImageSourcePickedListener;
    private boolean permissionsResultReceived;
    private boolean showImageAndVideoPickerWithoutChoose = false;
    private Fragment fragment;
    private boolean isMultiselect;
    private View.OnClickListener onRemovePhotoSelectedListener;
    private Type type = Type.DEFAULT;

    public ImageSourcePickDialogFragment() {
        systemPermissionHelper = new SystemPermissionHelper(this);
    }

    public static void show(FragmentManager fm, OnImageSourcePickedListener onImageSourcePickedListener, boolean isMultiselect) {
        showWithRemoveOption(fm, onImageSourcePickedListener, isMultiselect, null);
    }

    public static void showImageAndVideoPicker(Fragment resultFragment, FragmentManager fm, OnImageSourcePickedListener onImageSourcePickedListener) {
        ImageSourcePickDialogFragment fragment = new ImageSourcePickDialogFragment();
        //fragment.setShowImageAndVideoPickerWithoutChoose(true);
        fragment.setType(Type.IMAGE_OR_VIDEO);
        fragment.setOnImageSourcePickedListener(onImageSourcePickedListener);
        fragment.setFragment(resultFragment);
        fragment.setCancelable(true);
        fragment.show(fm, ImageSourcePickDialogFragment.class.getSimpleName());
    }

    public static void showWithRemoveOption(FragmentManager fm, OnImageSourcePickedListener onImageSourcePickedListener, boolean isMultiselect, View.OnClickListener removePhotoSelectedListener) {
        ImageSourcePickDialogFragment fragment = new ImageSourcePickDialogFragment();
        fragment.setCancelable(true);
        fragment.setOnRemovePhotoSelectedListener(removePhotoSelectedListener);
        fragment.setOnImageSourcePickedListener(onImageSourcePickedListener);
        fragment.setMultiselect(isMultiselect);
        fragment.show(fm, ImageSourcePickDialogFragment.class.getSimpleName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (type.equals(Type.DEFAULT_WITH_REMOVE))
            builder.setItems(R.array.dlg_image_pick_with_remove, null);
        else {
            builder.setTitle(R.string.dlg_choose_image_from);
            builder.setItems(R.array.dlg_image_pick, null);
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.getListView().setOnItemClickListener((adapterView, view, i, l) -> {
            switch (i) {
                case POSITION_GALLERY:
                    if (!systemPermissionHelper.isSaveImagePermissionGranted()) {
                        systemPermissionHelper.requestPermissionsForSaveFileImage();
                        return;
                    } else if (type.equals(Type.IMAGE_OR_VIDEO)) {
                        ImageUtils.startImageAndVideoPicker(fragment);
                    } else
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
                case POSITION_CANCEL_OR_REMOVE:
                    if (type.equals(Type.DEFAULT_WITH_REMOVE) && onRemovePhotoSelectedListener != null) {
                        onRemovePhotoSelectedListener.onClick(null);
                    }
                    dismiss();
                    break;
                case POSITION_CANCEL:
                    dismiss();
                    break;
            }
        });
        return alertDialog;
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

    public void setType(Type type) {
        this.type = type;
    }

    public void setShowImageAndVideoPickerWithoutChoose(boolean showImageAndVideoPickerWithoutChoose) {
        this.showImageAndVideoPickerWithoutChoose = showImageAndVideoPickerWithoutChoose;
    }

    public void setOnRemovePhotoSelectedListener(View.OnClickListener onRemovePhotoSelectedListener) {
        this.onRemovePhotoSelectedListener = onRemovePhotoSelectedListener;
        if (onRemovePhotoSelectedListener != null)
            type = Type.DEFAULT_WITH_REMOVE;
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
                if (type.equals(Type.IMAGE_OR_VIDEO)) {
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