package com.asmaamir.minisci.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.asmaamir.minisci.Quiz;
import com.asmaamir.minisci.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        db = FirebaseFirestore.getInstance();
        fetchData();
    }

    private void fetchData() {
        // quiz random ID interval: [1,1000]
        Random randGenerator = new Random();
        int random = randGenerator.nextInt(1000);
        db.collection("quizes").whereLessThanOrEqualTo("randomID", random).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.i(TAG, "size: " + queryDocumentSnapshots.size());
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            Quiz quiz = d.toObject(Quiz.class);
                            quiz.setId(d.getId());
                        }
                    }
                });

    }
}
