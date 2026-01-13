package com.example.onlinediary.core;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeStore {
    private static final String PREFS_NAME = "ui_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final SharedPreferences prefs;

    public ThemeStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, true);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }
}
