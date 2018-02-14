package com.varteq.catslovers.model;

import java.io.Serializable;
import java.util.Date;

public class PostComment implements Serializable {
    private String id;
    private Integer postId;
    private Integer userId;
    private String avatar;
    private String userName;
    private Date date;
    private String message;

    public PostComment(String id, Integer postId, Integer userId, String avatar, String userName, Date date, String message) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.avatar = avatar;
        this.userName = userName;
        this.date = date;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getPostId() {
        return postId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
