package com.varteq.catslovers.utils.qb.imagepick;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.varteq.catslovers.utils.qb.imagepick.fragment.ImagePickHelperFragment;
import com.varteq.catslovers.utils.qb.imagepick.fragment.ImageSourcePickDialogFragment;

public class ImagePickHelper {

    public void pickAnImage(Fragment fragment, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(fragment, requestCode);
        showImageSourcePickerDialog(fragment.getChildFragmentManager(), imagePickHelperFragment, false);
    }

    public void pickAnImage(FragmentActivity activity, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(activity, requestCode);
        showImageSourcePickerDialog(activity.getSupportFragmentManager(), imagePickHelperFragment, false);
    }

    public void pickAnImageWithRemoveOptions(FragmentActivity activity, int requestCode, View.OnClickListener removeListener) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(activity, requestCode);
        ImageSourcePickDialogFragment.showWithRemoveOption(activity.getSupportFragmentManager(),
                new ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(imagePickHelperFragment),
                false, removeListener);
    }

    public void pickAnImages(FragmentActivity activity, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(activity, requestCode);
        showImageSourcePickerDialog(activity.getSupportFragmentManager(), imagePickHelperFragment, true);
    }

    public void pickAnImageOrVideo(FragmentActivity activity, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(activity, requestCode);
        ImageSourcePickDialogFragment.showImageAndVideoPicker(imagePickHelperFragment, activity.getSupportFragmentManager(),
                new ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(imagePickHelperFragment));
    }

    private void showImageSourcePickerDialog(FragmentManager fm, ImagePickHelperFragment fragment, boolean isMultiselect) {
        ImageSourcePickDialogFragment.show(fm,
                new ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(fragment), isMultiselect);
    }
}
