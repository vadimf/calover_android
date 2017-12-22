package com.varteq.catslovers.model;

import android.net.Uri;

import java.io.File;
import java.util.Date;

public class FeedPost {
    private String id;
    private Date date;
    private Uri previewUri;
    private Uri mediaUri;
    private Uri avatarUri;
    private File previewFile;
    private String previewName;
    private String mediaName;
    private String avatarName;
    private String name;
    private String message;
    private int likes;
    private FeedPostType type;

    public enum FeedPostType {
        TEXT,
        PICTURE,
        VIDEO
    }

    public FeedPost(String id, Date date, Uri avatarUri, String name, String message, FeedPostType type) {
        this.id = id;
        this.date = date;
        this.avatarUri = avatarUri;
        this.name = name;
        this.message = message;
        this.type = type;
    }

    public FeedPost(String id, Date date, Uri avatarUri, String userName, String message, Uri mediaUri, FeedPostType type) {
        this(id, date, avatarUri, userName, message, type);
        this.mediaUri = mediaUri;
    }

    public FeedPost(String id, Date date, Uri avatarUri, String userName, String message, String previewName, String mediaName, FeedPostType type) {
        this.id = id;
        this.date = date;
        this.avatarUri = avatarUri;
        this.previewName = previewName;
        this.mediaName = mediaName;
        this.name = userName;
        this.message = message;
        this.type = type;
    }

    public FeedPost(String id, Date date, Uri previewUri, Uri mediaUri, Uri avatarUri, String name, String message, int likes, FeedPostType type) {
        this.id = id;
        this.date = date;
        this.previewUri = previewUri;
        this.mediaUri = mediaUri;
        this.avatarUri = avatarUri;
        this.name = name;
        this.message = message;
        this.likes = likes;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public FeedPostType getType() {
        return type;
    }

    public void setType(FeedPostType type) {
        this.type = type;
    }

    public String getPreviewName() {
        return previewName;
    }

    public String getMediaName() {
        return mediaName;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public File getPreviewFile() {
        return previewFile;
    }

    public void setPreviewFile(File previewFile) {
        this.previewFile = previewFile;
    }
}
