package com.example.andreyu.geofences;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

public class GPSListener implements LocationListener {

    public TextView textViewInitCoordinates;

    public GPSListener(GeoFencesMainActivity act) {
        textViewInitCoordinates = (TextView) act.findViewById(R.id.textViewInitCoordinates);
        textViewInitCoordinates.setText("GPS Listener Constructor");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            textViewInitCoordinates.setText("Current Lat:" + location.getLatitude() + " Lon:" + location.getLongitude());
        } else {
            textViewInitCoordinates.setText("Location is null !!!");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
