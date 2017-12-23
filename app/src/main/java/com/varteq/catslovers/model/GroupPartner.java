package com.varteq.catslovers.model;

import android.net.Uri;

public class GroupPartner {

    public enum Status {
        DEFAULT,
        PENDING
    }

    private Uri avatarUri;
    private String name;
    private Status status;
    private boolean isAdmin;

    public GroupPartner(Uri avatarUri, String name, Status status, boolean isAdmin) {
        this.avatarUri = avatarUri;
        this.name = name;
        this.status = status;
        this.isAdmin = isAdmin;
    }

    public Uri getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(Uri avatarUri) {
        this.avatarUri = avatarUri;
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
}
