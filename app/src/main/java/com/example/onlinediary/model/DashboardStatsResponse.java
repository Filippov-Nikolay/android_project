package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DashboardStatsResponse {
    @SerializedName("stats")
    public List<SubjectStat> stats;

    @SerializedName("skills")
    public List<SkillDatum> skills;
}
