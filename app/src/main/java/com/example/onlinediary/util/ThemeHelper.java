package com.example.onlinediary.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.onlinediary.R;
import com.example.onlinediary.core.ThemeStore;

public final class ThemeHelper {
    private ThemeHelper() {}

    public static void applySavedTheme(Context context) {
        ThemeStore store = new ThemeStore(context);
        int mode = store.isDarkMode()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void toggleTheme(Activity activity) {
        ThemeStore store = new ThemeStore(activity);
        boolean dark = !store.isDarkMode();
        store.setDarkMode(dark);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        activity.recreate();
    }

    public static void applySystemBars(Activity activity) {
        int bg = ContextCompat.getColor(activity, R.color.schedule_background);
        activity.getWindow().setStatusBarColor(bg);
        activity.getWindow().setNavigationBarColor(bg);

        boolean light = !isDarkMode(activity);
        int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
        if (light) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (light) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    public static void updateThemeIcon(ImageView button) {
        if (button == null) {
            return;
        }
        boolean dark = isDarkMode(button.getContext());
        button.setImageResource(dark ? R.drawable.ic_theme_light : R.drawable.ic_theme_dark);
    }

    private static boolean isDarkMode(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }
}
