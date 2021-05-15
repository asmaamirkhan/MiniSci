package com.asmaamir.minisci.entities;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Object implements Serializable {

    @Exclude
    private String id;


    private String name;
    private ArrayList<HashMap<String, String>> details;


    public Object() {
    }

    public Object(String name, ArrayList<HashMap<String, String>> details) {
        this.name = name;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDetails(ArrayList<HashMap<String, String>> details) {
        this.details = details;
    }

    public ArrayList<HashMap<String, String>> getDetails() {
        return details;
    }

    @NonNull
    @Override
    public String toString() {

        return String.format("id: %s, name: %s",
                id,
                name);
    }
}
