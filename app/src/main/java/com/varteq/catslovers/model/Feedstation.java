package com.varteq.catslovers.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

public class Feedstation implements Parcelable {

    public enum UserRole {
        ADMIN,
        USER
    }

    public enum FeedStatus {
        NORMAL,
        HUNGRY,
        STARVING
    }

    private Integer id;
    private String name;
    private String description;
    private LatLng location;
    private String address;
    private Boolean isPublic;
    private Date timeToFeed;
    private Date timeToEat1;
    private Date timeToEat2;
    private Date lastFeeding;
    private FeedStatus feedStatus;
    private String createdUserId;
    private UserRole userRole;
    private List<PhotoWithPreview> photos = null;
    private List<PhotoWithPreview> photosToRemove = null;
    private GroupPartner.Status status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Date getTimeToFeed() {
        return timeToFeed;
    }

    public void setTimeToFeed(Date timeToFeed) {
        this.timeToFeed = timeToFeed;
    }

    public void setCreatedUserId(String createdUserId) {
        this.createdUserId = createdUserId;
    }

    public String getCreatedUserId() {
        return createdUserId;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public GroupPartner.Status getStatus() {
        return status;
    }

    public void setStatus(GroupPartner.Status status) {
        this.status = status;
    }

    public List<PhotoWithPreview> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoWithPreview> photos) {
        this.photos = photos;
    }

    public Date getTimeToEat1() {
        return timeToEat1;
    }

    public void setTimeToEat1(Date timeToEat1) {
        this.timeToEat1 = timeToEat1;
    }

    public Date getTimeToEat2() {
        return timeToEat2;
    }

    public void setTimeToEat2(Date timeToEat2) {
        this.timeToEat2 = timeToEat2;
    }

    public FeedStatus getFeedStatus() {
        return feedStatus;
    }

    public void setFeedStatus(FeedStatus feedStatus) {
        this.feedStatus = feedStatus;
    }

    public Date getLastFeeding() {
        return lastFeeding;
    }

    public void setLastFeeding(Date lastFeeding) {
        this.lastFeeding = lastFeeding;
    }

    public List<PhotoWithPreview> getPhotosToRemove() {
        return photosToRemove;
    }

    public void setPhotosToRemove(List<PhotoWithPreview> photosToRemove) {
        this.photosToRemove = photosToRemove;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeParcelable(this.location, flags);
        dest.writeString(this.address);
        dest.writeValue(this.isPublic);
        dest.writeLong(this.timeToFeed != null ? this.timeToFeed.getTime() : -1);
        dest.writeLong(this.timeToEat1 != null ? this.timeToEat1.getTime() : -1);
        dest.writeLong(this.timeToEat2 != null ? this.timeToEat2.getTime() : -1);
        dest.writeLong(this.lastFeeding != null ? this.lastFeeding.getTime() : -1);
        dest.writeInt(this.feedStatus == null ? -1 : this.feedStatus.ordinal());
        dest.writeString(this.createdUserId);
        dest.writeInt(this.userRole == null ? -1 : this.userRole.ordinal());
        dest.writeTypedList(this.photos);
        dest.writeTypedList(this.photosToRemove);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
    }

    public Feedstation() {
    }

    protected Feedstation(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.name = in.readString();
        this.description = in.readString();
        this.location = in.readParcelable(LatLng.class.getClassLoader());
        this.address = in.readString();
        this.isPublic = (Boolean) in.readValue(Boolean.class.getClassLoader());
        long tmpTimeToFeed = in.readLong();
        this.timeToFeed = tmpTimeToFeed == -1 ? null : new Date(tmpTimeToFeed);
        long tmpTimeToEat1 = in.readLong();
        this.timeToEat1 = tmpTimeToEat1 == -1 ? null : new Date(tmpTimeToEat1);
        long tmpTimeToEat2 = in.readLong();
        this.timeToEat2 = tmpTimeToEat2 == -1 ? null : new Date(tmpTimeToEat2);
        long tmpLastFeeding = in.readLong();
        this.lastFeeding = tmpLastFeeding == -1 ? null : new Date(tmpLastFeeding);
        int tmpFeedStatus = in.readInt();
        this.feedStatus = tmpFeedStatus == -1 ? null : FeedStatus.values()[tmpFeedStatus];
        this.createdUserId = in.readString();
        int tmpUserRole = in.readInt();
        this.userRole = tmpUserRole == -1 ? null : UserRole.values()[tmpUserRole];
        this.photos = in.createTypedArrayList(PhotoWithPreview.CREATOR);
        this.photosToRemove = in.createTypedArrayList(PhotoWithPreview.CREATOR);
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : GroupPartner.Status.values()[tmpStatus];
    }

    public static final Creator<Feedstation> CREATOR = new Creator<Feedstation>() {
        @Override
        public Feedstation createFromParcel(Parcel source) {
            return new Feedstation(source);
        }

        @Override
        public Feedstation[] newArray(int size) {
            return new Feedstation[size];
        }
    };
}