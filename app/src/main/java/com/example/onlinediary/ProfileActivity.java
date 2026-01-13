package com.example.onlinediary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.PopupWindowCompat;

import com.bumptech.glide.Glide;
import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.PasswordUpdateRequest;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.util.MultipartUtils;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ImageView avatarImage;
    private TextView profileName;
    private TextView profileRole;
    private TextView profileEmail;
    private TextView profileLogin;
    private TextView profileGroup;
    private View profileGroupRow;
    private EditText editPassword;
    private EditText editPasswordConfirm;
    private ProgressBar progressBar;
    private Uri selectedAvatar;
    private ApiService apiService;
    private AuthStore authStore;
    private User currentUser;

    private TextView headerAvatar;
    private View headerMenuButton;
    private PopupWindow headerMenu;
    private TextView menuAvatar;
    private TextView menuName;
    private TextView menuEmail;
    private TextView menuGroup;

    private final ActivityResultLauncher<String> avatarPicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                selectedAvatar = uri;
                if (uri != null) {
                    avatarImage.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authStore = new AuthStore(this);
        getWindow().setStatusBarColor(getColor(R.color.schedule_background));
        getWindow().setNavigationBarColor(getColor(R.color.schedule_background));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        apiService = ApiClient.getService(this);

        avatarImage = findViewById(R.id.avatarImage);
        profileName = findViewById(R.id.profileName);
        profileRole = findViewById(R.id.profileRole);
        profileEmail = findViewById(R.id.profileEmail);
        profileLogin = findViewById(R.id.profileLogin);
        profileGroup = findViewById(R.id.profileGroup);
        profileGroupRow = findViewById(R.id.profileGroupRow);
        editPassword = findViewById(R.id.editPassword);
        editPasswordConfirm = findViewById(R.id.editPasswordConfirm);
        progressBar = findViewById(R.id.profileProgress);
        headerAvatar = findViewById(R.id.profileHeaderAvatar);
        headerMenuButton = findViewById(R.id.profileHeaderMenu);

        Button btnPickAvatar = findViewById(R.id.btnPickAvatar);
        Button btnUploadAvatar = findViewById(R.id.btnUploadAvatar);
        Button btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        btnPickAvatar.setOnClickListener(v -> avatarPicker.launch("image/*"));
        btnUploadAvatar.setOnClickListener(v -> uploadAvatar());
        btnUpdatePassword.setOnClickListener(v -> updatePassword());
        headerMenuButton.setOnClickListener(v -> showHeaderMenu());

        loadProfile();
    }

    private void loadProfile() {
        setLoading(true);
        apiService.getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    bindUser(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindUser(User user) {
        currentUser = user;
        String first = safe(user.firstName);
        String last = safe(user.lastName);
        String name = (first + " " + last).trim();
        profileName.setText(name.isEmpty() ? "User" : name);
        profileEmail.setText(safe(user.email, "--"));
        profileLogin.setText(safe(user.login, "--"));
        headerAvatar.setText(buildInitials(first, last));

        String role = safe(user.role).toUpperCase();
        if (role.isEmpty()) {
            profileRole.setVisibility(View.GONE);
        } else {
            profileRole.setVisibility(View.VISIBLE);
            profileRole.setText(role);
            applyRoleBadge(role);
        }

        if (user.groupName == null || user.groupName.trim().isEmpty()) {
            profileGroupRow.setVisibility(View.GONE);
        } else {
            profileGroupRow.setVisibility(View.VISIBLE);
            profileGroup.setText(user.groupName.trim());
        }

        if (user.avatarUrl != null && user.avatarUrl.startsWith("data:image")) {
            int comma = user.avatarUrl.indexOf(',');
            if (comma > 0) {
                String base64 = user.avatarUrl.substring(comma + 1);
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                avatarImage.setImageBitmap(bitmap);
                avatarImage.setColorFilter(null);
                return;
            }
        }

        if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            avatarImage.setColorFilter(null);
            Glide.with(this).load(user.avatarUrl).into(avatarImage);
        } else {
            setAvatarPlaceholder();
        }

        updateHeaderMenu(user);
    }

    private void uploadAvatar() {
        if (selectedAvatar == null) {
            Toast.makeText(this, "Choose an avatar first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MultipartBody.Part part = MultipartUtils.createFilePart(this, "file", selectedAvatar, "avatar");
            setLoading(true);
            apiService.updateAvatar(part).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    setLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Avatar updated", Toast.LENGTH_SHORT).show();
                        loadProfile();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update avatar", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        String password = editPassword.getText().toString().trim();
        String confirm = editPasswordConfirm.getText().toString().trim();
        if (password.isEmpty()) {
            Toast.makeText(this, "Enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (confirm.isEmpty()) {
            Toast.makeText(this, "Confirm the new password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        apiService.updatePassword(new PasswordUpdateRequest(password)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                    editPassword.setText("");
                    editPasswordConfirm.setText("");
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showHeaderMenu() {
        if (headerMenu == null) {
            View menuView = LayoutInflater.from(this)
                    .inflate(R.layout.view_profile_header_menu, null);
            menuAvatar = menuView.findViewById(R.id.menuAvatar);
            menuName = menuView.findViewById(R.id.menuName);
            menuEmail = menuView.findViewById(R.id.menuEmail);
            menuGroup = menuView.findViewById(R.id.menuGroup);

            View profileAction = menuView.findViewById(R.id.menuProfileAction);
            View logoutAction = menuView.findViewById(R.id.menuLogoutAction);
            profileAction.setOnClickListener(v -> headerMenu.dismiss());
            logoutAction.setOnClickListener(v -> {
                headerMenu.dismiss();
                performLogout();
            });

            headerMenu = new PopupWindow(
                    menuView,
                    dpToPx(240),
                    PopupWindow.LayoutParams.WRAP_CONTENT,
                    true
            );
            headerMenu.setOutsideTouchable(true);
            headerMenu.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            headerMenu.setElevation(dpToPx(8));
        }

        updateHeaderMenu(currentUser);
        PopupWindowCompat.showAsDropDown(
                headerMenu,
                headerMenuButton,
                0,
                dpToPx(8),
                Gravity.END
        );
    }

    private void updateHeaderMenu(User user) {
        if (user == null || menuName == null) {
            return;
        }
        String first = safe(user.firstName);
        String last = safe(user.lastName);
        String name = (first + " " + last).trim();
        menuName.setText(name.isEmpty() ? "User" : name);
        menuEmail.setText(safe(user.email, "--"));
        menuAvatar.setText(buildInitials(first, last));

        if (user.groupName == null || user.groupName.trim().isEmpty()) {
            menuGroup.setVisibility(View.GONE);
        } else {
            menuGroup.setVisibility(View.VISIBLE);
            menuGroup.setText(user.groupName.trim());
        }
    }

    private void applyRoleBadge(String role) {
        if ("TEACHER".equals(role)) {
            profileRole.setBackgroundResource(R.drawable.bg_admin_role_teacher);
            profileRole.setTextColor(ContextCompat.getColor(this, R.color.manage_stat_green));
        } else if ("ADMIN".equals(role)) {
            profileRole.setBackgroundResource(R.drawable.bg_admin_role_admin);
            profileRole.setTextColor(ContextCompat.getColor(this, R.color.manage_stat_red));
        } else if ("STUDENT".equals(role)) {
            profileRole.setBackgroundResource(R.drawable.bg_admin_role_student);
            profileRole.setTextColor(ContextCompat.getColor(this, R.color.manage_stat_blue));
        } else {
            profileRole.setBackgroundResource(R.drawable.bg_admin_role_default);
            profileRole.setTextColor(ContextCompat.getColor(this, R.color.schedule_text));
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value, String fallback) {
        String trimmed = safe(value);
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String buildInitials(String first, String last) {
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

    private void setAvatarPlaceholder() {
        avatarImage.setImageResource(android.R.drawable.ic_menu_camera);
        avatarImage.setColorFilter(ContextCompat.getColor(this, R.color.schedule_muted));
    }

    private void performLogout() {
        authStore.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
