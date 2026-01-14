package com.example.onlinediary.core;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeStore {
    private static final String PREFS_NAME = "ui_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_LAST_SYSTEM_MODE = "last_system_mode";

    private final SharedPreferences prefs;

    public ThemeStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();

        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public void resetToSystem() {
        prefs.edit().putInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply();
    }

    public void syncWithSystem(int currentSystemMask) {
        int lastSystemMask = prefs.getInt(KEY_LAST_SYSTEM_MODE, -1);

        if (lastSystemMask != -1 && lastSystemMask != currentSystemMask) {

            resetToSystem();
        }

        prefs.edit().putInt(KEY_LAST_SYSTEM_MODE, currentSystemMask).apply();
    }
}