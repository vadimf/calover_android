package com.varteq.catslovers.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.view.fragments.CatsFragment;
import com.varteq.catslovers.view.fragments.FeedFragment;
import com.varteq.catslovers.view.fragments.MapFragment;


public class MainActivity extends AppCompatActivity {

    View view;

    TextView timerText;
    BottomNavigationView mBottomNavigationView;
    Toolbar toolbar;
    View toolbarView;
    TextView toolbarTitle;
    ImageButton catsNotificationButton;
    ImageButton catsSearchButton;
    ImageButton catsAddButton;
    RelativeLayout catsToolsRelativeLayout;

    CatsFragment catsFragment;
    MapFragment mapFragment;
    FeedFragment feedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);

        view = findViewById(R.id.hiddenWindow);
        timerText = findViewById(R.id.timerText);
        toolbar = findViewById(R.id.mainToolbar);
        toolbarView = getLayoutInflater().inflate(R.layout.toolbar_main, toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        catsNotificationButton = findViewById(R.id.catsNotificationButton);
        catsSearchButton = findViewById(R.id.catsSearchButton);
        catsAddButton = findViewById(R.id.catsAddButton);
        setSupportActionBar(toolbar);
        catsToolsRelativeLayout = findViewById(R.id.catsToolsRelativeLayout);

        initListeners();

        // TODO: 16.11.17 check is timer need or not
        runDialogTimer();
        mBottomNavigationView.setSelectedItemId(R.id.action_map);
    }

    private void runDialogTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        }, 2200);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                timerText.setText("Success - 1 sec");
            }
        }, 1000);
    }


    private void setFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit();
        }
    }

    private void initListeners() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_map:
                    if (mapFragment == null)
                        mapFragment = new MapFragment();
                    setFragment(mapFragment);
                    toolbarTitle.setText("Map");
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.action_feed:
                    /*
                    if (feedFragment == null)
                        feedFragment = new FeedFragment();
                    setFragment(feedFragment);
                    */
                    toolbarTitle.setText("Feed");
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.action_chat:
                    toolbarTitle.setText("Chat");
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.action_cats:
                    if (catsFragment == null)
                        catsFragment = new CatsFragment();
                    setFragment(catsFragment);
                    toolbarTitle.setText("Cats");
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    break;
            }
            return true;
        });
    }

}
