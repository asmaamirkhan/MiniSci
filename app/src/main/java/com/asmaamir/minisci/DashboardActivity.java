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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initReceptionDialog();
    }

    private void initReceptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.result_dialog, null);
        ImageView ivRes = dialogLayout.findViewById(R.id.result_dlg_image);
        TextView tvTitle = dialogLayout.findViewById(R.id.result_dlg_title);
        TextView tvMessage = dialogLayout.findViewById(R.id.result_dlg_message);

        tvTitle.setText("Hoş geldin! 🎉");
        tvMessage.setText("Nasılsın bugün " + getUserName() + "?");
        ivRes.setImageResource(R.drawable.ic_baseline_tag_faces_24);
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
}