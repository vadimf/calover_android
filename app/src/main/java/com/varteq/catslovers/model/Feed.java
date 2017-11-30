package com.varteq.catslovers.model;

import android.net.Uri;

import java.util.Date;

public class Feed {
    private int id;
    private Date date;
    private Uri previewUri;
    private Uri mediaUri;
    private int mediaType;
    private Uri avatarUri;
    private String name;
    private String message;
    private int likes;

    public Feed(int id, Date date, int mediaType, Uri avatarUri, String name, String message, int likes) {
        this.id = id;
        this.date = date;
        this.mediaType = mediaType;
        this.avatarUri = avatarUri;
        this.name = name;
        this.message = message;
        this.likes = likes;
    }

    public Feed(int id, Date date, int mediaType, Uri previewUri, Uri mediaUri, Uri avatarUri, String name, String message, int likes) {
        this.id = id;
        this.date = date;
        this.mediaType = mediaType;
        this.previewUri = previewUri;
        this.mediaUri = mediaUri;
        this.avatarUri = avatarUri;
        this.name = name;
        this.message = message;
        this.likes = likes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Uri getPreviewUri() {
        return previewUri;
    }

    public void setPreviewUri(Uri previewUri) {
        this.previewUri = previewUri;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public Uri getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(Uri mediaUri) {
        this.mediaUri = mediaUri;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }
}
