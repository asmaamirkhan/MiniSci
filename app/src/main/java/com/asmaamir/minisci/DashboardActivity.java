package com.asmaamir.minisci;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    private final String[] EMOTIONS = {"HAPPY", "SAD", "DROWSY"};
    private final static float SMILE_THRESH = 0.6f;
    private final static float SAD_THRESH = 0.1f;
    private final static float EYE_OPEN_THRESH = 0.6f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initReceptionDialog(getUserEmotion());

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