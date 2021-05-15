package com.asmaamir.minisci.entities;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Quote implements Serializable {

    @Exclude
    private String id;

    private String title, content, author;
    private int randomID;


    public Quote() {
    }

    public Quote(String title, String content, int randomID, String author) {
        this.title = title;
        this.content = content;
        this.randomID = randomID;
        this.author = author;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setRandomID(int randomID) {
        this.randomID = randomID;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public int getRandomID() {
        return randomID;
    }

    @NonNull
    @Override
    public String toString() {

        return String.format("id: %s, randomID: %d, title: %s, content: %s, author: %s",
                id,
                randomID,
                title,
                content,
                author);
    }
}
