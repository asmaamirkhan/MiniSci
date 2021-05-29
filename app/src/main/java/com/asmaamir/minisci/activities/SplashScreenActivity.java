package com.asmaamir.minisci.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.asmaamir.minisci.R;

public class SplashScreenActivity extends AppCompatActivity {


    private final long SPLASH_TIME = 1500; // 1.5s
    ProgressBar splashProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_splash_screen);

        splashProgress = findViewById(R.id.splashProgress);
        playProgress();


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME);

    }

    private void playProgress() {
        ObjectAnimator.ofInt(splashProgress, "progress", 100)
                .setDuration(SPLASH_TIME)
                .start();
    }
}