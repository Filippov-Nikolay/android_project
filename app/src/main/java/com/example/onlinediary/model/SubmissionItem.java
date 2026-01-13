package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class SubmissionItem {
    @SerializedName("studentId")
    public long studentId;

    @SerializedName("studentName")
    public String studentName;

    @SerializedName("submitted")
    public boolean submitted;

    @SerializedName("submittedAt")
    public String submittedAt;

    @SerializedName("fileName")
    public String fileName;

    @SerializedName("grade")
    public Integer grade;

    @SerializedName("feedback")
    public String feedback;
}
