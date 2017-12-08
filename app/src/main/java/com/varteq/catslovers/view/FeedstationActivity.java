package com.varteq.catslovers.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.varteq.catslovers.Log;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.view.adapters.CatPhotosAdapter;
import com.varteq.catslovers.view.adapters.GroupPartnersAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedstationActivity extends PhotoPickerActivity {

    private String TAG = FeedstationActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    View toolbarView;
    TextView toolbarTitle;

    @BindView(R.id.avatar_imageView)
    ImageView avatarImageView;

    @BindView(R.id.expand_description_button)
    Button expandDescriptionButton;
    @BindView(R.id.description_textView)
    TextView descriptionTextView;
    @BindView(R.id.description_background)
    View descriptionBackground;

    @BindView(R.id.photos_RecyclerView)
    RecyclerView photosRecyclerView;

    @BindView(R.id.expand_partners_button)
    Button expandPartnersButton;
    @BindView(R.id.group_partners_RecyclerView)
    RecyclerView groupPartnersRecyclerView;

    private List<Uri> photoList;
    private List<GroupPartner> groupPartnersList;

    private CatPhotosAdapter photosAdapter;
    private GroupPartnersAdapter groupPartnersAdapter;

    ViewPager viewPager;
    HeaderPhotosViewPagerAdapter pagerAdapter;
    private int[] headerPhotos = {R.drawable.cat2, R.drawable.cat3, R.drawable.cat1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedstation);
        ButterKnife.bind(this);
        toolbarView = getLayoutInflater().inflate(R.layout.toolbar_feedstation, toolbar);
        toolbarView.findViewById(R.id.backButton).setOnClickListener(view -> onBackPressed());
        setSupportActionBar(toolbar);
        toolbarTitle = toolbarView.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Shylee's Babies");

        Glide.with(this)
                .load(getResources().getDrawable(R.drawable.cat2))
                .into(avatarImageView);

        pagerAdapter = new HeaderPhotosViewPagerAdapter(this);
        viewPager = findViewById(R.id.header_photo_viewPager);
        viewPager.setAdapter(pagerAdapter);

        groupPartnersList = new ArrayList<>();
        groupPartnersList.add(new GroupPartner(null, "Admin", true));
        groupPartnersList.add(new GroupPartner(null, "User1", false));
        groupPartnersAdapter = new GroupPartnersAdapter(groupPartnersList, true,
                new GroupPartnersAdapter.OnPersonClickListener() {

                    @Override
                    public void onPersonClicked(Uri imageUri) {
                        Log.d(TAG, "onPersonClicked " + imageUri);
                    }

                    @Override
                    public void onAddPerson() {
                        Log.d(TAG, "onAddPerson");
                        addGroupPartner();
                    }
                });
        groupPartnersRecyclerView.setAdapter(groupPartnersAdapter);
        groupPartnersRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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

    private void addGroupPartner() {
        groupPartnersList.add(1, new GroupPartner(null, "User" + ((int) (Math.random() * 999999) + 111111), false));
        groupPartnersAdapter.notifyItemInserted(1);
        groupPartnersRecyclerView.scrollToPosition(0);
    }

    @OnClick(R.id.upload_image_button)
    void uploadCatImage() {
        pickPhotoWithPermission(getString(R.string.select_cat_photo));
    }


    private class HeaderPhotosViewPagerAdapter extends PagerAdapter {
        Context context;

        public HeaderPhotosViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return headerPhotos.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            imageView.setImageDrawable(getResources().getDrawable(headerPhotos[position]));
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }

    @OnClick(R.id.expand_description_button)
    void expandCollapseDescription() {
        if (descriptionTextView.getVisibility() == View.VISIBLE) {
            expandDescriptionButton.setBackgroundResource(R.drawable.ic_expand_more_24dp);
            descriptionTextView.setVisibility(View.GONE);
            descriptionBackground.setVisibility(View.INVISIBLE);
        } else {
            expandDescriptionButton.setBackgroundResource(R.drawable.ic_expand_less_24dp);
            descriptionTextView.setVisibility(View.VISIBLE);
            descriptionBackground.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.expand_partners_button)
    void expandCollapseGroupPartners() {
        if (groupPartnersRecyclerView.getVisibility() == View.VISIBLE) {
            expandPartnersButton.setBackgroundResource(R.drawable.ic_expand_more_24dp);
            groupPartnersRecyclerView.setVisibility(View.GONE);
        } else {
            expandPartnersButton.setBackgroundResource(R.drawable.ic_expand_less_24dp);
            groupPartnersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

}
