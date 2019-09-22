package com.example.smartbot.view.introducao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.smartbot.controller.utils.Constants;
import com.example.smartbot.R;
import com.example.smartbot.menu.Menu;

public class IntroViewPager extends AppCompatActivity {
    private static final String TAG = "IntroViewPager";

    private Button btnProximo, btnPular;
    private LinearLayout dotsLayout;
    private ViewPager viewPager;
    private int[] layouts;
    private boolean firstStart;

    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_view_pager);
        setLayouts();
        init();
        addBottomDots(0);
        pular();
        proximo();
        checkPreferences();
    }

    private void setLayouts() {
        layouts = new int[]{
                R.layout.intro_slide1,
                R.layout.intro_slide2,
                R.layout.intro_slide3,
                R.layout.intro_slide4,
                R.layout.intro_slide5,
                R.layout.intro_slide6};
    }

    @SuppressLint("CommitPrefEdits")
    private void init() {
        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnPular = findViewById(R.id.btnPular);
        btnProximo = findViewById(R.id.btnProximo);

        ViewPagerAdapter adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        SharedPreferences mPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
        mEditor = mPreferences.edit();
        firstStart = mPreferences.getBoolean(Constants.INTRO_FIRST, true);
    }

    private void getPreferences() {
        mEditor.putBoolean(Constants.INTRO_FIRST, false);
        mEditor.apply();
    }

    private void checkPreferences() {
        if (firstStart) {
            getPreferences();
        } else {
            startActivity(new Intent(IntroViewPager.this, Menu.class));
            Animatoo.animateSplit(IntroViewPager.this);
            finish();
        }
    }

    public void pular() {
        btnPular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });
    }

    public void proximo() {
        btnProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem();
                if (current < layouts.length) {
                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            if (position == layouts.length - 1) {
                btnProximo.setText(getString(R.string.intro_comecar));
                btnPular.setVisibility(View.GONE);
            } else {
                btnProximo.setText(getString(R.string.intro_proximo));
                btnPular.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void addBottomDots(int currentPage) {
        TextView[] dots = new TextView[layouts.length];
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.dot_inactive));
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(getResources().getColor(R.color.dot_active));
    }

    private int getItem() {
        return viewPager.getCurrentItem() + 1;
    }

    private void launchHomeScreen() {
        startActivity(new Intent(this, com.example.smartbot.menu.Menu.class));
        Animatoo.animateSplit(IntroViewPager.this);
        finish();
    }

    public class ViewPagerAdapter extends PagerAdapter {
        ViewPagerAdapter() {
        }

        @Override
        public @NonNull
        Object instantiateItem(@NonNull ViewGroup viewGroup, int position) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(layouts[position], viewGroup, false);
            viewGroup.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}