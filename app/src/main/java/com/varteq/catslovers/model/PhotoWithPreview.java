package com.varteq.catslovers.model;

import java.io.Serializable;

public class PhotoWithPreview implements Serializable {

    private Integer id;
    private String photo;
    private String thumbnail;

    public PhotoWithPreview(Integer id, String photo, String thumbnail) {
        this(photo, thumbnail);
        this.id = id;
    }

    public PhotoWithPreview(String photo, String thumbnail) {
        this.photo = photo;
        this.thumbnail = thumbnail;
    }

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
