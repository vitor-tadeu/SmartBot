package com.example.smartbot.menu.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.smartbot.R;

import java.util.Objects;

public class Settings extends Fragment {
    private Button mTour;
    private View view;

    public Settings() {
    }

    public static Settings newInstance() {
        return new Settings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        Objects.requireNonNull(getActivity()).setTitle("Configuração");
        init();
        tour();
        return view;
    }

    private void init() {
        mTour = view.findViewById(R.id.btnTour);
    }

    private void tour() {
        mTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dashboard dashboard = new Dashboard();
                Bundle bundle = new Bundle();
                bundle.putString("configuracao", "");
                dashboard.setArguments(bundle);
                FragmentTransaction fragmentTransaction1 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.frame, dashboard, "configuracao");
                fragmentTransaction1.commit();
            }
        });
    }
}