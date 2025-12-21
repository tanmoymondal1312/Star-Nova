package com.mediaghor.starnova.Permissions;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MicPermissionHelper {

    private final Context context;
    private final Activity activity;
    private final ActivityResultLauncher<String> permissionLauncher;
    private final PermissionCallback callback;

    public interface PermissionCallback {
        void onGranted();
        void onDenied();
    }

    public MicPermissionHelper(
            ActivityResultCaller caller,
            Activity activity,
            PermissionCallback callback
    ) {
        this.context = activity;
        this.activity = activity;
        this.callback = callback;

        permissionLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        callback.onGranted();
                    } else {
                        if (isPermanentlyDenied()) {
                            showSettingsDialog();
                        } else {
                            showRationaleDialog();
                        }
                        callback.onDenied();
                    }
                }
        );
    }

    /* Public method */
    public void requestMicPermission() {
        if (hasPermission()) {
            callback.onGranted();
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.RECORD_AUDIO)) {
            showRationaleDialog();
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    /* Permission check */
    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    /* Permanent denial check */
    private boolean isPermanentlyDenied() {
        return !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.RECORD_AUDIO);
    }

    /* Rationale dialog */
    private void showRationaleDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Microphone Access Required")
                .setMessage("This app needs microphone access to record and recognize your voice.")
                .setCancelable(false)
                .setPositiveButton("Allow", (d, w) ->
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* Settings dialog */
    private void showSettingsDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Permission Required")
                .setMessage("Microphone permission is permanently denied. Please enable it from app settings.")
                .setCancelable(false)
                .setPositiveButton("Open Settings", (d, w) -> openAppSettings())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* Open app settings */
    private void openAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.getPackageName(), null)
        );
        context.startActivity(intent);
    }
}
