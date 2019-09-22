package com.example.smartbot.view.sensores;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.smartbot.BuildConfig;
import com.example.smartbot.R;
import com.example.smartbot.controller.sdl.Config;
import com.example.smartbot.controller.sdl.HMIScreenManager;
import com.example.smartbot.controller.sdl.SdlReceiver;
import com.example.smartbot.controller.sdl.SdlService;
import com.example.smartbot.controller.sdl.TelematicsCollector;
import com.example.smartbot.controller.sdl.VehicleData;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.enums.Result;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCResponseListener;

public class TemperaturaExterna extends AppCompatActivity {
    private static final String TAG = "Temperatura";
    private TextView mTemperatura;
    private Handler handler;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_temperatura_externa);
        toolbar();
        SDL();
        init();
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
                if (Config.isSubscribing) {
                    TelematicsCollector.getInstance().setUnssubscribeVehicleData();
                    menu.getItem(0).setTitle("ON");
                    initThreadVerificaLeitura();
                } else {
                    TelematicsCollector.getInstance().setSubscribeVehicleData();
                    menu.getItem(0).setTitle("OFF");
                }
            } else {
                Toast.makeText(this, "Conexão SYNC: Desconectado", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        finish();
        Animatoo.animateSlideRight(this);
        return super.onOptionsItemSelected(item);
    }

    private void toolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarTemperaturaExterna);
        toolbar.setNavigationIcon(R.drawable.menu_voltar);
        toolbar.setTitle("Temperatura Externa");
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
        mTemperatura = findViewById(R.id.txtSensorTemperaturaExterna);
    }

    private void getSensor() {
        if (Config.sdlServiceIsActive) {
            TelematicsCollector.getInstance().getVehicleData(new OnRPCResponseListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(int correlationId, RPCResponse response) {
                    Log.i(TAG, "Houve uma resposta RPC!");
                    if (response.getSuccess()) {
                        Double pedalPosition = ((GetVehicleDataResponse) response).getAccPedalPosition();
                        HMIScreenManager.getInstance().showAlert("Pedal Position status: " + pedalPosition.toString());
                        String s = pedalPosition.toString();

                        VehicleData data = new VehicleData();
                        data.setAccPedalPosition(s);
                        mTemperatura.setText("Pedal Position: " + data.getAccPedalPosition());
                    } else {
                        Log.i(TAG, "GetVehicleData was rejected.");
                    }
                }

                @Override
                public void onError(int correlationId, Result resultCode, String info) {
                    Log.e(TAG, "onError: " + resultCode + " | Info: " + info);
                }
            });
        } else {
            Toast.makeText(TemperaturaExterna.this, "Conexão com o SYNC não foi estabelecida", Toast.LENGTH_SHORT).show();
        }
    }

    private void initThreadVerificaLeitura() {
        handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    handler.post(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        public void run() {
                            mTemperatura.setText("Temperatura Externa: " + (VehicleData.getInstance().getExternalTemperature()));
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