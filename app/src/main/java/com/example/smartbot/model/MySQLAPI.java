package com.example.smartbot.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MySQLAPI implements Serializable {
    @SerializedName("id")
    public String id;
    @SerializedName("nome")
    public String nome;
}