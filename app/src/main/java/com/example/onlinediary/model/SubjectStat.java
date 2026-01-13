package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class SubjectStat {
    @SerializedName("label")
    public String label;

    @SerializedName("score")
    public int score;

    @SerializedName("maxScore")
    public int maxScore;

    @SerializedName("color")
    public String color;
}
