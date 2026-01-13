package com.example.onlinediary;

import android.app.Application;

import com.example.onlinediary.util.ThemeHelper;

public class OnlineDiaryApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applySavedTheme(this);
    }
}
