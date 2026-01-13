package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class ScheduleEvent {
    @SerializedName("id")
    public long id;

    @SerializedName("subjectName")
    public String subjectName;

    @SerializedName("teacherFullName")
    public String teacherFullName;

    @SerializedName("groupName")
    public String groupName;

    @SerializedName("date")
    public String date;

    @SerializedName("lessonNumber")
    public int lessonNumber;

    @SerializedName("type")
    public String type;

    @SerializedName("room")
    public String room;

    @SerializedName("subjectId")
    public long subjectId;

    @SerializedName("teacherId")
    public long teacherId;

    @SerializedName("groupId")
    public long groupId;

    @SerializedName("title")
    public String title;

    @SerializedName("startTime")
    public String startTime;

    @SerializedName("endTime")
    public String endTime;
}
