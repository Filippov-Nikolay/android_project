package com.example.onlinediary.core;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthStore {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "userRole";

    private final SharedPreferences prefs;

    public AuthStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuth(String token, String role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clear() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_ROLE)
                .apply();
    }
}
