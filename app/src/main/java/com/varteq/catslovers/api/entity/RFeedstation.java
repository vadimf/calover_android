package com.varteq.catslovers.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RFeedstation extends ErrorData {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("is_public")
    @Expose
    private Boolean isPublic;
    @SerializedName("time_to_feed")
    @Expose
    private Integer timeToFeed;
    @SerializedName("created")
    @Expose
    private String created;
    @SerializedName("distance")
    @Expose
    private Double distance;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("time_to_feed_morning")
    @Expose
    private Integer timeToFeedMorning;
    @SerializedName("time_to_feed_evening")
    @Expose
    private Integer timeToFeedEvening;
    @SerializedName("last_feeding")
    @Expose
    private Integer lastFeeding;
    @SerializedName("feed_status")
    @Expose
    private String feedStatus;
    @SerializedName("permission")
    @Expose
    private RFeedstationPermissions permissions;
    @SerializedName("photos")
    @Expose
    private List<RPhoto> photos = null;

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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
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

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public RFeedstationPermissions getPermissions() {
        return permissions;
    }

    public void setPermissions(RFeedstationPermissions permissions) {
        this.permissions = permissions;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<RPhoto> photos) {
        this.photos = photos;
    }

    public Integer getTimeToFeedMorning() {
        return timeToFeedMorning;
    }

    public void setTimeToFeedMorning(Integer timeToFeedMorning) {
        this.timeToFeedMorning = timeToFeedMorning;
    }

    public Integer getTimeToFeedEvening() {
        return timeToFeedEvening;
    }

    public void setTimeToFeedEvening(Integer timeToFeedEvening) {
        this.timeToFeedEvening = timeToFeedEvening;
    }

    public Integer getLastFeeding() {
        return lastFeeding;
    }

    public void setLastFeeding(Integer lastFeeding) {
        this.lastFeeding = lastFeeding;
    }

    public String getFeedStatus() {
        return feedStatus;
    }

    public void setFeedStatus(String feedStatus) {
        this.feedStatus = feedStatus;
    }
}