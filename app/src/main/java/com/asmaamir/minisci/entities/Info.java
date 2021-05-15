package com.asmaamir.minisci.entities;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Info implements Serializable {

    @Exclude
    private String id;

    private String content;
    private int randomID;


    public Info() {
    }

    public Info(String content, int randomID) {
        this.content = content;
        this.randomID = randomID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public void setRandomID(int randomID) {
        this.randomID = randomID;
    }

    public String getContent() {
        return content;
    }

    public int getRandomID() {
        return randomID;
    }

    @NonNull
    @Override
    public String toString() {

        return String.format("id: %s, randomID: %d, contnet: %s",
                id,
                randomID,
                content);
    }
}
