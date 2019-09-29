package com.example.smartbot.view.sensores;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.smartbot.BuildConfig;
import com.example.smartbot.R;
import com.example.smartbot.controller.sdl.Config;
import com.example.smartbot.controller.sdl.SdlReceiver;
import com.example.smartbot.controller.sdl.SdlService;
import com.example.smartbot.controller.sdl.TelematicsCollector;
import com.example.smartbot.controller.sdl.VehicleData;
import com.example.smartbot.menu.fragments.Nearby;

public class Combustivel extends AppCompatActivity {
    private static final String TAG = "Combustivel";
    private TextView mCombustivel, mMensagem;
    private FloatingActionButton mFAB;
    private String result, nivel;
    private Handler handler;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_combustivel);
        toolbar();
        SDL();
        init();
        fab();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_off) {
            if (Config.sdlServiceIsActive) {
                Toast.makeText(this, "Conexão SYNC: Conectado", Toast.LENGTH_SHORT).show();
                menu.getItem(0).setTitle("ON");
                if (Config.isSubscribing) {
                    TelematicsCollector.getInstance().setUnssubscribeVehicleData();
                    initThreadVerificaLeitura();
                } else {
                    TelematicsCollector.getInstance().setSubscribeVehicleData();
                }
            } else {
                Toast.makeText(this, "Conexão SYNC: Desconectado", Toast.LENGTH_SHORT).show();
                menu.getItem(0).setTitle("OFF");
            }
            return true;
        }
        finish();
        Animatoo.animateSlideRight(this);
        return super.onOptionsItemSelected(item);
    }

    private void toolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarCombustivel);
        toolbar.setNavigationIcon(R.drawable.menu_voltar);
        toolbar.setTitle("Combustível");
        setSupportActionBar(toolbar);
    }

    private void SDL() {
        //If we are connected to a module we want to start our SdlService
        if (BuildConfig.TRANSPORT.equals("MULTI") || BuildConfig.TRANSPORT.equals("MULTI_HB")) {
            SdlReceiver.queryForConnectedService(this);
        } else if (BuildConfig.TRANSPORT.equals("TCP")) {
            Intent proxyIntent = new Intent(this, SdlService.class);
            startService(proxyIntent);
        }
    }

    private void init() {
        mCombustivel = findViewById(R.id.txtSensorCombustivel);
        mMensagem = findViewById(R.id.txtNivelMsg);
        mFAB = findViewById(R.id.fabCombustivel);
    }

    @SuppressLint("SetTextI18n")
    private void initThreadVerificaLeitura() {
        handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    handler.post(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        public void run() {
                            result = String.valueOf((VehicleData.getInstance().getFuelLevel()));
                            nivel = result.substring(0, 2);
                            mCombustivel.setText(nivel + "%");
                            setMensagem();
                        }
                    });
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void setMensagem() {
        int value = Integer.valueOf(nivel);
        mMensagem.setVisibility(View.VISIBLE);
        try {
            if (value >= 80) {
                mMensagem.setText("O nível de combustível está alto!");
            } else if (value >= 60) {
                mMensagem.setText("O nível de combustível está razoável!");
            } else if (value >= 40) {
                mMensagem.setText("O nível de combustível está baixo!");
            } else if (value >= 20) {
                mMensagem.setText("Procure postos ao redor para abastecer!");
            } else mMensagem.setText("Abasteça para não ficar ser combustível!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fab() {
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                Nearby nearby = new Nearby();
                manager.beginTransaction().replace(R.id.container, nearby).commit();
                Bundle bundle = new Bundle();
                bundle.putString("Combustivel", "");
                nearby.setArguments(bundle);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }
}