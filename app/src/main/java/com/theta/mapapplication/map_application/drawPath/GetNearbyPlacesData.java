package com.theta.mapapplication.map_application.drawPath;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.theta.mapapplication.map_application.model.NearByModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ashish on 16/12/17.
 *
 * used to show NearbyPlaces
 *
 */

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    String googlePlacesData;
    GoogleMap mMap;
    String url;
    private ClusterManager<NearByModel> mClusterManager;
    Context context;

    public GetNearbyPlacesData(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlacesData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }//doInBackground

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String, String>> nearbyPlaceList = null;

        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String,String>> nearbyPlaceList)
    {
        mClusterManager = new ClusterManager(context, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        for(int i = 0;i<nearbyPlaceList.size() ; i++)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String , String> googlePlace = nearbyPlaceList.get(i);
            Log.d("onPostExecute","Entered into showing locations");

            // as I have used cluster no need to add individual marker
           /* String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble( googlePlace.get("lat") );
            double lng = Double.parseDouble( googlePlace.get("lng"));

            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName +" : "+ vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mMap.addMarker(markerOptions);*/
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble( googlePlace.get("lat")), Double.parseDouble( googlePlace.get("lng")))));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

            NearByModel nearByModel = new NearByModel(googlePlace.get("place_name"),googlePlace.get("vicinity"),
                    Double.parseDouble( googlePlace.get("lat")), Double.parseDouble( googlePlace.get("lng")));

            mClusterManager.addItem(nearByModel);
            mClusterManager.cluster();
        }


    }
}
