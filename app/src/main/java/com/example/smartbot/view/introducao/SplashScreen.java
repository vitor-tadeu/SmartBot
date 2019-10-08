package com.example.smartbot.view.introducao;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.smartbot.controller.utils.Constants;
import com.example.smartbot.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        GPSPermission();
    }

    public void GPSPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_ACCESS_FINE_LOCATION);
            Log.i(TAG, "Solicita permissao de acesso ao GPS");
        } else {
            if (checkGoogleServices()) {
                intro();
            }
            Log.i(TAG, "Acesso ao GPS permitido");
        }
    }

    private boolean checkGoogleServices() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (available == ConnectionResult.SUCCESS) {
            Log.i(TAG, "Google Play Services atualizado");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.i(TAG, "Google Play Services desatualizado");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, Constants.ERROR_DIALOG_REQUEST);
            dialog.show();
            return false;
        } else {
            Toast.makeText(this, "Atualize o Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void intro() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this, IntroViewPager.class));
                Animatoo.animateSplit(SplashScreen.this);
                finish();
            }
        }, 3000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkGoogleServices()) {
                    intro();
                }
                Log.i(TAG, "Permissao aceita");
            } else {
                GPSPermission();
                Log.i(TAG, "Permissao negada");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_ENABLE_GPS) {
            if (resultCode == RESULT_OK) {
                intro();
                Log.i(TAG, "GPS ativado");
            }
        }
    }
}