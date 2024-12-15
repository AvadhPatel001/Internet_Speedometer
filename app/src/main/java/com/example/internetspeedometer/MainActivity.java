package com.example.internetspeedometer;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int RC_NOTIFICATION = 11;
    Button startServiceButton;
    Button stopServiceButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startServiceButton = findViewById(R.id.start);
        stopServiceButton = findViewById(R.id.stop);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATION);
        }


        startServiceButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SpeedMonitor.class);
            startForegroundService(intent);
        });

        stopServiceButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SpeedMonitor.class);
            stopService(intent);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_NOTIFICATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(MainActivity.this, SpeedMonitor.class);
                startForegroundService(intent);
            } else {
                finish();
            }
        }
    }
}