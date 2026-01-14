package com.example.onlinediary;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.util.ThemeHelper; // Импортируем хелпер

public class MainActivity extends AppCompatActivity {
    private boolean isThemeReady = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen splashScreen =
                androidx.core.splashscreen.SplashScreen.installSplashScreen(this);

        ThemeHelper.applySavedTheme(this);

        isThemeReady = true;

        splashScreen.setKeepOnScreenCondition(() -> !isThemeReady);

        super.onCreate(savedInstanceState);

        ThemeHelper.applySystemBars(this);

        AuthStore authStore = new AuthStore(this);
        if (!authStore.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String role = authStore.getRole() == null ? "" : authStore.getRole();
        Intent intent;
        if ("ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminStatsActivity.class);
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            intent = new Intent(this, ManageHomeworkActivity.class);
        } else {
            intent = new Intent(this, DashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}