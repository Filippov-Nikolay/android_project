package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class GradeRequest {
    @SerializedName("grade")
    public int grade;

    @SerializedName("feedback")
    public String feedback;

    public GradeRequest(int grade, String feedback) {
        this.grade = grade;
        this.feedback = feedback;
    }
}
