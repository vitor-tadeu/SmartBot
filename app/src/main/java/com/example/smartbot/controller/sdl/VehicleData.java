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

    private String accPedalPosition;
    private String beltStatus;
    private String bodyInformation;
    private String clusterModeStatus;
    private String deviceStatus;
    private String driverBraking;
    private String emergencyEvent;
    private String engineOilLife;
    private String engineTorque;
    private Double externalTemperature;
    private Double fuelLevel;
    private String headLampStatus;
    private Double instantFuelConsumption;
    private int odometer;
    private String prndl;
    private Double speed;
    private String steeringWheelAngle;
    private String tirePressure;
    private Double turnSignal;
    private int rpm;
    private String vin;
    private String wiperStatus;

    public String getAccPedalPosition() {
        return accPedalPosition;
    }

    public void setAccPedalPosition(String accPedalPosition) {
        this.accPedalPosition = accPedalPosition;
    }

    public String getBeltStatus() {
        return beltStatus;
    }

    public void setBeltStatus(String beltStatus) {
        this.beltStatus = beltStatus;
    }

    public String getBodyInformation() {
        return bodyInformation;
    }

    public void setBodyInformation(String bodyInformation) {
        this.bodyInformation = bodyInformation;
    }

    public String getClusterModeStatus() {
        return clusterModeStatus;
    }

    public void setClusterModeStatus(String clusterModeStatus) {
        this.clusterModeStatus = clusterModeStatus;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
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

    public String getEngineOilLife() {
        return engineOilLife;
    }

    public void setEngineOilLife(String engineOilLife) {
        this.engineOilLife = engineOilLife;
    }

    public String getEngineTorque() {
        return engineTorque;
    }

    public void setEngineTorque(String engineTorque) {
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

    public int getOdometer() {
        return odometer;
    }

    public void setOdometer(int odometer) {
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

    public String getSteeringWheelAngle() {
        return steeringWheelAngle;
    }

    public void setSteeringWheelAngle(String steeringWheelAngle) {
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

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getWiperStatus() {
        return wiperStatus;
    }

    public void setWiperStatus(String wiperStatus) {
        this.wiperStatus = wiperStatus;
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
                setAccPedalPosition(value.toString());
                Log.i(TAG, "accPedalPosition: " + value.toString());
                break;
            case "beltStatus":
                setBeltStatus(value.toString());
                Log.i(TAG, "beltStatus: " + value.toString());
                break;
            case "bodyInformation":
                setBodyInformation(value.toString());
                Log.i(TAG, "bodyInformation: " + value.toString());
                break;
            case "clusterModeStatus":
                setClusterModeStatus(value.toString());
                Log.i(TAG, "clusterModeStatus: " + value.toString());
                break;
            case "deviceStatus":
                setDeviceStatus(value.toString());
                Log.i(TAG, "deviceStatus: " + value.toString());
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
                setEngineOilLife(value.toString());
                Log.i(TAG, "engineOilLife: " + value.toString());
                break;
            case "engineTorque":
                setEngineTorque(value.toString());
                Log.i(TAG, "engineTorque: " + value.toString());
                break;
            case "externalTemperature":
                setExternalTemperature(Double.parseDouble(value.toString()));
                Log.i(TAG, "externalTemperature: " + value.toString());
                break;
            case "fuelLevel":
                setFuelLevel(Double.parseDouble(value.toString()));
                Log.i(TAG, "fuelLevel: " + value.toString());
                break;
            case "headLampStatus":
                setHeadLampStatus(value.toString());
                Log.i(TAG, "headLampStatus: " + value.toString());
                break;
            case "instantFuelConsumption":
                setInstantFuelConsumption(Double.parseDouble(value.toString()));
                Log.i(TAG, "instantFuelConsumption: " + value.toString());
                break;
            case "odometer":
                setOdometer(Integer.parseInt(value.toString()));
                Log.i(TAG, "odometer: " + value.toString());
                break;
            case "prndl":
                setPrndl(value.toString());
                Log.i(TAG, "prndl: " + value.toString());
                break;
            case "speed":
                setSpeed(Double.parseDouble(value.toString()));
                Log.i(TAG, "speed: " + value.toString());
                break;
            case "steeringWheelAngle":
                setSteeringWheelAngle(value.toString());
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
            case "vin":
                setVin(value.toString());
                Log.i(TAG, "vin: " + value.toString());
                break;
            case "wiperStatus":
                setWiperStatus(value.toString());
                Log.i(TAG, "wiperStatus: " + value.toString());
                break;
        }
    }
}