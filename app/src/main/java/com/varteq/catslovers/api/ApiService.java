package com.varteq.catslovers.api;

import com.varteq.catslovers.api.entity.AuthToken;
import com.varteq.catslovers.api.entity.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("signin")
    Call<BaseResponse<AuthToken>> auth(@Field("AccessToken") String key);
}
