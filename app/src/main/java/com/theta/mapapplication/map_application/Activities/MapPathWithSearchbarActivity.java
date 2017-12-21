package com.theta.mapapplication.map_application.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.ClusterManager;
import com.theta.mapapplication.R;
import com.theta.mapapplication.map_application.adapter.RootInfoAdapter;
import com.theta.mapapplication.map_application.classes.MapHttpConnection;
import com.theta.mapapplication.map_application.classes.PathJSONParser;
import com.theta.mapapplication.map_application.drawPath.DataParser;
import com.theta.mapapplication.map_application.drawPath.DownloadUrl;
import com.theta.mapapplication.map_application.global.Constants;
import com.theta.mapapplication.map_application.global.Global;
import com.theta.mapapplication.map_application.model.NearByModel;
import com.theta.mapapplication.map_application.model.RouteDistanceAndDurationModel;
import com.theta.mapapplication.map_application.model.RouteInfo;
import com.theta.mapapplication.map_application.model.RouteStepInfo;
import com.theta.mapapplication.map_application.webservice.Api;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.theta.mapapplication.AppApplication.gson;
import static com.theta.mapapplication.map_application.global.Constants.PREF_KEY_DIAT_AND_TIME;
import static com.theta.mapapplication.map_application.global.Constants.PREF_KEY_ROUTE_INFO;
import static com.theta.mapapplication.map_application.global.Constants.PREF_KEY_ROUTE_INFO_CLASS;
import static com.theta.mapapplication.map_application.global.Global.getMapsApiDirectionsUrl;

public class MapPathWithSearchbarActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnPolylineClickListener, PlaceSelectionListener {

    private GoogleMap mMap;
    private Button view1;
    private Marker marker;
    private Button btndirections;
    private Button btnHospitals;
    private Button btnSchools;
    private Button btnRestarents;
    private TextView tvTotalDistance;
    private TextView tvMoreInfo;
    private TextView txDur;
    private TextView txYourLoc;
    private TextView txYourDest;
    private LinearLayout infoLayout;
    private List<LatLng> latLngList;
    private Polyline myPolyline;
    private double des_latitude;
    private double des_longitude;
    private ArrayList<Polyline> polylineArrayList;
    private Snackbar snackbar;
    private boolean flag;
    private String mode;
    private String destinationPlace;
    private String totalDestDur;
    private SupportMapFragment mapFragment;
    private ClusterManager<NearByModel> mClusterManager;
    private PlaceAutocompleteFragment autocompleteFragment;
    int PROXIMITY_RADIUS = 10000;

    //----- related to bottom sheet ------\\
    BottomSheetBehavior behavior;
    RecyclerView recyclerView;
    private RootInfoAdapter mAdapter;
    CoordinatorLayout coordinatorLayout;

    //Related to socket
    private Socket mSocket;
    private Marker socketMarker;
    private TextView tvStartTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_path_with_searchbar);

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete);

        init();
        socket();

        if (!Global.isNetworkAvailable(this)) {
            snackbar = Snackbar.make(tvTotalDistance, "No internet connection !", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MapPathWithSearchbarActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
            snackbar.show();
        } else {
            autocompleteFragment.setOnPlaceSelectedListener(this);
            btndirections.setOnClickListener(this);
            view1.setOnClickListener(this);
            btnHospitals.setOnClickListener(this);
            btnSchools.setOnClickListener(this);
            btnRestarents.setOnClickListener(this);
            tvMoreInfo.setOnClickListener(this);
        }
    }

    private void socket() {
        try {
            tvStartTrip = findViewById(R.id.tvStartTrip);
            tvStartTrip.setOnClickListener(this);
            mSocket = IO.socket(Api.SocketMainUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        infoLayout = findViewById(R.id.infoLayout);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        mode = "mode=driving";
        tvMoreInfo = findViewById(R.id.tvMoreInfo);
        txDur = findViewById(R.id.txDur);
        txYourDest = findViewById(R.id.txYourDest);
        txYourLoc = findViewById(R.id.txYourLoc);
        tvTotalDistance = findViewById(R.id.tvTotalDistance);
        btndirections = findViewById(R.id.btndirections);
        btnHospitals = findViewById(R.id.btnHospitals);
        btnSchools = findViewById(R.id.btnSchools);
        btnRestarents = findViewById(R.id.btnRestarents);
        view1 = findViewById(R.id.view1);

        //-----  bottomSheet
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }//init

    public void getPath() {
        latLngList = new ArrayList<>();
        latLngList.add(new LatLng(Constants.latitude, Constants.longitude));
        latLngList.add(new LatLng(des_latitude, des_longitude));

        String url = getMapsApiDirectionsUrl(latLngList.get(0), latLngList.get(1), mode);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.satelliteView:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapView:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.terrianView:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.modeDriving:
                mode = "mode=driving";
                getPath();
                Toast.makeText(this, "Driving", Toast.LENGTH_SHORT).show();
                break;
            case R.id.modeBicycling:
                mode = "mode=bicycling";
                getPath();
                Toast.makeText(this, "Bicycling", Toast.LENGTH_SHORT).show();
                break;
            case R.id.modeWalking:
                mode = "mode=walking";
                getPath();
                Toast.makeText(this, "Walking", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData(this);

        if (view.getId() == R.id.btnHospitals) {
            mMap.clear();
            flag = true;
            String hospital = "hospital";
            String url = getUrl(Constants.latitude, Constants.longitude, hospital);

            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(MapPathWithSearchbarActivity.this, "Showing Nearby Hospitals", Toast.LENGTH_LONG).show();
        }
        if (view.getId() == R.id.btnSchools) {
            mMap.clear();
            flag = true;
            String hospital = "school";
            String url = getUrl(Constants.latitude, Constants.longitude, hospital);

            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(MapPathWithSearchbarActivity.this, "Showing Nearby school", Toast.LENGTH_LONG).show();
        }
        if (view.getId() == R.id.btnRestarents) {
            mMap.clear();
            flag = true;
            String hospital = "restaurant";
            String url = getUrl(Constants.latitude, Constants.longitude, hospital);

            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(MapPathWithSearchbarActivity.this, "Showing Nearby school", Toast.LENGTH_LONG).show();
        }
        if (view.getId() == R.id.btndirections) {
            getPath();
        }
        if (view.getId() == R.id.view1) {
            marker.remove();
            if (polylineArrayList.size() > 0) {
                for (int i = 0; i < polylineArrayList.size(); i++) {
                    if (polylineArrayList.get(i) != null) {
                        polylineArrayList.get(i).remove();
                        // polylineArrayList.get(i) = null;
                    }
                }
            }
            myPolyline = null;
            // for single polyline
                /* if (myPolyline != null) { myPolyline.remove();    myPolyline = null;}*/
            des_latitude = 0.0;
            des_longitude = 0.0;
            tvTotalDistance.setVisibility(View.INVISIBLE);
            tvMoreInfo.setVisibility(View.INVISIBLE);
            Toast.makeText(MapPathWithSearchbarActivity.this, "You have cancle the destination", Toast.LENGTH_SHORT).show();
            autocompleteFragment.setText("");
           // disConnectSocket();
        }
        if (view.getId() == R.id.tvMoreInfo) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            txDur.setText(totalDestDur);
            txYourDest.setText(destinationPlace);
        }
        if (view.getId() == R.id.tvStartTrip){
            //connectSocket();
        }

    }

   /* private void connectSocket() {
        socketMarker = mMap.
                addMarker(new MarkerOptions().position(new LatLng(Constants.latitude, Constants.longitude)).
                        title("You Are Here!").
                        icon(BitmapDescriptorFactory.
                                fromResource(R.drawable.ic_carmarker))

                );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(Constants.latitude, Constants.longitude)), 16.0f));
        mSocket.connect();
        mSocket.on(Api.SocketActionGetKey + Api.SocketDriverID, onNewMessage);
        Log.e("SocketKey=" , Api.SocketActionGetKey + Api.SocketDriverID);
    }

    private  Emitter.Listener onNewMessage = new Emitter.Listener(){

        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson gson = new Gson();
                    try {
                        SocketGeoLocation response = gson.fromJson(data.toString(), SocketGeoLocation.class);


                            socketMarker.setPosition(new LatLng((response.getGeoLat()), response.getGeoLong()));
                            socketMarker.setRotation((float) response.getGeoHeading());
                            //rotateMarker(marker,Float.parseFloat(response.getGeoHeading()));
                            //mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(response.getGeoLat()), Double.parseDouble(response.getGeoLon()))));
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(response.getGeoLat(), response.getGeoLong()))             // Sets the center of the map to current location
                                    .zoom(15)// Sets the zoom
                                    //.bearing(bearingBetweenLocations(previous,current))
                                    // Sets the orientation of the camera to east
                                    .bearing((float) response.getGeoHeading())
                                    .tilt(0)
                                    // Sets the tilt of the camera to 0 degrees
                                    .build();
                            // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        if (response != null) {
                        }
                    } catch (Exception e) {
                        return;
                    }

                }
            });
        }
    };
*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
       // disConnectSocket();
    }

   /* private void disConnectSocket() {
        socketMarker.remove();
        addCurrentLatlng();
        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }
*/
    // for nearby places
    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        // googlePlacesUrl.append("&key=" + "AIzaSyAp2M4zPmphKnVOjtdaLt5y5fcZwWL9ofY");
        googlePlacesUrl.append("&key=" + "AIzaSyBj-cnmMUY21M0vnIKz0k3tD3bRdyZea-Y");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        Log.e("lat: ", String.valueOf(Constants.latitude));
        Log.e("lng: ", String.valueOf(Constants.longitude));

        addCurrentLatlng();
        googleMap.setOnPolylineClickListener(this);

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        int polylineID = (int) polyline.getTag();
        for (int i = 0; i < polylineArrayList.size() ; i++) {
            if (i == polylineID){
                polyline.setColor(Color.RED);
            }else {
                polylineArrayList.get(i).setColor(Color.BLUE);
            }
        }

        String data = String.valueOf(polyline.getClass());
        Toast.makeText(this, "You have clicked on " + polylineID + " polyline", Toast.LENGTH_SHORT).show();
        // retrive the RouteStepInfo list from shared prefrence
        String routeData = Global.getPreference(PREF_KEY_ROUTE_INFO_CLASS, "");
        Type type = new TypeToken<List<RouteInfo>>(){
        }.getType();
        ArrayList<RouteInfo> routeInfoList = gson.fromJson(routeData, type);
        Log.e("size :" , String.valueOf(routeInfoList.size()));
        RouteInfo routeInfo = routeInfoList.get(polylineID);
        tvTotalDistance.setText(routeInfo.getDuration() + "(" + routeInfo.getDistance() + ")");
        ArrayList<RouteStepInfo> routeStepInfos = (ArrayList<RouteStepInfo>) routeInfo.getRouteStepInfo();
        //mAdapter = new RootInfoAdapter(this, routeStepInfos, this);
        mAdapter.setData(routeStepInfos);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaceSelected(Place place) {
        if (flag) {
            Toast.makeText(MapPathWithSearchbarActivity.this, "" + flag, Toast.LENGTH_SHORT).show();
            mClusterManager.clearItems();
            mClusterManager.cluster();
            flag = false;
        }
        Log.i("msg", "Place: " + place.getName());
        Log.i("flag", String.valueOf(flag));
        addMarker(place);
    }

    @Override
    public void onError(Status status) {
        Log.e("onPlaceSelected error", status.getStatusMessage());
    }


    public void addCurrentLatlng() {
        LatLng current = new LatLng(Constants.latitude, Constants.longitude);
        mMap.addMarker(new MarkerOptions().position(current)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }//addCurrentLatlng

    public void addMarker(Place p) {
        // MarkerOptions markerOptions = new MarkerOptions();                // markerOptions.position(p.getLatLng());
        // markerOptions.title(p.getName() + "");                           //  mMap.addMarker(markerOptions);
        //  mMap.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));  //  mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        destinationPlace = (String) p.getName();
        MarkerOptions markerOptions = new MarkerOptions();
        marker = mMap.addMarker(markerOptions.position(p.getLatLng()).title(p.getName() + ""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        //  LatLng latLng = new LatLng(p.getLatLng().latitude,p.getLatLng().longitude);
        des_latitude = p.getLatLng().latitude;
        des_longitude = p.getLatLng().longitude;
        infoLayout.setVisibility(View.VISIBLE);
        btndirections.setVisibility(View.VISIBLE);
    }//addMarker


    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            // TODO Auto-generated method stub
            String data = "";
            try {
                MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);
            } catch (Exception e) {
                // TODO: handle exception
                Log.d("Background Task", e.toString());
            }
            return data;
        }//ReadTask

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> implements RootInfoAdapter.ItemListener {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            // TODO Auto-generated method stub
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }//doInBackground

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            polylineArrayList = new ArrayList<>();

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(12);
                if (i == 0) {
                    polyLineOptions.color(Color.RED);
                } else {
                    polyLineOptions.color(Color.BLUE);
                }

                myPolyline = mMap.addPolyline(polyLineOptions);
                myPolyline.setClickable(true);
                myPolyline.setTag(i);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.latitude, Constants.longitude), 8));
                polylineArrayList.add(myPolyline);
            }
            String distAndTime = Global.getPreference(PREF_KEY_DIAT_AND_TIME, "");
            RouteDistanceAndDurationModel routeDistanceAndDurationModel = gson.fromJson(distAndTime, RouteDistanceAndDurationModel.class);
            try {
                tvTotalDistance.setVisibility(View.VISIBLE);
                tvMoreInfo.setVisibility(View.VISIBLE);
                tvStartTrip.setVisibility(View.VISIBLE);
                tvTotalDistance.setText(routeDistanceAndDurationModel.getTime() + " (" + routeDistanceAndDurationModel.getDist() + ")");
                totalDestDur = routeDistanceAndDurationModel.getTime() + " (" + routeDistanceAndDurationModel.getDist() + ")";

            } catch (Exception e) {
                e.printStackTrace();
            }

            // retrive the RouteStepInfo list from shared prefrence
            String list = Global.getPreference(PREF_KEY_ROUTE_INFO, "");
            Type type1 = new TypeToken<List<RouteStepInfo>>() {
            }.getType();
            ArrayList<RouteStepInfo> infoArrayList = gson.fromJson(list, type1);
            Log.e("size", String.valueOf(infoArrayList.size()));

            mAdapter = new RootInfoAdapter(MapPathWithSearchbarActivity.this, infoArrayList, this);
            recyclerView.setAdapter(mAdapter);
        }//onPostExecute

        @Override
        public void onItemClick(String item) {
            //TODO
        }
    }//ParserTask

    public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

        private String googlePlacesData;
        private GoogleMap mMap;
        private String url;

        private Context context;


        public GetNearbyPlacesData(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Object... objects) {
            mMap = (GoogleMap) objects[0];
            url = (String) objects[1];

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
        }//onPostExecute

        private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
            mClusterManager = new ClusterManager(context, mMap);
            mMap.setOnCameraIdleListener(mClusterManager);
            for (int i = 0; i < nearbyPlaceList.size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> googlePlace = nearbyPlaceList.get(i);
                Log.d("onPostExecute", "Entered into showing locations");

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
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(googlePlace.get("lat")), Double.parseDouble(googlePlace.get("lng")))));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

                NearByModel nearByModel = new NearByModel(googlePlace.get("place_name"), googlePlace.get("vicinity"),
                        Double.parseDouble(googlePlace.get("lat")), Double.parseDouble(googlePlace.get("lng")));

                mClusterManager.addItem(nearByModel);
                mClusterManager.cluster();
            }
        }//showNearbyPlaces
    }//GetNearbyPlacesData
}
