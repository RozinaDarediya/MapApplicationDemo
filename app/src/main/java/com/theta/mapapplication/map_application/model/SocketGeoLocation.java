package com.theta.mapapplication.map_application.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SocketGeoLocation implements Serializable {

    @SerializedName("geo_lat")
    private double geoLat;

    @SerializedName("geo_long")
    private double geoLong;

    @SerializedName("geo_heading")
    private double geoHeading;

    @SerializedName("device_speed")
    private double deviceSpeed;

    @SerializedName("horizontalAccuracy")
    private double horizontalAccuracy;

    @SerializedName("geo_alt")
    private double geoAlt;

    public SocketGeoLocation() {
    }

    public SocketGeoLocation(double geoLat, double geoLong, double geoHeading, double deviceSpeed, double horizontalAccuracy, double geoAlt) {
        this.geoLat = geoLat;
        this.geoLong = geoLong;
        this.geoHeading = geoHeading;
        this.deviceSpeed = deviceSpeed;
        this.horizontalAccuracy = horizontalAccuracy;
        this.geoAlt = geoAlt;
    }

    public double getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(double geoLat) {
        this.geoLat = geoLat;
    }

    public double getGeoLong() {
        return geoLong;
    }

    public void setGeoLong(double geoLong) {
        this.geoLong = geoLong;
    }

    public double getGeoHeading() {
        return geoHeading;
    }

    public void setGeoHeading(double geoHeading) {
        this.geoHeading = geoHeading;
    }

    public double getDeviceSpeed() {
        return deviceSpeed;
    }

    public void setDeviceSpeed(double deviceSpeed) {
        this.deviceSpeed = deviceSpeed;
    }

    public double getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    public void setHorizontalAccuracy(double horizontalAccuracy) {
        this.horizontalAccuracy = horizontalAccuracy;
    }

    public double getGeoAlt() {
        return geoAlt;
    }

    public void setGeoAlt(double geoAlt) {
        this.geoAlt = geoAlt;
    }
}