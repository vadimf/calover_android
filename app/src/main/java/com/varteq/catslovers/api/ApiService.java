package com.varteq.catslovers.api;

import com.varteq.catslovers.api.entity.AuthToken;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.Cat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @FormUrlEncoded
    @POST("signin")
    Call<BaseResponse<AuthToken>> auth(@Field("AccessToken") String key);

    @FormUrlEncoded
    @POST("cats")
    Call<BaseResponse<Cat>> createCat(@Field("feedstation_id") int feedstationId,
                                      @Field("name") String name,
                                      @Field("nickname") String nickname,
                                      @Field("color") String colors,
                                      @Field("age") int age,
                                      @Field("sex") String sex,
                                      @Field("weight") float weight,
                                      @Field("castrated") boolean castrated,
                                      @Field("description") String description,
                                      @Field("type") String type,
                                      @Field("next_flea_treatment") int nextFleaTreatment,
                                      @Field("lat") double lat,
                                      @Field("lng") double lng);

    @FormUrlEncoded
    @POST("cats")
    Call<BaseResponse<Cat>> createPrivateCat(@Field("name") String name,
                                             @Field("nickname") String nickname,
                                             @Field("color") String colors,
                                             @Field("age") int age,
                                             @Field("sex") String sex,
                                             @Field("weight") float weight,
                                             @Field("castrated") boolean castrated,
                                             @Field("description") String description,
                                             @Field("type") String type,
                                             @Field("next_flea_treatment") int nextFleaTreatment,
                                             @Field("lat") double lat,
                                             @Field("lng") double lng);

    /*@PUT("cats/{id}")
    Call<BaseResponse<Cat>> updateCat(@Path("id") int id, @Body Cat cat);*/

    @FormUrlEncoded
    @PUT("cats/{id}")
    Call<BaseResponse<Cat>> updateCat(@Path("id") int id,
                                      @Field("name") String name,
                                      @Field("nickname") String nickname,
                                      @Field("color") String colors,
                                      @Field("age") int age,
                                      @Field("sex") String sex,
                                      @Field("weight") float weight,
                                      @Field("castrated") boolean castrated,
                                      @Field("description") String description,
                                      @Field("type") String type,
                                      @Field("next_flea_treatment") int nextFleaTreatment);

    @GET("cats")
    Call<BaseResponse<List<Cat>>> getCats();

}
