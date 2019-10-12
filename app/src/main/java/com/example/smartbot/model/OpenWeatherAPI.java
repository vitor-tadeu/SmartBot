package com.example.smartbot.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OpenWeatherAPI {
    public class Entrada implements Serializable {
        @SerializedName("results")
        public List<Response> response = new ArrayList<>();
    }

    public class Response implements Serializable {
        @SerializedName("coord")
        public coord coord;
        @SerializedName("main")
        public main main;
    }

    public class coord implements Serializable {
        @SerializedName("lat")
        public String lat;
        @SerializedName("lng")
        public String lng;
    }

    public class main implements Serializable {
        @SerializedName("temp")
        public String temp;
        @SerializedName("pressure")
        public String pressure;
        @SerializedName("humidity")
        public String humidity;
        @SerializedName("temp_min")
        public String temp_min;
        @SerializedName("temp_max")
        public String temp_max;
    }
}
