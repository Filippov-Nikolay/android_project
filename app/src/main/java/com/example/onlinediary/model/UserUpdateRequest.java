package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class UserUpdateRequest {
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

    public UserUpdateRequest(String firstName, String lastName, String email, String role, Long groupId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.groupId = groupId;
    }
}
