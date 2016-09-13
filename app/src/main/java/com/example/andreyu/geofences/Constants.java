package com.example.andreyu.geofences;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class Constants {
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    public static final float GEOFENCE_RADIUS_IN_METERS = 30;

    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<>();
    static {
        LANDMARKS.put("Edden    ", new LatLng(32.79011756,34.95823056));
        LANDMARKS.put("Mizrahi  ", new LatLng(32.79000163,34.95921098));
        LANDMARKS.put("Home     ", new LatLng(32.29506421,34.84522738));
    }
}
