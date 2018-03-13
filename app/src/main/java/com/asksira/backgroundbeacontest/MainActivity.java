package com.asksira.backgroundbeacontest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_PERMISSION_CODE = 1234;

    LinearLayout llSystemLog;
    TextView tvResult;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (tvResult != null && intent.hasExtra("beaconService")) {
                tvResult.setText(intent.getStringExtra("beaconService"));
            }
            if (intent.hasExtra("beaconServiceLog")) {
                appendNewLog(intent.getStringExtra("beaconServiceLog"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.result);
        llSystemLog = findViewById(R.id.system_log);

        findViewById(R.id.entrance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionUtils.isCoarseLocationGranted(MainActivity.this)) {
                    PermissionUtils.checkPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
                            LOCATION_PERMISSION_CODE);
                }
            }
        });

//        startForegroundService();
    }

    private void startForegroundService () {
        Intent intent = new Intent(this, SelfBeaconService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Log.i("selfBeacon", "Service start commanded");
    }

    private void appendNewLog (String message) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_system_log, llSystemLog, false);
        TextView tvLog = view.findViewById(R.id.log_item);
        tvLog.setText(message);
        llSystemLog.addView(view);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("beaconBroadcast"));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }
}
