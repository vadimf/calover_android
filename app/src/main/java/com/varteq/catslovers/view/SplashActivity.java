package com.varteq.catslovers.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.varteq.catslovers.Profile;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Profile.setUserLogin(this, true);
        if (Profile.isUserLoggedIn(this))
            startActivity(new Intent(this, MainActivity.class));
        else
            startActivity(new Intent(this, IntroActivity.class));
        finish();
    }
}
