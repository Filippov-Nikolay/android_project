package com.example.onlinediary.model;

import com.google.gson.annotations.SerializedName;

public class PasswordUpdateRequest {
    @SerializedName("password")
    public String password;

    public PasswordUpdateRequest(String password) {
        this.password = password;
    }
}
