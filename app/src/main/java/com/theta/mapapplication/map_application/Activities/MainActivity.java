package com.theta.mapapplication.map_application.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.theta.mapapplication.R;
import com.theta.mapapplication.map_application.global.AppDialog;
import com.theta.mapapplication.map_application.global.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity
        implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback{


    Button btnMarkerCluster;
    Button btnMapPath;
    Button btnMapPathSearchbar;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    // permission for location related
    private static final int REQUEST_LOCATION = 345;
    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    Context context;
    private Status status;
    private DialogInterface.OnClickListener positiveClick, negativeClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getCurrentLocationData();
        btnMarkerCluster = findViewById(R.id.btnMarkerCluster);
        btnMarkerCluster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MarkersAndClusterActivity.class));
            }
        });

        btnMapPath = findViewById(R.id.btnMapPath);
        btnMapPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapPathActivity.class));
            }
        });

        btnMapPathSearchbar = findViewById(R.id.btnMapPathSearchbar);
        btnMapPathSearchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapPathWithSearchbarActivity.class));
            }
        });

        positiveClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_LOCATION);
            }
        };

    }

    public void getCurrentLocationData() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            if (checkPlayServices()) {
                getCurrentLocation();
            }
        }
    }//getCurrentLocationData

    //checkPlayServices
    public boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_SHORT).show();
                Log.e("msg", "This device is not supported.");
            }
            return false;
        }
        return true;
    }//checkPlayServices


    //getCurrentLocation
    public void getCurrentLocation() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();

        int permissionLocation = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);

        if (permissionLocation == PERMISSION_GRANTED) {
            mGoogleApiClient.connect();
        }
    }//getCurrentLocation

    public void getLocation() {
        if (mGoogleApiClient.isConnected()) {
            final LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    status = locationSettingsResult.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Log.e("msg", "connected");
                            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                    ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                            Log.e("msg", "SUCCESS: " + mLastLocation);
                            try {
                                Constants.lat = mLastLocation.getLatitude();
                                Constants.lng = mLastLocation.getLongitude();
                                Constants.latitude = Constants.lat;
                                Constants.longitude = Constants.lng;
                                getAddress(Constants.lat, Constants.lng);
                                //23.018353,72.529878
                            } catch (Exception e) {
                                Log.e("msg", "latlang error : " + e.toString());
                            }
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(), // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e("msg", e.toString());
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }
    }

    private Address getAddress(double longitude, double latitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("msg", e.toString());
        }
        return null;
    }//getAddress(double longitude, double latitude)

    //result of  location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                //denied
                Log.e("msg", "permission denied");
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            } else {
                if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                    Log.e("msg", "permisson granted");
                    if (checkPlayServices()) {
                        getCurrentLocation();
                    }
                } else {
                    Log.e("msg", "set to never ask again");
                    AppDialog.showAppSettingDialogWithPositiveButton(this,
                            getString(R.string.txt_location_permission_title), getString(R.string.txt_location_permission),
                            positiveClick);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.e("msg", "onActivityResult SUCCESS: " + mLastLocation);
            try {
                Constants.latitude = mLastLocation.getLatitude();
                Constants.longitude = mLastLocation.getLongitude();
                Log.e("msg", "from try :: lat : " + Constants.latitude + "lng : " + Constants.longitude);
                getAddress(Constants.latitude, Constants.latitude);
            } catch (Exception e) {
                Log.e("msg", "latlang error : " + e.toString());
                Constants.latitude = Constants.lat;
                Constants.longitude = Constants.lng;
                Log.e("msg", "lat : " + Constants.latitude + "lng : " + Constants.longitude);
            }
        }
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode != RESULT_OK) {
            getRequest();
        }
        if (requestCode == REQUEST_LOCATION) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                //denied
                Log.e("msg", "permission denied from app settings");
            } else if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                Log.e("msg", "permisson granted");

                if (checkPlayServices()) {
                    getCurrentLocation();
                }
            } else {
                Log.e("msg", "set to never ask again from app settings");
                AppDialog.showAppSettingDialogWithPositiveButton(this,
                        getString(R.string.txt_location_permission_title), getString(R.string.txt_location_permission),
                        positiveClick);
            }
        }

    }

    private void getRequest() {
        try {
            status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("msg", " map connected");
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("msg", " map connection failed");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("msg", "map connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("msg","onLocationChanged SUCCESS: " + location);
        try {
            Constants.latitude = location.getLatitude();
            Constants.longitude = location.getLongitude();
            getAddress(Constants.latitude ,  Constants.latitude);
        } catch (Exception e) {
            Constants.latitude = Constants.lat;
            Constants.longitude = Constants.lng;
            Log.e("msg","latlang error : " + e.toString());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
    /*Get the API key for Google Places API

        We can create API key for Google Place API by clicking “Create new Browser key”  available at the “API Access” pane of the Google console URL : http://code.google.com/apis/console.

        Also ensure that, “Places API” is enabled in the “Services” pane of the Google console.*/