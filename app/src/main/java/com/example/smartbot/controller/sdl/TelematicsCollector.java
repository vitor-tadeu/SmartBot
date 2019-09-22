package com.example.smartbot.controller.sdl;

import android.util.Log;

import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCNotification;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleData;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleData;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleData;
import com.smartdevicelink.proxy.rpc.enums.Result;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCNotificationListener;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCResponseListener;

public class TelematicsCollector {
    private static final String TAG = "TelematicsCollector";
    private static String odometer = "null";
    //Singleton
    private static TelematicsCollector INSTANCE;

    public static TelematicsCollector getInstance() {
        if (INSTANCE == null)
            INSTANCE = new TelematicsCollector();
        return INSTANCE;
    }

    public void getVehicleData() {
        //só para garantir que o getVehicleData será executado apenas se a conexão com o SYNC for estabelecida
        if (!Config.sdlServiceIsActive)
            return;

        GetVehicleData vdRequest = new GetVehicleData();
        vdRequest.setAccPedalPosition(true);
        vdRequest.setBeltStatus(true);
        vdRequest.setBodyInformation(true);
        vdRequest.setClusterModeStatus(true);
        vdRequest.setDeviceStatus(true);
        vdRequest.setDriverBraking(true);
        vdRequest.setEmergencyEvent(true);
        vdRequest.setEngineOilLife(true);
        vdRequest.setEngineTorque(true);
        vdRequest.setExternalTemperature(true);
        vdRequest.setFuelLevel(true);
        vdRequest.setHeadLampStatus(true);
        vdRequest.setInstantFuelConsumption(true);
        vdRequest.setOdometer(true);
        vdRequest.setPrndl(true);
        vdRequest.setSpeed(true);
        vdRequest.setSteeringWheelAngle(true);
        vdRequest.setTirePressure(true);
        vdRequest.setTurnSignal(true);
        vdRequest.setRpm(true);
        vdRequest.setVin(true);
        vdRequest.setWiperStatus(true);

        vdRequest.setOnRPCResponseListener(new OnRPCResponseListener() {
            @Override
            public void onResponse(int correlationId, RPCResponse response) {
                Log.i(TAG, "Houve uma resposta RPC!");
                if (response.getSuccess()) {
                    VehicleData.getInstance().processResponse((GetVehicleDataResponse) response);
                } else {
                    Log.i("SdlService", "GetVehicleData was rejected.");
                }
            }

            @Override
            public void onError(int correlationId, Result resultCode, String info) {
                Log.e(TAG, "onError: " + resultCode + " | Info: " + info);
            }
        });
        Config.sdlManager.sendRPC(vdRequest);
    }

    //você pode ate criar um método getVehicleData recebendo um OnRPCResponseListener como parametro
    public void getVehicleData(OnRPCResponseListener rpcResponseListener) {
        //só para garantir que o getVehicleData será executado apenas se a conexão com o SYNC for estabelecida
        if (!Config.sdlServiceIsActive)
            return;

        GetVehicleData vdRequest = new GetVehicleData();
        vdRequest.setAccPedalPosition(true);
        vdRequest.setBeltStatus(true);
        vdRequest.setBodyInformation(true);
        vdRequest.setClusterModeStatus(true);
        vdRequest.setDeviceStatus(true);
        vdRequest.setDriverBraking(true);
        vdRequest.setEmergencyEvent(true);
        vdRequest.setEngineOilLife(true);
        vdRequest.setEngineTorque(true);
        vdRequest.setExternalTemperature(true);
        vdRequest.setFuelLevel(true);
        vdRequest.setHeadLampStatus(true);
        vdRequest.setInstantFuelConsumption(true);
        vdRequest.setOdometer(true);
        vdRequest.setPrndl(true);
        vdRequest.setSpeed(true);
        vdRequest.setSteeringWheelAngle(true);
        vdRequest.setTirePressure(true);
        vdRequest.setTurnSignal(true);
        vdRequest.setRpm(true);
        vdRequest.setVin(true);
        vdRequest.setWiperStatus(true);

        vdRequest.setOnRPCResponseListener(rpcResponseListener);
        Config.sdlManager.sendRPC(vdRequest);
    }

    //subscribe é um listener que fica escutando alteração nos valores dos parametros que vc quer ficar escutando
    //neste caso está setado para ficar escutando alterações no câmbio (PRNDL) - toda vez que mudar a posição do câmvbio, o método onNotified será acionado.
    public void setSubscribeVehicleData() {
        //só para garantir que o getVehicleData será executado apenas se a conexão com o SYNC for estabelecida
        if (!Config.sdlServiceIsActive)
            return;

        if (Config.isSubscribing)
            return;

        SubscribeVehicleData subscribeRequest = new SubscribeVehicleData();
        subscribeRequest.setAccPedalPosition(true);
        subscribeRequest.setBeltStatus(true);
        subscribeRequest.setBodyInformation(true);
        subscribeRequest.setClusterModeStatus(true);
        subscribeRequest.setDeviceStatus(true);
        subscribeRequest.setDriverBraking(true);
        subscribeRequest.setEmergencyEvent(true);
        subscribeRequest.setEngineOilLife(true);
        subscribeRequest.setEngineTorque(true);
        subscribeRequest.setExternalTemperature(true);
        subscribeRequest.setFuelLevel(true);
        subscribeRequest.setHeadLampStatus(true);
        subscribeRequest.setInstantFuelConsumption(true);
        subscribeRequest.setOdometer(true);
        subscribeRequest.setPrndl(true);
        subscribeRequest.setSpeed(true);
        subscribeRequest.setSteeringWheelAngle(true);
        subscribeRequest.setTirePressure(true);
        subscribeRequest.setTurnSignal(true);
        subscribeRequest.setRpm(true);
        subscribeRequest.setWiperStatus(true);

        Config.sdlManager.addOnRPCNotificationListener(FunctionID.ON_VEHICLE_DATA, new OnRPCNotificationListener() {
            @Override
            public void onNotified(RPCNotification notification) {
                VehicleData.getInstance().processResponse((OnVehicleData) notification);
            }
        });

        subscribeRequest.setOnRPCResponseListener(new OnRPCResponseListener() {
            @Override
            public void onResponse(int correlationId, RPCResponse response) {
                if (response.getSuccess()) {
                    Log.i(TAG, "Successfully subscribed to vehicle data.");
                    Config.isSubscribing = true;
                } else {
                    Log.i(TAG, "Request to subscribe to vehicle data was rejected.");
                }
            }

            @Override
            public void onError(int correlationId, Result resultCode, String info) {
                Log.e(TAG, "onError: " + resultCode + " | Info: " + info);
            }
        });
        Config.sdlManager.sendRPC(subscribeRequest);
    }

    public void setUnssubscribeVehicleData() {
        //if (!Config.dataCollectionActive) return;
        if (!Config.isSubscribing)
            return;

        UnsubscribeVehicleData unsubscribeRequest = new UnsubscribeVehicleData();
        unsubscribeRequest.setAccPedalPosition(true);
        unsubscribeRequest.setBeltStatus(true);
        unsubscribeRequest.setBodyInformation(true);
        unsubscribeRequest.setClusterModeStatus(true);
        unsubscribeRequest.setDeviceStatus(true);
        unsubscribeRequest.setDriverBraking(true);
        unsubscribeRequest.setEmergencyEvent(true);
        unsubscribeRequest.setEngineOilLife(true);
        unsubscribeRequest.setEngineTorque(true);
        unsubscribeRequest.setExternalTemperature(true);
        unsubscribeRequest.setFuelLevel(true);
        unsubscribeRequest.setHeadLampStatus(true);
        unsubscribeRequest.setInstantFuelConsumption(true);
        unsubscribeRequest.setOdometer(true);
        unsubscribeRequest.setPrndl(true);
        unsubscribeRequest.setSpeed(true);
        unsubscribeRequest.setSteeringWheelAngle(true);
        unsubscribeRequest.setTirePressure(true);
        unsubscribeRequest.setTurnSignal(true);
        unsubscribeRequest.setRpm(true);
        unsubscribeRequest.setWiperStatus(true);

        unsubscribeRequest.setOnRPCResponseListener(new OnRPCResponseListener() {
            @Override
            public void onResponse(int correlationId, RPCResponse response) {
                if (response.getSuccess()) {
                    Log.i(TAG, "Successfully subscribed to vehicle data.");
                    Config.isSubscribing = false;
                } else {
                    Log.i(TAG, "Request to subscribe to vehicle data was rejected.");
                }
            }

            @Override
            public void onError(int correlationId, Result resultCode, String info) {
                Log.e(TAG, "onError: " + resultCode + " | Info: " + info);
            }
        });
        Config.sdlManager.sendRPC(unsubscribeRequest);
    }
}