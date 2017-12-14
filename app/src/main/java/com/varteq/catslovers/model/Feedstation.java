package com.varteq.catslovers.model;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Feedstation implements Serializable {

    private Integer id;
    private String name;
    private String description;
    private LatLng location;
    private String address;
    private Boolean isPublic;
    private Integer timeToFeed;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getTimeToFeed() {
        return timeToFeed;
    }

    public void setTimeToFeed(int timeToFeed) {
        this.timeToFeed = timeToFeed;
    }

}