package com.example.smartbot.view.sensores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.smartbot.R;
import com.example.smartbot.controller.adapter.OnItemClickListener;
import com.example.smartbot.controller.adapter.PlacesViewAdapter;
import com.example.smartbot.controller.service.APIClientPlaces;
import com.example.smartbot.controller.service.APIServicePlaces;
import com.example.smartbot.controller.utils.Constants;
import com.example.smartbot.model.DistanceMatrixAPI;
import com.example.smartbot.model.Places;
import com.example.smartbot.model.PlacesAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Integer.valueOf;

public class Cloud extends AppCompatActivity implements OnItemClickListener {
    private static final String TAG = "Cloud";
    private String mCoordenadas, mRaio, mType, mName;

    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;

    private APIServicePlaces mServicePlaces;
    private List<Places> mPlaces;
    private List<PlacesAPI.Response> mPlacesResponses;
    private PlacesViewAdapter mAdapterPlaces;

    private RequestQueue mQueue;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_cloud);
        toolbar();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestAPIMySQL();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_raio) {
            alertDialogRaio();
            return true;
        } else if (itemId == R.id.menu_filtro) {
            alertDialogFiltro();
            return true;
        }
        finish();
        Animatoo.animateSlideRight(this);
        return super.onOptionsItemSelected(item);
    }

    private void toolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarCloud);
        toolbar.setNavigationIcon(R.drawable.menu_voltar);
        toolbar.setTitle("Hotéis Próximos");
        setSupportActionBar(toolbar);
    }

    private void init() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPreferences = Objects.requireNonNull(this).getSharedPreferences("Preferences", this.MODE_PRIVATE);
        mEditor = mPreferences.edit();

        mPlaces = new ArrayList<>();
        mServicePlaces = APIClientPlaces.getClient().create(APIServicePlaces.class);

        mQueue = Volley.newRequestQueue(this);

        mType = "hotel";
        mName = "hotel";
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

    private void alertDialogRaio() {
        final String[] array = getResources().getStringArray(R.array.raio);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_raio, null);
        builder.setCustomTitle(view);
        String position = mPreferences.getString(Constants.POSITION_RAIO, "");
        Log.i(TAG, "Posicao Raio: " + mPreferences.getString(Constants.POSITION_RAIO, ""));
        assert position != null;
        int checkedItem = Integer.parseInt(position);
        builder.setSingleChoiceItems(array, checkedItem, new DialogInterface.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(DialogInterface dialog, int position) {
                getRaioPosition(position, array[position]);
                converteKM();
                responseAPIPlaces();
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setRaio() {
        mRaio = mPreferences.getString(Constants.RAIO, "");
        Log.i(TAG, "Raio enviado a API: " + mRaio);
    }

    private void alertDialogFiltro() {
        final String[] array = getResources().getStringArray(R.array.filtro);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_filtro, null);
        builder.setCustomTitle(view);
        String position = mPreferences.getString(Constants.POSITION_FILTRO, "");
        Log.i(TAG, "Posicao Filtro: " + mPreferences.getString(Constants.POSITION_FILTRO, ""));
        assert position != null;
        int checkedItem = Integer.parseInt(position);
        builder.setSingleChoiceItems(array, checkedItem, new DialogInterface.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(DialogInterface dialog, int position) {
                getFiltroPosition(position, array[position]);
                if (!mPlaces.isEmpty()) {
                    setFiltro();
                    mAdapterPlaces.notifyDataSetChanged();
                    mRecyclerView.setAdapter(mAdapterPlaces);
                }
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getRaioPosition(int position, String value) {
        mPreferences.edit().putString(Constants.RAIO, value).apply();
        String strPosition = String.valueOf(position);
        mPreferences.edit().putString(Constants.POSITION_RAIO, strPosition).apply();
    }

    private void converteKM() {
        if (Objects.equals(mPreferences.getString(Constants.RAIO, ""), "1 km")
                || Objects.equals(mPreferences.getString(Constants.RAIO, ""), "2 km")
                || Objects.equals(mPreferences.getString(Constants.RAIO, ""), "5 km")) {
            String strRaio = String.valueOf(valueOf(Objects.requireNonNull(mPreferences.getString(Constants.RAIO, "")).substring(0, 1)) * 1000);
            mPreferences.edit().putString(Constants.RAIO, strRaio).apply();
        } else {
            String strRaio = String.valueOf(valueOf(Objects.requireNonNull(mPreferences.getString(Constants.RAIO, "")).substring(0, 2)) * 1000);
            mPreferences.edit().putString(Constants.RAIO, strRaio).apply();
        }
        Log.i(TAG, "Preferences Raio: " + mPreferences.getString(Constants.RAIO, "") + "m");
    }

    private void getFiltroPosition(int position, String value) {
        mPreferences.edit().putString(Constants.FILTRO, value).apply();
        String strPosition = String.valueOf(position);
        mPreferences.edit().putString(Constants.POSITION_FILTRO, strPosition).apply();
    }

    private void setFiltro() {
        switch (Objects.requireNonNull(mPreferences.getString(Constants.FILTRO, ""))) {
            case "Ordem alfabética": {
                String strRaio = mPreferences.getString(Constants.FILTRO, "");
                mPreferences.edit().putString(Constants.FILTRO, strRaio).apply();
                Collections.sort(mPlaces, Places.ORDEM_ALFABETICA_CRESCENTE);
                break;
            }
            case "Distância mais curta": {
                String strRaio = mPreferences.getString(Constants.FILTRO, "");
                mPreferences.edit().putString(Constants.FILTRO, strRaio).apply();
                Collections.sort(mPlaces, Places.ORDEM_DISTANCIA_CURTA);
                break;
            }
            case "Tempo mais curto": {
                String strRaio = mPreferences.getString(Constants.FILTRO, "");
                mPreferences.edit().putString(Constants.FILTRO, strRaio).apply();
                Collections.sort(mPlaces, Places.ORDEM_TEMPO_CURTO);
                break;
            }
            case "Maior avaliação": {
                String strRaio = mPreferences.getString(Constants.FILTRO, "");
                mPreferences.edit().putString(Constants.FILTRO, strRaio).apply();
                Collections.sort(mPlaces, Places.OREDEM_MAIOR_AVALIACAO);
                break;
            }
        }
        Log.i(TAG, "Preferences Filtro: " + mPreferences.getString(Constants.FILTRO, ""));
    }

    private void responseAPIPlaces() {
        try {
            if (!mCoordenadas.isEmpty()) {
                setRaio();
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
        mProgressDialog.setTitle("Buscando hotéis...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    public void requestAPIPlaces() {
        Call<PlacesAPI.Entrada> call = mServicePlaces.getPlaces(mCoordenadas, mRaio, mType, true, mName, Constants.API_KEY);
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

                            mPlaces.add(new Places(retorno.name, retorno.vicinity, retorno.geometry.location.lat, retorno.geometry.location.lng, retorno.rating, Distancia, Tempo));
                            if (mPlaces.size() == mPlacesResponses.size()) {
                                mAdapterPlaces = new PlacesViewAdapter(Cloud.this, mPlacesResponses, mPlaces, Cloud.this);
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

    public void requestAPIMySQL() {
        final String url = "http://api.limogrid.com/API/AndroidAPI/teste.cfc?method=buscaStatusDois&fbclid=IwAR0ZKSEvl5QVVFvGDELBbcCJ7aHlA9e2P78HYBMxq7j0JkE5_OEg5DMwYb0";
        final String urlUpdate = "http://api.limogrid.com/API/AndroidAPI/teste.cfc?method=updateStatusDois&fbclid=IwAR0yxsoGqE7_K10qtDWvAxIiKE0_6a0Mj4QiCvvAq6ukgWxbPrjDFjYx0Uw";

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            new com.android.volley.Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i(TAG, "response: " + response);
                                    try {
                                        JSONObject jsonResponse = new JSONObject(String.valueOf(response));
                                        String status = jsonResponse.getString("STATUS");
                                        if (status.equals("true")) {
                                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlUpdate, null,
                                                    new com.android.volley.Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                        }
                                                    }, new com.android.volley.Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    error.printStackTrace();
                                                    Log.i(TAG, "error update: " + error);
                                                }
                                            });
                                            mQueue.add(request);

                                            Log.i(TAG, "status: " + status);
                                            GPSOnOff();

                                            delay();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.i(TAG, "error: " + error);
                        }
                    });
                    mQueue.add(request);
                }
            }
        }).start();
    }

    private void delay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 1000);
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