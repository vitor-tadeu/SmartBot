package com.example.smartbot.view.sensores;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class OleoMotor extends AppCompatActivity {
    private static final String TAG = "OleoMotor";
    private TextView mOleoMotor;
    private Handler handler;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_oleo_motor);
        toolbar();
        SDL();
        init();
        Button button = findViewById(R.id.bun);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initThreadVerificaLeitura();
            }
        });
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
        Toolbar toolbar = findViewById(R.id.toolbarOleoMotor);
        toolbar.setNavigationIcon(R.drawable.menu_voltar);
        toolbar.setTitle("Nível do Óleo");
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
        mOleoMotor = findViewById(R.id.txtSensorOleoMotor);
    }

    private void initThreadVerificaLeitura() {
        handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    handler.post(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        public void run() {
                            mOleoMotor.setText("pedal do freio: " + (VehicleData.getInstance().getDriverBraking())
                                    + "\n" + "Evento de emergência: " + (VehicleData.getInstance().getEmergencyEvent())
                                    + "\n" + "Oleo do motor: " + (VehicleData.getInstance().getEngineOilLife())
                                    + "\n" + "Torque do motor: " + (VehicleData.getInstance().getEngineTorque())
                                    + "\n" + "Status da lâmpada: " + (VehicleData.getInstance().getHeadLampStatus())
                                    + "\n" + "Consumo instantâneo de combustível: " + (VehicleData.getInstance().getInstantFuelConsumption())
                                    + "\n" + "Odômetro: " + (VehicleData.getInstance().getOdometer())
                                    + "\n" + "Ângulo do volante: " + (VehicleData.getInstance().getSteeringWheelAngle())
                                    + "\n" + "Pressão do pneu: " + (VehicleData.getInstance().getTirePressure())
                                    + "\n" + "Status do pisca-pisca: " + (VehicleData.getInstance().getTurnSignal()));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }
}