package com.varteq.catslovers.model;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedPost implements Serializable {
    private String id;
    private Integer userId;
    private Integer stationId;
    private Date date;
    private Uri previewUri;
    private Uri mediaUri;
    private String avatar;
    private File previewFile;
    private String previewName;
    private String mediaName;
    private String avatarName;
    private String name;
    private String message;
    private List<Integer> likes;
    private List<String> commentsIds;
    private FeedPostType type;

    public enum FeedPostType {
        TEXT,
        PICTURE,
        VIDEO
    }

    public FeedPost(String id, Date date, String avatar, String name, String message, FeedPostType type) {
        this.id = id;
        this.date = date;
        this.avatar = avatar;
        this.name = name;
        this.message = message;
        this.type = type;
    }

    public FeedPost(String id, Date date, String avatar, String userName, String message, Uri mediaUri, FeedPostType type) {
        this(id, date, avatar, userName, message, type);
        this.mediaUri = mediaUri;
    }

    public FeedPost(String id, Integer userId, Date date, String avatar, String userName, String message,
                    String previewName, String mediaName, List<Integer> likes, Integer stationId, List<String> commentsIds, FeedPostType type) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.avatar = avatar;
        this.previewName = previewName;
        this.mediaName = mediaName;
        this.name = userName;
        this.message = message;
        this.type = type;
        this.likes = likes;
        this.stationId = stationId;
        this.commentsIds = commentsIds;
    }

    public FeedPost(String id, Date date, Uri previewUri, Uri mediaUri, String avatar, String name, String message, List<Integer> likes, FeedPostType type) {
        this.id = id;
        this.date = date;
        this.previewUri = previewUri;
        this.mediaUri = mediaUri;
        this.avatar = avatar;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public List<Integer> getLikes() {
        return likes;
    }

    public void setLikes(List<Integer> likes) {
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

    public Integer getUserId() {
        return userId;
    }

    public Integer getStationId() {
        return stationId;
    }

    public List<String> getCommentsIds() {
        return commentsIds;
    }

    public int getCommentsCount() {
        return commentsIds != null ? commentsIds.size() : 0;
    }

    public boolean getIsUserLiked(Integer id) {
        if (likes != null && likes.contains(id))
            return true;
        else return false;
    }

    public void onUserLiked(Integer currentUserId, boolean isLiked) {
        if (isLiked) {
            if (likes == null)
                likes = new ArrayList<>();
            likes.add(currentUserId);
        } else if (likes != null)
            likes.remove(currentUserId);
    }
}
