package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class Accrual {
    @SerializedName("id")
    public String id;

    @SerializedName("subject")
    public String subject;

    @SerializedName("title")
    public String title;

    @SerializedName("kind")
    public String kind;

    @SerializedName("score")
    public int score;

    @SerializedName("maxScore")
    public int maxScore;

    @SerializedName("date")
    public String date;
}
