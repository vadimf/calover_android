package com.varteq.catslovers.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class PhotoWithPreview implements Serializable, Parcelable {

    private Integer id;
    private String photo;
    private String thumbnail;
    private Action expectedAction;

    public enum Action {
        ADD,
        CHANGE,
        DELETE
    }

    public PhotoWithPreview(Integer id, String photo, String thumbnail) {
        this(photo, thumbnail, null);
        this.id = id;
    }

    public PhotoWithPreview(String photo, String thumbnail, Action expectedAction) {
        this.photo = photo;
        this.thumbnail = thumbnail;
        this.expectedAction = expectedAction;
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

    public Action getExpectedAction() {
        return expectedAction;
    }

    public void setExpectedAction(Action expectedAction) {
        this.expectedAction = expectedAction;
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
        dest.writeInt(this.expectedAction == null ? -1 : this.expectedAction.ordinal());
    }

    protected PhotoWithPreview(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.photo = in.readString();
        this.thumbnail = in.readString();
        int tmpExpectedAction = in.readInt();
        this.expectedAction = tmpExpectedAction == -1 ? null : Action.values()[tmpExpectedAction];
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
