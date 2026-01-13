package com.example.onlinediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.HomeworkAdapter;
import com.example.onlinediary.util.SimpleItemSelectedListener;
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
    private final List<HomeworkItem> allItems = new ArrayList<>();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework);

        progressBar = findViewById(R.id.homeworkProgress);
        RecyclerView recyclerView = findViewById(R.id.homeworkList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HomeworkAdapter(item -> {
            Intent intent = new Intent(HomeworkActivity.this, HomeworkDetailActivity.class);
            intent.putExtra("homeworkJson", gson.toJson(item));
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        Spinner filterSpinner = findViewById(R.id.homeworkFilter);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Todo", "Pending", "Done"}
        );
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setSelection(0);
        filterSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> applyFilter(position)));

        apiService = ApiClient.getService(this);
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
                    applyFilter(0);
                } else {
                    Toast.makeText(HomeworkActivity.this, "Failed to load homework", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<HomeworkItem>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(HomeworkActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter(int position) {
        String status = null;
        if (position == 1) {
            status = "todo";
        } else if (position == 2) {
            status = "pending";
        } else if (position == 3) {
            status = "done";
        }

        List<HomeworkItem> filtered = new ArrayList<>();
        for (HomeworkItem item : allItems) {
            if (status == null || status.equalsIgnoreCase(item.status)) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
