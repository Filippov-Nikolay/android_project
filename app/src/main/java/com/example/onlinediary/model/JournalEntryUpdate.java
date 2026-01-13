package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class JournalEntryUpdate {
    @SerializedName("scheduleId")
    public long scheduleId;

    @SerializedName("studentId")
    public long studentId;

    @SerializedName("attendance")
    public String attendance;

    @SerializedName("workType")
    public String workType;

    @SerializedName("grade")
    public String grade;

    public JournalEntryUpdate(long scheduleId, long studentId, String attendance, String workType, String grade) {
        this.scheduleId = scheduleId;
        this.studentId = studentId;
        this.attendance = attendance;
        this.workType = workType;
        this.grade = grade;
    }
}
