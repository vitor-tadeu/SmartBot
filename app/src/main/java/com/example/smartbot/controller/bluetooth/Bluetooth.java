package com.example.smartbot.controller.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.smartbot.R;

import java.util.ArrayList;
import java.util.Set;

public class Bluetooth extends AppCompatActivity {
    private static final String TAG = "Bluetooth";
    private static final int REQUEST_BLUETOOTH = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    private Set<BluetoothDevice> mDevices;
    public ArrayList<BluetoothDevice> mBTDevices;
    //  public DeviceListAdapter mDeviceListAdapter;

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                //     mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_linha, mBTDevices);
                //        mListView.setAdapter(mDeviceListAdapter);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);
        init();
        BluetoothON();
        BluetoothOFF();
        buscarDispositivo();
        parearDispositivos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        buscarDispositivo();
        parearDispositivos();
    }

    private void init() {
        mListView = findViewById(R.id.list);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevices = mBluetoothAdapter.getBondedDevices();
        mBTDevices = new ArrayList<>();
    }

    private void BluetoothON() {
        Button bluetoothOn = findViewById(R.id.bluetoothOn);
        bluetoothOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter == null) {
                    Log.i(TAG, "Bluetooth não é suportado por este dispositivo");
                } else if (!mBluetoothAdapter.isEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_BLUETOOTH);
                    BluetoothVisivel();
                } else Log.i(TAG, "Bluetooth já esta conectado");
            }
        });
    }

    private void BluetoothOFF() {
        Button bluetoothOff = findViewById(R.id.bluetoothOff);
        bluetoothOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    Log.i(TAG, "Bluetooth desativado");
                }
            }
        });
    }

    private void buscarDispositivo() {
        Button buscar = findViewById(R.id.buscar);
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Buscando dispositivos");
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");

                    //check BT permissions in manifest
                    //checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
                if (!mBluetoothAdapter.isDiscovering()) {

                    //check BT permissions in manifest
                    //  checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
            }
        });
    }

    private void parearDispositivos() {
        Button parear = findViewById(R.id.parear);
        parear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Pareando dispositivos");
                if (mDevices.size() > 0) {
                    Log.i(TAG, "Pareando dispositivos if " + mDevices.size());
                    for (BluetoothDevice device : mDevices) {
                        Log.i(TAG, "Pareando dispositivos 2");

//                        mArrayList.add(device.getName() + "\n" + device.getAddress());
                        Log.i(TAG, "Pareando dispositivos 3");
                    }
//                    mAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mArrayList);
//                    mListView.setAdapter(mAdapter);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Bluetooth ativado");
            }
        }
    }

    private void BluetoothVisivel() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}