package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class Subject {
    @SerializedName("id")
    public long id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;
}
