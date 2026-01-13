package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    public String token;

    @SerializedName("role")
    public String role;
}
