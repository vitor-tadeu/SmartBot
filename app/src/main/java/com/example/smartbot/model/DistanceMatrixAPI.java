package com.example.smartbot.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DistanceMatrixAPI {
    @SerializedName("rows")
    public List<Elements> rows;
    @SerializedName("status")
    public String status;

    public class Elements {
        @SerializedName("elements")
        public List<Resposta> elements;

        public class Resposta {
            @SerializedName("status")
            public String status;
            @SerializedName("duration")
            public ValueItem duration;
            @SerializedName("distance")
            public ValueItem distance;
        }

        public class ValueItem {
            @SerializedName("text")
            public String text;
        }
    }
}