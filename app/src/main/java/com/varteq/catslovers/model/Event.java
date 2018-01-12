package com.varteq.catslovers.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Event {

    public enum Type {
        WARNING, EMERGENCY
    }

    public enum EventType {
        NEWBORN_KITTENS, MUNICIPALITY_INSPECTOR, CAT_IN_HEAT, STRAY_CAT, POISON, MISSING_CAT, CARCASS
    }

    private int id;
    private EventType mEventType;
    private Type type;
    private String address;
    private LatLng latLng;
    private String description;
    private Date date;
    private String name;
    private String typeName;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EventType getEventType() {
        return mEventType;
    }

    public void setEventType(EventType eventType) {
        this.mEventType = eventType;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
