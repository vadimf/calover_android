package com.varteq.catslovers.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CatProfile implements Serializable {

    private Integer id;

    private PhotoWithPreview avatar;
    private String petName;
    private String nickname;
    private Date birthday;
    private String sex;
    private Float weight;
    private String description;
    private Boolean isCastrated;
    private Date fleaTreatmentDate;
    private Integer feedstationId;

    private List<GroupPartner> groupPartnersList;
    private List<Integer> colorsList;
    private Status type;
    private Feedstation.UserRole userRole;
    private GroupPartner.Status status;
    private List<PhotoWithPreview> photos = null;

    public enum Status {
        PET,
        STRAY
    }

    public CatProfile() {
    }

    public CatProfile(boolean isAdmin) {
        userRole = Feedstation.UserRole.ADMIN;
    }

    public CatProfile(int id, String petName) {
        this.id = id;
        this.petName = petName;
    }

    public CatProfile(String petName) {
        this.petName = petName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PhotoWithPreview getAvatar() {
        return avatar;
    }

    public void setAvatar(PhotoWithPreview avatar) {
        this.avatar = avatar;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCastrated() {
        return isCastrated != null ? isCastrated : false;
    }

    public void setCastrated(Boolean castrated) {
        isCastrated = castrated;
    }

    public Date getFleaTreatmentDate() {
        return fleaTreatmentDate;
    }

    public void setFleaTreatmentDate(Date fleaTreatmentDate) {
        this.fleaTreatmentDate = fleaTreatmentDate;
    }

    public List<GroupPartner> getGroupPartnersList() {
        return groupPartnersList;
    }

    public void setGroupPartnersList(List<GroupPartner> groupPartnersList) {
        this.groupPartnersList = groupPartnersList;
    }

    public List<Integer> getColorsList() {
        return colorsList;
    }

    public void setColorsList(List<Integer> colorsList) {
        this.colorsList = colorsList;
    }

    public boolean isNew() {
        return id == -1;
    }

    public Status getType() {
        return type;
    }

    public void setType(Status type) {
        this.type = type;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Integer getFeedstationId() {
        return feedstationId;
    }

    public void setFeedstationId(int feedstationId) {
        this.feedstationId = feedstationId;
    }

    public Feedstation.UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(Feedstation.UserRole userRole) {
        this.userRole = userRole;
    }

    public GroupPartner.Status getStatus() {
        return status;
    }

    public void setStatus(GroupPartner.Status status) {
        this.status = status;
    }

    public List<PhotoWithPreview> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoWithPreview> photos) {
        this.photos = photos;
    }
}
