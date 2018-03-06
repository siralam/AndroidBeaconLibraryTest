package com.asksira.backgroundbeacontest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
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
        beaconManager.setEnableScheduledScanJobs(false); //Stop scanning
        beaconManager.setBackgroundScanPeriod(2000);
        beaconManager.setBackgroundBetweenScanPeriod(2000);
        beaconManager.bind(this);
        region = new Region("foreground region",
                Identifier.parse("EBEFD083-70A2-47C8-9837-E7B5634DF524"), null, null);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i("selfBeaconService", "onBeaconServiceConnect");
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if (collection.size() > 0) {
                    noBeaconDetectedCount = 0;
                    Log.i("selfBeaconService", "didRangeBeaconsInRegion, the first beacon is about" +
                            collection.iterator().next().getDistance() + " meters away.");
                } else {
                    noBeaconDetectedCount++;
                    if (noBeaconDetectedCount > 10) { //10*(2000ms+2000ms) = 40 seconds
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
        terminate();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void terminate () {
        Log.i("selfBeaconService", "service start terminating");
        beaconManager.unbind(this);
        beaconManager.setEnableScheduledScanJobs(true);
        ((BeaconApplication)getApplication()).resumeScanning();
        stopSelf();
        Log.i("selfBeaconService", "service terminated");
    }
}
