package com.varteq.catslovers.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class PhotoWithPreview implements Serializable, Parcelable {

    private Integer id;
    private String photo;
    private String thumbnail;
    private boolean needToUpdate;

    public PhotoWithPreview(Integer id, String photo, String thumbnail) {
        this(photo, thumbnail);
        this.id = id;
    }

    public PhotoWithPreview(String photo, String thumbnail) {
        this.photo = photo;
        this.thumbnail = thumbnail;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isNeedToUpdate() {
        return needToUpdate;
    }

    public void setNeedToUpdate(boolean needToUpdate) {
        this.needToUpdate = needToUpdate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.photo);
        dest.writeString(this.thumbnail);
        dest.writeByte(this.needToUpdate ? (byte) 1 : (byte) 0);
    }

    protected PhotoWithPreview(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.photo = in.readString();
        this.thumbnail = in.readString();
        this.needToUpdate = in.readByte() != 0;
    }

    public static final Creator<PhotoWithPreview> CREATOR = new Creator<PhotoWithPreview>() {
        @Override
        public PhotoWithPreview createFromParcel(Parcel source) {
            return new PhotoWithPreview(source);
        }

        @Override
        public PhotoWithPreview[] newArray(int size) {
            return new PhotoWithPreview[size];
        }
    };
}
