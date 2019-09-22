package com.example.smartbot.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlacesAPI {
    public class Entrada implements Serializable {
        @SerializedName("results")
        public List<Response> response = new ArrayList<>();
        @SerializedName("status")
        public String status;
    }

    public class Response implements Serializable {
        @SerializedName("geometry")
        public Geometry geometry;
        @SerializedName("name")
        public String name;
        @SerializedName("vicinity")
        public String vicinity;
        @SerializedName("rating")
        public String rating;
    }

    public class Geometry implements Serializable {
        @SerializedName("location")
        public Location location;
    }

    public class Location implements Serializable {
        @SerializedName("lat")
        public String lat;
        @SerializedName("lng")
        public String lng;
    }
}
