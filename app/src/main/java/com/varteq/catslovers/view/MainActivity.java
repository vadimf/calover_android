package com.varteq.catslovers.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.view.fragments.CatsFragment;
import com.varteq.catslovers.view.fragments.FeedFragment;
import com.varteq.catslovers.view.fragments.MapFragment;
import com.varteq.catslovers.view.fragments.MessagesFragment;
import com.varteq.catslovers.view.presenter.MainPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.varteq.catslovers.utils.SystemPermissionHelper.REQUEST_CHECK_SETTINGS;


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
    ImageButton menuButton;
    LinearLayout catsToolsRelativeLayout;
    @BindView(R.id.frameLayout)
    FrameLayout mainLayout;

    CatsFragment catsFragment;
    MapFragment mapFragment;
    FeedFragment feedFragment;
    MessagesFragment messagesFragment;
    private MainPresenter presenter;

    private List<Feedstation> invitations;
    final String STATE_NAVIGATION_SELECTED = "navigationSelected";
    int navigationSelectedItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Log.d(TAG, "onCreate");

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
        drawNavigationBar();

        presenter = new MainPresenter(this);

        view = findViewById(R.id.hiddenWindow);
        timerText = findViewById(R.id.timerText);
        toolbar = findViewById(R.id.mainToolbar);
        toolbarView = getLayoutInflater().inflate(R.layout.toolbar_main, toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        menuButton = findViewById(R.id.menu_imageButton);
        menuButton.setOnClickListener(view -> Toaster.shortToast(R.string.coming_soon));

        catsNotificationButton = findViewById(R.id.catsNotificationButton);
        catsNotificationButton.setOnClickListener(view -> {
            if (invitations != null && !invitations.isEmpty()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Accept invite to " + invitations.get(0).getName())
                        .setNeutralButton(android.R.string.cancel, null)
                        .setNegativeButton(R.string.no, (dialogInterface, i) ->
                                presenter.leaveFeedstation(invitations.get(0).getId()))
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) ->
                                presenter.joinFeedstation(invitations.get(0).getId()))
                        .create()
                        .show();
            } else
                Toaster.shortToast("No notifications");
        });

        catsSearchButton = findViewById(R.id.catsSearchButton);
        catsSearchButton.setOnClickListener(view -> Toaster.shortToast(R.string.coming_soon));
        catsAddButton = findViewById(R.id.catsAddButton);
        catsAddButton.setOnClickListener(view -> {
            if (navigationSelectedItemId == R.id.action_map)
                onPlusClick();
            else if (navigationSelectedItemId == R.id.action_chat && messagesFragment != null)
                messagesFragment.onStartNewChatClick(null);
            else if (navigationSelectedItemId == R.id.action_feed) {
                presenter.checkMyPrivateFeedstation();
            }
        });
        setSupportActionBar(toolbar);
        catsToolsRelativeLayout = findViewById(R.id.catsToolsRelativeLayout);

        initListeners();

        // TODO: 16.11.17 check is timer need or not
        //runDialogTimer();
        if (savedInstanceState != null) {
            mBottomNavigationView.setSelectedItemId(savedInstanceState.getInt(STATE_NAVIGATION_SELECTED, 0));
        } else {
            mBottomNavigationView.setSelectedItemId(R.id.action_map);
        }
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

    //@OnClick(R.id.catsAddButton)
    void onPlusClick() {
        CatProfileActivity.startInCreateMode(this);
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
            if (item.getItemId() != navigationSelectedItemId) {
                if (navigationSelectedItemId == item.getItemId()) return true;
                showNotificationAndAndPlusIcons();
                switch (item.getItemId()) {
                    case R.id.action_map:
                        Log.d(TAG, "action_map");
                        if (mapFragment == null)
                            mapFragment = new MapFragment();
                        setFragment(mapFragment);
                        toolbarTitle.setText(getResources().getString(R.string.toolbar_title_feedstations));
                        catsToolsRelativeLayout.setVisibility(View.VISIBLE);

                        presenter.checkInvitations();
                        break;
                    case R.id.action_feed:
                        Log.d(TAG, "action_feed");

                        if (feedFragment == null)
                            feedFragment = new FeedFragment();
                        setFragment(feedFragment);

                        toolbarTitle.setText(getResources().getString(R.string.toolbar_title_feed));
                        catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.action_chat:
                        Log.d(TAG, "action_chat");
                        showChat();
                        toolbarTitle.setText(getResources().getString(R.string.toolbar_title_chat));
                        catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.action_cats:
                        hideNotificationAndPlusIcons();
                        Log.d(TAG, "action_cats");
                        if (catsFragment == null)
                            catsFragment = new CatsFragment();
                        setFragment(catsFragment);
                        toolbarTitle.setText(getResources().getString(R.string.toolbar_title_cats));
                        catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
            navigationSelectedItemId = item.getItemId();
            return true;
        });
    }

    private void hideNotificationAndPlusIcons() {
        catsNotificationButton.setVisibility(View.GONE);
        catsAddButton.setVisibility(View.GONE);
    }

    private void showNotificationAndAndPlusIcons() {
        catsNotificationButton.setVisibility(View.VISIBLE);
        catsAddButton.setVisibility(View.VISIBLE);
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

    private void drawNavigationBar() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);
        FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) menuView.getLayoutParams();
        layoutParams1.bottomMargin = Utils.convertDpToPx(3, this);
        menuView.setLayoutParams(layoutParams1);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final ImageView iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
            if (i == 2) {
                layoutParams.height = Utils.convertDpToPx(50, this);
                layoutParams.width = Utils.convertDpToPx(60, this);
            } else if (i == 3) {
                layoutParams.height = Utils.convertDpToPx(50, this);
                layoutParams.width = Utils.convertDpToPx(65, this);
            } else {
                layoutParams.height = Utils.convertDpToPx(50, this);
                layoutParams.width = Utils.convertDpToPx(50, this);
            }
            iconView.setLayoutParams(layoutParams);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (mapFragment != null)
                            mapFragment.onLocationAvailable();
                        break;
                    default:
                        break;
                }
                break;
        }
        if (messagesFragment != null)
            messagesFragment.onUsersSelescted(requestCode, resultCode, data);
    }


    public void showNewFeedPostActivity() {
        startActivity(new Intent(this, NewFeedPostActivity.class));
    }

    public void onPrivateFeedstationNotFound() {
        Toaster.shortToast("You should create own cat");
    }

    public void invitationsLoaded(List<Feedstation> invitations) {
        this.invitations = invitations;
    }

    public void onSuccessJoin() {
        Toaster.shortToast("You have successfully joined");
    }
}
