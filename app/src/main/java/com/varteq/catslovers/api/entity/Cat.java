package com.varteq.catslovers.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Cat extends ErrorData {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("nickname")
    @Expose
    private String nickname;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("age")
    @Expose
    private Integer age;
    @SerializedName("sex")
    @Expose
    private Object sex;
    @SerializedName("weight")
    @Expose
    private Float weight;
    @SerializedName("castrated")
    @Expose
    private Boolean castrated;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("next_flea_treatment")
    @Expose
    private Integer nextFleaTreatment;
    @SerializedName("permission")
    @Expose
    private RFeedstationPermissions permissions;
    @SerializedName("photos")
    @Expose
    private List<RPhoto> photos = null;
    @SerializedName("feedstation")
    @Expose
    private RFeedstation feedstation;
    @SerializedName("avatar_url")
    @Expose
    private String avatarUrl;
    @SerializedName("avatar_url_thumbnail")
    @Expose
    private String avatarUrlThumbnail;

    public RFeedstation getFeedstation() {
        return feedstation;
    }

    public void setFeedstation(RFeedstation feedstation) {
        this.feedstation = feedstation;
    }

    public List<RPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<RPhoto> photos) {
        this.photos = photos;
    }

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Object getSex() {
        return sex;
    }

    public void setSex(Object sex) {
        this.sex = sex;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Boolean getCastrated() {
        return castrated;
    }

    public void setCastrated(Boolean castrated) {
        this.castrated = castrated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNextFleaTreatment() {
        return nextFleaTreatment;
    }

    public void setNextFleaTreatment(Integer nextFleaTreatment) {
        this.nextFleaTreatment = nextFleaTreatment;
    }

    public RFeedstationPermissions getPermissions() {
        return permissions;
    }

    public void setPermissions(RFeedstationPermissions permissions) {
        this.permissions = permissions;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrlThumbnail() {
        return avatarUrlThumbnail;
    }

    public void setAvatarUrlThumbnail(String avatarUrlThumbnail) {
        this.avatarUrlThumbnail = avatarUrlThumbnail;
    }
}
