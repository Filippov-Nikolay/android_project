package com.example.onlinediary.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Window;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.onlinediary.R;
import com.example.onlinediary.core.ThemeStore;

public final class ThemeHelper {
    private ThemeHelper() {}

    public static void applySavedTheme(Context context) {
        ThemeStore store = new ThemeStore(context);
        int mode = store.getThemeMode();

       AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void toggleTheme(Activity activity) {
        ThemeStore store = new ThemeStore(activity);

        int newMode = isDarkMode(activity)
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;

        store.setThemeMode(newMode);
        activity.recreate();
    }

    public static void applySystemBars(Activity activity) {
        Window window = activity.getWindow();
        int color = ContextCompat.getColor(activity, R.color.schedule_background);

        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);

        boolean isDark = isDarkMode(activity);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());

        controller.setAppearanceLightStatusBars(!isDark);
        controller.setAppearanceLightNavigationBars(!isDark);
    }

    public static void updateThemeIcon(ImageView button) {
        if (button == null) return;
        boolean dark = isDarkMode(button.getContext());
        button.setImageResource(dark ? R.drawable.ic_theme_light : R.drawable.ic_theme_dark);
    }

    public static boolean isDarkMode(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }
}