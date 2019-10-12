package com.example.smartbot.menu.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartbot.R;
import com.example.smartbot.controller.utils.Constants;

import java.util.Objects;

public class Settings extends Fragment {
    private Button mTour, mUUID;
    private TextView mResposta;
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
        getUUID();
        return view;
    }

    private void init() {
        mTour = view.findViewById(R.id.btnTour);
        mUUID = view.findViewById(R.id.btnUUID);
        mResposta = view.findViewById(R.id.txtUUID);
    }

    private void tour() {
        mTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dashboard dashboard = new Dashboard();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.CONFIGURACOES, "");
                dashboard.setArguments(bundle);
                FragmentTransaction fragmentTransaction1 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.frame, dashboard);
                fragmentTransaction1.commit();
            }
        });
    }

    private void getUUID() {
        mUUID.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HardwareIds")
            @Override
            public void onClick(View v) {
                TelephonyManager tManager = (TelephonyManager) Objects.requireNonNull(getActivity()).getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mResposta.setText(tManager.getDeviceId());
            }
        });
    }
}