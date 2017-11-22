package com.varteq.catslovers.model;

import android.net.Uri;

public class GroupPartner {

    private Uri avatarUri;
    private String name;
    private boolean isAdmin;

    public GroupPartner(Uri avatarUri, String name, boolean isAdmin) {
        this.avatarUri = avatarUri;
        this.name = name;
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
}
