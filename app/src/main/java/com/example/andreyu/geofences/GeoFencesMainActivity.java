package com.example.andreyu.geofences;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Map;

public class GeoFencesMainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    protected ArrayList<Geofence> mGeofencingList;
    protected GoogleApiClient mGoogleApiClient;
    public TextView textViewLog;
    public LocationManager manager;
    public GPSListener listener;
    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fences_main);
        textViewLog = (TextView) findViewById(R.id.textViewLog);

        assert textViewLog != null;
        textViewLog.append("onCreate\n");

        mGeofencingList = new ArrayList<>();

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }

        populateGeofenceList();
        buildGoogleApiClient();
    }

    public void requestLocation(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        textViewLog.append("Creating GPS Listener ...\n");
        listener = new GPSListener(GeoFencesMainActivity.this);
        textViewLog.append("Requesting Location Updates ...\n");
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
    }

    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.LANDMARKS.entrySet()) {
            textViewLog.append(entry.getKey() + " Lat:" + entry.getValue().latitude + " Long:" + entry.getValue().longitude + "\n");
            mGeofencingList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
        textViewLog.append("populateGeofenceList\n");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        textViewLog.append("buildGoogleApiClient\n");
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            textViewLog.append("Connect GoogleApiClient\n");
        }
    }

    public void addGeofencesButtonHandler(View view) {

        textViewLog.append("addGeofencesButtonHandler\n");
        if (!mGoogleApiClient.isConnected()) {
            textViewLog.append("Google API Client not connected!\n");
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            textViewLog.append("SecurityException\n");
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofencingList);
        textViewLog.append("getGeofencingRequest\n");
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        textViewLog.append("getGeofencePendingIntent\n");
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            textViewLog.append("onStart\n");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            textViewLog.append("onStop\n");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        if (listener != null) {
            manager.removeUpdates(listener);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        textViewLog.append("onConnected\n");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        textViewLog.append("onConnectionSuspended\n");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        textViewLog.append("onConnectionFailed\n");
    }

    @Override
    public void onResult(@NonNull Status status) {
        textViewLog.append("onResult\n");
        if (status.isSuccess()) {
            textViewLog.append("Geofences Added\n");
        } else {
            textViewLog.append("Geofences Failed\n");
            textViewLog.append(status.toString()+"\n");
        }
    }
}