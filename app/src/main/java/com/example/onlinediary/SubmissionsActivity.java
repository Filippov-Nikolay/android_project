package com.example.onlinediary;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.GradeRequest;
import com.example.onlinediary.model.SubmissionItem;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.SubmissionAdapter;
import com.example.onlinediary.util.ApiUrls;
import com.example.onlinediary.util.FileDownloadHelper;
import com.example.onlinediary.util.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmissionsActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private SubmissionAdapter adapter;
    private ApiService apiService;
    private long assessmentId;
    private EditText searchInput;
    private TextView emptyState;
    private final List<SubmissionItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submissions);

        getWindow().setStatusBarColor(getColor(R.color.schedule_background));
        getWindow().setNavigationBarColor(getColor(R.color.schedule_background));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        progressBar = findViewById(R.id.submissionsProgress);
        searchInput = findViewById(R.id.inputSubmissionSearch);
        emptyState = findViewById(R.id.submissionsEmpty);
        RecyclerView recyclerView = findViewById(R.id.submissionsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnSubmissionsBack).setOnClickListener(v -> finish());

        assessmentId = getIntent().getLongExtra("assessmentId", -1);
        if (assessmentId <= 0) {
            Toast.makeText(this, "Missing assessment id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new SubmissionAdapter(new SubmissionAdapter.SubmissionActionListener() {
            @Override
            public void onDownload(SubmissionItem item) {
                downloadSubmission(item);
            }

            @Override
            public void onGrade(SubmissionItem item) {
                showGradeDialog(item);
            }
        });
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getService(this);
        searchInput.addTextChangedListener(new SimpleTextWatcher(text -> applyFilters()));
        loadSubmissions();
    }

    private void loadSubmissions() {
        setLoading(true);
        apiService.getSubmissions(assessmentId).enqueue(new Callback<List<SubmissionItem>>() {
            @Override
            public void onResponse(Call<List<SubmissionItem>> call, Response<List<SubmissionItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allItems.clear();
                    allItems.addAll(response.body());
                    applyFilters();
                } else {
                    Toast.makeText(SubmissionsActivity.this, "Failed to load submissions", Toast.LENGTH_SHORT).show();
                    toggleEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<SubmissionItem>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SubmissionsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                toggleEmpty(true);
            }
        });
    }

    private void downloadSubmission(SubmissionItem item) {
        if (item.fileName == null || item.fileName.isEmpty()) {
            return;
        }
        AuthStore authStore = new AuthStore(this);
        FileDownloadHelper.downloadFile(this, ApiUrls.fileDownloadUrl(item.fileName), item.fileName, authStore.getToken());
    }

    private void showGradeDialog(SubmissionItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_grade_submission, null);
        TextView gradeInput = dialogView.findViewById(R.id.inputGradeValue);
        TextView feedbackInput = dialogView.findViewById(R.id.inputGradeFeedback);
        View btnCancel = dialogView.findViewById(R.id.btnGradeCancel);
        View btnSave = dialogView.findViewById(R.id.btnGradeSave);

        if (item.grade != null) {
            gradeInput.setText(String.valueOf(item.grade));
        }
        if (item.feedback != null) {
            feedbackInput.setText(item.feedback);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String gradeText = gradeInput.getText().toString().trim();
            int grade;
            try {
                grade = gradeText.isEmpty() ? 0 : Integer.parseInt(gradeText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter valid grade", Toast.LENGTH_SHORT).show();
                return;
            }
            String feedback = feedbackInput.getText().toString().trim();
            dialog.dismiss();
            submitGrade(item, grade, feedback);
        });

        dialog.show();
    }

    private void submitGrade(SubmissionItem item, int grade, String feedback) {
        setLoading(true);
        apiService.gradeSubmission(assessmentId, item.studentId, new GradeRequest(grade, feedback))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(SubmissionsActivity.this, "Grade saved", Toast.LENGTH_SHORT).show();
                            loadSubmissions();
                        } else {
                            Toast.makeText(SubmissionsActivity.this, "Failed to save grade", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(SubmissionsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void applyFilters() {
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.US);
        List<SubmissionItem> filtered = new ArrayList<>();
        for (SubmissionItem item : allItems) {
            if (item == null) {
                continue;
            }
            if (query.isEmpty()) {
                filtered.add(item);
                continue;
            }
            String name = item.studentName == null ? "" : item.studentName.toLowerCase(Locale.US);
            if (name.contains(query)) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
        toggleEmpty(filtered.isEmpty());
    }

    private void toggleEmpty(boolean isEmpty) {
        if (emptyState != null) {
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }
}
