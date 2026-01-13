package com.example.onlinediary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
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

        Button btnPickAvatar = findViewById(R.id.btnPickAvatar);
        Button btnUploadAvatar = findViewById(R.id.btnUploadAvatar);
        Button btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        btnPickAvatar.setOnClickListener(v -> avatarPicker.launch("image/*"));
        btnUploadAvatar.setOnClickListener(v -> uploadAvatar());
        btnUpdatePassword.setOnClickListener(v -> updatePassword());

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
        String first = safe(user.firstName);
        String last = safe(user.lastName);
        String name = (first + " " + last).trim();
        profileName.setText(name.isEmpty() ? "User" : name);
        profileEmail.setText(safe(user.email, "--"));
        profileLogin.setText(safe(user.login, "--"));

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

    private void setAvatarPlaceholder() {
        avatarImage.setImageResource(android.R.drawable.ic_menu_camera);
        avatarImage.setColorFilter(ContextCompat.getColor(this, R.color.schedule_muted));
    }
}
