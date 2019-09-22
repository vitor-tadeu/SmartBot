package com.example.smartbot.controller.service;

import com.example.smartbot.model.DistanceMatrixAPI;
import com.example.smartbot.model.PlacesAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIServicePlaces {
    @GET("place/nearbysearch/json?")
    Call<PlacesAPI.Entrada> getPlaces(
            @Query("location") String location,
            @Query("radius") String radius,
            @Query("type") String type,
            @Query("opennow") boolean opennow,
            @Query("name") String name,
            @Query("key") String key
    );

    @GET("distancematrix/json")
    Call<DistanceMatrixAPI> getDistance(@Query("origins") String origins,
                                        @Query("destinations") String destinations,
                                        @Query("key") String key);
}