package com.example.smartbot.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.smartbot.R;
import com.example.smartbot.menu.fragments.Dashboard;
import com.example.smartbot.menu.fragments.Nearby;
import com.example.smartbot.menu.fragments.Settings;

public class Menu extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        BottomNavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(this);
        dashboard();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dashboard:
                dashboard();
                break;
            case R.id.menu_places:
                fragment = Nearby.newInstance();
                openFragment(fragment);
                break;
            case R.id.menu_settings:
                fragment = Settings.newInstance();
                openFragment(fragment);
                break;
        }
        return true;
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    private void dashboard() {
        fragment = Dashboard.newInstance();
        openFragment(fragment);
    }
}