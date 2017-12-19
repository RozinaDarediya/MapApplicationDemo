package com.theta.mapapplication.map_application.model;

/**
 * Created by ashish on 18/12/17.
 */

public class RouteStepInfo {

    String duration;
    String distance;
    String html_instructions;
    String maneuver;

    public RouteStepInfo(String duration, String distance, String html_instructions, String maneuver) {
        this.duration = duration;
        this.distance = distance;
        this.html_instructions = html_instructions;
        this.maneuver = maneuver;
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

    public String getHtml_instructions() {
        return html_instructions;
    }

    public void setHtml_instructions(String html_instructions) {
        this.html_instructions = html_instructions;
    }

    public String getManeuver() {
        return maneuver;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }
}
