package com.example.onlinediary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
    private EditText editPassword;
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

        apiService = ApiClient.getService(this);

        avatarImage = findViewById(R.id.avatarImage);
        profileName = findViewById(R.id.profileName);
        profileRole = findViewById(R.id.profileRole);
        profileEmail = findViewById(R.id.profileEmail);
        editPassword = findViewById(R.id.editPassword);
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
        String name = (user.firstName == null ? "" : user.firstName) + " " + (user.lastName == null ? "" : user.lastName);
        profileName.setText(name.trim());
        profileRole.setText(user.role == null ? "" : user.role);
        profileEmail.setText(user.email == null ? "" : user.email);

        if (user.avatarUrl != null && user.avatarUrl.startsWith("data:image")) {
            int comma = user.avatarUrl.indexOf(',');
            if (comma > 0) {
                String base64 = user.avatarUrl.substring(comma + 1);
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                avatarImage.setImageBitmap(bitmap);
                return;
            }
        }

        if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            Glide.with(this).load(user.avatarUrl).into(avatarImage);
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
        if (password.isEmpty()) {
            Toast.makeText(this, "Enter a new password", Toast.LENGTH_SHORT).show();
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
}
