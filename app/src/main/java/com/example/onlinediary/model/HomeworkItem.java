package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class HomeworkItem {
    @SerializedName("id")
    public long id;

    @SerializedName("subjectName")
    public String subjectName;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("teacherName")
    public String teacherName;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("pointsMax")
    public int pointsMax;

    @SerializedName("date")
    public String date;

    @SerializedName("deadline")
    public String deadline;

    @SerializedName("deadlineText")
    public String deadlineText;

    @SerializedName("isOverdue")
    public boolean isOverdue;

    @SerializedName("month")
    public String month;

    @SerializedName("status")
    public String status;

    @SerializedName("fileName")
    public String fileName;

    @SerializedName("submissionFileName")
    public String submissionFileName;

    @SerializedName("iconFileName")
    public String iconFileName;

    @SerializedName("grade")
    public Integer grade;

    @SerializedName("feedback")
    public String feedback;
}
