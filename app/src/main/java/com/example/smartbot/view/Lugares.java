package com.example.smartbot.view;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartbot.R;

public class Lugares extends BottomSheetDialogFragment {
    private BottomSheetListener mSheetListener;

    public Lugares() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_lugares, container, false);
        FloatingActionButton fab1 = view.findViewById(R.id.fab_gas_station);
        FloatingActionButton fab2 = view.findViewById(R.id.fab_parking);
        FloatingActionButton fab3 = view.findViewById(R.id.fab_mecanica);
        FloatingActionButton fab4 = view.findViewById(R.id.fab_hospital);
        FloatingActionButton fab5 = view.findViewById(R.id.fab_restaurante);
        FloatingActionButton fab6 = view.findViewById(R.id.fab_cafe);

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSheetListener != null) {
                    mSheetListener.onOptionClick(R.id.fab_gas_station);
                    dismiss();
                }
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSheetListener != null) {
                    mSheetListener.onOptionClick(R.id.fab_parking);
                    dismiss();
                }
            }
        });

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSheetListener != null) {
                    mSheetListener.onOptionClick(R.id.fab_mecanica);
                    dismiss();
                }
            }
        });

        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSheetListener != null) {
                    mSheetListener.onOptionClick(R.id.fab_hospital);
                    dismiss();
                }
            }
        });

        fab5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSheetListener != null) {
                    mSheetListener.onOptionClick(R.id.fab_restaurante);
                    dismiss();
                }
            }
        });

        fab6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSheetListener != null) {
                    mSheetListener.onOptionClick(R.id.fab_cafe);
                    dismiss();
                }
            }
        });
        return view;
    }

    public void setSheetListener(BottomSheetListener listener) {
        this.mSheetListener = listener;
    }

    public interface BottomSheetListener {
        void onOptionClick(int id);
    }
}