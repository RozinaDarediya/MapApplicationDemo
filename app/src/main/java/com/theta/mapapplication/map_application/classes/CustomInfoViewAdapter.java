package com.theta.mapapplication.map_application.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.theta.mapapplication.R;

/**
 * Created by ashish on 16/12/17.
 */
 /*
   *  CustomInfoViewAdapter class
   *  to show info on click of perticular marker
   */
public class CustomInfoViewAdapter implements GoogleMap.InfoWindowAdapter {

    private final LayoutInflater mInflater;

    public CustomInfoViewAdapter(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
            /*final View popup = mInflater.inflate(R.layout.info_window_layout, null);

            ((TextView) popup.findViewById(R.id.title)).setText(marker.getTitle());
            return popup;*/
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        final View popup = mInflater.inflate(R.layout.info_window_layout, null);

        ((TextView) popup.findViewById(R.id.title)).setText(marker.getSnippet());

        return popup;
        // return null;
    }
}
