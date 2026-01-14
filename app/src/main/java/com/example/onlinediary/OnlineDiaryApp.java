package com.example.onlinediary;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import com.example.onlinediary.core.ThemeStore;
import com.example.onlinediary.util.ThemeHelper;

public class OnlineDiaryApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ThemeHelper.applySavedTheme(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeStore store = new ThemeStore(this);
        int currentSystemMask = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        store.syncWithSystem(currentSystemMask);

        ThemeHelper.applySavedTheme(this);
    }
}