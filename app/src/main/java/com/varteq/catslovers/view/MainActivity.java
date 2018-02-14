package com.varteq.catslovers.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
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

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.varteq.catslovers.utils.SystemPermissionHelper.REQUEST_CHECK_SETTINGS;


public class MainActivity extends BaseActivity {

    private String TAG = MainActivity.class.getSimpleName();
    View view;

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
    ViewPager mainLayout;
    RoundedImageView avatarImageView;
    TextView usernameTextView;
    ImageButton drawerBackButton;
    TextView appVersionTextView;

    CatsFragment catsFragment;
    MapFragment mapFragment;
    FeedFragment feedFragment;
    MessagesFragment messagesFragment;
    private MainPresenter presenter;

    private List<Feedstation> invitations;
    final String STATE_NAVIGATION_SELECTED = "navigationSelected";
    int navigationSelectedItemId;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    @Nullable ImageButton navigationEditImageButton;
    View navigationHeaderLayout;

    private int catsTabClickCount = 0;
    boolean doubleBackToExitPressedOnce = false;
    private boolean needCheckUserSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Log.d(TAG, "onCreate");

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
        //drawNavigationBar();

        presenter = new MainPresenter(this);
        presenter.loadUserInfo();

        toolbar = findViewById(R.id.mainToolbar);
        toolbarView = getLayoutInflater().inflate(R.layout.toolbar_main, toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        menuButton = findViewById(R.id.menu_imageButton);
        menuButton.setOnClickListener(view -> {
            drawerLayout.openDrawer(Gravity.LEFT);
        });

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
            if (navigationSelectedItemId == R.id.action_cats)
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

        initNavigationDrawer();
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager());
        mainLayout.setAdapter(adapter);
        mainLayout.setOffscreenPageLimit(2);
        mainLayout.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                onTabChanged(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    //@OnClick(R.id.catsAddButton)
    void onPlusClick() {
        CatProfileActivity.startInCreateMode(this);
    }

    private void setFragment(int pos) {
        if (mainLayout.getCurrentItem() == pos) return;
        mainLayout.setCurrentItem(pos);
        /*if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit();
        }*/

    }

    public class FragmentPagerAdapter extends FragmentStatePagerAdapter {
        public FragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new MapFragment();
                case 1:
                    return new FeedFragment();
                case 2:
                    return new MessagesFragment();
                case 3:
                    return new CatsFragment();
                default:
                    return new MapFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }


    private void initListeners() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            /*if (item.getItemId() == R.id.action_cats)
                onCatsTabClicked();
            else catsTabClickCount = 0;*/
            if (item.getItemId() != navigationSelectedItemId) {
                switch (item.getItemId()) {
                    case R.id.action_map:
                        onTabChanged(0, false);
                        break;
                    case R.id.action_feed:
                        onTabChanged(1, false);
                        break;
                    case R.id.action_chat:
                        onTabChanged(2, false);
                        break;
                    case R.id.action_cats:
                        onTabChanged(3, false);
                        break;
                }
            }
            navigationSelectedItemId = item.getItemId();
            return true;
        });
    }

    private void onTabChanged(int pos, boolean isFromViewPager) {
        showAllowedIcons(pos);
        switch (pos) {
            case 0:
                if (isFromViewPager)
                    mBottomNavigationView.setSelectedItemId(R.id.action_map);
                else {
                    Log.d(TAG, "action_map");
                    toolbarTitle.setText(getResources().getString(R.string.toolbar_title_feedstations));
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    presenter.checkInvitations();
                    setFragment(0);
                }
                break;
            case 1:
                if (isFromViewPager)
                    mBottomNavigationView.setSelectedItemId(R.id.action_feed);
                else {
                    Log.d(TAG, "action_feed");
                    toolbarTitle.setText(getResources().getString(R.string.toolbar_title_feed));
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    setFragment(1);
                }
                break;
            case 2:
                if (isFromViewPager)
                    mBottomNavigationView.setSelectedItemId(R.id.action_chat);
                else {
                    Log.d(TAG, "action_chat");
                    toolbarTitle.setText(getResources().getString(R.string.toolbar_title_chat));
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    setFragment(2);
                }
                break;
            case 3:
                if (isFromViewPager)
                    mBottomNavigationView.setSelectedItemId(R.id.action_cats);
                else {
                    Log.d(TAG, "action_cats");
                    toolbarTitle.setText(getResources().getString(R.string.toolbar_title_cats));
                    catsToolsRelativeLayout.setVisibility(View.VISIBLE);
                    setFragment(3);
                }
                break;
        }
    }

    private Handler sendLogsTimer;

//    private void onCatsTabClicked() {
//        catsTabClickCount++;
//        if (catsTabClickCount > 7) {
//            catsTabClickCount = 0;
//            sendLogs();
//        } else if (catsTabClickCount == 3) {
//            if (sendLogsTimer == null)
//                sendLogsTimer = new Handler();
//            sendLogsTimer.postDelayed(() -> catsTabClickCount = 0, 1500);
//        }
//    }

    private void showAllowedIcons(int pos) {
        catsNotificationButton.setVisibility(View.VISIBLE);
        catsAddButton.setVisibility(View.VISIBLE);

        switch (pos) {
            case 0:
                catsAddButton.setVisibility(View.GONE);
                break;
            case 3:
                catsNotificationButton.setVisibility(View.GONE);
                break;
        }
    }

    public void showChat() {
        /*if (messagesFragment == null)
            messagesFragment = new MessagesFragment();
        setFragment(messagesFragment);*/
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
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (mapFragment != null) {
                            mapFragment.onLocationAvailable();
                            mapFragment.enableMyLocation();
                        }
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
        Toaster.shortToast("Please create first pet cat to make posts");
    }

    public void invitationsLoaded(List<Feedstation> invitations) {
        this.invitations = invitations;
    }

    public void onSuccessJoin() {
        Toaster.shortToast(R.string.you_have_successfully_joined);
    }

    private void initNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        navigationHeaderLayout = navigationView.getHeaderView(0);
        navigationEditImageButton = navigationHeaderLayout.findViewById(R.id.imageButton_edit);
        avatarImageView = navigationHeaderLayout.findViewById(R.id.imageView_avatar);
        usernameTextView = navigationHeaderLayout.findViewById(R.id.textView_email);
        drawerBackButton = navigationHeaderLayout.findViewById(R.id.button_navigation_drawer_back);
        appVersionTextView = navigationView.findViewById(R.id.textView_version);

        if (navigationEditImageButton != null) {
            navigationEditImageButton.setOnClickListener(view -> {
                startSettingsEdit();
            });
        }

        navigationHeaderLayout.findViewById(R.id.change_button).setOnClickListener(view -> startSettingsEdit());
        drawerBackButton.setOnClickListener(view -> drawerLayout.closeDrawer(Gravity.LEFT));
        appVersionTextView.setText(Utils.getAppInfo(this));
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_menu_add_business:
                    String addBusinessUrl = "http://catslovers-web.clients.in.ua/partners";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(addBusinessUrl));
                    startActivity(i);
                    break;
               /* case R.id.navigation_menu_clear_history:
                    break;*/
                case R.id.navigation_menu_share:
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "CatLovers");
                        intent.putExtra(Intent.EXTRA_TEXT, "I recommend you that app:\nhttps://play.google.com/store/apps/details?id=com.varteq.catslovers");
                        startActivity(Intent.createChooser(intent, "choose one"));
                    } catch (Exception e) {
                        //e.toString();
                    }
                    break;
                case R.id.navigation_menu_send_feedback:
                    sendFeedback();
                    break;
                case R.id.navigation_menu_send_logs:
                    sendLogs();
                    break;
                case R.id.navigation_menu_sign_out:
                    signOut();
                    break;
                /*case R.id.navigation_menu_info:
                    break;*/
            }
            return false;
        });
    }

    private void sendFeedback() {
        startActivity(new Intent(this, FeedbackActivity.class));
    }

    private void startSettingsEdit() {
        needCheckUserSettings = true;
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    public void setNavigationUsername(String username) {
        if (username != null)
            usernameTextView.setText(username);
    }

    public void updateNavigationAvatar(String path) {
        Glide.with(this)
                .load(path != null ? path : R.drawable.user_avatar_default)
                .apply(new RequestOptions().error(R.drawable.user_avatar_default))
                .into(avatarImageView);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    private void sendLogs() {
        File fileDir = Log.getLogFile();
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileDir));
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"amakhovyk@varteq.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "CatsLovers logs");
        startActivity(Intent.createChooser(email, "Send CatsLovers logs"));
    }

    private void signOut() {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to sign out?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        presenter.signOut();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toaster.shortToast("Press back again to exit");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2500);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (needCheckUserSettings) {
            presenter.loadUserInfo();
            needCheckUserSettings = false;
        }
    }

    public void onSignOutSuccess() {
        startActivity(new Intent(this, SplashActivity.class));
        finishAffinity();
    }
}
