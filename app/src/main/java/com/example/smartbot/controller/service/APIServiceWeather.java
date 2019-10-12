package com.example.smartbot.controller.service;

import com.example.smartbot.model.OpenWeatherAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIServiceWeather {
    @GET("place/nearbysearch/json?")
    Call<OpenWeatherAPI.Entrada> getWeather(
            @Query("coord") String coord,
            @Query("key") String key
    );
}