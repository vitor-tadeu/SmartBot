package com.example.smartbot.model;

import java.io.Serializable;
import java.util.Comparator;

public class Places implements Serializable {
    private String name;
    private String vicinity;
    private String lat;
    private String lng;
    private String rating;
    private String distancia;
    private String tempo;

    public Places(String name, String vicinity, String lat, String lng, String rating, String distancia, String tempo) {
        this.name = name;
        this.vicinity = vicinity;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.distancia = distancia;
        this.tempo = tempo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDistancia() {
        return distancia;
    }

    public void setDistancia(String distancia) {
        this.distancia = distancia;
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public static final Comparator<Places> ORDEM_ALFABETICA_CRESCENTE = new Comparator<Places>() {
        @Override
        public int compare(Places o1, Places o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public static final Comparator<Places> ORDEM_DISTANCIA_CURTA = new Comparator<Places>() {
        @Override
        public int compare(Places o1, Places o2) {
            return o1.getDistancia().compareTo(o2.getDistancia());
        }
    };

    public static final Comparator<Places> ORDEM_TEMPO_CURTO = new Comparator<Places>() {
        @Override
        public int compare(Places o1, Places o2) {
            return o1.getTempo().compareTo(o2.getTempo());
        }
    };

    public static final Comparator<Places> OREDEM_MAIOR_AVALIACAO = new Comparator<Places>() {
        @Override
        public int compare(Places o1, Places o2) {
            return o2.getRating().compareTo(o1.getRating());
        }
    };
}