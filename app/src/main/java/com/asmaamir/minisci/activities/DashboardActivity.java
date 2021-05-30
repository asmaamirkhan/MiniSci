package com.asmaamir.minisci.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.asmaamir.minisci.R;
import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity {
    private final String[] EMOTIONS = {"HAPPY", "SAD", "DROWSY"};
    private final static float SMILE_THRESH = 0.6f;
    private final static String TAG = "DashboardActivity";
    private final static float SAD_THRESH = 0.1f;
    private final static float EYE_OPEN_THRESH = 0.6f;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Button exploreButton;
    private Button solveQuizButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initReceptionDialog(getUserEmotion());
        initNavigationDrawer();
        initButtons();
    }

    private void initButtons() {
        exploreButton = findViewById(R.id.dashboard_explore_button);
        solveQuizButton = findViewById(R.id.dashboard_solve_quiz_button);
        exploreButton.setOnClickListener(v -> switchActivity(ExploreActivity.class));
        solveQuizButton.setOnClickListener(v -> switchActivity(QuizActivity.class));
    }

    private void initNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                Log.i(TAG, "onDrawerClosed");
            }

            public void onDrawerOpened(View drawerView) {
                Log.i(TAG, "onDrawerOpened");
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener((MenuItem menuItem) -> {
                    int id = menuItem.getItemId();
                    switch (id) {
                        case R.id.nav_explore:
                            Log.i(TAG, "nav_explore");
                            switchActivity(ExploreActivity.class);
                            return true;
                        case R.id.nav_quiz:
                            Log.i(TAG, "nav quiz");
                            switchActivity(QuizActivity.class);
                            return true;
                        case R.id.nav_quick_info:
                            Log.i(TAG, "quick info");
                            switchActivity(InfoActivity.class);
                            return true;
                        case R.id.nav_get_quote:
                            Log.i(TAG, "get quote");
                            switchActivity(QuoteActivity.class);
                            return true;
                        case R.id.nav_translate:
                            Log.i(TAG, "Translate");
                            switchActivity(TranslationActivity.class);
                            return true;
                        default:
                            Log.i(TAG, "item clicked");
                            switchActivity(QuizActivity.class);
                            return true;
                    }
                }
        );
    }

    private void switchActivity(Class<?> cls) {
        Intent dashRedirect = new Intent(DashboardActivity.this, cls);
        startActivity(dashRedirect);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    private String getUserEmotion() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            float smileProb = extras.getFloat("smileProb");
            float eyeOpenProbAvg = extras.getFloat("eyeOpenProbAvg");
            if (smileProb > SMILE_THRESH)
                return EMOTIONS[0]; // happy
            if (eyeOpenProbAvg < EYE_OPEN_THRESH)
                return EMOTIONS[2]; // drowsy
            if (smileProb < SAD_THRESH)
                return EMOTIONS[1]; // sad
        }
        return EMOTIONS[1]; // sad
    }

    private void initReceptionDialog(String emotion) {
        getCorrespondedQuote(emotion);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.result_dialog, null);
        ImageView ivRes = dialogLayout.findViewById(R.id.result_dlg_image);
        TextView tvTitle = dialogLayout.findViewById(R.id.result_dlg_title);
        TextView tvMessage = dialogLayout.findViewById(R.id.result_dlg_message);

        tvTitle.setText(String.format("Hoş geldin %s! 🎉", getUserName()));
        ivRes.setImageResource(R.drawable.ic_baseline_tag_faces_24);
        tvMessage.setText(getCorrespondedQuote(emotion));
        builder.setPositiveButton("Devam", (dlg, i) -> {
            dlg.dismiss();
        });
        builder.setView(dialogLayout);
        builder.show();

    }

    private String getUserName() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String name = pref.getString("user_name", "NONE");
        return name;
    }

    private String getCorrespondedQuote(String emotion) {
        if (emotion.equals(EMOTIONS[0]))
            return "MUTLUSUN 😊";
        if (emotion.equals(EMOTIONS[1]))
            return "MUTSUZSUN 😢";
        if (emotion.equals(EMOTIONS[2]))
            return "UYKULUSUN 😴";
        return "MUTLUSUN 😊";
    }
}