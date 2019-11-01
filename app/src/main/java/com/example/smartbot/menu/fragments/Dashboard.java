package com.example.smartbot.menu.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.smartbot.R;
import com.example.smartbot.controller.adapter.OnItemClickListener;
import com.example.smartbot.controller.adapter.SensorViewAdapter;
import com.example.smartbot.controller.utils.AssistenteDicas;
import com.example.smartbot.controller.utils.Constants;
import com.example.smartbot.model.Sensor;
import com.example.smartbot.view.Grafico;
import com.example.smartbot.view.sensores.Combustivel;
import com.example.smartbot.view.sensores.Temperatura;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class Dashboard extends Fragment implements OnItemClickListener {
    private static final String TAG = "Dashboard";
    private static int flagRaio = 0;
    private static int flagFiltro = 0;

    private TextView mTempo, mNome;
    private FloatingActionButton mMicrofone;
    private Button mGrafico;
    private String mCoordenadas, mRaio = null, mFiltro = null, mLugar;
    private boolean firstStart;
    private View view;

    private RecyclerView mRecyclerView;
    private List<Sensor> mSensors;
    private ArrayList<String> nomeSensor;
    private ArrayList<Integer> imagem;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private ShowcaseView.Builder showcaseView;

    private TextToSpeech mTTS;
    private SpeechRecognizer mSR;

    public Dashboard() {
    }

    public static Dashboard newInstance() {
        return new Dashboard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Objects.requireNonNull(getActivity()).setTitle("Monitoramento");
        setHasOptionsMenu(true);
        init();
        setAdapter();
        getUsuario();
        checkPreferences();
        microfone();
        GPSOnOff();
        openGrafico();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        calendar();
        initializeSpeechRecognizer();
        initializeTextToSpeech();
    }

    @Override
    public void onPause() {
        super.onPause();
        mTTS.shutdown();
    }

    private void GPSOnOff() {
        LocationManager manager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "GPS desativado");
            openGPSSettings();
        } else {
            getCoordenadas();
            Log.i(TAG, "GPS ativado");
        }
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

    @SuppressLint("CommitPrefEdits")
    private void init() {
        mMicrofone = view.findViewById(R.id.fabMicrofone);
        mTempo = view.findViewById(R.id.txtTempo);
        mNome = view.findViewById(R.id.txtNome);

        mRecyclerView = view.findViewById(R.id.recyclerViewSensor);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setHasFixedSize(true);

        mSensors = new ArrayList<>();
        nomeSensor = new ArrayList<>();
        imagem = new ArrayList<>();

        mPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences("Preferences", getActivity().MODE_PRIVATE);
        mEditor = mPreferences.edit();

        firstStart = mPreferences.getBoolean(Constants.APRESENTACAO_1, true);
    }

    private void setAdapter() {
        if (mSensors.size() == nomeSensor.size()) {
            SensorViewAdapter mAdapter = new SensorViewAdapter(getActivity(), mSensors, nomeSensor(), imagem(), Dashboard.this);
            mRecyclerView.setAdapter(mAdapter);
        }
        for (int i = 0; i < nomeSensor.size(); i++) {
            mSensors.add(new Sensor(nomeSensor, imagem));
        }
    }

    private void calendar() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay < 12) {
            mTempo.setText(getString(R.string.dashboard_bom_dia));
        } else if (timeOfDay < 18) {
            mTempo.setText(getString(R.string.dashboard_bom_tarde));
        } else {
            mTempo.setText(getString(R.string.dashboard_bom_noite));
        }
    }

    private void getUsuario() {
        mNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v7.app.AlertDialog.Builder mBuilder = new android.support.v7.app.AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                LinearLayout layout = new LinearLayout(getActivity());
                TextView view = new TextView(getActivity());
                final EditText nome = new EditText(getActivity());

                view.setText(getString(R.string.dashboard_usuario));
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
                nome.setSingleLine();
                nome.setImeOptions(EditorInfo.IME_ACTION_DONE);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(view);
                layout.addView(nome);
                layout.setPadding(50, 40, 50, 10);
                mBuilder.setView(layout);

                mBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (nome.getText().toString().isEmpty()) {
                            dialogInterface.dismiss();
                        } else {
                            String name = nome.getText().toString();
                            mNome.setText(name);
                            mPreferences.edit().putString(Constants.USUARIO, name).apply();
                        }
                    }
                });
                mBuilder.create().show();
            }
        });
    }

    private void checkPreferences() {
//        mPreferences.edit().clear().commit();
        if (!mPreferences.contains(Constants.USUARIO)) {
            mEditor.putString(Constants.USUARIO, "Motorista");
            mEditor.apply();
        } else {
            mNome.setText(mPreferences.getString(Constants.USUARIO, ""));
        }
        if (firstStart) {
            mEditor.putBoolean(Constants.APRESENTACAO_1, false);
            mEditor.apply();
            apresentacao1();
            Log.i(TAG, "1");
        } else {
            tour1();
            Log.i(TAG, "2");
        }
    }

    private ArrayList<String> nomeSensor() {
        nomeSensor.add("Combustível");
        nomeSensor.add("Temperatura");
        return nomeSensor;
    }

    private ArrayList<Integer> imagem() {
        imagem.add(R.drawable.sensor_combustivel);
        imagem.add(R.drawable.sensor_temperatura);
        return imagem;
    }

    private void apresentacao1() {
        Log.i(TAG, "apresentacao1");
        showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                .setTarget(new ViewTarget(mNome))
                .setStyle(R.style.ShowCase1)
                .blockAllTouches()
                .setContentTitle("Nome de Usuário")
                .setContentText("Clique em 'Motorista' para alterar o nome.")
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

    private void apresentacao2() {
        showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                .setTarget(new ViewTarget(mRecyclerView))
                .setStyle(R.style.ShowCase1)
                .blockAllTouches()
                .setContentTitle("Sensores")
                .setContentText("Veja o status das peças do seu veiculo" + "\n" + "em tempo real.")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        apresentacao3();
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

    private void apresentacao3() {
        showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                .setTarget(new ViewTarget(mMicrofone))
                .setStyle(R.style.ShowCase1)
                .blockAllTouches()
                .setContentTitle("Assistente")
                .setContentText("Segure o microfone para ver sugestões.")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        apresentacao4();
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

    private void apresentacao4() {
        showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                .setTarget(new ViewTarget(R.id.menu_places, getActivity()))
                .setStyle(R.style.ShowCase3)
                .blockAllTouches()
                .setContentTitle("Nearby")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        Nearby nearby = new Nearby();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.APRESENTACAO, "");
                        nearby.setArguments(bundle);
                        FragmentTransaction fragmentTransaction2 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                        fragmentTransaction2.replace(R.id.frame, nearby);
                        fragmentTransaction2.commit();
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
            bundle.getString(Constants.CONFIGURACOES);
            showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                    .setTarget(new ViewTarget(mNome))
                    .setStyle(R.style.ShowCase1)
                    .blockAllTouches()
                    .setContentTitle("Nome de Usuário")
                    .setContentText("Clique em 'Motorista' para alterar o nome.")
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
                .setStyle(R.style.ShowCase1)
                .blockAllTouches()
                .setContentTitle("Sensores")
                .setContentText("Veja o status das peças do seu veiculo" + "\n" + "em tempo real.")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        tour3();
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

    private void tour3() {
        showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                .setTarget(new ViewTarget(mMicrofone))
                .setStyle(R.style.ShowCase1)
                .blockAllTouches()
                .setContentTitle("Assistente")
                .setContentText("Segure o microfone para ver sugestões.")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        tour4();
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

    private void tour4() {
        showcaseView = new ShowcaseView.Builder(Objects.requireNonNull(getActivity()))
                .setTarget(new ViewTarget(R.id.menu_places, getActivity()))
                .setStyle(R.style.ShowCase3)
                .blockAllTouches()
                .setContentTitle("Nearby")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        Nearby nearby = new Nearby();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.TOUR, "");
                        nearby.setArguments(bundle);
                        FragmentTransaction fragmentTransaction2 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                        fragmentTransaction2.replace(R.id.frame, nearby);
                        fragmentTransaction2.commit();
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

    private void openGrafico() {
        mGrafico = view.findViewById(R.id.imgaeGrafico);
        mGrafico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getActivity();
                startActivity(new Intent(getActivity(), Grafico.class));
                Animatoo.animateFade(Objects.requireNonNull(context));
            }
        });
    }

    private void microfone() {
        mMicrofone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.RECORD_AUDIO}, Constants.RESULT_SPEECH);
                } else {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                    mSR.startListening(intent);
                }
            }
        });
        initializeTextToSpeech();
        initializeSpeechRecognizer();
        mMicrofone.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Context context = getActivity();
                startActivity(new Intent(getActivity(), AssistenteDicas.class));
                Animatoo.animateFade(Objects.requireNonNull(context));
                return true;
            }
        });
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(getActivity())) {
            mSR = SpeechRecognizer.createSpeechRecognizer(getActivity());
            mSR.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float v) {

                }

                @Override
                public void onBufferReceived(byte[] bytes) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int i) {

                }

                @Override
                public void onResults(Bundle bundle) {
                    List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    assert results != null;
                    processResult(results.get(0));
                }

                @Override
                public void onPartialResults(Bundle bundle) {

                }

                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });
        }
    }

    private void processResult(String command) {
        Context context = getActivity();
        assert context != null;
        command = command.toLowerCase();
        if (command.contains("qual") || command.contains("como")) {
            if (command.contains("seu nome")) {
                speak(mTempo.getText().toString() + " " + mNome.getText().toString() + ". Meu nome é Lóri, tudo bem?");
            } else if (command.contains("nível")) {
                if (command.contains("combustível")) {
                    speak("O nível do combustível atual é 80%");
                }
            } else if (command.contains("temperatura")) {
                speak("No momento a temperatura é 29º");
            }
        } else if (command.contains("horas")) {
            String time = DateUtils.formatDateTime(getActivity(), new Date().getTime(), DateUtils.FORMAT_SHOW_TIME);
            speak("Agora é: " + time);
        } else if (command.contains("concessionária")) {
            if (command.contains("próximas")) {
                if (command.contains("ford")) {
                    speak("Aqui está a lista de concessionária perto da sua região");
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/Concessionária+Ford/@" + mCoordenadas + ",12z")));
                        }
                    }, 4000);
                }
            }
        } else if (command.contains("abrir")) {
            if (command.contains("combustível")) {
                startActivity(new Intent(getActivity(), Combustivel.class));
                Animatoo.animateSlideLeft(context);
            } else if (command.contains("temperatura")) {
                startActivity(new Intent(getActivity(), Temperatura.class));
                Animatoo.animateSlideLeft(context);
            }
        } else if (command.contains("buscar")) {
            if (command.contains("hotel")
                    || (command.contains("posto de gasolina"))
                    || (command.contains("estacionamento"))
                    || (command.contains("mecânica"))
                    || (command.contains("hospital"))
                    || (command.contains("restaurante"))
                    || (command.contains("cafeteria"))) {
                mLugar = command.substring(7);
                speak("Você quer definir um raio?");
                flagRaio = 1;
                delay(2000);
            } else {
                speak("Não tenho base suficiente para responder, procure por outras coisas.");
                delay(5500);
            }
        } else {
            speak("Não posso responder isso com precisão, tente perguntar de outra maneira.");
            mSR.stopListening();
        }

        switch (flagRaio) {
            case 1:
                if (command.contains("sim")) {
                    speak("Qual o raio desejado em quilômetro?");
                    flagRaio = 2;
                    delay(3500);
                    break;
                } else if (command.contains("não")) {
                    speak("E algum filtro?");
                    mRaio = "10000";
                    flagFiltro = 1;
                    break;
                }
                break;
            case 2:
                if (command.contains("1")) {
                    mRaio = "1000";
                    speak("Aplicar filtro também?");
                    flagFiltro = 1;
                    delay(2500);
                    break;
                } else if (command.contains("2")) {
                    mRaio = "2000";
                    speak("Aplicar filtro também?");
                    flagFiltro = 1;
                    delay(2500);
                    break;
                } else if (command.contains("5")) {
                    mRaio = "5000";
                    speak("Aplicar filtro também?");
                    flagFiltro = 1;
                    delay(2500);
                    break;
                } else if (command.contains("10")) {
                    mRaio = "10000";
                    speak("Aplicar filtro também?");
                    flagFiltro = 1;
                    delay(2500);
                    break;
                } else if (command.contains("20")) {
                    mRaio = "20000";
                    speak("Aplicar filtro também?");
                    flagFiltro = 1;
                    delay(2500);
                    break;
                } else if (command.contains("30")) {
                    mRaio = "30000";
                    speak("Aplicar filtro também?");
                    flagFiltro = 1;
                    delay(2500);
                    break;
                } else if (command.contains("40")) {
                    mRaio = "40000";
                    speak("Aplicar filtro também?");
                    flagFiltro = 1;
                    delay(2500);
                    break;
                } else if (command.contains("50")) {
                    mRaio = "50000";
                    speak("Aplicar filtro também?");
                    flagFiltro = 1;
                    delay(2500);
                    break;
                } else {
                    speak("Entre com o raio disponível na lista de raio.");
                    flagRaio = 2;
                    delay(4500);
                    break;
                }
        }

        switch (flagFiltro) {
            case 1:
                if (command.contains("sim")) {
                    speak("Qual o filtro desejado?");
                    flagFiltro = 2;
                    delay(2500);
                    break;
                } else if (command.contains("não obrigado")) {
                    mFiltro = "Distância mais curta";
                    openNearby();
                    break;
                }
                break;
            case 2:
                if (command.contains("ordem alfabética")) {
                    mFiltro = "Ordem alfabética";
                    openNearby();
                    break;
                } else if (command.contains("distância mais curta")) {
                    mFiltro = "Distância mais curta";
                    openNearby();
                    break;
                } else if (command.contains("tempo mais curto")) {
                    mFiltro = "Tempo mais curto";
                    openNearby();
                    break;
                } else if (command.contains("maior avaliação")) {
                    mFiltro = "Maior avaliação";
                    openNearby();
                    break;
                } else {
                    speak("Entre com o filtro disponível na lista de filtros.");
                    flagFiltro = 2;
                    delay(4500);
                    break;
                }
        }
    }

    private void openNearby() {
        mSR.destroy();
        speak("Fazendo busca de " + mLugar + " na sua região");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Nearby nearby = new Nearby();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PLACE_ASSISTENTE, mLugar);
                bundle.putString(Constants.RAIO_ASSISTENTE, mRaio);
                bundle.putString(Constants.FILTRO_ASSISTENTE, mFiltro);
                Log.i(TAG, "Lugar: " + mLugar);
                Log.i(TAG, "Raio: " + mRaio);
                Log.i(TAG, "Filtro: " + mFiltro);
                nearby.setArguments(bundle);
                FragmentTransaction fragmentTransaction2 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                fragmentTransaction2.replace(R.id.frame, nearby);
                fragmentTransaction2.commit();
            }
        }, 5000);
    }

    private void delay(int delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                inicializaSPR();
            }
        }, delay);
    }

    private void inicializaSPR() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);
        mSR.startListening(intent);
    }

    private void initializeTextToSpeech() {
        mTTS = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (mTTS.getEngines().size() == 0) {
                    Toast.makeText(getActivity(), "Não tem suporte", Toast.LENGTH_LONG).show();
                    Objects.requireNonNull(getActivity()).finish();
                } else {
                    mTTS.setLanguage((Locale.getDefault()));
                }
            }
        });
    }

    private void speak(String message) {
//        mTTS.setPitch(1);
        mTTS.setSpeechRate(.9f);
        if (Build.VERSION.SDK_INT >= 21) {
            mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
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
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.RESULT_SPEECH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permissao aceita");
            } else {
                microfone();
                Log.i(TAG, "Permissao negada");
            }
        } else if (requestCode == Constants.REQUEST_ENABLE_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "GPS ativado");
            }
        }
    }

    @Override
    public void OnItemClick(int posicao) {
        Context context = getActivity();
        assert context != null;
        switch (nomeSensor.get(posicao)) {
            case "Combustível":
                startActivity(new Intent(getActivity(), Combustivel.class));
                Animatoo.animateSlideLeft(context);
                break;
            case "Temperatura":
                startActivity(new Intent(getActivity(), Temperatura.class));
                Animatoo.animateSlideLeft(context);
                break;
        }
    }
}