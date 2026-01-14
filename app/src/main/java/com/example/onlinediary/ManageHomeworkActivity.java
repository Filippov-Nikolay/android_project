package com.example.onlinediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.onlinediary.model.TeacherTask;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.TeacherTaskAdapter;
import com.example.onlinediary.util.BottomNavHelper;
import com.example.onlinediary.util.DialogHelper;
import com.example.onlinediary.util.TopHeaderHelper;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageHomeworkActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TeacherTaskAdapter adapter;
    private ApiService apiService;
    private TextView statSubjectsValue;
    private TextView statUpcomingValue;
    private TextView statSubmittedValue;
    private TextView emptyText;
    private RecyclerView tasksList;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_homework);
        TopHeaderHelper.bind(this);

        progressBar = findViewById(R.id.manageHomeworkProgress);
        statSubjectsValue = findViewById(R.id.manageStatSubjectsValue);
        statUpcomingValue = findViewById(R.id.manageStatUpcomingValue);
        statSubmittedValue = findViewById(R.id.manageStatSubmittedValue);
        emptyText = findViewById(R.id.manageHomeworkEmpty);
        refreshLayout = findViewById(R.id.manageHomeworkRefresh);

        View btnCreate = findViewById(R.id.btnCreateHomework);
        tasksList = findViewById(R.id.teacherTasksList);
        tasksList.setLayoutManager(new LinearLayoutManager(this));
        tasksList.setNestedScrollingEnabled(false);

        adapter = new TeacherTaskAdapter(new TeacherTaskAdapter.TaskActionListener() {
            @Override
            public void onSubmissions(TeacherTask task) {
                Intent intent = new Intent(ManageHomeworkActivity.this, SubmissionsActivity.class);
                intent.putExtra("assessmentId", task.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(TeacherTask task) {
                confirmDeleteTask(task);
            }
        });
        tasksList.setAdapter(adapter);

        if (refreshLayout != null) {
            refreshLayout.setColorSchemeResources(R.color.schedule_accent);
            refreshLayout.setOnRefreshListener(this::loadTasks);
        }
        btnCreate.setOnClickListener(v -> {
            startActivity(new Intent(ManageHomeworkActivity.this, HomeworkCreateActivity.class));
        });

        apiService = ApiClient.getService(this);
        BottomNavHelper.setupTeacherNav(this, R.id.navTeacherHomework);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        setLoading(true);
        apiService.getTeacherTasks().enqueue(new Callback<List<TeacherTask>>() {
            @Override
            public void onResponse(Call<List<TeacherTask>> call, Response<List<TeacherTask>> response) {
                setLoading(false);
                stopRefreshing();
                if (response.isSuccessful() && response.body() != null) {
                    List<TeacherTask> tasks = response.body();
                    adapter.setItems(tasks);
                    updateStats(tasks);
                    toggleEmpty(tasks == null || tasks.isEmpty());
                } else {
                    Toast.makeText(ManageHomeworkActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                    toggleEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<TeacherTask>> call, Throwable t) {
                setLoading(false);
                stopRefreshing();
                Toast.makeText(ManageHomeworkActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                toggleEmpty(true);
            }
        });
    }

    private void confirmDeleteTask(TeacherTask task) {
        if (task == null) {
            return;
        }
        String label = task == null || task.title == null || task.title.trim().isEmpty()
                ? "this homework"
                : "\"" + task.title.trim() + "\"";
        DialogHelper.showConfirm(
                this,
                "Delete homework",
                "Are you sure you want to delete " + label + "?",
                "Delete",
                "Cancel",
                () -> deleteTask(task.id)
        );
    }

    private void deleteTask(long id) {
        setLoading(true);
        apiService.deleteAssessment(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    loadTasks();
                } else {
                    Toast.makeText(ManageHomeworkActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ManageHomeworkActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void updateStats(List<TeacherTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            statSubjectsValue.setText("0");
            statUpcomingValue.setText("0");
            statSubmittedValue.setText("0");
            return;
        }

        int upcoming = 0;
        int submitted = 0;
        java.util.HashSet<String> subjects = new java.util.HashSet<>();
        for (TeacherTask task : tasks) {
            if (!task.isOverdue) {
                upcoming++;
            }
            if (task.subjectName != null && !task.subjectName.trim().isEmpty()) {
                subjects.add(task.subjectName.trim());
            }
            submitted += getSubmittedCount(task);
        }

        statSubjectsValue.setText(String.valueOf(subjects.size()));
        statUpcomingValue.setText(String.valueOf(upcoming));
        statSubmittedValue.setText(String.valueOf(submitted));
    }

    private void toggleEmpty(boolean isEmpty) {
        emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        tasksList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void stopRefreshing() {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    private int getSubmittedCount(TeacherTask task) {
        if (task == null) {
            return 0;
        }
        if (task.stats != null) {
            return task.stats.submitted;
        }
        return task.submissionCount == null ? 0 : task.submissionCount;
    }
}
