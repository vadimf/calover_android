package com.varteq.catslovers.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RPhoto {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("photo")
    @Expose
    private String photo;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

}
