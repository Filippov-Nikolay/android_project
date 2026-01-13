package com.example.onlinediary;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.Accrual;
import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.SimpleTextAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {
    private TextView userInfoText;
    private TextView statsText;
    private ProgressBar progressBar;
    private SimpleTextAdapter accrualsAdapter;
    private SimpleTextAdapter scheduleAdapter;
    private ApiService apiService;
    private int pendingCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        userInfoText = findViewById(R.id.dashboardUserInfo);
        statsText = findViewById(R.id.dashboardStats);
        progressBar = findViewById(R.id.dashboardProgress);

        RecyclerView accrualsList = findViewById(R.id.accrualsList);
        RecyclerView todayScheduleList = findViewById(R.id.todayScheduleList);

        accrualsAdapter = new SimpleTextAdapter();
        scheduleAdapter = new SimpleTextAdapter();

        accrualsList.setLayoutManager(new LinearLayoutManager(this));
        accrualsList.setAdapter(accrualsAdapter);

        todayScheduleList.setLayoutManager(new LinearLayoutManager(this));
        todayScheduleList.setAdapter(scheduleAdapter);

        apiService = ApiClient.getService(this);

        loadData();
    }

    private void loadData() {
        pendingCalls = 0;
        lessonsToday = null;
        todoCount = null;
        overdueCount = null;
        setLoading(true);
        loadUser();
        loadAccruals();
        loadScheduleAndHomeworkStats();
    }

    private void loadUser() {
        pendingCalls++;
        apiService.getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String name = (user.firstName == null ? "" : user.firstName) + " " + (user.lastName == null ? "" : user.lastName);
                    String info = name.trim() + " (" + user.role + ")";
                    if (user.groupName != null && !user.groupName.isEmpty()) {
                        info = info + " - " + user.groupName;
                    }
                    userInfoText.setText(info);
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                pendingCalls--;
                setLoading(pendingCalls > 0);
                Toast.makeText(DashboardActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAccruals() {
        pendingCalls++;
        apiService.getAccruals().enqueue(new Callback<List<Accrual>>() {
            @Override
            public void onResponse(Call<List<Accrual>> call, Response<List<Accrual>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    List<String> items = new ArrayList<>();
                    for (Accrual accrual : response.body()) {
                        String text = accrual.subject + " - " + accrual.title + " (" + accrual.score + "/" + accrual.maxScore + ")";
                        items.add(text);
                    }
                    accrualsAdapter.setItems(items);
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<Accrual>> call, Throwable t) {
                pendingCalls--;
                setLoading(pendingCalls > 0);
                Toast.makeText(DashboardActivity.this, "Failed to load accruals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScheduleAndHomeworkStats() {
        final String today = LocalDate.now().toString();

        pendingCalls++;
        apiService.getSchedule(null, null).enqueue(new Callback<List<ScheduleEvent>>() {
            @Override
            public void onResponse(Call<List<ScheduleEvent>> call, Response<List<ScheduleEvent>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    List<String> items = new ArrayList<>();
                    int todayCount = 0;
                    for (ScheduleEvent event : response.body()) {
                        String eventDate = dateOnly(event.date);
                        if (today.equals(eventDate)) {
                            todayCount++;
                            items.add(event.subjectName + " - Lesson " + event.lessonNumber + " (" + event.teacherFullName + ")");
                        }
                    }
                    scheduleAdapter.setItems(items);
                    updateStats(todayCount, null, null);
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<ScheduleEvent>> call, Throwable t) {
                pendingCalls--;
                setLoading(pendingCalls > 0);
                Toast.makeText(DashboardActivity.this, "Failed to load schedule", Toast.LENGTH_SHORT).show();
            }
        });

        pendingCalls++;
        apiService.getHomeworks().enqueue(new Callback<List<HomeworkItem>>() {
            @Override
            public void onResponse(Call<List<HomeworkItem>> call, Response<List<HomeworkItem>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    int todoCount = 0;
                    int overdueCount = 0;
                    for (HomeworkItem item : response.body()) {
                        if ("todo".equalsIgnoreCase(item.status)) {
                            todoCount++;
                        }
                        if (item.isOverdue) {
                            overdueCount++;
                        }
                    }
                    updateStats(null, todoCount, overdueCount);
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<HomeworkItem>> call, Throwable t) {
                pendingCalls--;
                setLoading(pendingCalls > 0);
                Toast.makeText(DashboardActivity.this, "Failed to load homework", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer lessonsToday;
    private Integer todoCount;
    private Integer overdueCount;

    private void updateStats(Integer lessons, Integer todo, Integer overdue) {
        if (lessons != null) {
            lessonsToday = lessons;
        }
        if (todo != null) {
            todoCount = todo;
        }
        if (overdue != null) {
            overdueCount = overdue;
        }

        String stats = "Lessons today: " + (lessonsToday == null ? "-" : lessonsToday)
                + " | Todo: " + (todoCount == null ? "-" : todoCount)
                + " | Overdue: " + (overdueCount == null ? "-" : overdueCount);
        statsText.setText(stats);
    }

    private String dateOnly(String value) {
        if (value == null) {
            return "";
        }
        int idx = value.indexOf('T');
        return idx > 0 ? value.substring(0, idx) : value;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
