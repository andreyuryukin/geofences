package com.example.andreyu.geofences;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            Log.e("ERROR", "GeofencingEvent Error: " + event.getErrorCode());
            return;
        }

        String description = getGeofenceTransitionDetails(event);
        sendNotification(description);
    }

    private static String getGeofenceTransitionDetails(GeofencingEvent event) {
        String transitionString;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("mm-ss");
        String formattedDate = df.format(c.getTime());

        int geofenceTransition = event.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            transitionString = "IN-" + formattedDate;
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            transitionString = "OUT-" + formattedDate;
        } else {
            transitionString = "OTHER-" + formattedDate;
        }
        List<String> triggeringIDs;
        triggeringIDs = new ArrayList<>();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        return String.format("%s: %s", transitionString, TextUtils.join(", ", triggeringIDs));
    }

    private void sendNotification(String notificationDetails) {

        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setColor(Notification.COLOR_DEFAULT)
                .setContentTitle(notificationDetails)
                .setContentText("Click notification to remove")
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(new long[]{1000, 1000})
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(generateRandom(), builder.build());
    }

    public int generateRandom() {
        Random random = new Random();
        return random.nextInt(10000 - 100) + 100;
    }
}