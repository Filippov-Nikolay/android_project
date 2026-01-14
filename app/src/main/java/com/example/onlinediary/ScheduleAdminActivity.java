package com.example.onlinediary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.ScheduleAdminAdapter;
import com.example.onlinediary.util.DialogHelper;
import com.example.onlinediary.util.MultipartUtils;
import com.example.onlinediary.util.SimpleItemSelectedListener;
import com.example.onlinediary.util.SimpleTextWatcher;
import com.example.onlinediary.util.TopHeaderHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleAdminActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ScheduleAdminAdapter adapter;
    private ApiService apiService;
    private TextView subtitleText;
    private TextView emptyState;
    private EditText searchInput;
    private Spinner groupFilter;
    private Spinner subjectFilter;
    private SwipeRefreshLayout refreshLayout;
    private final List<ScheduleEvent> allItems = new ArrayList<>();
    private List<String> groupOptions = new ArrayList<>();
    private List<String> subjectOptions = new ArrayList<>();

    private final ActivityResultLauncher<String> importPicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::importSchedule
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_admin);
        TopHeaderHelper.bind(this);

        progressBar = findViewById(R.id.scheduleAdminProgress);
        subtitleText = findViewById(R.id.scheduleAdminSubtitle);
        emptyState = findViewById(R.id.scheduleAdminEmpty);
        searchInput = findViewById(R.id.inputScheduleSearch);
        groupFilter = findViewById(R.id.spinnerScheduleGroupFilter);
        subjectFilter = findViewById(R.id.spinnerScheduleSubjectFilter);
        refreshLayout = findViewById(R.id.scheduleAdminRefresh);
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
                confirmDeleteSchedule(event);
            }
        });
        recyclerView.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> startActivity(new Intent(this, ScheduleEditActivity.class)));
        btnImport.setOnClickListener(v -> importPicker.launch("*/*"));
        btnExport.setOnClickListener(v -> exportSchedule());

        apiService = ApiClient.getService(this);
        setupFilters();
        if (refreshLayout != null) {
            refreshLayout.setColorSchemeResources(R.color.schedule_accent);
            refreshLayout.setOnRefreshListener(this::loadSchedule);
        }
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
                stopRefreshing();
                if (response.isSuccessful() && response.body() != null) {
                    allItems.clear();
                    allItems.addAll(response.body());
                    updateFilterOptions();
                    applyFilters();
                } else {
                    Toast.makeText(ScheduleAdminActivity.this, "Failed to load schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleEvent>> call, Throwable t) {
                setLoading(false);
                stopRefreshing();
                Toast.makeText(ScheduleAdminActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteSchedule(ScheduleEvent event) {
        if (event == null) {
            return;
        }
        String label = buildScheduleLabel(event);
        DialogHelper.showConfirm(
                this,
                "Delete lesson",
                "Are you sure you want to delete " + label + "?",
                "Delete",
                "Cancel",
                () -> deleteSchedule(event.id)
        );
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

    private String buildScheduleLabel(ScheduleEvent event) {
        if (event == null) {
            return "this lesson";
        }
        String subject = event.subjectName == null ? "" : event.subjectName.trim();
        String group = event.groupName == null ? "" : event.groupName.trim();
        StringBuilder label = new StringBuilder();
        if (!subject.isEmpty()) {
            label.append("\"").append(subject).append("\"");
        }
        if (!group.isEmpty()) {
            if (label.length() > 0) {
                label.append(" for ");
            }
            label.append(group);
        }
        return label.length() == 0 ? "this lesson" : label.toString();
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

    private void stopRefreshing() {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    private void setupFilters() {
        ArrayAdapter<String> groupAdapter = createSpinnerAdapter(new ArrayList<>(), "All groups");
        ArrayAdapter<String> subjectAdapter = createSpinnerAdapter(new ArrayList<>(), "All subjects");
        groupFilter.setAdapter(groupAdapter);
        subjectFilter.setAdapter(subjectAdapter);

        searchInput.addTextChangedListener(new SimpleTextWatcher(text -> applyFilters()));
        groupFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> applyFilters()));
        subjectFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> applyFilters()));
    }

    private void updateFilterOptions() {
        String selectedGroup = getSelected(groupFilter, groupOptions);
        String selectedSubject = getSelected(subjectFilter, subjectOptions);

        Set<String> groupSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> subjectSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ScheduleEvent event : allItems) {
            if (event.groupName != null && !event.groupName.trim().isEmpty()) {
                groupSet.add(event.groupName.trim());
            }
            if (event.subjectName != null && !event.subjectName.trim().isEmpty()) {
                subjectSet.add(event.subjectName.trim());
            }
        }

        groupOptions = new ArrayList<>(groupSet);
        subjectOptions = new ArrayList<>(subjectSet);

        groupFilter.setAdapter(createSpinnerAdapter(groupOptions, "All groups"));
        subjectFilter.setAdapter(createSpinnerAdapter(subjectOptions, "All subjects"));

        restoreSelection(groupFilter, groupOptions, selectedGroup);
        restoreSelection(subjectFilter, subjectOptions, selectedSubject);
    }

    private void applyFilters() {
        if (adapter == null) {
            return;
        }
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.US);
        String group = getSelected(groupFilter, groupOptions);
        String subject = getSelected(subjectFilter, subjectOptions);

        List<ScheduleEvent> filtered = new ArrayList<>();
        for (ScheduleEvent event : allItems) {
            if (group != null && !equalsIgnoreCase(event.groupName, group)) {
                continue;
            }
            if (subject != null && !equalsIgnoreCase(event.subjectName, subject)) {
                continue;
            }
            if (!query.isEmpty() && !matchesQuery(event, query)) {
                continue;
            }
            filtered.add(event);
        }

        adapter.setItems(filtered);
        if (subtitleText != null) {
            subtitleText.setText("Found lessons: " + filtered.size());
        }
        if (emptyState != null) {
            emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private boolean matchesQuery(ScheduleEvent event, String query) {
        if (event == null) {
            return false;
        }
        return contains(event.subjectName, query)
                || contains(event.teacherFullName, query)
                || contains(event.groupName, query)
                || contains(event.room, query)
                || contains(event.type, query)
                || contains(event.date, query)
                || String.valueOf(event.lessonNumber).contains(query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.US).contains(query);
    }

    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private ArrayAdapter<String> createSpinnerAdapter(List<String> items, String placeholder) {
        List<String> values = new ArrayList<>();
        values.add(placeholder);
        values.addAll(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, values);
        adapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
        return adapter;
    }

    private String getSelected(Spinner spinner, List<String> options) {
        if (spinner == null || options == null) {
            return null;
        }
        int position = spinner.getSelectedItemPosition();
        if (position <= 0 || position - 1 >= options.size()) {
            return null;
        }
        return options.get(position - 1);
    }

    private void restoreSelection(Spinner spinner, List<String> options, String selected) {
        if (spinner == null || options == null || selected == null) {
            return;
        }
        for (int i = 0; i < options.size(); i++) {
            if (selected.equalsIgnoreCase(options.get(i))) {
                spinner.setSelection(i + 1);
                return;
            }
        }
    }
}
