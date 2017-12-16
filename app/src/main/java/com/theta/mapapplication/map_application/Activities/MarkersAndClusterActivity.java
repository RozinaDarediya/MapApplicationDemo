package com.theta.mapapplication.map_application.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.theta.mapapplication.R;
import com.theta.mapapplication.map_application.classes.CustomInfoViewAdapter;
import com.theta.mapapplication.map_application.global.Global;
import com.theta.mapapplication.map_application.model.LocationModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.theta.mapapplication.AppApplication.gson;
import static com.theta.mapapplication.map_application.global.Constants.PREF_KEY_MODEL_LIST;

public class MarkersAndClusterActivity extends AppCompatActivity implements OnMapReadyCallback {


    private LocationModel locationModel;
    private List<LocationModel> locationList;
    private MapFragment mapFragment;
    private List<LocationModel> locationModels;
    private List<String> places;
    private CameraUpdate cu;
    private ClusterManager<LocationModel> mClusterManager;
    private AutoCompleteTextView autoCompleteTextView1;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        addData();
        init();
        //storeAndRetrive();

        autoCompleteTextView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String selectedItem = (String) parent.getItemAtPosition(i);
                int pos = places.indexOf(selectedItem);
                // Toast.makeText(MarkersAndClusterActivity.this, selectedItem, Toast.LENGTH_LONG).show();
                //Toast.makeText(MarkersAndClusterActivity.this, pos + "", Toast.LENGTH_LONG).show();

                // to hide keyboard
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // move camera to selected place from the autoCompleteTextView
                LatLng current = new LatLng(locationList.get(pos).getLatitude(), locationList.get(pos).getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(current));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(9));
            }
        });

    }

    // Add data to the LocationModel
    private void addData() {
        locationList = new ArrayList<>();
        places = new ArrayList<>();

        locationModel = new LocationModel("Ahmedabad", 23.0225, 72.5714);
        places.add("Ahmedabad");
        locationList.add(locationModel);

        locationModel = new LocationModel("Surat", 21.1702, 72.8311);
        locationList.add(locationModel);
        places.add("Surat");

        locationModel = new LocationModel("Rajkot", 22.3039, 70.8022);
        locationList.add(locationModel);
        places.add("Rajkot");

        locationModel = new LocationModel("Porbandar", 21.6417, 69.6293);
        locationList.add(locationModel);
        places.add("Porbandar");

        locationModel = new LocationModel("Jamnagar", 22.4707, 70.0577);
        locationList.add(locationModel);
        places.add("Jamnagar");
    }

    private void init() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);
        autoCompleteTextView1 = findViewById(R.id.autoCompleteTextView1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, places);
        autoCompleteTextView1.setAdapter(adapter);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        this.googleMap = googleMap;
        //TODO : MAKE SURE YOU WILL NEED MODEL TO USE ClusterManager
        mClusterManager = new ClusterManager<LocationModel>(this, googleMap);

        //it will expand the cluster while clicking on it
        googleMap.setOnCameraIdleListener(mClusterManager);

        //click listener of marker
        googleMap.setOnMarkerClickListener(mClusterManager);

        // CustomClusterRenderer to change the marker color or set image
        final CustomClusterRenderer renderer = new CustomClusterRenderer(this, googleMap, mClusterManager);
        mClusterManager.setRenderer(renderer);

        // for Custom Info View Adapter
        mClusterManager.getMarkerCollection()
                .setOnInfoWindowAdapter(new CustomInfoViewAdapter(LayoutInflater.from(this)));

        //setting infowindowadapter on the marker in cluster
        googleMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        //click on infowindowadapter on the marker in cluster
        googleMap.setOnInfoWindowClickListener(mClusterManager);

        int size = locationList.size();
        for (int i = 0; i < locationList.size(); i++) {
            LocationModel model = locationList.get(i);
            String place = model.getPlaceName();
            double lat = model.getLatitude();
            double lon = model.getLongitude();

            mClusterManager.addItem(model);

            // As we have used ClusterManager only pass the array of latlng no need to pass individual marker
            /*googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title(place));*/
            // .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            cu = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 6);
        }

        mClusterManager.cluster();

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                googleMap.animateCamera(cu);
            }
        });


        //cluster click listener
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<LocationModel>() {
            @Override
            public boolean onClusterClick(Cluster<LocationModel> cluster) {

                Toast.makeText(MarkersAndClusterActivity.this, "Cluster click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<LocationModel>() {
            @Override
            public boolean onClusterItemClick(LocationModel locationModel) {
                Toast.makeText(MarkersAndClusterActivity.this, "You have clicked on " + locationModel.getPlaceName(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<LocationModel>() {
            @Override
            public void onClusterItemInfoWindowClick(LocationModel locationModel) {
                Toast.makeText(MarkersAndClusterActivity.this, "Clicked info window: " + locationModel.getPlaceName(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

//------------------
    /*
   *  CustomInfoViewAdapter class
   *  to show info on click of perticular marker
   */
    /*public class CustomInfoViewAdapter implements GoogleMap.InfoWindowAdapter {

        private final LayoutInflater mInflater;

        public CustomInfoViewAdapter(LayoutInflater inflater) {
            this.mInflater = inflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            *//*final View popup = mInflater.inflate(R.layout.info_window_layout, null);

            ((TextView) popup.findViewById(R.id.title)).setText(marker.getTitle());
            return popup;*//*
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            final View popup = mInflater.inflate(R.layout.info_window_layout, null);

            ((TextView) popup.findViewById(R.id.title)).setText(marker.getSnippet());

            return popup;
            // return null;
        }
    }*/

    /*
    *  CustomClusterRenderer class
    *  to create marker/ cluster icon using bitmap or using colors
    */
    public class CustomClusterRenderer extends DefaultClusterRenderer<LocationModel> {

        Context context;

        public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<LocationModel> clusterManager) {
            super(context, map, clusterManager);
            this.context = MarkersAndClusterActivity.this;
        }

        ////customize the marker
        @Override
        protected void onBeforeClusterItemRendered(LocationModel item, MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);

            // for image
            final BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker);
            markerOptions.icon(markerDescriptor).snippet(item.getPlaceName());
            // for change color
            //final BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        }

        //customize the cluster icon
        @Override
        protected void onBeforeClusterRendered(Cluster<LocationModel> cluster, MarkerOptions markerOptions) {
            super.onBeforeClusterRendered(cluster, markerOptions);

            //to customize the icon of cluster on map
            final IconGenerator mClusterIconGenerator;
            // in constructor
            mClusterIconGenerator = new IconGenerator(context.getApplicationContext());

            mClusterIconGenerator.setBackground(ContextCompat.getDrawable(context, R.drawable.background_circle));
            mClusterIconGenerator.setTextAppearance(R.style.AppTheme_WhiteTextAppearance);

            final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }


    /*
     *
     * This functions is to store the array in Shared-Prefrence usin GSON
     * butnot used in this project
     *
   */
    private void storeAndRetrive() {
        //store list/arraylist in sharedprefrence
        String list = gson.toJson(locationList);
        Global.storePreference(PREF_KEY_MODEL_LIST, list);

        //retrive list/arraylist in sharedprefrence
        Type type1 = new TypeToken<List<LocationModel>>() {
        }.getType();
        locationModels = gson.fromJson(list, type1);
        //getData();
    }

    private void getData() {
        //get data from sp and display it
        /* for (int i = 0; i < locationModels.size(); i++) {
            LocationModel model = locationModels.get(i);
            Log.e("place", model.getPlaceName());
            Log.e("latitude", String.valueOf(model.getLatitude()));
            Log.e("longitude", String.valueOf(model.getLongitude()));
        }*/
    }

}
