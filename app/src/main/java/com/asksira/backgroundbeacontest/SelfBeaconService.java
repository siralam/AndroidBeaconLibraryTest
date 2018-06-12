package com.asksira.backgroundbeacontest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class SelfBeaconService extends Service implements BeaconConsumer {

    private BeaconManager beaconManager;
    private Region region;
    private int noBeaconDetectedCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("selfBeaconService", "onCreate");
        beaconManager = BeaconManager.getInstanceForApplication(this);
        region = new Region("foreground region",
                Identifier.parse("EBEFD083-70A2-47C8-9837-E7B5634DF524"), null, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("selfBeaconService", "onStartCommand");
        broadcastSystemLog("Beacon Service onCreate triggered.");
        beaconManager.setEnableScheduledScanJobs(false); //Stop scanning
        beaconManager.setForegroundScanPeriod(2000);
        beaconManager.setForegroundBetweenScanPeriod(2000);
        beaconManager.bind(this);

        startForeground();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i("selfBeaconService", "onBeaconServiceConnect");
        broadcastSystemLog("onBeaconServiceConnect triggered.");
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if (collection.size() > 0) {
                    noBeaconDetectedCount = 0;
                    double closestDistance = -1;
                    for (Beacon each : collection) {
                        if (closestDistance == -1) {
                            closestDistance = each.getDistance();
                        } else if (each.getDistance() < closestDistance) {
                            closestDistance = each.getDistance();
                        }
                    }
                    Log.i("selfBeaconService", "didRangeBeaconsInRegion, the closest beacon is about " +
                            closestDistance + " meters away.");
                    broadcastToActivity("The closest beacon is about " +
                            closestDistance + " meters away.");
                } else {
                    noBeaconDetectedCount++;
                    broadcastToActivity("No beacon has been detected for " + noBeaconDetectedCount + " times");
                    if (noBeaconDetectedCount > 10) { //10*(2000ms+2000ms) = 40 seconds
                        stopForeground(true);
                        stopSelf();
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Log.i("selfBeacon", "onDestroy");
        broadcastSystemLog("BeaconService onDestroy triggered.");
        super.onDestroy();
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.removeAllRangeNotifiers();
        beaconManager.unbind(this);
        beaconManager.setEnableScheduledScanJobs(true);
        ((BeaconApplication) getApplication()).resumeScanning();
        Log.i("selfBeaconService", "service stopped");
        broadcastSystemLog("BeaconSevice stopped.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void broadcastToActivity(String message) {
        Intent intent = new Intent();
        intent.setAction("beaconBroadcast");
        intent.putExtra("beaconService", message);
        sendBroadcast(intent);
    }

    private void broadcastSystemLog(String message) {
        Intent intent = new Intent();
        intent.setAction("beaconBroadcast");
        intent.putExtra("beaconServiceLog", message);
        sendBroadcast(intent);
    }

    private void startForeground() {
        NotificationCompat.Builder notificationBuilder = null;
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("rangingService", "Beacon Scanning",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(this, "rangingService")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Beacon Background Test")
                    .setContentText("App is scanning for nearby beacons");
            notification = notificationBuilder.build();
        } else {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Beacon Background Test")
                    .setContentText("App is scanning for nearby beacons");
            notification = notificationBuilder.build();
        }
        startForeground(1234, notification);
    }
}
