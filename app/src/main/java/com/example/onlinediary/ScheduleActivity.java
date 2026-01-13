package com.example.onlinediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.ScheduleAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ScheduleAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        progressBar = findViewById(R.id.scheduleProgress);
        RecyclerView recyclerView = findViewById(R.id.scheduleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AuthStore authStore = new AuthStore(this);
        String role = authStore.getRole() == null ? "" : authStore.getRole();
        boolean canSeeJournal = "TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        adapter = new ScheduleAdapter(canSeeJournal, event -> {
            Intent intent = new Intent(ScheduleActivity.this, JournalActivity.class);
            intent.putExtra("scheduleId", event.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchedule();
    }

    private void loadSchedule() {
        setLoading(true);
        apiService.getSchedule(null, null).enqueue(new Callback<List<ScheduleEvent>>() {
            @Override
            public void onResponse(Call<List<ScheduleEvent>> call, Response<List<ScheduleEvent>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(ScheduleActivity.this, "Failed to load schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleEvent>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ScheduleActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
