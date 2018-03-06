package com.asksira.backgroundbeacontest;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class BeaconApplication extends Application implements BootstrapNotifier {

    private Region region;
    private RegionBootstrap regionBootstrap;
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setBackgroundScanPeriod(10000);
        beaconManager.setBackgroundBetweenScanPeriod(5000);
        region = new Region("backgroundRegion",
                Identifier.parse("EBEFD083-70A2-47C8-9837-E7B5634DF524"), null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        Log.i("selfBeacon", "Bootstrap created");
    }


    @Override
    public void didEnterRegion(Region region) {
        Log.i("selfBeacon", "Bootstrap didEnterRegion");
        regionBootstrap.disable();
        Intent intent = new Intent(this, BeaconService.class);
        startService(intent);
        Log.i("selfBeacon", "Service start commanded");
    }

    @Override
    public void didExitRegion(Region region) {

    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    public void resumeScanning () {
        regionBootstrap = new RegionBootstrap(this, region);
        Log.i("selfBeacon", "scanning resumed");
    }

}
