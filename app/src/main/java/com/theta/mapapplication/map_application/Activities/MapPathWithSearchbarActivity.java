package com.theta.mapapplication.map_application.Activities;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.android.SphericalUtil;
import com.theta.mapapplication.R;
import com.theta.mapapplication.map_application.classes.MapHttpConnection;
import com.theta.mapapplication.map_application.classes.PathJSONParser;
import com.theta.mapapplication.map_application.drawPath.GetNearbyPlacesData;
import com.theta.mapapplication.map_application.global.Constants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.theta.mapapplication.map_application.global.Global.getMapsApiDirectionsUrl;

public class MapPathWithSearchbarActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private Button view1;
    private Marker marker;
    private Button btndirections;
    private Button btnHospitals;
    private Button btnSchools;
    private Button btnRestarents;
    private TextView tvTotalDistance;
    private List<LatLng> latLngList;
    private Polyline myPolyline;
    private double des_latitude;
    private double des_longitude;
    private ArrayList<Polyline> polylineArrayList;
    String mode;
    SupportMapFragment mapFragment;

    int PROXIMITY_RADIUS = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_path_with_searchbar);

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete);

        init();


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i("msg", "Place: " + place.getName());
                addMarker(place);
            }

            @Override
            public void onError(Status status) {
                Log.i("msg", "An error occurred: " + status);
            }
        });

        btndirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                latLngList = new ArrayList<>();
                latLngList.add(new LatLng(Constants.latitude, Constants.longitude));
                latLngList.add(new LatLng(des_latitude, des_longitude));

                String url = getMapsApiDirectionsUrl(latLngList.get(0), latLngList.get(1), mode);
                //ReadTask downloadTask = new ReadTask(mMap);
                ReadTask downloadTask = new ReadTask();
                downloadTask.execute(url);

                float[] results = new float[1];
                Location.distanceBetween(latLngList.get(0).latitude, latLngList.get(0).longitude,
                        latLngList.get(1).latitude, latLngList.get(1).longitude,
                        results);
                Log.e("distance", String.valueOf(results[0] / 1000));
                tvTotalDistance.setVisibility(View.VISIBLE);
                tvTotalDistance.setText(String.valueOf(results[0] / 1000) + " KM");

                double distance = SphericalUtil.computeDistanceBetween(latLngList.get(0), latLngList.get(1));
            }
        });

        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
               /* if (myPolyline != null) {
                    myPolyline.remove();
                    myPolyline = null;
                }*/
                des_latitude = 0.0;
                des_longitude =0.0;
                tvTotalDistance.setVisibility(View.INVISIBLE);
                Toast.makeText(MapPathWithSearchbarActivity.this, "You have cancle the destination", Toast.LENGTH_SHORT).show();
                autocompleteFragment.setText("");
            }
        });

        btnHospitals.setOnClickListener(this);
        btnSchools.setOnClickListener(this);
        btnRestarents.setOnClickListener(this);
    }

    private void init() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        mode = "mode=driving";
        tvTotalDistance = findViewById(R.id.tvTotalDistance);
        btndirections = findViewById(R.id.btndirections);
        btnHospitals = findViewById(R.id.btnHospitals);
        btnSchools = findViewById(R.id.btnSchools);
        btnRestarents = findViewById(R.id.btnRestarents);
        view1 = findViewById(R.id.view1);
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
                Toast.makeText(this, "Driving", Toast.LENGTH_SHORT).show();
                break;
            case R.id.modeBicycling:
                mode = "mode=bicycling";
                Toast.makeText(this, "Bicycling", Toast.LENGTH_SHORT).show();
                break;
            case R.id.modeWalking:
                mode = "mode=walking";
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
            String hospital = "hospital";
            String url = getUrl(Constants.latitude, Constants.longitude, hospital);

            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(MapPathWithSearchbarActivity.this, "Showing Nearby Hospitals", Toast.LENGTH_LONG).show();
        }
        if (view.getId() == R.id.btnSchools) {
            mMap.clear();
            String hospital = "school";
            String url = getUrl(Constants.latitude, Constants.longitude, hospital);

            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(MapPathWithSearchbarActivity.this, "Showing Nearby school", Toast.LENGTH_LONG).show();
        }
        if (view.getId() == R.id.btnRestarents) {
            mMap.clear();
            String hospital = "restaurant";
            String url = getUrl(Constants.latitude, Constants.longitude, hospital);

            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(MapPathWithSearchbarActivity.this, "Showing Nearby school", Toast.LENGTH_LONG).show();
        }

    }

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

        LatLng current = new LatLng(Constants.latitude, Constants.longitude);
        mMap.addMarker(new MarkerOptions().position(current)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    public void addMarker(Place p) {

        // MarkerOptions markerOptions = new MarkerOptions();
        // markerOptions.position(p.getLatLng());
        // markerOptions.title(p.getName() + "");
        //  mMap.addMarker(markerOptions);
        //  mMap.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        //  mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        MarkerOptions markerOptions = new MarkerOptions();
        marker = mMap.addMarker(markerOptions.position(p.getLatLng()).title(p.getName() + ""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        //  LatLng latLng = new LatLng(p.getLatLng().latitude,p.getLatLng().longitude);
        des_latitude = p.getLatLng().latitude;
        des_longitude = p.getLatLng().longitude;
        btndirections.setVisibility(View.VISIBLE);
    }

    /*private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest, String mode) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false&mode=driving&alternatives=true";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }*/


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
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    /*public class MapHttpConnection {
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.d("excptn in reading url", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }*/

    /*public class PathJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }

                }

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
    }*/

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
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
        }

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
                polyLineOptions.color(Color.BLUE);

                myPolyline = mMap.addPolyline(polyLineOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.latitude, Constants.longitude), 8));
                polylineArrayList.add(myPolyline);


            }


        }
    }


}
