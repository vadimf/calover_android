package com.varteq.catslovers.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.utils.qb.imagepick.ImagePickHelper;
import com.varteq.catslovers.utils.qb.imagepick.OnImagePickedListener;
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


public class MainActivity extends BaseActivity  implements OnImagePickedListener {

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
    RoundedImageView avatarImageView;
    Button changeAvatarButton;
    TextView emailTextView;
    ImageButton drawerBackButton;

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
    ImageButton navigationEditImageButton;
    View navigationHeaderLayout;
    private String avatar;
    private int catsTabClickCount = 0;

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
        presenter.loadUserInfo();

        avatar = Profile.getUserAvatar(this);

        view = findViewById(R.id.hiddenWindow);
        timerText = findViewById(R.id.timerText);
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

        initNavigationDrawer();

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
            if (item.getItemId() == R.id.action_cats)
                onCatsTabClicked();
            else catsTabClickCount = 0;
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

    private Handler sendLogsTimer;

    private void onCatsTabClicked() {
        catsTabClickCount++;
        if (catsTabClickCount > 7) {
            catsTabClickCount = 0;
            sendLogs();
        } else if (catsTabClickCount == 3) {
            if (sendLogsTimer == null)
                sendLogsTimer = new Handler();
            sendLogsTimer.postDelayed(() -> catsTabClickCount = 0, 1500);
        }
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
        super.onActivityResult(requestCode, resultCode, data);
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
        changeAvatarButton = navigationHeaderLayout.findViewById(R.id.button_change_avatar);
        emailTextView = navigationHeaderLayout.findViewById(R.id.textView_email);
        drawerBackButton = navigationHeaderLayout.findViewById(R.id.button_navigation_drawer_back);

        navigationEditImageButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
        changeAvatarButton.setOnClickListener(view -> new ImagePickHelper().pickAnImage(MainActivity.this, 0));
        drawerBackButton.setOnClickListener(view -> drawerLayout.closeDrawer(Gravity.LEFT));
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_menu_add_business:
                    String addBusinessUrl = "http://catslovers-web.clients.in.ua/partners";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(addBusinessUrl));
                    startActivity(i);
                    break;
                case R.id.navigation_menu_clear_history:
                    break;
                case R.id.navigation_menu_share:
                    break;
                case R.id.navigation_menu_info:
                    break;
            }
            return false;
        });
    }

    public void setNavigationEmail(String email) {
        if (email != null)
            emailTextView.setText(email);
        else
            emailTextView.setText(R.string.no_email);
    }

    public void setNavigationUsername(String name) {

    }

    public void updateNavigationAvatar(String url) {
        if (url != null)
            Glide.with(this)
                    .asBitmap()
                    .load(url)
                    .into(avatarImageView);
        else
            updateNavigationAvatar();
    }

    private void updateNavigationAvatar() {
        if (avatar != null)
            Glide.with(this)
                    .asBitmap()
                    .load(avatar)
                    .into(new SimpleTarget<Bitmap>() {
                        final int THUMBSIZE = 250;

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (resource.getWidth() > THUMBSIZE)
                                avatarImageView.setImageBitmap(ThumbnailUtils.extractThumbnail(resource,
                                        THUMBSIZE, THUMBSIZE));
                            else
                                avatarImageView.setImageBitmap(resource);
                        }
                    });
        else
            avatarImageView.setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        if (null != file) {
            avatar = file.getPath();
            Profile.saveUserAvatar(this, avatar);
            updateAvatar();
            presenter.uploadAvatar();
        }
    }

    @Override
    public void onImagesPicked(int requestCode, List<File> file) {

    }

    @Override
    public void onVideoPicked(int requestCode, File file, Bitmap preview) {

    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {

    }

    @Override
    public void onImagePickClosed(int requestCode) {

    }

    private void updateAvatar() {
        if (avatar != null)
            Glide.with(this)
                    .asBitmap()
                    .load(avatar)
                    .into(new SimpleTarget<Bitmap>() {
                        final int THUMBSIZE = 250;

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (resource.getWidth() > THUMBSIZE)
                                avatarImageView.setImageBitmap(ThumbnailUtils.extractThumbnail(resource,
                                        THUMBSIZE, THUMBSIZE));
                            else
                                avatarImageView.setImageBitmap(resource);
                        }
                    });
        else
            avatarImageView.setImageBitmap(Utils.getBitmapWithColor(getResources().getColor(R.color.transparent)));
    }

    public void updateAvatar(String url) {
        if (url != null)
            Glide.with(this)
                    .asBitmap()
                    .load(url)
                    .into(avatarImageView);
        else
            updateAvatar();
    }

    private void sendLogs() {
        File fileDir = Log.getLogFile();
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileDir));
        //email.putExtra(Intent.EXTRA_EMAIL, new String[]{"youremail@yahoo.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "CatsLovers logs");
        startActivity(Intent.createChooser(email, "Send CatsLovers logs"));
    }
}
