package com.varteq.catslovers.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthToken extends ErrorData {

    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("token")
    @Expose
    private String token;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
