package com.example.onlinediary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.ScheduleAdminAdapter;
import com.example.onlinediary.util.MultipartUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleAdminActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ScheduleAdminAdapter adapter;
    private ApiService apiService;

    private final ActivityResultLauncher<String> importPicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::importSchedule
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_admin);

        progressBar = findViewById(R.id.scheduleAdminProgress);
        Button btnCreate = findViewById(R.id.btnCreateSchedule);
        Button btnImport = findViewById(R.id.btnImportSchedule);
        Button btnExport = findViewById(R.id.btnExportSchedule);
        RecyclerView recyclerView = findViewById(R.id.scheduleAdminList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdminAdapter(new ScheduleAdminAdapter.ScheduleActionListener() {
            @Override
            public void onEdit(ScheduleEvent event) {
                Intent intent = new Intent(ScheduleAdminActivity.this, ScheduleEditActivity.class);
                intent.putExtra("scheduleId", event.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(ScheduleEvent event) {
                deleteSchedule(event.id);
            }
        });
        recyclerView.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> startActivity(new Intent(this, ScheduleEditActivity.class)));
        btnImport.setOnClickListener(v -> importPicker.launch("*/*"));
        btnExport.setOnClickListener(v -> exportSchedule());

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
                    Toast.makeText(ScheduleAdminActivity.this, "Failed to load schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleEvent>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ScheduleAdminActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSchedule(long id) {
        setLoading(true);
        apiService.deleteSchedule(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    loadSchedule();
                } else {
                    Toast.makeText(ScheduleAdminActivity.this, "Failed to delete schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ScheduleAdminActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void importSchedule(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            MultipartBody.Part filePart = MultipartUtils.createFilePart(this, "file", uri, "schedule");
            setLoading(true);
            apiService.importSchedule(filePart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    setLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(ScheduleAdminActivity.this, "Schedule imported", Toast.LENGTH_SHORT).show();
                        loadSchedule();
                    } else {
                        Toast.makeText(ScheduleAdminActivity.this, "Import failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(ScheduleAdminActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportSchedule() {
        List<ScheduleEvent> items = adapter == null ? null : adapter.getItemsForExport();
        if (items == null || items.isEmpty()) {
            Toast.makeText(this, "No schedule data", Toast.LENGTH_SHORT).show();
            return;
        }

        File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            Toast.makeText(this, "Storage not available", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(dir, "schedule_export.csv");
        try (FileOutputStream output = new FileOutputStream(file)) {
            String header = "Date,Lesson,Subject,Teacher,Group,Type,Room\n";
            output.write(header.getBytes());
            for (ScheduleEvent event : items) {
                String line = safe(event.date) + "," + event.lessonNumber + ","
                        + safe(event.subjectName) + "," + safe(event.teacherFullName) + ","
                        + safe(event.groupName) + "," + safe(event.type) + "," + safe(event.room) + "\n";
                output.write(line.getBytes());
            }
            Toast.makeText(this, "Exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to export", Toast.LENGTH_SHORT).show();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
