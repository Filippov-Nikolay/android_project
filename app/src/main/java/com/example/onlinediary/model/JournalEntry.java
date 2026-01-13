package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class JournalEntry {
    @SerializedName("studentId")
    public long studentId;

    @SerializedName("studentFullName")
    public String studentFullName;

    @SerializedName("avatarUrl")
    public String avatarUrl;

    @SerializedName("attendance")
    public String attendance;

    @SerializedName("workType")
    public String workType;

    @SerializedName("grade")
    public String grade;
}
