package com.theta.mapapplication.map_application.classes;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.theta.mapapplication.map_application.global.Global;
import com.theta.mapapplication.map_application.model.RouteDistanceAndDurationModel;
import com.theta.mapapplication.map_application.model.RouteInfo;
import com.theta.mapapplication.map_application.model.RouteStepInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.theta.mapapplication.AppApplication.gson;
import static com.theta.mapapplication.map_application.global.Constants.PREF_KEY_DIAT_AND_TIME;
import static com.theta.mapapplication.map_application.global.Constants.PREF_KEY_ROUTE_INFO;
import static com.theta.mapapplication.map_application.global.Constants.PREF_KEY_ROUTE_INFO_CLASS;

/**
 * Created by ashish on 15/12/17.
 */

public class PathJSONParser {

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        String html_instructions;
        String duration;
        String distance;
        String dist;
        String time;
        String img_icon = null;
        ArrayList<RouteStepInfo> infoArrayList = null;
        ArrayList<RouteInfo> routeInfoList = new ArrayList<>();
        try {
            jRoutes = jObject.getJSONArray("routes");
            routeInfoList = new ArrayList<>();
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                JSONObject jsonLeg = jLegs.getJSONObject(0);
                JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                dist = (String) jsonDistance.get("text");
                time = (String) jsonDuration.get("text");
                RouteDistanceAndDurationModel distanceAndDurationModel = new RouteDistanceAndDurationModel(dist, time); //---
                String distAndTime = gson.toJson(distanceAndDurationModel); //--
                Global.storePreference(PREF_KEY_DIAT_AND_TIME, distAndTime);

                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
                for (int j = 0; j < jLegs.length(); j++) {
                    infoArrayList = new ArrayList<>();
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }

                        duration = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("duration")).get("text");
                        distance = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).get("text");
                        html_instructions = (String) ((JSONObject) jSteps.get(k)).get("html_instructions");
                        if ((boolean) ((JSONObject) jSteps.get(k)).has("maneuver")) {
                            img_icon = (String) ((JSONObject) jSteps.get(k)).get("maneuver");
                        }

                        RouteStepInfo routeStepInfo = new RouteStepInfo(duration, distance, html_instructions, img_icon);
                        infoArrayList.add(routeStepInfo);
                    }//jSteps
                    if (i == 0) {
                        String stepInfo = gson.toJson(infoArrayList);
                        Global.storePreference(PREF_KEY_ROUTE_INFO, stepInfo);
                    }

                    routes.add(path);
                }// one route
                RouteInfo routeInfo = new RouteInfo(i, i, time, dist, infoArrayList);
                routeInfoList.add(routeInfo);
            }// jRoutestotal routes

            String routeInfoClass = gson.toJson(routeInfoList);
            Global.storePreference(PREF_KEY_ROUTE_INFO_CLASS, routeInfoClass);
            String rd = Global.getPreference(PREF_KEY_ROUTE_INFO_CLASS, "");
            Log.e("msg", rd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;

    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
