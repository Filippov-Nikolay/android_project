package com.example.onlinediary;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.Subject;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.AdminRecentUserAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStatsActivity extends AppCompatActivity {
    private TextView usersValue;
    private TextView teachersValue;
    private TextView groupsValue;
    private TextView subjectsValue;
    private TextView emptyText;
    private ProgressBar progressBar;
    private AdminRecentUserAdapter adapter;
    private ApiService apiService;
    private int pendingCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stats);

        getWindow().setStatusBarColor(getColor(R.color.schedule_background));
        getWindow().setNavigationBarColor(getColor(R.color.schedule_background));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        usersValue = findViewById(R.id.adminStatUsersValue);
        teachersValue = findViewById(R.id.adminStatTeachersValue);
        groupsValue = findViewById(R.id.adminStatGroupsValue);
        subjectsValue = findViewById(R.id.adminStatSubjectsValue);
        emptyText = findViewById(R.id.adminRecentEmpty);
        progressBar = findViewById(R.id.adminStatsProgress);

        RecyclerView recentList = findViewById(R.id.adminRecentList);
        recentList.setLayoutManager(new LinearLayoutManager(this));
        recentList.setNestedScrollingEnabled(false);
        adapter = new AdminRecentUserAdapter();
        recentList.setAdapter(adapter);

        apiService = ApiClient.getService(this);
        loadData();
    }

    private void loadData() {
        pendingCalls = 0;
        setLoading(true);
        loadUsers();
        loadTeachers();
        loadGroups();
        loadSubjects();
    }

    private void loadUsers() {
        pendingCalls++;
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    usersValue.setText(String.valueOf(users.size()));
                    updateRecentUsers(users);
                } else {
                    usersValue.setText("0");
                    updateRecentUsers(new ArrayList<>());
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                pendingCalls--;
                usersValue.setText("0");
                updateRecentUsers(new ArrayList<>());
                setLoading(pendingCalls > 0);
                Toast.makeText(AdminStatsActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeachers() {
        pendingCalls++;
        apiService.getTeachers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    teachersValue.setText(String.valueOf(response.body().size()));
                } else {
                    teachersValue.setText("0");
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                pendingCalls--;
                teachersValue.setText("0");
                setLoading(pendingCalls > 0);
                Toast.makeText(AdminStatsActivity.this, "Failed to load teachers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroups() {
        pendingCalls++;
        apiService.getGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    groupsValue.setText(String.valueOf(response.body().size()));
                } else {
                    groupsValue.setText("0");
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                pendingCalls--;
                groupsValue.setText("0");
                setLoading(pendingCalls > 0);
                Toast.makeText(AdminStatsActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSubjects() {
        pendingCalls++;
        apiService.getSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    subjectsValue.setText(String.valueOf(response.body().size()));
                } else {
                    subjectsValue.setText("0");
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                pendingCalls--;
                subjectsValue.setText("0");
                setLoading(pendingCalls > 0);
                Toast.makeText(AdminStatsActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecentUsers(List<User> users) {
        if (users == null) {
            users = new ArrayList<>();
        }
        users.sort(Comparator.comparingLong(u -> u.id));
        List<User> recent = new ArrayList<>();
        for (int i = users.size() - 1; i >= 0 && recent.size() < 5; i--) {
            recent.add(users.get(i));
        }
        adapter.setItems(recent);
        emptyText.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
