package com.varteq.catslovers.model;

import com.google.android.gms.maps.model.LatLng;

public class Event {

    public enum Type {
        WARNING, EMERGENCY
    }

    public enum Name {
        NEWBORN_KITTENS, MUNICIPALITY_INSPECTOR, CAT_IN_HEAT, STRAY_CAT, POISON, MISSING_CAT, CARCASS
    }

    private int id;
    private Name name;
    private Type type;
    private String address;
    private LatLng latLng;
    private String description;
    private String createdAt;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
