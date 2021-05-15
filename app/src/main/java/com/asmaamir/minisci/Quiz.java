package com.asmaamir.minisci;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Quiz implements Serializable {

    @Exclude
    private String id;

    private String question, choiceA, choiceB, choiceC;
    private int answer;

    public Quiz() {

    }

    public Quiz(String question, String choiceA, String choiceB, String choiceC, int answer) {
        this.question = question;
        this.choiceA = choiceA;
        this.choiceB = choiceB;
        this.choiceC = choiceC;
        this.answer = answer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setChoiceA(String choiceA) {
        this.choiceA = choiceA;
    }

    public void setChoiceB(String choiceB) {
        this.choiceB = choiceB;
    }

    public void setChoiceC(String choiceC) {
        this.choiceC = choiceC;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }


    public String getQuestion() {
        return question;
    }

    public String getChoiceA() {
        return choiceA;
    }

    public String getChoiceB() {
        return choiceB;
    }

    public String getChoiceC() {
        return choiceC;
    }

    public int getAnswer(int answer) {
        return answer;
    }

    @NonNull
    @Override
    public String toString() {

        return String.format("id: %s, question: %s, choiceA: %s, choiceB: %s, choiceC: %s, answer: %d",
                id,
                question,
                choiceA,
                choiceB,
                choiceC,
                answer);
    }
}
