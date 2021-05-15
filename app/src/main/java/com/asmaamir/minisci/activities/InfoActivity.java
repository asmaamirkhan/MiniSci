package com.asmaamir.minisci.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.asmaamir.minisci.R;
import com.asmaamir.minisci.entities.Info;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

public class InfoActivity extends AppCompatActivity {
    private static final String TAG = "InfoActivity";
    private FirebaseFirestore db;
    private Info info;
    private TextView tvContent;
    private MaterialButton butNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        db = FirebaseFirestore.getInstance();
        fetchData();
        tvContent = findViewById(R.id.info_content);
        butNext = findViewById(R.id.info_next_button);
        butNext.setOnClickListener(v -> fetchData());
    }

    private void fetchData() {
        // quiz random ID interval: [1,1000]
        Random randGenerator = new Random();
        int random = randGenerator.nextInt(1000);
        db.collection("info").whereLessThanOrEqualTo("randomID", random).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.i(TAG, "size: " + queryDocumentSnapshots.size());
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        info = list.get(0).toObject(Info.class);
                        tvContent.setText(info.getContent());
                    }
                });
    }
}