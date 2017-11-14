package com.varteq.catslovers.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.varteq.catslovers.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends AppCompatActivity {

    float initialX;
    private ImageSwitcher myImageSwitcher;
    private int counter = 0;
    private int imageSwitcherImages[] = {R.drawable.cat1, R.drawable.cat2, R.drawable.cat3};
    private MyViewPagerAdapter myViewPagerAdapter;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    private List<View> layouts = new ArrayList<>();
    private TextView[] dots;
    @BindView(R.id.dots_layout)
    LinearLayout dotsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        ButterKnife.bind(this);

        for (int i = 0; i < imageSwitcherImages.length; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT
            ));
            imageView.setImageResource(imageSwitcherImages[i]);
            layouts.add(imageView);
        }

        dots = new TextView[imageSwitcherImages.length];
        for (int i = 0; i < imageSwitcherImages.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(30);
            dotsLayout.addView(dots[i]);
        }

        selectBottomDot(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        viewPager.setOffscreenPageLimit(2);
    }

    @OnClick(R.id.go_to_login_button)
    void goToLogin() {
        startActivity(new Intent(IntroActivity.this, LoginActivity.class));
    }

    private void selectBottomDot(int currentPage) {
        for (int i = 0; i < imageSwitcherImages.length; i++) {
            dots[i].setTextColor(getResources().getColor(R.color.colorPrimaryLight));
        }
        dots[currentPage].setTextColor(getResources().getColor(R.color.white));
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            selectBottomDot(position);
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
