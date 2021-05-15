package com.asmaamir.minisci.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.asmaamir.minisci.R;
import com.asmaamir.minisci.entities.Quiz;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private FirebaseFirestore db;
    private TextView tvQuestion;
    private RadioButton rbChoiceA;
    private RadioButton rbChoiceB;
    private RadioButton rbChoiceC;
    private Quiz quiz;
    private CardView quizCard;
    private MaterialButton butSubmit;
    private MaterialButton butNext;
    private RadioGroup rgChoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        db = FirebaseFirestore.getInstance();
        fetchData();
        tvQuestion = findViewById(R.id.quiz_question);
        rbChoiceA = findViewById(R.id.choice_a_radio);
        rbChoiceB = findViewById(R.id.choice_b_radio);
        rbChoiceC = findViewById(R.id.choice_c_radio);
        quizCard = findViewById(R.id.quiz_card);
        rgChoices = findViewById(R.id.quiz_choices_group);
        butSubmit = findViewById(R.id.quiz_submit_button);
        butNext = findViewById(R.id.quiz_next_button);
        butSubmit.setOnClickListener(v -> {
            /*int selectedId = rgChoices.getCheckedRadioButtonId();
            RadioButton checkedButton = findViewById(selectedId);
            int idx = rgChoices.indexOfChild(checkedButton);
            Log.i(TAG, "id: " + idx);*/
            RadioButton rButton;
            for (int i = 0; i < 3; i++) {
                rButton = (RadioButton) rgChoices.getChildAt(i);
                if (i == quiz.getAnswer() - 1)
                    rButton.setTextColor(Color.GREEN);
                else
                    rButton.setTextColor(Color.RED);

            }
            rbChoiceA.setTextColor(Color.RED);
            butSubmit.setVisibility(View.GONE);
            butNext.setVisibility(View.VISIBLE);
        });
        butNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
                RadioButton rButton;
                for (int i = 0; i < 3; i++) {
                    rButton = (RadioButton) rgChoices.getChildAt(i);
                    rButton.setTextColor(Color.BLACK);

                }
                butSubmit.setVisibility(View.VISIBLE);
                butNext.setVisibility(View.GONE);
                rgChoices.clearCheck();
            }
        });
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
                        quiz = list.get(0).toObject(Quiz.class);
                        tvQuestion.setText(quiz.getQuestion());
                        rbChoiceA.setText(quiz.getChoiceA());
                        rbChoiceB.setText(quiz.getChoiceB());
                        rbChoiceC.setText(quiz.getChoiceC());
                        quizCard.setVisibility(View.VISIBLE);
                    }
                });
    }


}
