package com.varteq.catslovers.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity class for greeting user logged for first-time.
 */
public class IntroActivity extends AppCompatActivity {

    private static final String TAG = IntroActivity.class.getSimpleName();

    private final int imageSwitcherImages[] = {R.drawable.onboarding_illustration_1,
            R.drawable.onboarding_illustration_2,
            R.drawable.onboarding_illustration_3};

    private final int onboardingLayouts[] = {R.layout.onboarding_layout_1,
            R.layout.onboarding_layout_2,
            R.layout.onboarding_layout_3};

    private List<View> layouts;
    private TextView[] dots;

    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.dots_layout)
    LinearLayout dotsLayout;
    @BindView(R.id.go_to_login_button)
    TextView goToLoginButton;
    @BindView(R.id.start_button)
    TextView startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Log.d(TAG, "onCreate");
        ButterKnife.bind(this);

        initUI();
    }

    @OnClick({R.id.go_to_login_button, R.id.start_button})
    void goToLogin() {
        Log.d(TAG, "goToLogin");
        finish();
        // TODO complete auth and stitch to SignInActivity
        startActivity(new Intent(this, SignUpActivity.class));
    }

    private void initUI() {

        initializeOnboardingLayouts();

        initializeDots();

        initializeViewPager();
    }

    private void initializeOnboardingLayouts() {

        layouts = new ArrayList<>();

        for (int i = 0; i < onboardingLayouts.length; i++) {
            int resId = onboardingLayouts[i];

            View layout = LayoutInflater.from(getApplicationContext())
                    .inflate(resId, null, false);

            scaleOnboardingView(layout);

            layouts.add(layout);
        }
    }

    private void initializeDots() {

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.rightMargin = Utils.convertDpToPx(23, this);
        dots = new TextView[imageSwitcherImages.length];
        for (int i = 0; i < imageSwitcherImages.length; i++) {
            dots[i] = new TextView(this);
            if (i < imageSwitcherImages.length - 1) {
                dots[i].setLayoutParams(layoutParams);
            }
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(40);
            dotsLayout.addView(dots[i]);
        }

        selectBottomDot(0);
    }

    private void initializeViewPager() {

        viewPager.setAdapter(new IntroPagerAdapter());
        viewPager.addOnPageChangeListener(new IntroPageChangeListener());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setClipToPadding(false);
    }

    /**
     * Scale onboarding view for high resolution displays.
     *
     * @param view View
     */
    private void scaleOnboardingView(View view) {

        final float PIXEL_DISPLAY_WIDTH_DP = 360.0f;
        final float PIXEL_DISPLAY_HEIGHT_DP = 640.0f;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        float displayDensity = displayMetrics.density;

        int displayWidthInDp = (int) (displayWidth / displayDensity);
        int displayHeightInDp = (int) (displayHeight / displayDensity);

        // Compare it with Pixel sizes (1080 x 1920, density - 3.0)
        if (displayWidthInDp > PIXEL_DISPLAY_WIDTH_DP || displayHeightInDp > PIXEL_DISPLAY_HEIGHT_DP) {
            float factorW = displayWidthInDp / PIXEL_DISPLAY_WIDTH_DP;
            float factorH = displayHeightInDp / PIXEL_DISPLAY_HEIGHT_DP;

            // Get lower factor
            float factor = factorW < factorH ? factorW : factorH;

            // Scale onboarding view
            view.setScaleX(factor);
            view.setScaleY(factor);
            view.requestLayout();
        }
    }

    private void selectBottomDot(int currentPage) {
        for (int i = 0; i < imageSwitcherImages.length; i++) {
            dots[i].setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        dots[currentPage].setTextColor(getResources().getColor(R.color.white));
    }

    /**
     * Custom page adapter.
     */
    private class IntroPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(layouts.get(position));
            return layouts.get(position);
        }

        @Override
        public int getCount() {
            return layouts.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    /**
     * Custom listener.
     */
    private class IntroPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            selectBottomDot(position);
            if (position == imageSwitcherImages.length - 1) {
                dotsLayout.setVisibility(View.INVISIBLE);
                goToLoginButton.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
            } else {
                dotsLayout.setVisibility(View.VISIBLE);
                goToLoginButton.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // Do nothing
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // Do nothing
        }
    }
}
