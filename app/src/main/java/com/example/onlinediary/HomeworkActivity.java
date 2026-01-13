package com.example.onlinediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.HomeworkAdapter;
import com.example.onlinediary.util.BottomNavHelper;
import com.example.onlinediary.util.SimpleItemSelectedListener;
import com.example.onlinediary.util.TopHeaderHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeworkActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private HomeworkAdapter adapter;
    private ApiService apiService;
    private TextView emptyText;
    private TextView countText;
    private Spinner subjectFilter;
    private View tabTodo;
    private View tabPending;
    private View tabDone;
    private final List<HomeworkItem> allItems = new ArrayList<>();
    private final Gson gson = new Gson();
    private String activeStatus = "todo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework);
        TopHeaderHelper.bind(this);

        progressBar = findViewById(R.id.homeworkProgress);
        emptyText = findViewById(R.id.homeworkEmpty);
        countText = findViewById(R.id.homeworkCount);
        subjectFilter = findViewById(R.id.homeworkFilter);
        tabTodo = findViewById(R.id.btnHomeworkTabTodo);
        tabPending = findViewById(R.id.btnHomeworkTabPending);
        tabDone = findViewById(R.id.btnHomeworkTabDone);
        RecyclerView recyclerView = findViewById(R.id.homeworkList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HomeworkAdapter(item -> {
            Intent intent = new Intent(HomeworkActivity.this, HomeworkDetailActivity.class);
            intent.putExtra("homeworkJson", gson.toJson(item));
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        tabTodo.setOnClickListener(v -> {
            activeStatus = "todo";
            updateTabs();
            applyFilter();
        });
        tabPending.setOnClickListener(v -> {
            activeStatus = "pending";
            updateTabs();
            applyFilter();
        });
        tabDone.setOnClickListener(v -> {
            activeStatus = "done";
            updateTabs();
            applyFilter();
        });

        apiService = ApiClient.getService(this);
        setupSubjectFilter();
        updateTabs();
        BottomNavHelper.setupStudentNav(this, R.id.navStudentHomework);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomework();
    }

    private void loadHomework() {
        setLoading(true);
        apiService.getHomeworks().enqueue(new Callback<List<HomeworkItem>>() {
            @Override
            public void onResponse(Call<List<HomeworkItem>> call, Response<List<HomeworkItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allItems.clear();
                    allItems.addAll(response.body());
                    updateSubjectFilter();
                    applyFilter();
                } else {
                    Toast.makeText(HomeworkActivity.this, "Failed to load homework", Toast.LENGTH_SHORT).show();
                    toggleEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<HomeworkItem>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(HomeworkActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                toggleEmpty(true);
            }
        });
    }

    private void applyFilter() {
        String status = activeStatus;
        String subject = getSelectedSubject();
        List<HomeworkItem> filtered = new ArrayList<>();
        for (HomeworkItem item : allItems) {
            if (item == null) {
                continue;
            }
            if (status != null && (item.status == null || !status.equalsIgnoreCase(item.status))) {
                continue;
            }
            if (subject != null && item.subjectName != null
                    && !subject.equalsIgnoreCase(item.subjectName.trim())) {
                continue;
            }
            if (subject != null && item.subjectName == null) {
                continue;
            }
            filtered.add(item);
        }
        adapter.setItems(filtered);
        if (countText != null) {
            countText.setText("Available tasks: " + filtered.size());
        }
        toggleEmpty(filtered.isEmpty());
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toggleEmpty(boolean isEmpty) {
        if (emptyText != null) {
            emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void updateTabs() {
        updateTabStyles(tabTodo, "todo".equals(activeStatus));
        updateTabStyles(tabPending, "pending".equals(activeStatus));
        updateTabStyles(tabDone, "done".equals(activeStatus));
    }

    private void updateTabStyles(View tab, boolean selected) {
        if (!(tab instanceof com.google.android.material.button.MaterialButton)) {
            return;
        }
        com.google.android.material.button.MaterialButton button =
                (com.google.android.material.button.MaterialButton) tab;
        button.setBackgroundTintList(getColorStateList(
                selected ? R.color.schedule_surface_muted : R.color.schedule_surface
        ));
        button.setTextColor(getColor(selected ? R.color.schedule_text : R.color.schedule_muted));
    }

    private void setupSubjectFilter() {
        subjectFilter.setAdapter(createSpinnerAdapter(new ArrayList<>(), "All subjects"));
        subjectFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> applyFilter()));
    }

    private void updateSubjectFilter() {
        java.util.Set<String> subjects = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (HomeworkItem item : allItems) {
            if (item != null && item.subjectName != null && !item.subjectName.trim().isEmpty()) {
                subjects.add(item.subjectName.trim());
            }
        }
        List<String> subjectList = new ArrayList<>(subjects);
        String selected = getSelectedSubject();
        subjectFilter.setAdapter(createSpinnerAdapter(subjectList, "All subjects"));
        restoreSelection(subjectList, selected);
    }

    private String getSelectedSubject() {
        int position = subjectFilter.getSelectedItemPosition();
        if (position <= 0) {
            return null;
        }
        Object item = subjectFilter.getSelectedItem();
        return item == null ? null : item.toString();
    }

    private void restoreSelection(List<String> items, String selected) {
        if (selected == null || items == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            if (selected.equalsIgnoreCase(items.get(i))) {
                subjectFilter.setSelection(i + 1);
                return;
            }
        }
    }

    private ArrayAdapter<String> createSpinnerAdapter(List<String> items, String placeholder) {
        List<String> values = new ArrayList<>();
        values.add(placeholder);
        values.addAll(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, values);
        adapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
        return adapter;
    }
}
