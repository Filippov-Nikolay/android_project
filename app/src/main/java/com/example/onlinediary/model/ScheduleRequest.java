package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class ScheduleRequest {
    @SerializedName("groupId")
    public long groupId;

    @SerializedName("subjectId")
    public long subjectId;

    @SerializedName("teacherId")
    public long teacherId;

    @SerializedName("date")
    public String date;

    @SerializedName("lessonNumber")
    public int lessonNumber;

    @SerializedName("room")
    public String room;

    @SerializedName("type")
    public String type;

    public ScheduleRequest(long groupId, long subjectId, long teacherId, String date, int lessonNumber, String room, String type) {
        this.groupId = groupId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.date = date;
        this.lessonNumber = lessonNumber;
        this.room = room;
        this.type = type;
    }
}
