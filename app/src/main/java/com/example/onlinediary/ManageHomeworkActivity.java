package com.example.onlinediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.TeacherTask;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.TeacherTaskAdapter;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageHomeworkActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TeacherTaskAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_homework);

        progressBar = findViewById(R.id.manageHomeworkProgress);
        Button btnCreate = findViewById(R.id.btnCreateHomework);
        RecyclerView recyclerView = findViewById(R.id.teacherTasksList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TeacherTaskAdapter(new TeacherTaskAdapter.TaskActionListener() {
            @Override
            public void onSubmissions(TeacherTask task) {
                Intent intent = new Intent(ManageHomeworkActivity.this, SubmissionsActivity.class);
                intent.putExtra("assessmentId", task.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(TeacherTask task) {
                deleteTask(task.id);
            }
        });
        recyclerView.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> {
            startActivity(new Intent(ManageHomeworkActivity.this, HomeworkCreateActivity.class));
        });

        apiService = ApiClient.getService(this);
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
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(ManageHomeworkActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TeacherTask>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ManageHomeworkActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
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
}
