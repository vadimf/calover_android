package com.varteq.catslovers.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.varteq.catslovers.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SuccessLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.next_button)
    void onNextClick() {
        CatProfileActivity.startInCreateMode(this);
        finishAffinity();
    }

    @OnClick(R.id.later_button)
    void onLaterClick() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finishAffinity();
    }

}
