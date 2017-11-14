package com.varteq.catslovers.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.varteq.catslovers.R;

public class ConfirmNumberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_number);

        findViewById(R.id.confirm_number_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ConfirmNumberActivity.this, ValidateNumberActivity.class));
            }
        });
    }
}
