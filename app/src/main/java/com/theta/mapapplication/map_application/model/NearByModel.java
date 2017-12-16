package com.theta.mapapplication.map_application.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ashish on 16/12/17.
 */

public class NearByModel implements ClusterItem {

    String place_name;
    String vicinity;
    double lat;
    double lng;
    LatLng latLng;

    public NearByModel(String place_name, String vicinity, double lat, double lng) {
        this.place_name = place_name;
        this.vicinity = vicinity;
        this.lat = lat;
        this.lng = lng;
        latLng = getPosition();
    }

    @Override
    public LatLng getPosition() {
        LatLng latLng = new LatLng(lat, lng);
        return latLng;
    }

    @Override
    public String getTitle() {
        return place_name;
    }

    @Override
    public String getSnippet() {
        return vicinity;
    }
}
