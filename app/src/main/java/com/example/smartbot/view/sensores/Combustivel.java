package com.example.smartbot.view.sensores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.smartbot.BuildConfig;
import com.example.smartbot.R;
import com.example.smartbot.controller.adapter.OnItemClickListener;
import com.example.smartbot.controller.adapter.PlacesViewAdapter;
import com.example.smartbot.controller.sdl.Config;
import com.example.smartbot.controller.sdl.SdlReceiver;
import com.example.smartbot.controller.sdl.SdlService;
import com.example.smartbot.controller.sdl.TelematicsCollector;
import com.example.smartbot.controller.sdl.VehicleData;
import com.example.smartbot.controller.service.APIClientPlaces;
import com.example.smartbot.controller.service.APIServicePlaces;
import com.example.smartbot.controller.utils.Constants;
import com.example.smartbot.model.DistanceMatrixAPI;
import com.example.smartbot.model.Places;
import com.example.smartbot.model.PlacesAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Combustivel extends AppCompatActivity implements OnItemClickListener {
    private static final String TAG = "Combustivel";
    private TextView mCombustivel, mMensagem;
    private FloatingActionButton mFAB;
    private String result, nivel, mCoordenadas;
    private Handler handler;
    private Menu menu;

    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;

    private APIServicePlaces mServicePlaces;
    private List<Places> mPlaces;
    private List<PlacesAPI.Response> mPlacesResponses;
    private PlacesViewAdapter mAdapterPlaces;

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

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPlaces = new ArrayList<>();
        mServicePlaces = APIClientPlaces.getClient().create(APIServicePlaces.class);
    }

    private void GPSOnOff() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "GPS desativado");
            openGPSSettings();
        } else getCoordenadas();
        Log.i(TAG, "GPS ativado");
    }

    private void openGPSSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Desativado");
        builder.setMessage("O GPS não está ativado, deseja ir para as configurações?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, Constants.REQUEST_ENABLE_GPS);
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void getCoordenadas() {
        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        mCoordenadas = location.getLatitude() + "," + location.getLongitude();
                        Log.i(TAG, "Coordenadas: " + mCoordenadas);
                        responseAPIPlaces();
                    }
                });
    }

    private void responseAPIPlaces() {
        try {
            if (!mCoordenadas.isEmpty()) {
                loading();
                requestAPIPlaces();
            } else {
                GPSOnOff();
                Log.i(TAG, "GPS nao encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loading() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Buscando postos...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    public void requestAPIPlaces() {
        Call<PlacesAPI.Entrada> call = mServicePlaces.getPlaces(mCoordenadas, "50000", "gas_station", true, "gas_station", Constants.API_KEY);
        call.enqueue(new Callback<PlacesAPI.Entrada>() {
            @Override
            public void onResponse(@NonNull Call<PlacesAPI.Entrada> call, @NonNull Response<PlacesAPI.Entrada> response) {
                Log.i(TAG, "URL Place: " + response);
                PlacesAPI.Entrada entrada = response.body();
                assert entrada != null;
                mPlacesResponses = entrada.response;
                if (response.isSuccessful()) {
                    if (entrada.status.equals("OK")) {
                        mPlaces.clear();
                        insertPlaces();
                        mProgressDialog.dismiss();
                    } else if (entrada.status.equals("ZERO_RESULTS")) {
                        removePlaces();
                    }
                } else Log.i(TAG, "Resposta sem sucesso places");
                mProgressDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<PlacesAPI.Entrada> call, @NonNull Throwable t) {
                Log.i(TAG, t.getMessage());
            }
        });
    }

    private void insertPlaces() {
        for (int i = 0; i < mPlacesResponses.size(); i++) {
            PlacesAPI.Response retorno = mPlacesResponses.get(i);
            requestDistancia(retorno);
        }
    }

    private void removePlaces() {
        for (int i = 0; i >= mPlacesResponses.size(); i--) {
            if (!mPlaces.isEmpty()) {
                mPlaces.removeAll(mPlaces);
                mAdapterPlaces.notifyItemRemoved(i);
                mRecyclerView.setAdapter(mAdapterPlaces);
            }
            Log.i(TAG, "Nenhum hotel encontrado");
        }
    }

    public void requestDistancia(final PlacesAPI.Response retorno) {
        Call<DistanceMatrixAPI> call = mServicePlaces.getDistance(mCoordenadas, retorno.geometry.location.lat + "," + retorno.geometry.location.lng, Constants.API_KEY);
        call.enqueue(new Callback<DistanceMatrixAPI>() {
            @Override
            public void onResponse(@NonNull Call<DistanceMatrixAPI> call, @NonNull Response<DistanceMatrixAPI> response) {
//                Log.i(TAG, "URL DistanceMatrixAPI: " + response);
                DistanceMatrixAPI distanceMatrixAPI = response.body();
                assert distanceMatrixAPI != null;
                if (response.isSuccessful()) {
                    if (distanceMatrixAPI.status.equals("OK")) {
                        DistanceMatrixAPI.Elements elements = distanceMatrixAPI.rows.get(0);
                        DistanceMatrixAPI.Elements.Resposta resposta = elements.elements.get(0);
                        if (resposta.status.equals("OK")) {
                            DistanceMatrixAPI.Elements.ValueItem getDistancia = resposta.distance;
                            DistanceMatrixAPI.Elements.ValueItem getTempo = resposta.duration;

                            String Distancia = String.valueOf(getDistancia.text);
                            String Tempo = String.valueOf(getTempo.text);

                            mPlaces.add(new com.example.smartbot.model.Places(retorno.name, retorno.vicinity, retorno.geometry.location.lat, retorno.geometry.location.lng, retorno.rating, Distancia, Tempo));
                            if (mPlaces.size() == mPlacesResponses.size()) {
                                mAdapterPlaces = new PlacesViewAdapter(Combustivel.this, mPlacesResponses, mPlaces, Combustivel.this);
                                mRecyclerView.setAdapter(mAdapterPlaces);
                                Collections.sort(mPlaces, Places.ORDEM_DISTANCIA_CURTA);
                            }
                            mProgressDialog.dismiss();
                        }
                    }
                } else Log.i(TAG, "Resposta sem sucesso distancia");
            }

            @Override
            public void onFailure(@NonNull Call<DistanceMatrixAPI> call, @NonNull Throwable t) {
                Log.i(TAG, t.getMessage());
            }
        });
    }

    @Override
    public void OnItemClick(int posicao) {
        String coordenadas = mAdapterPlaces.mPlaces.get(posicao).getLat() + "," + mAdapterPlaces.mPlaces.get(posicao).getLng();
        Log.i(TAG, "Coordenada enviada ao Google Maps: " + coordenadas);
        openGoogleMaps(coordenadas);
    }

    private void openGoogleMaps(String coordenadas) {
        Uri uri = Uri.parse("google.navigation:q=" + coordenadas);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        } catch (NullPointerException e) {
            Log.i(TAG, "Não foi possivel abrir o mapa" + e.getMessage());
        }
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
                GPSOnOff();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_ENABLE_GPS) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "GPS ativado");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }
}