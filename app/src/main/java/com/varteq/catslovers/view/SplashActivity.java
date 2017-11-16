package com.varteq.catslovers.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.varteq.catslovers.Auth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Auth.isUserLoggedIn(this))
            startActivity(new Intent(this, CatProfileActivity.class));
        else
            startActivity(new Intent(this, IntroActivity.class));
        finish();
    }
}
