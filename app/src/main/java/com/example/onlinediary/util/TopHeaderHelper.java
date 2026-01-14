package com.example.onlinediary.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.PopupWindowCompat;

import com.example.onlinediary.AdminStatsActivity;
import com.example.onlinediary.DashboardActivity;
import com.example.onlinediary.LoginActivity;
import com.example.onlinediary.ManageHomeworkActivity;
import com.example.onlinediary.ProfileActivity;
import com.example.onlinediary.R;
import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class TopHeaderHelper {
    private static User cachedUser;
    private static boolean loadingUser;

    private TopHeaderHelper() {}

    public static void bind(Activity activity) {
        View header = activity.findViewById(R.id.topHeaderRoot);
        if (header == null) {
            return;
        }

        ThemeHelper.applySystemBars(activity);
        applySystemBarInsets(findContentRoot(activity));

        View brand = activity.findViewById(R.id.headerBrand);
        ImageButton themeButton = activity.findViewById(R.id.headerThemeButton);
        ImageButton notificationButton = activity.findViewById(R.id.headerNotificationButton);
        View menuButton = activity.findViewById(R.id.headerMenuButton);

        if (brand != null) {
            brand.setOnClickListener(v -> navigateHome(activity));
        }
        if (themeButton != null) {
            ThemeHelper.updateThemeIcon(themeButton);
            themeButton.setOnClickListener(v -> ThemeHelper.toggleTheme(activity));
        }
        if (notificationButton != null) {
            notificationButton.setOnClickListener(v ->
                    Toast.makeText(activity, "No notifications yet", Toast.LENGTH_SHORT).show()
            );
        }
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> showMenu(activity, menuButton));
        }

        applyHeaderAvatar(activity, cachedUser);
        if (cachedUser == null && !loadingUser) {
            fetchUser(activity);
        }
    }

    public static void updateHeaderUser(Activity activity, User user) {
        cachedUser = user;
        applyHeaderAvatar(activity, user);
    }

    private static void navigateHome(Activity activity) {
        AuthStore authStore = new AuthStore(activity);
        String role = authStore.getRole();
        Class<?> target = DashboardActivity.class;
        if ("ADMIN".equalsIgnoreCase(role)) {
            target = AdminStatsActivity.class;
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            target = ManageHomeworkActivity.class;
        }

        if (activity.getClass().equals(target)) {
            return;
        }
        Intent intent = new Intent(activity, target);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void fetchUser(Activity activity) {
        loadingUser = true;
        ApiService apiService = ApiClient.getService(activity);
        apiService.getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                loadingUser = false;
                if (response.isSuccessful() && response.body() != null) {
                    cachedUser = response.body();
                    applyHeaderAvatar(activity, cachedUser);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loadingUser = false;
            }
        });
    }

    private static void applyHeaderAvatar(Activity activity, User user) {
        TextView avatar = activity.findViewById(R.id.headerAvatar);
        if (avatar == null) {
            return;
        }
        if (user == null) {
            avatar.setText("JB");
            return;
        }
        String first = safe(user.firstName);
        String last = safe(user.lastName);
        avatar.setText(buildInitials(first, last, user.login));
    }

    private static void showMenu(Activity activity, View anchor) {
        View menuView = LayoutInflater.from(activity)
                .inflate(R.layout.view_profile_header_menu, null);

        TextView menuAvatar = menuView.findViewById(R.id.menuAvatar);
        TextView menuName = menuView.findViewById(R.id.menuName);
        TextView menuEmail = menuView.findViewById(R.id.menuEmail);
        TextView menuGroup = menuView.findViewById(R.id.menuGroup);

        bindMenuUser(menuAvatar, menuName, menuEmail, menuGroup);

        View profileAction = menuView.findViewById(R.id.menuProfileAction);
        View logoutAction = menuView.findViewById(R.id.menuLogoutAction);

        PopupWindow menu = new PopupWindow(
                menuView,
                dpToPx(activity, 240),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        menu.setOutsideTouchable(true);
        menu.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menu.setElevation(dpToPx(activity, 8));

        profileAction.setOnClickListener(v -> {
            menu.dismiss();
            activity.startActivity(new Intent(activity, ProfileActivity.class));
        });

        logoutAction.setOnClickListener(v -> {
            menu.dismiss();
            AuthStore authStore = new AuthStore(activity);
            authStore.clear();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        });

        PopupWindowCompat.showAsDropDown(
                menu,
                anchor,
                0,
                dpToPx(activity, 8),
                Gravity.END
        );
    }

    private static void bindMenuUser(
            TextView menuAvatar,
            TextView menuName,
            TextView menuEmail,
            TextView menuGroup
    ) {
        if (menuAvatar == null || menuName == null || menuEmail == null || menuGroup == null) {
            return;
        }
        User user = cachedUser;
        if (user == null) {
            menuAvatar.setText("JB");
            menuName.setText("User");
            menuEmail.setText("--");
            menuGroup.setVisibility(View.GONE);
            return;
        }
        String first = safe(user.firstName);
        String last = safe(user.lastName);
        String name = (first + " " + last).trim();
        menuAvatar.setText(buildInitials(first, last, user.login));
        menuName.setText(name.isEmpty() ? "User" : name);
        menuEmail.setText(safe(user.email, "--"));

        if (user.groupName == null || user.groupName.trim().isEmpty()) {
            menuGroup.setVisibility(View.GONE);
        } else {
            menuGroup.setVisibility(View.VISIBLE);
            menuGroup.setText(user.groupName.trim());
        }
    }

    public static String buildInitials(String first, String last, String login) {
        String base = buildInitials(first, last);
        if (!"JB".equals(base) || (first != null && !first.isEmpty()) || (last != null && !last.isEmpty())) {
            return base;
        }
        String loginClean = login == null ? "" : login.trim();
        if (!loginClean.isEmpty()) {
            String trimmed = loginClean.replaceAll("\\s+", "");
            if (trimmed.length() >= 2) {
                return trimmed.substring(0, 2).toUpperCase();
            }
            return trimmed.substring(0, 1).toUpperCase();
        }
        return base;
    }

    private static String buildInitials(String first, String last) {
        StringBuilder builder = new StringBuilder();
        if (first != null && !first.isEmpty()) {
            builder.append(first.substring(0, 1));
        }
        if (last != null && !last.isEmpty()) {
            builder.append(last.substring(0, 1));
        }
        String initials = builder.toString().toUpperCase();
        return initials.isEmpty() ? "JB" : initials;
    }

    private static void applySystemBarInsets(View view) {
        if (view == null) {
            return;
        }
        final int paddingLeft = view.getPaddingLeft();
        final int paddingTop = view.getPaddingTop();
        final int paddingRight = view.getPaddingRight();
        final int paddingBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + systemBars.bottom
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    private static View findContentRoot(Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        if (content instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) content;
            if (group.getChildCount() > 0) {
                return group.getChildAt(0);
            }
        }
        return content;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safe(String value, String fallback) {
        String trimmed = safe(value);
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static int dpToPx(Activity activity, int dp) {
        return Math.round(dp * activity.getResources().getDisplayMetrics().density);
    }
}
