package com.example.onlinediary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmissionsActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private SubmissionAdapter adapter;
    private ApiService apiService;
    private long assessmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submissions);

        progressBar = findViewById(R.id.submissionsProgress);
        RecyclerView recyclerView = findViewById(R.id.submissionsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        loadSubmissions();
    }

    private void loadSubmissions() {
        setLoading(true);
        apiService.getSubmissions(assessmentId).enqueue(new Callback<List<SubmissionItem>>() {
            @Override
            public void onResponse(Call<List<SubmissionItem>> call, Response<List<SubmissionItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(SubmissionsActivity.this, "Failed to load submissions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SubmissionItem>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SubmissionsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
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
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText gradeInput = new EditText(this);
        gradeInput.setHint("Grade");
        gradeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (item.grade != null) {
            gradeInput.setText(String.valueOf(item.grade));
        }

        EditText feedbackInput = new EditText(this);
        feedbackInput.setHint("Feedback");
        if (item.feedback != null) {
            feedbackInput.setText(item.feedback);
        }

        layout.addView(gradeInput);
        layout.addView(feedbackInput);

        new AlertDialog.Builder(this)
                .setTitle("Grade submission")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String gradeText = gradeInput.getText().toString().trim();
                    int grade = gradeText.isEmpty() ? 0 : Integer.parseInt(gradeText);
                    String feedback = feedbackInput.getText().toString().trim();
                    submitGrade(item, grade, feedback);
                })
                .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> dialog.dismiss())
                .show();
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
}
