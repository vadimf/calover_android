package com.varteq.catslovers.api;

import com.varteq.catslovers.api.entity.RIPGeolocation;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IPGeolocationService {

    @GET("json")
    Call<RIPGeolocation> getIPGeolocation();

}
