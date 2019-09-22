package com.example.smartbot.model;

import java.util.ArrayList;

public class Sensor {
    private ArrayList nomeSensor;
    private ArrayList<Integer> image;

    public Sensor(ArrayList nomeSensor, ArrayList<Integer> image) {
        this.nomeSensor = nomeSensor;
        this.image = image;
    }

    public ArrayList getNomeSensor() {
        return nomeSensor;
    }

    public void setNomeSensor(ArrayList nomeSensor) {
        this.nomeSensor = nomeSensor;
    }

    public ArrayList<Integer> getImage() {
        return image;
    }

    public void setImage(ArrayList<Integer> image) {
        this.image = image;
    }
}