package com.asksira.backgroundbeacontest;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_PERMISSION_CODE = 1234;

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
                    Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
