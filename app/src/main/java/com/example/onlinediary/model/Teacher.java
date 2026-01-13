package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class Teacher {
    @SerializedName("id")
    public long id;

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;
}
