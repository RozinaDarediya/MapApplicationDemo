package com.theta.mapapplication.map_application.model;

/**
 * Created by ashish on 18/12/17.
 */

public class RouteDistanceAndDurationModel {

    String dist;
    String time;

    public RouteDistanceAndDurationModel(String dist, String time) {
        this.dist = dist;
        this.time = time;
    }

    public String getDist() {
        return dist;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
