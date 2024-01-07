package com.example.musicdemo.Activity;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.READ_MEDIA_VIDEO;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.musicdemo.R;

public class SplashActivity extends AppCompatActivity {

    private final int BELOW_ANDROID_13 = 101;
    private final int ABOVE_ANDROID_13 = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (checkPermissionGranted()) {
            nextScreen();
        } else {
            takePermission();
        }
    }

    private void nextScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        }, 2000);
    }

    private void takePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{
                            READ_EXTERNAL_STORAGE}, BELOW_ANDROID_13);
        } else {

            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{
                            READ_MEDIA_AUDIO}, ABOVE_ANDROID_13);
        }
    }

    public boolean checkPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            int read_external_storage = ContextCompat.checkSelfPermission(SplashActivity.this, READ_EXTERNAL_STORAGE);

            return read_external_storage == PackageManager.PERMISSION_GRANTED;

        } else {

            int read_media_audio = ContextCompat.checkSelfPermission(SplashActivity.this, READ_MEDIA_AUDIO);

            return read_media_audio == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case BELOW_ANDROID_13:
                boolean gotPermission1 = grantResults.length > 0;

                for (int result : grantResults) {
                    gotPermission1 &= result == PackageManager.PERMISSION_GRANTED;
                }

                if (gotPermission1) {
                    nextScreen();
                } else {
                    Toast.makeText(SplashActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case ABOVE_ANDROID_13:
                boolean gotPermission2 = grantResults.length > 0;

                for (int result : grantResults) {
                    gotPermission2 &= result == PackageManager.PERMISSION_GRANTED;
                }

                if (gotPermission2) {
                    nextScreen();
                } else {
                    Toast.makeText(SplashActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}