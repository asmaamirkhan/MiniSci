package com.asmaamir.minisci.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.asmaamir.minisci.R;
import com.asmaamir.minisci.entities.Quote;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

public class QuoteActivity extends AppCompatActivity {
    private static final String TAG = "QuoteActivity";
    private FirebaseFirestore db;
    private Quote quote;
    private TextView tvContent;
    private TextView tvTitle;
    private TextView tvAuthor;
    private MaterialButton butNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote);
        db = FirebaseFirestore.getInstance();
        fetchData();
        tvContent = findViewById(R.id.quote_content);
        tvTitle = findViewById(R.id.quote_title);
        tvAuthor = findViewById(R.id.quote_author);
        butNext = findViewById(R.id.quote_next_button);
        butNext.setOnClickListener(v -> fetchData());
    }

    private void fetchData() {
        // quiz random ID interval: [1,1000]
        Random randGenerator = new Random();
        int random = randGenerator.nextInt(1000);
        db.collection("quotes").whereLessThanOrEqualTo("randomID", random).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.i(TAG, "size: " + queryDocumentSnapshots.size());
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        quote = list.get(0).toObject(Quote.class);
                        tvContent.setText(quote.getContent());
                        tvTitle.setText(quote.getTitle());
                        tvAuthor.setText(quote.getAuthor());
                    }
                });
    }
}