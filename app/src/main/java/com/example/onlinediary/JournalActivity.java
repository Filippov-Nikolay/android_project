package com.example.onlinediary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.JournalEntry;
import com.example.onlinediary.model.JournalEntryUpdate;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.JournalAdapter;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JournalActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private JournalAdapter adapter;
    private ApiService apiService;
    private long scheduleId;
    private TextView titleText;
    private TextView metaText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        titleText = findViewById(R.id.journalTitle);
        metaText = findViewById(R.id.journalMeta);
        progressBar = findViewById(R.id.journalProgress);
        Button btnSave = findViewById(R.id.btnSaveJournal);

        scheduleId = getIntent().getLongExtra("scheduleId", -1);
        if (scheduleId <= 0) {
            Toast.makeText(this, "Missing schedule id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.journalList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JournalAdapter();
        recyclerView.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveJournal());

        apiService = ApiClient.getService(this);
        loadData();
    }

    private void loadData() {
        setLoading(true);
        apiService.getScheduleItem(scheduleId).enqueue(new Callback<ScheduleEvent>() {
            @Override
            public void onResponse(Call<ScheduleEvent> call, Response<ScheduleEvent> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ScheduleEvent event = response.body();
                    titleText.setText("Journal - " + event.subjectName);
                    metaText.setText(event.groupName + " | " + event.date + " | Lesson " + event.lessonNumber);
                }
            }

            @Override
            public void onFailure(Call<ScheduleEvent> call, Throwable t) {
                Toast.makeText(JournalActivity.this, "Failed to load schedule info", Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getJournal(scheduleId).enqueue(new Callback<List<JournalEntry>>() {
            @Override
            public void onResponse(Call<List<JournalEntry>> call, Response<List<JournalEntry>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(JournalActivity.this, "Failed to load journal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JournalEntry>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(JournalActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveJournal() {
        List<JournalEntryUpdate> updates = new ArrayList<>();
        for (JournalEntry entry : adapter.getItems()) {
            updates.add(new JournalEntryUpdate(
                    scheduleId,
                    entry.studentId,
                    entry.attendance,
                    entry.workType,
                    entry.grade == null ? "" : entry.grade
            ));
        }

        setLoading(true);
        apiService.saveJournal(scheduleId, updates).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(JournalActivity.this, "Journal saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(JournalActivity.this, "Failed to save journal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(JournalActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
