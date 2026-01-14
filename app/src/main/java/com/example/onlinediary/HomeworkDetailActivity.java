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
import androidx.core.content.ContextCompat;

import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.util.ApiUrls;
import com.example.onlinediary.util.FileDownloadHelper;
import com.example.onlinediary.util.FileUtils;
import com.example.onlinediary.util.MultipartUtils;
import com.example.onlinediary.util.TopHeaderHelper;
import com.example.onlinediary.util.DialogHelper;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_homework_detail);


        apiService = ApiClient.getService(this);
        progressBar = findViewById(R.id.homeworkDetailProgress);
        selectedFilesLabel = findViewById(R.id.selectedFilesLabel);
        commentInput = findViewById(R.id.editComment);

        // Привязка UI элементов
        TextView subjectText = findViewById(R.id.detailSubject);
        TextView titleText = findViewById(R.id.detailTitle);
        TextView statusText = findViewById(R.id.detailStatus);
        TextView deadlineText = findViewById(R.id.detailDeadline);
        TextView descriptionText = findViewById(R.id.detailDescription);
        TextView assignedDate = findViewById(R.id.detailAssignedDate);
        TextView teacherAvatar = findViewById(R.id.detailTeacherAvatar);
        TextView teacherName = findViewById(R.id.detailTeacherName);
        TextView remainingTag = findViewById(R.id.detailRemainingTag);
        TextView gradeBadge = findViewById(R.id.detailGradeBadge);
        TextView feedbackText = findViewById(R.id.detailFeedback);
        TextView answerTitle = findViewById(R.id.detailAnswerTitle);
        View gradeContainer = findViewById(R.id.detailGradeContainer);
        View uploadSection = findViewById(R.id.homeworkUploadSection);
        View pendingCard = findViewById(R.id.homeworkPendingCard);

        Button btnDownloadTeacherFile = findViewById(R.id.btnDownloadTeacherFile);
        Button btnDownloadSubmission = findViewById(R.id.btnDownloadSubmission);
        Button btnPickFiles = findViewById(R.id.btnPickFiles);
        Button btnSubmitHomework = findViewById(R.id.btnSubmitHomework);
        Button btnCancelSubmission = findViewById(R.id.btnCancelSubmission);
        Button btnBackToList = findViewById(R.id.btnBackToList);

        View btnClose = findViewById(R.id.btnHomeworkDetailClose);

        // Получение данных
        String json = getIntent().getStringExtra("homeworkJson");
        if (json == null) {
            Toast.makeText(this, "Missing homework data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        item = gson.fromJson(json, HomeworkItem.class);

        // Заполнение данных
        subjectText.setText(safeUpper(item.subjectName, "SUBJECT"));
        titleText.setText(safe(item.title, "Homework"));

        String assignedLabel = dateOnly(item.createdAt);
        if (assignedLabel.equals("--")) {
            assignedLabel = dateOnly(item.date);
        }
        assignedDate.setText("Assigned " + assignedLabel);

        String teacherLabel = safe(item.teacherName, "Teacher");
        teacherName.setText(teacherLabel);
        teacherAvatar.setText(buildInitials(teacherLabel));

        applyStatus(statusText, item);
        deadlineText.setText(dateOnly(item.deadline));
        applyRemainingTag(remainingTag, item);

        descriptionText.setText(safe(item.description, "No description."));

        // Логика оценок
        if (item.grade != null) {
            gradeContainer.setVisibility(View.VISIBLE);
            String gradeText = item.pointsMax > 0
                    ? item.grade + "/" + item.pointsMax
                    : String.valueOf(item.grade);
            gradeBadge.setText(gradeText);

            if (item.feedback != null && !item.feedback.trim().isEmpty()) {
                feedbackText.setText(item.feedback);
                feedbackText.setVisibility(View.VISIBLE);
            } else {
                feedbackText.setVisibility(View.GONE);
            }
        } else {
            gradeContainer.setVisibility(View.GONE);
        }

        // Видимость кнопок скачивания
        btnDownloadTeacherFile.setVisibility((item.fileName == null || item.fileName.isEmpty()) ? View.GONE : View.VISIBLE);
        btnDownloadSubmission.setVisibility((item.submissionFileName == null || item.submissionFileName.isEmpty()) ? View.GONE : View.VISIBLE);

        // Состояние отправки
        String normalizedStatus = normalizeStatus(item.status);
        boolean hasSubmission = item.submissionFileName != null && !item.submissionFileName.trim().isEmpty();
        boolean isDone = "done".equals(normalizedStatus) || item.grade != null;
        boolean isPending = "pending".equals(normalizedStatus) || (!isDone && hasSubmission);
        boolean canSubmit = !isPending && !isDone;

        answerTitle.setText(isPending || isDone ? "Your submission" : "Your answer");

        // Настройка доступности UI
        btnPickFiles.setEnabled(canSubmit);
        btnSubmitHomework.setEnabled(canSubmit);
        commentInput.setEnabled(canSubmit);

        uploadSection.setAlpha(canSubmit ? 1f : 0.5f);
        btnSubmitHomework.setVisibility(canSubmit ? View.VISIBLE : View.GONE);
        pendingCard.setVisibility(isPending ? View.VISIBLE : View.GONE);

        uploadSection.setVisibility(canSubmit ? View.VISIBLE : View.GONE);
        selectedFilesLabel.setVisibility(canSubmit ? View.VISIBLE : View.GONE);
        commentInput.setVisibility(canSubmit ? View.VISIBLE : View.GONE);
        btnCancelSubmission.setVisibility(isPending ? View.VISIBLE : View.GONE);

        // Слушатели
        btnDownloadTeacherFile.setOnClickListener(v -> prepareDownload(item.fileName));
        btnDownloadSubmission.setOnClickListener(v -> prepareDownload(item.submissionFileName));
        btnPickFiles.setOnClickListener(v -> filePicker.launch(new String[]{"*/*"}));
        btnSubmitHomework.setOnClickListener(v -> submitHomework());
        btnCancelSubmission.setOnClickListener(v -> showCancelSubmissionConfirm());

        btnBackToList.setOnClickListener(v -> finish());
        btnClose.setOnClickListener(v -> finish());

        updateSelectedFilesLabel();
    }

    private void prepareDownload(String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.equals("null")) {
            Toast.makeText(this, "File not found on server", Toast.LENGTH_SHORT).show();
            return;
        }
        AuthStore authStore = new AuthStore(this);
        String token = "Bearer " + authStore.getToken();
        String fullUrl = ApiUrls.fileDownloadUrl(fileName);

        FileDownloadHelper.downloadFile(this, apiService.downloadFileDirect(fullUrl, token), fileName);
    }

    private void updateSelectedFilesLabel() {
        if (selectedFiles.isEmpty()) {
            selectedFilesLabel.setText("No files selected");
        } else {
            selectedFilesLabel.setText(formatSelectedFiles());
        }
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

                parts.add(MultipartUtils.createFilePart(this, "file", uri, "submission"));
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

    private void showCancelSubmissionConfirm() {
        DialogHelper.showConfirm(this, "Cancel submission", "Are you sure you want to delete your submission?", "Yes, delete", "No", this::cancelSubmission);
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

    private void applyStatus(TextView view, HomeworkItem item) {
        String normalized = normalizeStatus(item.status);
        if ("pending".equals(normalized)) {
            view.setText("PENDING");
            view.setBackgroundResource(R.drawable.bg_homework_status_pending);
            view.setTextColor(ContextCompat.getColor(this, R.color.manage_stat_orange));
            return;
        }
        if ("done".equals(normalized)) {
            view.setText("DONE");
            view.setBackgroundResource(R.drawable.bg_homework_status_done);
            view.setTextColor(ContextCompat.getColor(this, R.color.manage_stat_green));
            return;
        }
        if (item.isOverdue) {
            view.setText("OVERDUE");
            view.setBackgroundResource(R.drawable.bg_homework_status_overdue);
            view.setTextColor(ContextCompat.getColor(this, R.color.manage_stat_red));
            return;
        }
        view.setText("TODO");
        view.setBackgroundResource(R.drawable.bg_homework_status_todo);
        view.setTextColor(ContextCompat.getColor(this, R.color.schedule_text));
    }

    private void applyRemainingTag(TextView view, HomeworkItem item) {
        if (item.isOverdue) {
            view.setText("Overdue");
            view.setBackgroundResource(R.drawable.bg_homework_remaining_overdue);
            return;
        }
        String label = safe(item.deadlineText, "");
        if (label.isEmpty()) {
            LocalDate deadline = parseDate(item.deadline);
            if (deadline != null) {
                long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
                if (days < 0) {
                    view.setText("Overdue");
                    view.setBackgroundResource(R.drawable.bg_homework_remaining_overdue);
                    return;
                }
                label = (days == 0) ? "Due today" : (days == 1) ? "1 day left" : days + " days left";
            }
        }
        view.setText(label.isEmpty() ? "--" : label);
        view.setBackgroundResource(R.drawable.bg_homework_remaining_tag);
    }

    private String formatSelectedFiles() {
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(3, selectedFiles.size());
        for (int i = 0; i < limit; i++) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(FileUtils.getFileName(this, selectedFiles.get(i)));
        }
        if (selectedFiles.size() > limit) builder.append(" +").append(selectedFiles.size() - limit);
        return "Selected: " + builder;
    }

    private String normalizeStatus(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    private String safe(String value, String fallback) {
        if (value == null) return fallback;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String safeUpper(String value, String fallback) {
        String safeValue = safe(value, "");
        return safeValue.isEmpty() ? fallback : safeValue.toUpperCase(Locale.US);
    }

    private String dateOnly(String value) {
        if (value == null || value.trim().isEmpty()) return "--";
        int idx = value.indexOf('T');
        return idx > 0 ? value.substring(0, idx) : value;
    }

    private LocalDate parseDate(String value) {
        String date = dateOnly(value);
        if (date.equals("--")) return null;
        try { return LocalDate.parse(date); } catch (Exception ignored) { return null; }
    }

    private String buildInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "T";
        String[] parts = name.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) builder.append(part.substring(0, 1));
            if (builder.length() >= 2) break;
        }
        String initials = builder.toString().toUpperCase(Locale.US);
        return initials.isEmpty() ? "T" : initials;
    }
}