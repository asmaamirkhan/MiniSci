package com.asmaamir.minisci.activities;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asmaamir.minisci.R;
import com.asmaamir.minisci.entities.Object;
import com.asmaamir.minisci.helpers.ObjDetailAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ObjInfoActivity extends AppCompatActivity {
    private static final String TAG = "ObjInfoActivity";
    private FirebaseFirestore db;

    private String objectName = "";
    private RecyclerView recyclerView;
    ArrayList<Pair<String, String>> detailsArray;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obj_info);
        db = FirebaseFirestore.getInstance();
        detailsArray = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            objectName = extras.getString("objectName");
        }
        fetchData();
        recyclerView = findViewById(R.id.obj_info_recycler_view);
    }

    private void fillView() {
        ObjDetailAdapter newsAdapter = new ObjDetailAdapter(detailsArray);
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void fetchData() {
        db.collection("objects").whereEqualTo("name", objectName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.i(TAG, "size: " + queryDocumentSnapshots.size());
                        Object object = queryDocumentSnapshots.getDocuments().get(0).toObject(Object.class);
                        for (String key : object.getDetails().get(0).keySet()) {
                            Pair<String, String> pair = new Pair<>(key, object.getDetails().get(0).get(key));
                            detailsArray.add(pair);
                        }
                        fillView();
                    }
                });

    }
}