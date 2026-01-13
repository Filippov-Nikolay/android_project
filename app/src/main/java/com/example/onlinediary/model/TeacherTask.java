package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class TeacherTask {
    @SerializedName("id")
    public long id;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("subjectName")
    public String subjectName;

    @SerializedName("groupName")
    public String groupName;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("deadline")
    public String deadline;

    @SerializedName("isOverdue")
    public boolean isOverdue;

    @SerializedName("iconFileName")
    public String iconFileName;

    @SerializedName("stats")
    public Stats stats;

    public static class Stats {
        @SerializedName("submitted")
        public int submitted;

        @SerializedName("total")
        public int total;
    }
}
