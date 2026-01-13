package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class SubjectAssignmentRequest {
    @SerializedName("teacherId")
    public long teacherId;

    @SerializedName("subjectId")
    public long subjectId;

    @SerializedName("groupId")
    public long groupId;

    public SubjectAssignmentRequest(long teacherId, long subjectId, long groupId) {
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.groupId = groupId;
    }
}
