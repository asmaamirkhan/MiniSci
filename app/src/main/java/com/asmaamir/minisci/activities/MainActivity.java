package com.asmaamir.minisci.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.asmaamir.minisci.R;
import com.asmaamir.minisci.helpers.NotificationBroadcastReceiver;

public class MainActivity extends AppCompatActivity {
    private Button butSignin;
    private Button butSignup;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        startAlert();
        butSignin = findViewById(R.id.button_signin);
        butSignup = findViewById(R.id.button_signup);
        butSignup.setOnClickListener(v -> {
            Intent regRedirect = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(regRedirect);
        });
        butSignin.setOnClickListener(v -> {
            Intent loginRedirect = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginRedirect);
        });
    }

    private void startAlert() {
        int REQUEST_CODE = 2726287;
        int i = 5;
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), REQUEST_CODE, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC,
                System.currentTimeMillis(),
                AlarmManager.INTERVAL_HALF_DAY,
                //i * 1000,
                pendingIntent);
        //Toast.makeText(this, "Alarm set in " + i + " seconds", Toast.LENGTH_LONG).show();
    }
}