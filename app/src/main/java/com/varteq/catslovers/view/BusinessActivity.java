package com.varteq.catslovers.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.Business;
import com.varteq.catslovers.view.fragments.MapFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BusinessActivity extends AppCompatActivity {

    @BindView(R.id.textView_business_name)
    TextView nameTextView;
    @BindView(R.id.textView_business_address)
    TextView addressTextView;
    @BindView(R.id.textView_business_description)
    TextView descriptionTextView;
    @BindView(R.id.textView_business_hours)
    TextView hoursTextView;
    @BindView(R.id.textView_business_phone)
    TextView phoneTextView;
    @BindView(R.id.textView_business_webLink)
    TextView webLinkTextView;

    private Business business;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        ButterKnife.bind(this);

        phoneTextView.setPaintFlags(phoneTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        webLinkTextView.setPaintFlags(webLinkTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Intent intent = getIntent();
        business = intent.getParcelableExtra("business");
        if (business != null) {
            nameTextView.setText(business.getName());
            addressTextView.setText(business.getAddress());
            descriptionTextView.setText(business.getDescription());
            hoursTextView.setText(business.getOpenHours());
            phoneTextView.setText(business.getPhone());
            webLinkTextView.setText(business.getLink());
        }
    }


    @OnClick(R.id.textView_business_webLink)
    public void onBusinessWeblinkClick(View view) {
        String url = ((TextView) view).getText().toString();
        if (!url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    @OnClick(R.id.textView_business_phone)
    public void onBusinessPhoneClick(View view) {
        String phone = ((TextView) view).getText().toString();
        if (!phone.isEmpty()) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MapFragment.BUSINESS_PERMISSIONS_REQUEST_CALL_PHONE);
            } else {
                callPhone(phone);
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void callPhone(String phone) {
        if (phone != null && !phone.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MapFragment.BUSINESS_PERMISSIONS_REQUEST_CALL_PHONE:
                if (permissions[0].equals(Manifest.permission.CALL_PHONE)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPhone(this.business.getPhone());
                }
                break;
        }
    }
}
