package com.theta.mapapplication.map_application.model;

import java.util.List;

/**
 * Created by ashish on 19/12/17.
 */

public class RouteInfo {

    int tag;
    int route;
    String duration;
    String distance;

    List<RouteStepInfo> routeStepInfo;

    public RouteInfo(int tag, int route, String duration, String distance, List<RouteStepInfo> routeStepInfo) {
        this.tag = tag;
        this.route = route;
        this.duration = duration;
        this.distance = distance;
        this.routeStepInfo = routeStepInfo;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getRoute() {
        return route;
    }

    public void setRoute(int route) {
        this.route = route;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public List<RouteStepInfo> getRouteStepInfo() {
        return routeStepInfo;
    }

    public void setRouteStepInfo(List<RouteStepInfo> routeStepInfo) {
        this.routeStepInfo = routeStepInfo;
    }
}
