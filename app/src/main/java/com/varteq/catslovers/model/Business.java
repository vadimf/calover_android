package com.varteq.catslovers.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Business implements Parcelable {

    private Integer id;
    private String name;
    private String address;
    private LatLng location;
    private String link;
    private String description;
    private String phone;
    private String distance;
    private Category category;
    private String openHours;

    public Business(Parcel in) {
        this.id = in.readInt();

        double[] doubles = new double[2];
        in.readDoubleArray(doubles);
        this.location = new LatLng(doubles[0], doubles[1]);

        String[] strings = new String[8];
        in.readStringArray(strings);
        this.name = strings[0];
        this.address = strings[1];
        this.link = strings[2];
        this.description = strings[3];
        this.phone = strings[4];
        this.distance = strings[5];
        this.openHours = strings[6];
        String category = strings[7];
        if (category != null)
            this.category = Category.valueOf(category);
    }

    public Business() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        int id = 0;
        if (getId() != null)
            id = getId();
        parcel.writeInt(id);
        parcel.writeDoubleArray(new double[]{getLocation().latitude, getLocation().longitude});
        String category = null;
        if (getCategory() != null)
            category = getCategory().name();
        parcel.writeStringArray(new String[]{getName(), getAddress(), getLink(), getDescription(), getPhone(), getDistance(), getOpenHours(), category});
    }

    public static final Parcelable.Creator<Business> CREATOR = new Parcelable.Creator<Business>() {

        @Override
        public Business createFromParcel(Parcel source) {
            return new Business(source);
        }

        @Override
        public Business[] newArray(int size) {
            return new Business[size];
        }
    };

    public enum Category {
        FOOD,
        VETERINARY
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getOpenHours() {
        return openHours;
    }

    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }
}
