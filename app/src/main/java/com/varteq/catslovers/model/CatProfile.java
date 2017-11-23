package com.varteq.catslovers.model;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CatProfile implements Serializable {

    private int id = -1;

    private Uri avatarUri;
    private String petName;
    private String nickname;
    private String description;
    private boolean isCastrated;
    private Date fleaTreatmentDate;

    private List<Uri> photoList;
    private List<GroupPartner> groupPartnersList;
    private List<Integer> colorsList;

    public CatProfile() {
    }

    public CatProfile(int id, String petName) {
        this.id = id;
        this.petName = petName;
    }

    public CatProfile(String petName) {
        this.petName = petName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Uri getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(Uri avatarUri) {
        this.avatarUri = avatarUri;
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
        return isCastrated;
    }

    public void setCastrated(boolean castrated) {
        isCastrated = castrated;
    }

    public Date getFleaTreatmentDate() {
        return fleaTreatmentDate;
    }

    public void setFleaTreatmentDate(Date fleaTreatmentDate) {
        this.fleaTreatmentDate = fleaTreatmentDate;
    }

    public List<Uri> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<Uri> photoList) {
        this.photoList = photoList;
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
}
