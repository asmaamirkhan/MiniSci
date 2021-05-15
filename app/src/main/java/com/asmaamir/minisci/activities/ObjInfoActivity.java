package com.asmaamir.minisci.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.asmaamir.minisci.R;

public class ObjInfoActivity extends AppCompatActivity {
    private String objectName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obj_info);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            objectName = extras.getString("objectName");
        }
        TextView tvTitle = findViewById(R.id.obj_title);
        tvTitle.setText(objectName);
    }
}