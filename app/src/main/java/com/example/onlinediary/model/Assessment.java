package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class Assessment {
    @SerializedName("id")
    public long id;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("teacherName")
    public String teacherName;

    @SerializedName("type")
    public String type;

    @SerializedName("maxScore")
    public int maxScore;

    @SerializedName("subjectId")
    public long subjectId;

    @SerializedName("deadline")
    public String deadline;
}
