package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class SkillDatum {
    @SerializedName("skill")
    public String skill;

    @SerializedName("value")
    public int value;
}
