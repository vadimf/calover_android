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
import android.widget.ImageSwitcher;
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

public class IntroActivity extends AppCompatActivity {

    private String TAG = IntroActivity.class.getSimpleName();
    float initialX;
    private ImageSwitcher myImageSwitcher;
    private int counter = 0;
    private int imageSwitcherImages[] = {R.drawable.illustration_onbording_1, R.drawable.illustration_onboarding_2, R.drawable.onboarding_illustration_3};
    private int imageSwitcherTexts[] = {R.string.onboarding_text_1, R.string.onboarding_text_2, R.string.onboarding_text_3};
    private MyViewPagerAdapter myViewPagerAdapter;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    private List<View> layouts = new ArrayList<>();
    private TextView[] dots;
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

    private void initUI() {
        for (int i = 0; i < imageSwitcherImages.length; i++) {
            int resId = R.layout.onbording_layout_1;
            if (i == 1)
                resId = R.layout.onbording_layout_2;
            else if (i == 2)
                resId = R.layout.onbording_layout_3;

            View layout = LayoutInflater.from(getApplicationContext())
                    .inflate(resId, null, false);

            scaleOnboardingView(layout);

            layouts.add(layout);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.rightMargin = Utils.convertDpToPx(23, this);
        dots = new TextView[imageSwitcherImages.length];
        for (int i = 0; i < imageSwitcherImages.length; i++) {
            dots[i] = new TextView(this);
            if (i < imageSwitcherImages.length - 1)
                dots[i].setLayoutParams(layoutParams);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(40);
            dotsLayout.addView(dots[i]);
        }

        selectBottomDot(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setClipToPadding(false);
    }

    @OnClick({R.id.go_to_login_button, R.id.start_button})
    void goToLogin() {
        Log.d(TAG, "goToLogin");
        finish();
        // TODO complete auth and stitch to SignInActivity
        startActivity(new Intent(this, SignUpActivity.class));
    }

    private void selectBottomDot(int currentPage) {
        for (int i = 0; i < imageSwitcherImages.length; i++) {
            dots[i].setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        dots[currentPage].setTextColor(getResources().getColor(R.color.white));
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

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

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    public class MyViewPagerAdapter extends PagerAdapter {

        public MyViewPagerAdapter() {
        }

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
     * Scale onboarding view for high resolution displays.
     *
     * @param view View
     */
    private void scaleOnboardingView(View view) {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        float displayDensity = displayMetrics.density;

        int displayWidthInDp = (int) (displayWidth / displayDensity);
        int displayHeightInDp = (int) (displayHeight / displayDensity);

        // Compare it with Pixel sizes (1080 x 1920, density - 3.0)
        if (displayWidthInDp > 360 || displayHeightInDp > 640) {
            float factorW = displayWidthInDp / 360.0f;
            float factorH = displayHeightInDp / 640.0f;

            // Get lower factor
            float factor = factorW < factorH ? factorW : factorH;

            // Scale onboarding view
            view.setScaleX(factor);
            view.setScaleY(factor);
            view.requestLayout();
        }
    }
}
