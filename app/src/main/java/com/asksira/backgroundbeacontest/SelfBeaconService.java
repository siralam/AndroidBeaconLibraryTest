package com.asksira.backgroundbeacontest;

import android.app.IntentService;
import android.content.Intent;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class SelfBeaconService extends IntentService implements BeaconConsumer {

    private BeaconManager beaconManager;
    private Region region;
    private Timer timer;
    private boolean hasDetectedBeaconAgain = false;

    public SelfBeaconService() {
        super("BeaconScanningService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("selfBeaconService", "onHandleIntent");
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setEnableScheduledScanJobs(false); //Stop scanning
        beaconManager.bind(this);
        region = new Region("foreground region",
                Identifier.parse("EBEFD083-70A2-47C8-9837-E7B5634DF524"), null, null);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i("selfBeaconService", "onBeaconServiceConnect");
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i("selfBeaconService", "beacon detected");
                hasDetectedBeaconAgain = true;
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i("selfBeaconService", "beacon exited");
                hasDetectedBeaconAgain = false;
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (hasDetectedBeaconAgain) {
                                cancel();
                                timer = null;
                                hasDetectedBeaconAgain = false;
                            } else {
                                timer = null;
                                terminate();
                            }
                        }
                    }, 20000); //In production it should be like 300,000 which means 5 minutes
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if (collection.size() > 0) {
                    Log.i("selfBeaconService", "didRangeBeaconsInRegion, the first beacon is about" +
                            collection.iterator().next().getDistance() + " meters away.");
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(region);
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        beaconManager.unbind(this);
        super.onDestroy();
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
