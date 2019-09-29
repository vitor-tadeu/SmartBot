package com.example.smartbot.controller.sdl;

import android.util.Log;

import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.OnVehicleData;

import java.util.Hashtable;
import java.util.Map;

public class VehicleData {
    private static final String TAG = "VehicleData";
    private static VehicleData INSTANCE;

    public static VehicleData getInstance() {
        if (INSTANCE == null)
            INSTANCE = new VehicleData();
        return INSTANCE;
    }

    private Double accPedalPosition;
    private String driverBraking;
    private String emergencyEvent;
    private Double engineOilLife;
    private Double engineTorque;
    private Double externalTemperature;
    private Double fuelLevel;
    private String headLampStatus;
    private Double instantFuelConsumption;
    private Double odometer;
    private String prndl;
    private Double speed;
    private Double steeringWheelAngle;
    private String tirePressure;
    private Double turnSignal;
    private int rpm;

    public Double getAccPedalPosition() {
        return accPedalPosition;
    }

    public void setAccPedalPosition(Double accPedalPosition) {
        this.accPedalPosition = accPedalPosition;
    }

    public String getDriverBraking() {
        return driverBraking;
    }

    public void setDriverBraking(String driverBraking) {
        this.driverBraking = driverBraking;
    }

    public String getEmergencyEvent() {
        return emergencyEvent;
    }

    public void setEmergencyEvent(String emergencyEvent) {
        this.emergencyEvent = emergencyEvent;
    }

    public Double getEngineOilLife() {
        return engineOilLife;
    }

    public void setEngineOilLife(Double engineOilLife) {
        this.engineOilLife = engineOilLife;
    }

    public Double getEngineTorque() {
        return engineTorque;
    }

    public void setEngineTorque(Double engineTorque) {
        this.engineTorque = engineTorque;
    }

    public Double getExternalTemperature() {
        return externalTemperature;
    }

    public void setExternalTemperature(Double externalTemperature) {
        this.externalTemperature = externalTemperature;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public String getHeadLampStatus() {
        return headLampStatus;
    }

    public void setHeadLampStatus(String headLampStatus) {
        this.headLampStatus = headLampStatus;
    }

    public Double getInstantFuelConsumption() {
        return instantFuelConsumption;
    }

    public void setInstantFuelConsumption(Double instantFuelConsumption) {
        this.instantFuelConsumption = instantFuelConsumption;
    }

    public Double getOdometer() {
        return odometer;
    }

    public void setOdometer(Double odometer) {
        this.odometer = odometer;
    }

    public String getPrndl() {
        return prndl;
    }

    public void setPrndl(String prndl) {
        this.prndl = prndl;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getSteeringWheelAngle() {
        return steeringWheelAngle;
    }

    public void setSteeringWheelAngle(Double steeringWheelAngle) {
        this.steeringWheelAngle = steeringWheelAngle;
    }

    public String getTirePressure() {
        return tirePressure;
    }

    public void setTirePressure(String tirePressure) {
        this.tirePressure = tirePressure;
    }

    public Double getTurnSignal() {
        return turnSignal;
    }

    public void setTurnSignal(Double turnSignal) {
        this.turnSignal = turnSignal;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    //processar a resposta para o SUBSCRIBE
    public void processResponse(GetVehicleDataResponse responseGet) {
        Log.i(TAG, "Entrou processResponse");
        //só para garantir que o getVehicleData será executado apenas se a conexão com o SYNC for estabelecida
        if (!Config.sdlServiceIsActive)
            return;

        for (Map.Entry<String, Object> responseHash : responseGet.getStore().entrySet()) {
            if (responseHash.getKey().equals("response")) {
                for (Map.Entry<String, Object> parametersHash : ((Hashtable<String, Object>) responseHash.getValue()).entrySet()) {
                    if (parametersHash.getKey().equals("parameters")) {
                        for (Map.Entry<String, Object> item : ((Hashtable<String, Object>) parametersHash.getValue()).entrySet()) {
                            setValue(item.getKey(), item.getValue());
                        }
                    }
                }
            }
        }
    }

    //processar a resposta para o GetVehicleData
    public void processResponse(OnVehicleData responseSubs) {
        //só para garantir que o getVehicleData será executado apenas se a conexão com o SYNC for estabelecida
        if (!Config.sdlServiceIsActive)
            return;

        for (Map.Entry<String, Object> responseHash : responseSubs.getStore().entrySet()) {
            if (responseHash.getKey().equals("notification")) {
                for (Map.Entry<String, Object> parametersHash : ((Hashtable<String, Object>) responseHash.getValue()).entrySet()) {
                    if (parametersHash.getKey().equals("parameters")) {
                        for (Map.Entry<String, Object> item : ((Hashtable<String, Object>) parametersHash.getValue()).entrySet()) {
                            setValue(item.getKey(), item.getValue());
                        }
                    }
                }
            }
        }
    }

    private void setValue(String key, Object value) {
        Log.i(TAG, "key: " + key);
        switch (key) {
            case "accPedalPosition":
                setAccPedalPosition(Double.valueOf(value.toString()));
                Log.i(TAG, "accPedalPosition: " + value.toString());
                break;
            case "driverBraking":
                setDriverBraking(value.toString());
                Log.i(TAG, "driverBraking: " + value.toString());
                break;
            case "emergencyEvent":
                setEmergencyEvent(value.toString());
                Log.i(TAG, "emergencyEvent: " + value.toString());
                break;
            case "engineOilLife":
                setEngineOilLife(Double.valueOf(value.toString()));
                Log.i(TAG, "engineOilLife: " + value.toString());
                break;
            case "engineTorque":
                setEngineTorque(Double.valueOf(value.toString()));
                Log.i(TAG, "engineTorque: " + value.toString());
                break;
            case "externalTemperature":
                setExternalTemperature(Double.valueOf((value.toString())));
                Log.i(TAG, "externalTemperature: " + value.toString());
                break;
            case "fuelLevel":
                setFuelLevel(Double.valueOf(value.toString()));
                Log.i(TAG, "fuelLevel: " + value.toString());
                break;
            case "headLampStatus":
                setHeadLampStatus(value.toString());
                Log.i(TAG, "headLampStatus: " + value.toString());
                break;
            case "instantFuelConsumption":
                setInstantFuelConsumption(Double.valueOf(value.toString()));
                Log.i(TAG, "instantFuelConsumption: " + value.toString());
                break;
            case "odometer":
                setOdometer(Double.valueOf(value.toString()));
                Log.i(TAG, "odometer: " + value.toString());
                break;
            case "prndl":
                setPrndl(value.toString());
                Log.i(TAG, "prndl: " + value.toString());
                break;
            case "speed":
                setSpeed(Double.valueOf(value.toString()));
                Log.i(TAG, "speed: " + value.toString());
                break;
            case "steeringWheelAngle":
                setSteeringWheelAngle(Double.valueOf(value.toString()));
                Log.i(TAG, "steeringWheelAngle: " + value.toString());
                break;
            case "tirePressure":
                for (Map.Entry<String, Object> itemTire : ((Hashtable<String, Object>) value).entrySet()) {
                    if (!itemTire.getKey().equals("pressureTelltale"))
                        setValue(itemTire.getKey(), ((Hashtable<String, Object>) itemTire.getValue()).get("status").toString());
                }
                break;
            case "turnSignal":
                setTurnSignal(Double.valueOf(value.toString()));
                Log.i(TAG, "turnSignal: " + value.toString());
                break;
            case "rpm":
                setRpm(Integer.parseInt(value.toString()));
                Log.i(TAG, "rpm: " + value.toString());
                break;
        }
    }
}