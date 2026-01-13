package com.example.onlinediary;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.model.Subject;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.util.FileUtils;
import com.example.onlinediary.util.MultipartUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeworkCreateActivity extends AppCompatActivity {
    private final List<Subject> subjects = new ArrayList<>();
    private final List<Group> groups = new ArrayList<>();
    private Spinner subjectSpinner;
    private Spinner groupSpinner;
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText deadlineInput;
    private TextView fileLabel;
    private TextView iconLabel;
    private ImageView iconPreview;
    private ProgressBar progressBar;

    private Uri assignmentFile;
    private Uri iconFile;

    private ApiService apiService;
    private static final String DEFAULT_TYPE = "REGULAR";

    private final ActivityResultLauncher<String> filePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                assignmentFile = uri;
                if (uri == null) {
                    fileLabel.setText("No file selected");
                } else {
                    fileLabel.setText(FileUtils.getFileName(this, uri));
                }
            }
    );

    private final ActivityResultLauncher<String> iconPicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                iconFile = uri;
                if (uri == null) {
                    iconLabel.setText("No icon selected");
                    iconPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    iconPreview.setColorFilter(getColor(R.color.schedule_muted));
                } else {
                    iconLabel.setText(FileUtils.getFileName(this, uri));
                    iconPreview.setColorFilter(null);
                    iconPreview.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_create);

        getWindow().setStatusBarColor(getColor(R.color.schedule_background));
        getWindow().setNavigationBarColor(getColor(R.color.schedule_background));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        subjectSpinner = findViewById(R.id.spinnerSubject);
        groupSpinner = findViewById(R.id.spinnerGroup);
        titleInput = findViewById(R.id.inputHomeworkTitle);
        descriptionInput = findViewById(R.id.inputHomeworkDescription);
        deadlineInput = findViewById(R.id.inputHomeworkDeadline);
        fileLabel = findViewById(R.id.labelHomeworkFile);
        iconLabel = findViewById(R.id.labelHomeworkIcon);
        iconPreview = findViewById(R.id.homeworkIconPreview);
        progressBar = findViewById(R.id.homeworkCreateProgress);

        Button btnPickFile = findViewById(R.id.btnPickHomeworkFile);
        Button btnPickIcon = findViewById(R.id.btnPickHomeworkIcon);
        Button btnCreate = findViewById(R.id.btnSubmitHomeworkCreate);
        View btnCancel = findViewById(R.id.btnHomeworkCancel);
        View btnClose = findViewById(R.id.btnHomeworkClose);

        btnPickFile.setOnClickListener(v -> filePicker.launch("*/*"));
        btnPickIcon.setOnClickListener(v -> iconPicker.launch("image/*"));
        btnCreate.setOnClickListener(v -> createHomework());
        btnCancel.setOnClickListener(v -> finish());
        btnClose.setOnClickListener(v -> finish());

        apiService = ApiClient.getService(this);

        loadSubjects();
        loadGroups();
    }

    private void loadSubjects() {
        apiService.getMySubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects.clear();
                    subjects.addAll(response.body());
                    List<String> names = new ArrayList<>();
                    for (Subject subject : subjects) {
                        names.add(subject.name);
                    }
                    subjectSpinner.setAdapter(createSpinnerAdapter(names, "Select subject"));
                } else {
                    Toast.makeText(HomeworkCreateActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(HomeworkCreateActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroups() {
        apiService.getGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groups.clear();
                    groups.addAll(response.body());
                    List<String> names = new ArrayList<>();
                    for (Group group : groups) {
                        names.add(group.name);
                    }
                    groupSpinner.setAdapter(createSpinnerAdapter(names, "Select group"));
                } else {
                    Toast.makeText(HomeworkCreateActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Toast.makeText(HomeworkCreateActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createHomework() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String deadline = deadlineInput.getText().toString().trim();

        int subjectIndex = subjectSpinner.getSelectedItemPosition() - 1;
        int groupIndex = groupSpinner.getSelectedItemPosition() - 1;

        if (title.isEmpty() || deadline.isEmpty() || subjectIndex < 0 || groupIndex < 0) {
            Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = DEFAULT_TYPE;

        Subject subject = subjects.get(subjectIndex);
        Group group = groups.get(groupIndex);

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("title", MultipartUtils.toTextBody(title));
        fields.put("description", MultipartUtils.toTextBody(description));
        fields.put("type", MultipartUtils.toTextBody(type));
        fields.put("subjectId", MultipartUtils.toTextBody(String.valueOf(subject.id)));
        fields.put("groupId", MultipartUtils.toTextBody(String.valueOf(group.id)));
        fields.put("deadline", MultipartUtils.toTextBody(deadline + "T23:59:59"));

        List<MultipartBody.Part> parts = new ArrayList<>();
        try {
            if (assignmentFile != null) {
                parts.add(MultipartUtils.createFilePart(this, "file", assignmentFile, "assessment"));
            }
            if (iconFile != null) {
                parts.add(MultipartUtils.createFilePart(this, "icon", iconFile, "icon"));
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to read files", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        apiService.createHomework(fields, parts).enqueue(new Callback<HomeworkItem>() {
            @Override
            public void onResponse(Call<HomeworkItem> call, Response<HomeworkItem> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(HomeworkCreateActivity.this, "Homework created", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(HomeworkCreateActivity.this, "Failed to create homework", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HomeworkItem> call, Throwable t) {
                setLoading(false);
                Toast.makeText(HomeworkCreateActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
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
