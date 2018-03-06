package com.asksira.backgroundbeacontest;

import android.Manifest;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    public static final int LOCATION_PERMISSION_CODE = 1234;

    private BeaconManager beaconManager;
    private Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.entrance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionUtils.isCoarseLocationGranted(MainActivity.this)) {
                    PermissionUtils.checkPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
                            LOCATION_PERMISSION_CODE);
                } else {
                    if (beaconManager != null) return;
                    beaconManager = BeaconManager.getInstanceForApplication(MainActivity.this);
                    beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
                    beaconManager.bind(MainActivity.this);
                    Toast.makeText(MainActivity.this, "BeaconManager bound", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i("selfBeaconActivity", "onBeaconServiceConnect");
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.i("selfBeaconActivity", "didRangeBeaconsInRegion, the first beacon is about " +
                        collection.iterator().next().getDistance() + " meters away.");
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("backgroundRegion",
                    Identifier.parse("EBEFD083-70A2-47C8-9837-E7B5634DF524"), null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
