package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class Group {
    @SerializedName("id")
    public long id;

    @SerializedName("name")
    public String name;

    @SerializedName("course")
    public Integer course;
}
