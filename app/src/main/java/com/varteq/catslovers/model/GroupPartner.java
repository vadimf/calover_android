package com.varteq.catslovers.model;

import android.net.Uri;

public class GroupPartner {

    public enum Status {
        JOINED,
        INVITED,
        REQUESTED
    }

    private Uri avatarUri;
    private Integer userId;
    private String name;
    private String phone;
    private Status status;
    private boolean isAdmin;

    public GroupPartner(Uri avatarUri, String name, Status status, boolean isAdmin) {
        this.avatarUri = avatarUri;
        this.name = name;
        this.status = status;
        this.isAdmin = isAdmin;
    }

    public GroupPartner(Uri avatarUri, Integer userId, String name, String phone, Status status, boolean isAdmin) {
        this(avatarUri, name, status, isAdmin);
        this.userId = userId;
        this.phone = phone;
    }

    public Uri getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(Uri avatarUri) {
        this.avatarUri = avatarUri;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
