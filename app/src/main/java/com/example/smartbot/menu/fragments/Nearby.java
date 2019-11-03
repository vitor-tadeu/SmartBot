package com.example.smartbot.menu.fragments;

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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smartbot.R;
import com.example.smartbot.controller.adapter.OnItemClickListener;
import com.example.smartbot.controller.adapter.PlacesViewAdapter;
import com.example.smartbot.controller.service.APIClientPlaces;
import com.example.smartbot.controller.service.APIServicePlaces;
import com.example.smartbot.controller.utils.Constants;
import com.example.smartbot.model.DistanceMatrixAPI;
import com.example.smartbot.model.Places;
import com.example.smartbot.model.PlacesAPI;
import com.example.smartbot.view.Lugares;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static java.lang.Integer.valueOf;

public class Nearby extends Fragment implements OnItemClickListener, Lugares.BottomSheetListener {
    private static final String TAG = "Nearby";

    public String mCoordenadas, mRaio, mLugar, mFiltro, mType, mName, mProgress;
    private FloatingActionButton mFAB;
    private TextView mErro;
    private boolean firstStart;

    private View view;

    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;

    private APIServicePlaces mServicePlaces;
    private List<com.example.smartbot.model.Places> mPlaces;
    private List<PlacesAPI.Response> mPlacesResponses;
    private PlacesViewAdapter mAdapterPlaces;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private ShowcaseView.Builder showcaseView;

    public Nearby() {
    }

    public static Nearby newInstance() {
        return new Nearby();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nearby, container, false);
        Objects.requireNonNull(getActivity()).setTitle("Hotéis Próximos");
        setHasOptionsMenu(true);
        init();
        GPSOnOff();
        checkPreferences();
        fab();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("CommitPrefEdits")
    private void init() {
        mErro = view.findViewById(R.id.txtErro);
        mFAB = view.findViewById(R.id.fabPlaces);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPlaces = new ArrayList<>();
        mServicePlaces = APIClientPlaces.getClient().create(APIServicePlaces.class);

        mPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences("Preferences", getActivity().MODE_PRIVATE);
        mEditor = mPreferences.edit();

        firstStart = mPreferences.getBoolean(Constants.APRESENTACAO_2, true);

        mType = "hotel";
        mName = "hotel";
        mProgress = "Buscando hotéis...";
    }

    private void GPSOnOff() {
        LocationManager manager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            openGPSSettings();
            Log.i(TAG, "GPS desativado");
        } else getCoordenadas();
        Log.i(TAG, "GPS ativado");
    }

    private void apresentacao1() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            bundle.getString(Constants.APRESENTACAO);
            showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                    .setTarget(new ViewTarget(mFAB))
                    .setStyle(R.style.ShowCase1)
                    .blockAllTouches()
                    .setContentTitle("Lugares")
                    .setContentText("Encontre os melhores lugares próximos" + "\n" + "a sua localização atual.")
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                            apresentacao2();
                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
                        }

                        @Override
                        public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {
                        }
                    });
            showcaseView.build();
        }
    }

    private void apresentacao2() {
        showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                .setTarget(new ViewTarget(mRecyclerView))
                .setStyle(R.style.ShowCase2)
                .blockAllTouches()
                .setContentTitle("Buscas")
                .setContentText("Clique em um card para enviar" + "\n" + "a rota ao Google Maps.")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        Dashboard dashboard = new Dashboard();
                        FragmentTransaction fragmentTransaction1 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                        fragmentTransaction1.replace(R.id.frame, dashboard, "apresentacao");
                        fragmentTransaction1.commit();
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {
                    }
                });
        showcaseView.build();
    }

    private void tour1() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            bundle.getString(Constants.TOUR);
            showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                    .setTarget(new ViewTarget(mFAB))
                    .setStyle(R.style.ShowCase1)
                    .blockAllTouches()
                    .setContentTitle("Lugares")
                    .setContentText("Encontre os melhores lugares próximos" + "\n" + "a sua localização atual.")
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                            tour2();
                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
                        }

                        @Override
                        public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {
                        }
                    });
            showcaseView.build();
        }
    }

    private void tour2() {
        showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                .setTarget(new ViewTarget(mRecyclerView))
                .setStyle(R.style.ShowCase2)
                .blockAllTouches()
                .setContentTitle("Lugares")
                .setContentText("Clique em um card para enviar" + "\n" + "a rota ao Google Maps.")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        Settings settings = new Settings();
                        FragmentTransaction fragmentTransaction1 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                        fragmentTransaction1.replace(R.id.frame, settings, "tour");
                        fragmentTransaction1.commit();
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {
                    }
                });
        showcaseView.build();
    }

    private void openGPSSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void checkPreferences() {
//        mPreferences.edit().clear().commit();
        if (!mPreferences.contains(Constants.RAIO)) {
            mEditor.putString(Constants.RAIO, "10000");
            mEditor.apply();
        }
        if (!mPreferences.contains(Constants.POSITION_RAIO)) {
            mEditor.putString(Constants.POSITION_RAIO, "3");
            mEditor.apply();
        }
        if (!mPreferences.contains(Constants.FILTRO)) {
            mEditor.putString(Constants.FILTRO, "Distância mais curta");
            mEditor.apply();
        }
        if (!mPreferences.contains(Constants.POSITION_FILTRO)) {
            mEditor.putString(Constants.POSITION_FILTRO, "1");
            mEditor.apply();
        }
    }

    private void checkTour() {
        if (firstStart) {
            mEditor.putBoolean(Constants.APRESENTACAO_2, false);
            mEditor.apply();
            apresentacao1();
        } else {
            //tour1();
        }
    }

    private void getCoordenadas() {
        SmartLocation.with(getActivity()).location()
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
                setRaio();
                setPlaces();
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

    private void setRaio() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mRaio = bundle.getString(Constants.RAIO_ASSISTENTE);
            Log.i(TAG, "Raio assistente: " + mRaio);
        } else {
            mRaio = mPreferences.getString(Constants.RAIO, "");
            Log.i(TAG, "Raio enviado a API: " + mRaio);
        }
    }

    private void alertDialogFiltro() {
        final String[] array = getResources().getStringArray(R.array.filtro);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void setFiltroAssistente() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mFiltro = bundle.getString(Constants.FILTRO_ASSISTENTE);
            Log.i(TAG, "Filtro assistente: " + mFiltro);
            switch (mFiltro) {
                case "Ordem alfabética":
                    Collections.sort(mPlaces, Places.ORDEM_ALFABETICA_CRESCENTE);
                    break;
                case "Distância mais curta":
                    Collections.sort(mPlaces, Places.ORDEM_DISTANCIA_CURTA);
                    break;
                case "Tempo mais curto":
                    Collections.sort(mPlaces, Places.ORDEM_TEMPO_CURTO);
                    break;
                case "Maior avaliação":
                    Collections.sort(mPlaces, Places.OREDEM_MAIOR_AVALIACAO);
                    break;
            }
        }
    }

    private void setPlaces() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mLugar = bundle.getString(Constants.PLACE_ASSISTENTE);
            Log.i(TAG, "Lugar assistente: " + mLugar);
            switch (mLugar) {
                case "hotel":
                    mType = "hotel";
                    mName = "hotel";
                    mProgress = "Buscando hotéis...";
                    mErro.setText("Nenhum hotel encontrado aberto, tente aumentar o raio.");
                    Objects.requireNonNull(getActivity()).setTitle("Hotéis Próximos");
                    break;
                case "posto de gasolina":
                    mType = "gas_station";
                    mName = "gas_station";
                    mProgress = "Buscando postos...";
                    mErro.setText("Nenhum posto encontrado aberto, tente aumentar o raio.");
                    Objects.requireNonNull(getActivity()).setTitle("Postos de Gasolina");
                    break;
                case "estacionamento":
                    mType = "parking";
                    mName = "parking";
                    mProgress = "Buscando estacionamentos...";
                    mErro.setText("Nenhum estacionamento encontrado aberto, tente aumentar o raio.");
                    Objects.requireNonNull(getActivity()).setTitle("Estacionamentos");
                    break;
                case "mecânica":
                    mType = "car_repair";
                    mName = "car_repair";
                    mProgress = "Buscando mecânicas...";
                    mErro.setText("Nenhuma mecânica encontrada aberta, tente aumentar o raio.");
                    Objects.requireNonNull(getActivity()).setTitle("Mecânicas");
                    break;
                case "hospital":
                    mType = "hospital";
                    mName = "hospital";
                    mProgress = "Buscando hospitais...";
                    mErro.setText("Nenhum hospital encontrado aberto, tente aumentar o raio.");
                    Objects.requireNonNull(getActivity()).setTitle("Hospitais");
                    break;
                case "restaurante":
                    mType = "restaurant";
                    mName = "restaurant";
                    mProgress = "Buscando restaurantes...";
                    mErro.setText("Nenhum restaurante encontrado aberto, tente aumentar o raio.");
                    Objects.requireNonNull(getActivity()).setTitle("Restaurantes");
                    break;
                case "cafeteria":
                    mType = "cafe";
                    mName = "cafe";
                    mProgress = "Buscando cafeterias...";
                    mErro.setText("Nenhuma cafeteria encontrada aberta, tente aumentar o raio.");
                    Objects.requireNonNull(getActivity()).setTitle("Cafeterias");
                    break;
            }
        }
    }

    private void alertDialogRaio() {
        final String[] array = getResources().getStringArray(R.array.raio);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void fab() {
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lugares lugares = new Lugares();
                lugares.setSheetListener(Nearby.this);
                lugares.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "Lugares");
            }
        });
    }

    private void loading() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(mProgress);
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
                checkTour();
            }

            @Override
            public void onFailure(@NonNull Call<PlacesAPI.Entrada> call, @NonNull Throwable t) {
                Log.i(TAG, t.getMessage());
            }
        });
    }

    private void insertPlaces() {
        for (int i = 0; i < mPlacesResponses.size(); i++) {
            if (mErro.getVisibility() == View.VISIBLE) {
                mErro.setVisibility(View.GONE);
            }
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
            if (mErro.getVisibility() == View.GONE) {
                mErro.setVisibility(View.VISIBLE);
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
                                mAdapterPlaces = new PlacesViewAdapter(getActivity(), mPlacesResponses, mPlaces, Nearby.this);
                                mRecyclerView.setAdapter(mAdapterPlaces);
                                setFiltro();
                                setFiltroAssistente();
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
            if (mapIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        } catch (NullPointerException e) {
            Log.i(TAG, "Não foi possivel abrir o mapa" + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_ENABLE_GPS) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "GPS ativado");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOptionClick(int id) {
        if (id == R.id.fab_gas_station) {
            mType = "gas_station";
            mName = "gas_station";
            mProgress = "Buscando postos...";
            mErro.setText("Nenhum posto encontrado aberto, tente aumentar o raio.");
            Objects.requireNonNull(getActivity()).setTitle("Postos de Gasolina");
            responseAPIPlaces();
        } else if (id == R.id.fab_parking) {
            mType = "parking";
            mName = "parking";
            mProgress = "Buscando estacionamentos...";
            mErro.setText("Nenhum estacionamento encontrado aberto, tente aumentar o raio.");
            Objects.requireNonNull(getActivity()).setTitle("Estacionamentos");
            responseAPIPlaces();
        } else if (id == R.id.fab_mecanica) {
            mType = "car_repair";
            mName = "car_repair";
            mProgress = "Buscando mecânicas...";
            mErro.setText("Nenhuma mecânica encontrada aberta, tente aumentar o raio.");
            Objects.requireNonNull(getActivity()).setTitle("Mecânicas");
            responseAPIPlaces();
        } else if (id == R.id.fab_hospital) {
            mType = "hospital";
            mName = "hospital";
            mProgress = "Buscando hospitais...";
            mErro.setText("Nenhum hospital encontrado aberto, tente aumentar o raio.");
            Objects.requireNonNull(getActivity()).setTitle("Hospitais");
            responseAPIPlaces();
        } else if (id == R.id.fab_restaurante) {
            mType = "restaurant";
            mName = "restaurant";
            mProgress = "Buscando restaurantes...";
            mErro.setText("Nenhum restaurante encontrado aberto, tente aumentar o raio.");
            Objects.requireNonNull(getActivity()).setTitle("Restaurantes");
            responseAPIPlaces();
        } else if (id == R.id.fab_cafe) {
            mType = "cafe";
            mName = "cafe";
            mProgress = "Buscando cafeterias...";
            mErro.setText("Nenhuma cafeteria encontrada aberta, tente aumentar o raio.");
            Objects.requireNonNull(getActivity()).setTitle("Cafeterias");
            responseAPIPlaces();
        }
        Log.i(TAG, "Lugar selecionado: " + mType);
    }
}