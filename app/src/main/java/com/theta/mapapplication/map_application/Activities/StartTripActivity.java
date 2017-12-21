package com.theta.mapapplication.map_application.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.theta.mapapplication.R;
import com.theta.mapapplication.map_application.global.Constants;
import com.theta.mapapplication.map_application.model.SocketGeoLocation;
import com.theta.mapapplication.map_application.webservice.Api;

import org.json.JSONObject;

import java.net.URISyntaxException;

public class StartTripActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private SupportMapFragment mapTripFragment;
    private Button btnStartTrip;

    // related to google map
    private GoogleMap mMap;

    //Related to socket
    private Socket mSocket;
    private Marker socketMarker;

    private Boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_trip);

        init();
    }

    private void init() {
        mapTripFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapTripFragment);
        mapTripFragment.getMapAsync(this);
        btnStartTrip = findViewById(R.id.btnStartTrip);
        btnStartTrip.setOnClickListener(this);
        socket();
        connectSocket();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        socketMarker = mMap.
                addMarker(new MarkerOptions().position(new LatLng(Constants.latitude, Constants.longitude)).
                        title("You Are Here!").
                        icon(BitmapDescriptorFactory.
                                fromResource(R.drawable.ic_carmarker))

                );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(Constants.latitude, Constants.longitude)), 16.0f));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnStartTrip) {
            socket();
            connectSocket();
        }
    }


    private void socket() {
        try {
            mSocket = IO.socket(Api.SocketMainUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void connectSocket() {

        try {
            mSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socketio", "socket connected");
                if (!isConnected) {
                    isConnected = true;
                }
            }
        });

        // 2nd segment test without connecting to 1 long method
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                Log.d("socketio", "socket event connect error");
                Snackbar snackbar = Snackbar.make(btnStartTrip, "Socket is not connected..", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(StartTripActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });
                snackbar.show();
            }
        });


        mSocket.on(Api.SocketActionGetKey + Api.SocketDriverID, onNewMessage);

        Log.e("SocketKey=", Api.SocketActionGetKey + Api.SocketDriverID);
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {

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
}
