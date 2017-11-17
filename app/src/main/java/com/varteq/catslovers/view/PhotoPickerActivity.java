package com.varteq.catslovers.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class PhotoPickerActivity extends AppCompatActivity {

    private final int RESULT_LOAD_IMAGE = 0;
    private final int REQUEST_CODE_IMAGE_PERMISSION = 100;
    private String pickerTitle;

    private void performSelectIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, pickerTitle),
                RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (null != data)
                    onImageSelected(data.getData());
                else onImageSelected(null);
                break;
        }
    }

    public void pickPhotoWithPermission(String title) {
        this.pickerTitle = title;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_IMAGE_PERMISSION);
        } else performSelectIntent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performSelectIntent();
            } else {
                //Permission denied
            }
        }
    }

    protected void onImageSelected(Uri uri) {
    }
}
