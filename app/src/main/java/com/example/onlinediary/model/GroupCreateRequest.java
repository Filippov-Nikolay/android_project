package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class GroupCreateRequest {
    @SerializedName("name")
    public String name;

    @SerializedName("course")
    public int course;

    public GroupCreateRequest(String name, int course) {
        this.name = name;
        this.course = course;
    }
}
