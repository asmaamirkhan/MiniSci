package com.asmaamir.minisci.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.asmaamir.minisci.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ObjInfoActivity extends AppCompatActivity {
    private String objectName = "";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obj_info);
        db = FirebaseFirestore.getInstance();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            objectName = extras.getString("objectName");
        }
        TextView tvTitle = findViewById(R.id.obj_title);
        tvTitle.setText(objectName);
    }
}