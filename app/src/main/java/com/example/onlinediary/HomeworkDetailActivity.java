package com.example.onlinediary;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.util.ApiUrls;
import com.example.onlinediary.util.FileDownloadHelper;
import com.example.onlinediary.util.MultipartUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeworkDetailActivity extends AppCompatActivity {
    private final Gson gson = new Gson();
    private HomeworkItem item;
    private ApiService apiService;
    private ProgressBar progressBar;
    private TextView selectedFilesLabel;
    private EditText commentInput;
    private final List<Uri> selectedFiles = new ArrayList<>();

    private final ActivityResultLauncher<String[]> filePicker = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            uris -> {
                selectedFiles.clear();
                if (uris != null) {
                    selectedFiles.addAll(uris);
                }
                updateSelectedFilesLabel();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_detail);

        apiService = ApiClient.getService(this);
        progressBar = findViewById(R.id.homeworkDetailProgress);
        selectedFilesLabel = findViewById(R.id.selectedFilesLabel);
        commentInput = findViewById(R.id.editComment);

        TextView subjectText = findViewById(R.id.detailSubject);
        TextView titleText = findViewById(R.id.detailTitle);
        TextView statusText = findViewById(R.id.detailStatus);
        TextView deadlineText = findViewById(R.id.detailDeadline);
        TextView descriptionText = findViewById(R.id.detailDescription);

        Button btnDownloadTeacherFile = findViewById(R.id.btnDownloadTeacherFile);
        Button btnDownloadSubmission = findViewById(R.id.btnDownloadSubmission);
        Button btnPickFiles = findViewById(R.id.btnPickFiles);
        Button btnSubmitHomework = findViewById(R.id.btnSubmitHomework);
        Button btnCancelSubmission = findViewById(R.id.btnCancelSubmission);

        String json = getIntent().getStringExtra("homeworkJson");
        if (json == null) {
            Toast.makeText(this, "Missing homework data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        item = gson.fromJson(json, HomeworkItem.class);

        subjectText.setText(item.subjectName);
        titleText.setText(item.title);
        String status = "Status: " + (item.status == null ? "" : item.status);
        if (item.grade != null) {
            status = status + " | Grade: " + item.grade;
        }
        statusText.setText(status);
        deadlineText.setText("Deadline: " + (item.deadline == null ? "" : item.deadline));
        String description = item.description == null ? "" : item.description;
        if (item.feedback != null && !item.feedback.isEmpty()) {
            description = description + "\nFeedback: " + item.feedback;
        }
        descriptionText.setText(description);

        if (item.fileName == null || item.fileName.isEmpty()) {
            btnDownloadTeacherFile.setVisibility(View.GONE);
        }
        if (item.submissionFileName == null || item.submissionFileName.isEmpty()) {
            btnDownloadSubmission.setVisibility(View.GONE);
        }

        boolean canSubmit = item.status == null || !item.status.equalsIgnoreCase("done");
        btnPickFiles.setEnabled(canSubmit);
        btnSubmitHomework.setEnabled(canSubmit);

        btnCancelSubmission.setVisibility(item.status != null && item.status.equalsIgnoreCase("pending")
                ? View.VISIBLE : View.GONE);

        btnDownloadTeacherFile.setOnClickListener(v -> downloadFile(item.fileName));
        btnDownloadSubmission.setOnClickListener(v -> downloadFile(item.submissionFileName));
        btnPickFiles.setOnClickListener(v -> filePicker.launch(new String[]{"*/*"}));
        btnSubmitHomework.setOnClickListener(v -> submitHomework());
        btnCancelSubmission.setOnClickListener(v -> cancelSubmission());

        updateSelectedFilesLabel();
    }

    private void updateSelectedFilesLabel() {
        if (selectedFiles.isEmpty()) {
            selectedFilesLabel.setText("No files selected");
        } else {
            selectedFilesLabel.setText("Selected files: " + selectedFiles.size());
        }
    }

    private void downloadFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        AuthStore authStore = new AuthStore(this);
        FileDownloadHelper.downloadFile(this, ApiUrls.fileDownloadUrl(fileName), fileName, authStore.getToken());
    }

    private void submitHomework() {
        String comment = commentInput.getText().toString().trim();
        if (selectedFiles.isEmpty() && comment.isEmpty()) {
            Toast.makeText(this, "Select files or enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        List<MultipartBody.Part> parts = new ArrayList<>();
        try {
            for (Uri uri : selectedFiles) {
                parts.add(MultipartUtils.createFilePart(this, "files", uri, "submission"));
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to read files", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody commentBody = MultipartUtils.toTextBody(comment);

        setLoading(true);
        apiService.submitHomework(item.id, parts, commentBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(HomeworkDetailActivity.this, "Submitted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(HomeworkDetailActivity.this, "Failed to submit", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(HomeworkDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelSubmission() {
        setLoading(true);
        apiService.deleteSubmission(item.id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(HomeworkDetailActivity.this, "Submission canceled", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(HomeworkDetailActivity.this, "Failed to cancel", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(HomeworkDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
