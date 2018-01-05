package com.varteq.catslovers.model;

public class GroupPartner {

    public enum Status {
        JOINED,
        INVITED,
        REQUESTED
    }

    private String avatar;
    private Integer userId;
    private String name;
    private String phone;
    private Status status;
    private boolean isAdmin;

    public GroupPartner(String avatar, String name, Status status, boolean isAdmin) {
        this.avatar = avatar;
        this.name = name;
        this.status = status;
        this.isAdmin = isAdmin;
    }

    public GroupPartner(String avatar, Integer userId, String name, String phone, Status status, boolean isAdmin) {
        this(avatar, name, status, isAdmin);
        this.userId = userId;
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
