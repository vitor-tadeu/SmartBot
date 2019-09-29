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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.smartbot.BuildConfig;
import com.example.smartbot.R;
import com.example.smartbot.controller.adapter.OnItemClickListener;
import com.example.smartbot.controller.adapter.SensorViewAdapter;
import com.example.smartbot.controller.sdl.Config;
import com.example.smartbot.controller.sdl.SdlReceiver;
import com.example.smartbot.controller.sdl.SdlService;
import com.example.smartbot.controller.sdl.TelematicsCollector;
import com.example.smartbot.controller.sdl.VehicleData;
import com.example.smartbot.controller.utils.Constants;
import com.example.smartbot.model.Sensor;
import com.example.smartbot.view.sensores.Combustivel;
import com.example.smartbot.view.sensores.Marcha;
import com.example.smartbot.view.sensores.OleoMotor;
import com.example.smartbot.view.sensores.PosicaoPedal;
import com.example.smartbot.view.sensores.RPM;
import com.example.smartbot.view.sensores.Temperatura;
import com.example.smartbot.view.sensores.Velocidade;
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

    private TextView mTempo, mNome;
    private FloatingActionButton mMicrofone;
    private String mCoordenadas, mCombustivel, mTemperatura, mVelocidade, mRPM, mMarcha, mPosicaoPedal;
    private boolean firstStart;
    private Menu menu;
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
        SDL();
        init();
        setAdapter();
        getUsuario();
        checkPreferences();
        microfone();
        GPSOnOff();
        getSensor();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.dashboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_off) {
//            menu.getItem(0).setVisible(true);
//            menu.getItem(1).setVisible(false);
            if (Config.sdlServiceIsActive) {
                Toast.makeText(getActivity(), "Conexão SYNC: Conectado", Toast.LENGTH_SHORT).show();
                if (Config.isSubscribing) {
                    TelematicsCollector.getInstance().setUnssubscribeVehicleData();
                    menu.getItem(0).setTitle("ON");
                } else {
                    TelematicsCollector.getInstance().setSubscribeVehicleData();
                    menu.getItem(0).setTitle("OFF");
                }
            } else {
                Toast.makeText(getActivity(), "Conexão SYNC: Desconectado", Toast.LENGTH_SHORT).show();
            }
            return true;
//        } else if (id == R.id.menu_on){
//            menu.getItem(1).setVisible(true);
//            menu.getItem(0).setVisible(false);
        }
        return super.onOptionsItemSelected(item);
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

    private void SDL() {
        //If we are connected to a module we want to start our SdlService
        if (BuildConfig.TRANSPORT.equals("MULTI") || BuildConfig.TRANSPORT.equals("MULTI_HB")) {
            SdlReceiver.queryForConnectedService(getActivity());
        } else if (BuildConfig.TRANSPORT.equals("TCP")) {
            Intent proxyIntent = new Intent(getActivity(), SdlService.class);
            getActivity().startService(proxyIntent);
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void init() {
        mMicrofone = view.findViewById(R.id.fabMicrofone);
        mTempo = view.findViewById(R.id.txtTempo);
        mNome = view.findViewById(R.id.txtNome);

        mRecyclerView = view.findViewById(R.id.recyclerViewSensor);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
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
                LinearLayout mLayout = new LinearLayout(getActivity());
                TextView mTvMessage = new TextView(getActivity());
                final EditText mEtInput = new EditText(getActivity());

                mTvMessage.setText(getString(R.string.dashboard_usuario));
                mTvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
                mEtInput.setSingleLine();
                mLayout.setOrientation(LinearLayout.VERTICAL);
                mLayout.addView(mTvMessage);
                mLayout.addView(mEtInput);
                mLayout.setPadding(50, 40, 50, 10);

                mBuilder.setView(mLayout);

                mBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mEtInput.getText().toString().isEmpty()) {
                            dialogInterface.dismiss();
                        } else {
                            String name = mEtInput.getText().toString();
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
        nomeSensor.add("Velocidade");
        nomeSensor.add("RPM");
        nomeSensor.add("Marcha");
        nomeSensor.add("Posição do Pedal");
        nomeSensor.add("Óleo do Motor");
        return nomeSensor;
    }

    private ArrayList<Integer> imagem() {
        imagem.add(R.drawable.sensor_combustivel);
        imagem.add(R.drawable.sensor_temperatura);
        imagem.add(R.drawable.sensor_velocimetro);
        imagem.add(R.drawable.sensor_velocimetro);
        imagem.add(R.drawable.sensor_marcha);
        imagem.add(R.drawable.sensor_pneu);
        imagem.add(R.drawable.sensor_oleo_motor);
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
                        bundle.putString("apresentacao", "");
                        nearby.setArguments(bundle);
                        FragmentTransaction fragmentTransaction2 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                        fragmentTransaction2.replace(R.id.frame, nearby, "apresentacao");
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
            bundle.getString("configuracao");
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
                        bundle.putString("tour", "");
                        nearby.setArguments(bundle);
                        FragmentTransaction fragmentTransaction2 = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                        fragmentTransaction2.replace(R.id.frame, nearby, "tour");
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
                startActivity(new Intent(getActivity(), Temperatura.class));
                Animatoo.animateFade(context);
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
            } else if (command.contains("temperatura")) {
                speak("A temperatura externa é de " + mTemperatura + "º");
            } else if (command.contains("nível")) {
                if (command.contains("combustível")) {
                    speak("O nível do combustível atual é " + mCombustivel.substring(0, 2) + "%");
                }
            } else if (command.contains("velocidade")) {
                speak("A sua velocidade é de " + mVelocidade);
            }
        } else if (command.contains("horas")) {
            String time = DateUtils.formatDateTime(getActivity(), new Date().getTime(), DateUtils.FORMAT_SHOW_TIME);
            speak("Agora é: " + time);
        } else if (command.contains("próximo")) {
            if (command.contains("concessionária ford")) {
                speak("Aqui está a lista de concessionária perto da sua região");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/Concessionária+Ford/@" + mCoordenadas + ",12z")));
                    }
                }, 4000);
            }
        } else if (command.contains("abrir")) {
            if (command.contains("combustível")) {
                startActivity(new Intent(getActivity(), Combustivel.class));
                Animatoo.animateSlideLeft(context);
            } else if (command.contains("temperatura")) {
                startActivity(new Intent(getActivity(), Temperatura.class));
                Animatoo.animateSlideLeft(context);
            } else if (command.contains("velocidade")) {
                startActivity(new Intent(getActivity(), Velocidade.class));
                Animatoo.animateSlideLeft(context);
            } else if (command.contains("rpm")) {
                startActivity(new Intent(getActivity(), RPM.class));
                Animatoo.animateSlideLeft(context);
            } else if (command.contains("marcha")) {
                startActivity(new Intent(getActivity(), Marcha.class));
                Animatoo.animateSlideLeft(context);
            } else if (command.contains("posição do pedal")) {
                startActivity(new Intent(getActivity(), PosicaoPedal.class));
                Animatoo.animateSlideLeft(context);
            }
        } else speak("Não posso responder isso com precisão, tente perguntar de outra maneira.");
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

    private void getSensor() {
        mCombustivel = String.valueOf(((VehicleData.getInstance().getFuelLevel())));
        mTemperatura = String.valueOf(((VehicleData.getInstance().getExternalTemperature())));
        mVelocidade = String.valueOf(((VehicleData.getInstance().getSpeed())));
        mRPM = String.valueOf(((VehicleData.getInstance().getRpm())));
        mMarcha = ((VehicleData.getInstance().getPrndl()));
        mPosicaoPedal = String.valueOf(((VehicleData.getInstance().getEngineOilLife())));
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
            case "Velocidade":
                startActivity(new Intent(getActivity(), Velocidade.class));
                Animatoo.animateSlideLeft(context);
                break;
            case "RPM":
                startActivity(new Intent(getActivity(), RPM.class));
                Animatoo.animateSlideLeft(context);
                break;
            case "Marcha":
                startActivity(new Intent(getActivity(), Marcha.class));
                Animatoo.animateSlideLeft(context);
                break;
            case "Posição do Pedal":
                startActivity(new Intent(getActivity(), PosicaoPedal.class));
                Animatoo.animateSlideLeft(context);
                break;
            case "Óleo do Motor":
                startActivity(new Intent(getActivity(), OleoMotor.class));
                Animatoo.animateSlideLeft(context);
                break;
        }
    }
}