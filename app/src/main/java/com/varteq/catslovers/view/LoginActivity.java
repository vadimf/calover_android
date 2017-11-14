package com.varteq.catslovers.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.varteq.catslovers.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.continue_button).setOnClickListener(
                view -> startActivity(new Intent(LoginActivity.this, ConfirmNumberActivity.class))
        );
    }
}
