package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class SubjectRequest {
    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    public SubjectRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
