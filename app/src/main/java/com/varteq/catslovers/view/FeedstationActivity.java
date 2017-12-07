package com.varteq.catslovers.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.varteq.catslovers.Log;
import com.varteq.catslovers.R;
import com.varteq.catslovers.view.adapters.CatPhotosAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedstationActivity extends PhotoPickerActivity  {

    private String TAG = FeedstationActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    View toolbarView;
    TextView toolbarTitle;

    @BindView(R.id.photos_RecyclerView)
    RecyclerView photosRecyclerView;

    private List<Uri> photoList;

    private CatPhotosAdapter photosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedstation);
        ButterKnife.bind(this);
        toolbarView = getLayoutInflater().inflate(R.layout.toolbar_feedstation, toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = toolbarView.findViewById(R.id.toolbar_title);

        toolbarTitle.setText("Shylee's Babies");


        photoList = new ArrayList<>();
        photosAdapter = new CatPhotosAdapter(photoList, this::showImage);
        photosRecyclerView.setAdapter(photosAdapter);
        photosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }


    @Override
    protected void onImageSelected(Uri uri) {
        super.onImageSelected(uri);
        if (uri != null) {
            Log.d(TAG, "onImageSelected " + uri);
            photoList.add(0, uri);
            photosAdapter.notifyItemInserted(0);
            photosRecyclerView.scrollToPosition(0);
        }
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

    @OnClick(R.id.upload_image_button)
    void uploadCatImage() {
        pickPhotoWithPermission(getString(R.string.select_cat_photo));
    }

}
