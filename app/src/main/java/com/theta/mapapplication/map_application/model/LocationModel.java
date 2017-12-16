package com.theta.mapapplication.map_application.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ashish on 14/12/17.
 */

public class LocationModel implements ClusterItem {

    String placeName;
    double latitude;
    double longitude;

    public LocationModel(String placeName, double latitude, double longitude) {
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public LatLng getPosition() {
        LatLng latLng = new LatLng(latitude, longitude);
        return latLng;
    }

    @Override
    public String getTitle() {
        return placeName;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
