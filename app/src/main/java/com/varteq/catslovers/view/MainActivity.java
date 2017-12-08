package com.varteq.catslovers.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.varteq.catslovers.Log;
import com.varteq.catslovers.R;
import com.varteq.catslovers.view.fragments.CatsFragment;
import com.varteq.catslovers.view.fragments.FeedFragment;
import com.varteq.catslovers.view.fragments.MapFragment;
import com.varteq.catslovers.view.fragments.MessagesFragment;
import com.varteq.catslovers.view.presenter.MainPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity {

    private String TAG = MainActivity.class.getSimpleName();
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
    @BindView(R.id.frameLayout)
    FrameLayout mainLayout;

    CatsFragment catsFragment;
    MapFragment mapFragment;
    FeedFragment feedFragment;
    MessagesFragment messagesFragment;
    private MainPresenter presenter;

    final String STATE_NAVIGATION_SELECTED = "navigationSelected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Log.d(TAG, "onCreate");
        presenter = new MainPresenter(this);

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
        //runDialogTimer();
        if (savedInstanceState != null)
            mBottomNavigationView.setSelectedItemId(savedInstanceState.getInt(STATE_NAVIGATION_SELECTED, 0));
        else
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
                    Log.d(TAG, "action_map");
                    if (mapFragment == null)
                        mapFragment = new MapFragment();
                    setFragment(mapFragment);
                    toolbarTitle.setText("Feedstations");
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.action_feed:
                    Log.d(TAG, "action_feed");
                    /*
                    if (feedFragment == null)
                        feedFragment = new FeedFragment();
                    setFragment(feedFragment);
                    */
                    toolbarTitle.setText("Feed");
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.action_chat:
                    Log.d(TAG, "action_chat");
                    presenter.checkQBLogin();
                    toolbarTitle.setText("Chat");
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.action_cats:
                    Log.d(TAG, "action_cats");
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

    public void showChat() {
        if (messagesFragment == null)
            messagesFragment = new MessagesFragment();
        setFragment(messagesFragment);
        //startActivity(new Intent(MainActivity.this, DialogsActivity.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_NAVIGATION_SELECTED, mBottomNavigationView.getSelectedItemId());
    }

    @Override
    protected View getSnackbarAnchorView() {
        return mainLayout;
    }
}
