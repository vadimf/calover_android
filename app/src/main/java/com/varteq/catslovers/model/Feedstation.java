package com.varteq.catslovers.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Feedstation implements Parcelable {

    public enum UserRole {
        ADMIN,
        USER
    }

    private Integer id;
    private String name;
    private String description;
    private LatLng location;
    private String address;
    private Boolean isPublic;
    private Date timeToFeed;
    private String createdUserId;
    private UserRole userRole;
    private List<PhotoWithPreview> photos = null;
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
        dest.writeString(this.createdUserId);
        dest.writeInt(this.userRole == null ? -1 : this.userRole.ordinal());
        dest.writeList(this.photos);
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
        this.createdUserId = in.readString();
        int tmpUserRole = in.readInt();
        this.userRole = tmpUserRole == -1 ? null : UserRole.values()[tmpUserRole];
        this.photos = new ArrayList<PhotoWithPreview>();
        in.readList(this.photos, PhotoWithPreview.class.getClassLoader());
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