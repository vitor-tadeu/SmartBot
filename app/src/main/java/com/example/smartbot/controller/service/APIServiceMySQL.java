package com.example.smartbot.controller.service;

import com.example.smartbot.model.MySQLAPI;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APIServiceMySQL {
    @GET("usuario")
    Call<MySQLAPI> getData(

    );
}