package com.varteq.catslovers.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
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
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        for (int i = 0; i < imageSwitcherImages.length; i++) {
            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackground(getResources().getDrawable(R.drawable.boarding_rounded));
            int padding = Utils.convertDpToPx(12, this);
            layout.setPadding(padding, padding, padding, padding);

            ImageView imageView = new ImageView(getApplicationContext());
            TableLayout.LayoutParams imageLayoutParams = new TableLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT, 1);
            imageView.setLayoutParams(imageLayoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageResource(imageSwitcherImages[i]);
            layout.addView(imageView);

            TextView textView = new TextView(getApplicationContext());
            TableLayout.LayoutParams textLayoutParams = new TableLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT, 1);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setLayoutParams(textLayoutParams);
            textView.setText(imageSwitcherTexts[i]);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(getResources().getColor(R.color.colorPrimary));
            textView.setTextSize(30);
            if (dpWidth <= 320)
                textView.setTextSize(20);
            layout.addView(textView);

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
        viewPager.setPadding(Utils.convertDpToPx(46, this), 0, Utils.convertDpToPx(46, this), 0);
        viewPager.setPageMargin(Utils.convertDpToPx(16.5f, this));
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

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_intro);
//
//        myImageSwitcher = (ImageSwitcher) findViewById(R.id.intro_switcher);
//
//        myImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
//            @Override
//            public View makeView() {
//                ImageView switcherImageView = new ImageView(getApplicationContext());
//                switcherImageView.setLayoutParams(new ImageSwitcher.LayoutParams(
//                        ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT
//                ));
//                switcherImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                switcherImageView.setImageResource(R.drawable.cat1);
//                return switcherImageView;
//            }
//        });
//
//        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
//        Animation animationIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
//
//        myImageSwitcher.setOutAnimation(animationIn);
//        myImageSwitcher.setInAnimation(animationIn);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                initialX = event.getX();
//                break;
//            case MotionEvent.ACTION_UP:
//                float finalX = event.getX();
//                if (initialX > finalX) {
//                    counter++;
//                    nextImage();
//                }
//                else if (counter > 0) {
//                    counter--;
//                    myImageSwitcher.showPrevious();
//                }
//                break;
//        }
//        return false;
//    }
//
//    public void nextImage() {
//        if (counter >= imageCount)
//            counter = 0;
//        if (counter < 0)
//            counter = 0;
//        myImageSwitcher.setImageResource(imageSwitcherImages[counter]);
//    }
}
