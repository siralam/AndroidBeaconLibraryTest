package com.asksira.backgroundbeacontest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtils {

    public static void checkPermission (Activity activity, String permissionString, int permissionCode) {
        if ((android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) return;
        int existingPermissionStatus = ContextCompat.checkSelfPermission(activity,
                permissionString);
        if (existingPermissionStatus == PackageManager.PERMISSION_GRANTED) return;
        ActivityCompat.requestPermissions(activity, new String[]{permissionString}, permissionCode);
    }

    public static boolean isCoarseLocationGranted (Context context) {
        int cameraPermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return cameraPermissionGranted == PackageManager.PERMISSION_GRANTED;
    }

}
