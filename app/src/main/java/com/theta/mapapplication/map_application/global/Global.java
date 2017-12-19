package com.theta.mapapplication.map_application.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.maps.model.LatLng;
import com.theta.mapapplication.AppApplication;

import static com.theta.mapapplication.AppApplication.sharedPref;

/**
 * Created by ashish on 14/12/17.
 */

public class Global {

    public static String getMapsApiDirectionsUrl(LatLng origin, LatLng dest, String mode) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        //String sensor = "sensor=false&mode=driving&alternatives=true";
        String sensor = "sensor=false&mode=driving&alternatives=true";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }//getMapsApiDirectionsUrl

    // Function to check Internet Connectivity
    public static synchronized boolean isNetworkAvailable(Context context) {
        boolean isConnected = false;
        if (context != null) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }

        return isConnected;
    }//isNetworkAvailable


    // stores string value
    public static void storePreference(String key, String value) {
       SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    // stores int value
    public static void storePreference(String key, int value) {
        SharedPreferences.Editor editor = sharedPref
                .edit();
        editor.putInt(key, value);
        editor.commit();
    }

    // stores boolean value
    public static void storePreference(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPref
                .edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    // get string value from SharedPreference
    public static String getPreference(String key, String defValue) {
        return AppApplication.sharedPref.getString(key, defValue);
    }

    // get string int from SharedPreference
    public static int getPreference(String key, Integer defValue) {
        return sharedPref.getInt(key, defValue);
    }

    // get boolean value from SharedPreference
    public static Boolean getPreference(String key, Boolean defValue) {
        return sharedPref.getBoolean(key, defValue);
    }

    // remove whole preference
    public static void removeAllPreferences() {

        //Global.removePreferences(new String[]{Constants.IS_LOGGED_IN});
    }

    // remove string preference
    public static void removePreference(String key) {
        SharedPreferences.Editor editor = sharedPref
                .edit();
        editor.remove(key);
        editor.commit();
    }

    // clears the prefernce
    public static void clearPreferences() {
        SharedPreferences.Editor editor = sharedPref
                .edit();
        editor.clear();
        editor.commit();
    }

}
