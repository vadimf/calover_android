package com.varteq.catslovers.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RGeoSearch extends ErrorData {

    @SerializedName("feedstations")
    @Expose
    private List<RFeedstation> feedstations;
    @SerializedName("events")
    @Expose
    private List<REvent> events;
    @SerializedName("businesses")
    @Expose
    private List<Object> businesses;

    public List<RFeedstation> getFeedstations() {
        return feedstations;
    }

    public void setFeedstations(List<RFeedstation> feedstations) {
        this.feedstations = feedstations;
    }

    public List<REvent> getEvents() {
        return events;
    }

    public void setEvents(List<REvent> events) {
        this.events = events;
    }

    public List<Object> getBusinesses() {
        return businesses;
    }

    public void setBusinesses(List<Object> businesses) {
        this.businesses = businesses;
    }
}
