package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    public long id;

    @SerializedName("login")
    public String login;

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("email")
    public String email;

    @SerializedName("role")
    public String role;

    @SerializedName("groupId")
    public Long groupId;

    @SerializedName("groupName")
    public String groupName;

    // Backend returns base64 avatar string (no data: prefix)
    @SerializedName("avatar")
    public String avatar;
}
