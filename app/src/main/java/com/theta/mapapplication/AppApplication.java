package com.theta.mapapplication;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

/**
 * Created by ashish on 14/12/17.
 */

public class AppApplication extends Application {

    public static SharedPreferences sharedPref;
    public static Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        gson = new Gson();
    }
}
