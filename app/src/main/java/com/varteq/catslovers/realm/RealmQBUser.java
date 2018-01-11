package com.varteq.catslovers.realm;

import com.quickblox.users.model.QBUser;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmQBUser extends RealmObject {

    @PrimaryKey
    private Integer id;
    private String fullName;
    private String email;
    private String login;
    private String phone;
    private String website;
    private String externalId;
    private String facebookId;
    private String twitterId;
    private String twitterDigitsId;
    private Integer blobId;
    private String tags;
    private String password;
    private String oldPassword;
    private String customData;
    private Date updatedAt;

    public RealmQBUser() {
    }

    public RealmQBUser(QBUser user) {
        init(user);
    }

    public void init(QBUser user) {
        id = user.getId();
        fullName = user.getFullName();
        email = user.getEmail();
        login = user.getLogin();
        phone = user.getPhone();
        website = user.getWebsite();
        externalId = user.getExternalId();
        facebookId = user.getFacebookId();
        twitterId = user.getTwitterId();
        twitterDigitsId = user.getTwitterDigitsId();
        blobId = user.getFileId();
        tags = user.getCustomData();
        password = user.getPassword();
        oldPassword = user.getOldPassword();
        customData = user.getCustomData();
        updatedAt = user.getUpdatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public String getTwitterDigitsId() {
        return twitterDigitsId;
    }

    public void setTwitterDigitsId(String twitterDigitsId) {
        this.twitterDigitsId = twitterDigitsId;
    }

    public Integer getBlobId() {
        return blobId;
    }

    public void setBlobId(Integer blobId) {
        this.blobId = blobId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public QBUser getAsQBUser() {
        QBUser user = new QBUser();
        user.setId(id);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setLogin(login);
        user.setPhone(phone);
        user.setWebsite(website);
        user.setExternalId(externalId);
        user.setFacebookId(facebookId);
        user.setTwitterId(twitterId);
        user.setTwitterDigitsId(twitterDigitsId);
        user.setFileId(blobId);
        user.setCustomData(tags);
        user.setPassword(password);
        user.setOldPassword(oldPassword);
        user.setCustomData(customData);
        user.setUpdatedAt(updatedAt);
        return user;
    }
}
