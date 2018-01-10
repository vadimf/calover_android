package com.varteq.catslovers.api;

import com.varteq.catslovers.api.entity.AuthToken;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.RCat;
import com.varteq.catslovers.api.entity.REvent;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RGeoSearch;
import com.varteq.catslovers.api.entity.RUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
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
    Call<BaseResponse<RCat>> createCat(@Field("feedstation_id") int feedstationId,
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

    @FormUrlEncoded
    @POST("cats")
    Call<BaseResponse<RCat>> createPrivateCat(@Field("name") String name,
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
    Call<BaseResponse<RCat>> updateFeedstation(@Path("id") int id, @Body RCat cat);*/

    @FormUrlEncoded
    @PUT("cats/{id}")
    Call<BaseResponse<RCat>> updateCat(@Path("id") int id,
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
    Call<BaseResponse<List<RCat>>> getCats();

    @FormUrlEncoded
    @POST("geo/search/cats")
    Call<BaseResponse<List<RCat>>> getCatsInRadius(@Field("lat") double lat,
                                                   @Field("lng") double lng,
                                                   @Field("distance") Integer distance);

    @FormUrlEncoded
    @POST("feedstations")
    Call<BaseResponse<RFeedstation>> createFeedstation(@Field("name") String name,
                                                       @Field("address") String address,
                                                       @Field("description") String description,
                                                       @Field("time_to_feed") int timeToFeed,
                                                       @Field("lat") double lat,
                                                       @Field("lng") double lng);

    @FormUrlEncoded
    @PUT("feedstations/{id}")
    Call<BaseResponse<RFeedstation>> updateFeedstation(@Path("id") int id,
                                                       @Field("name") String name,
                                                       @Field("address") String address,
                                                       @Field("description") String description,
                                                       @Field("time_to_feed") int timeToFeed,
                                                       @Field("lat") double lat,
                                                       @Field("lng") double lng);

    @GET("feedstations")
    Call<BaseResponse<List<RFeedstation>>> getFeedstations();

    @FormUrlEncoded
    @POST("geo/search")
    Call<BaseResponse<RGeoSearch>> getGeoFeedstations(
            @Field("lat") double lat,
            @Field("lng") double lng,
            @Field("distance") Integer distance);

    @GET("feedstations/invitations")
    Call<BaseResponse<List<RFeedstation>>> getInvitations();

    @FormUrlEncoded
    @POST("feedstations/{id}/users")
    Call<BaseResponse<RUser>> inviteUserToFeedstation(@Path("id") int feedstationId,
                                                      @Field("phone") String phone);

    @POST("feedstations/{id}/follow")
    Call<BaseResponse<ErrorData>> followFeedstation(@Path("id") int feedstationId);

    @DELETE("feedstations/{id}/follow")
    Call<BaseResponse<ErrorData>> leaveFeedstation(@Path("id") int feedstationId);

    @POST("feedstations/{id}/join")
    Call<BaseResponse<RUser>> joinFeedstation(@Path("id") int feedstationId);

    @DELETE("feedstations/{id}/users/{user_id}")
    Call<BaseResponse<ErrorData>> deleteUserFromFeedstation(@Path("id") int feedstationId, @Path("user_id") int userId);

    @GET("feedstations/{id}/users")
    Call<BaseResponse<List<RUser>>> getFeedstationUsers(@Path("id") int feedstationId);

    @GET("cats/{id}/users")
    Call<BaseResponse<List<RUser>>> getCatUsers(@Path("id") int catId);

    @GET("feedstations/users/joined")
    Call<BaseResponse<List<RUser>>> getAllowedUsers();

    @GET("events/types")
    Call<BaseResponse<REvent>> getEventsTypes();

    @FormUrlEncoded
    @POST("events")
    Call<BaseResponse> createEvent(@Field("name") String name,
                                   @Field("address") String address,
                                   @Field("lat") double lat,
                                   @Field("lng") double lng,
                                   @Field("type_id") int type_id);
}
