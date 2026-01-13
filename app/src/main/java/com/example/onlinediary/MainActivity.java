package com.example.onlinediary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthStore authStore = new AuthStore(this);
        if (!authStore.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        TextView userInfoText = findViewById(R.id.userInfoText);
        Button btnDashboard = findViewById(R.id.btnDashboard);
        Button btnSchedule = findViewById(R.id.btnSchedule);
        Button btnHomework = findViewById(R.id.btnHomework);
        Button btnManageHomework = findViewById(R.id.btnManageHomework);
        Button btnAdminStats = findViewById(R.id.btnAdminStats);
        Button btnUsers = findViewById(R.id.btnUsers);
        Button btnSubjects = findViewById(R.id.btnSubjects);
        Button btnScheduleAdmin = findViewById(R.id.btnScheduleAdmin);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        String role = authStore.getRole() == null ? "" : authStore.getRole();
        boolean isTeacher = "TEACHER".equalsIgnoreCase(role);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        btnManageHomework.setVisibility(isTeacher || isAdmin ? android.view.View.VISIBLE : android.view.View.GONE);
        btnAdminStats.setVisibility(isAdmin ? android.view.View.VISIBLE : android.view.View.GONE);
        btnUsers.setVisibility(isAdmin ? android.view.View.VISIBLE : android.view.View.GONE);
        btnSubjects.setVisibility(isAdmin ? android.view.View.VISIBLE : android.view.View.GONE);
        btnScheduleAdmin.setVisibility(isAdmin ? android.view.View.VISIBLE : android.view.View.GONE);

        btnDashboard.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        btnSchedule.setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));
        btnHomework.setOnClickListener(v -> startActivity(new Intent(this, HomeworkActivity.class)));
        btnManageHomework.setOnClickListener(v -> startActivity(new Intent(this, ManageHomeworkActivity.class)));
        btnAdminStats.setOnClickListener(v -> startActivity(new Intent(this, AdminStatsActivity.class)));
        btnUsers.setOnClickListener(v -> startActivity(new Intent(this, UsersActivity.class)));
        btnSubjects.setOnClickListener(v -> startActivity(new Intent(this, SubjectsActivity.class)));
        btnScheduleAdmin.setOnClickListener(v -> startActivity(new Intent(this, ScheduleAdminActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            authStore.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        ApiService apiService = ApiClient.getService(this);
        apiService.getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String name = (user.firstName == null ? "" : user.firstName) + " " + (user.lastName == null ? "" : user.lastName);
                    String info = name.trim() + " (" + user.role + ")";
                    if (user.groupName != null && !user.groupName.isEmpty()) {
                        info = info + " - " + user.groupName;
                    }
                    userInfoText.setText(info);
                } else {
                    userInfoText.setText("Signed in as " + role);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                userInfoText.setText("Signed in as " + role);
                Toast.makeText(MainActivity.this, "Unable to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
