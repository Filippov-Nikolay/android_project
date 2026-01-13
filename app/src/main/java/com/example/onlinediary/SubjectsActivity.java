package com.example.onlinediary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.GroupCreateRequest;
import com.example.onlinediary.model.Subject;
import com.example.onlinediary.model.SubjectAssignmentRequest;
import com.example.onlinediary.model.SubjectRequest;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubjectsActivity extends AppCompatActivity {
    private final List<Group> groups = new ArrayList<>();
    private final List<Subject> subjects = new ArrayList<>();
    private final List<User> teachers = new ArrayList<>();

    private LinearLayout groupsContainer;
    private LinearLayout subjectsContainer;
    private ProgressBar progressBar;

    private Spinner assignSubjectSpinner;
    private Spinner assignGroupSpinner;
    private Spinner assignTeacherSpinner;

    private EditText groupNameInput;
    private EditText groupCourseInput;
    private EditText subjectNameInput;
    private EditText subjectDescriptionInput;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);

        groupsContainer = findViewById(R.id.groupsContainer);
        subjectsContainer = findViewById(R.id.subjectsContainer);
        progressBar = findViewById(R.id.subjectsProgress);

        assignSubjectSpinner = findViewById(R.id.spinnerAssignSubject);
        assignGroupSpinner = findViewById(R.id.spinnerAssignGroup);
        assignTeacherSpinner = findViewById(R.id.spinnerAssignTeacher);

        groupNameInput = findViewById(R.id.inputGroupName);
        groupCourseInput = findViewById(R.id.inputGroupCourse);
        subjectNameInput = findViewById(R.id.inputSubjectName);
        subjectDescriptionInput = findViewById(R.id.inputSubjectDescription);

        Button btnCreateGroup = findViewById(R.id.btnCreateGroup);
        Button btnCreateSubject = findViewById(R.id.btnCreateSubject);
        Button btnAssign = findViewById(R.id.btnAssignTeacher);

        btnCreateGroup.setOnClickListener(v -> createGroup());
        btnCreateSubject.setOnClickListener(v -> createSubject());
        btnAssign.setOnClickListener(v -> assignTeacher());

        apiService = ApiClient.getService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        setLoading(true);
        apiService.getGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groups.clear();
                    groups.addAll(response.body());
                    renderGroups();
                    updateGroupSpinner();
                }
                setLoading(false);
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SubjectsActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects.clear();
                    subjects.addAll(response.body());
                    renderSubjects();
                    updateSubjectSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(SubjectsActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getTeachers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teachers.clear();
                    teachers.addAll(response.body());
                    updateTeacherSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(SubjectsActivity.this, "Failed to load teachers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderGroups() {
        groupsContainer.removeAllViews();
        for (Group group : groups) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView text = new TextView(this);
            text.setText(group.name + (group.course != null ? " (" + group.course + ")" : ""));
            text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            Button deleteBtn = new Button(this);
            deleteBtn.setText("Delete");
            deleteBtn.setOnClickListener(v -> deleteGroup(group.id));

            row.addView(text);
            row.addView(deleteBtn);
            groupsContainer.addView(row);
        }
    }

    private void renderSubjects() {
        subjectsContainer.removeAllViews();
        for (Subject subject : subjects) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView text = new TextView(this);
            text.setText(subject.name);
            text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            Button editBtn = new Button(this);
            editBtn.setText("Edit");
            editBtn.setOnClickListener(v -> showEditSubjectDialog(subject));

            Button deleteBtn = new Button(this);
            deleteBtn.setText("Delete");
            deleteBtn.setOnClickListener(v -> deleteSubject(subject.id));

            row.addView(text);
            row.addView(editBtn);
            row.addView(deleteBtn);
            subjectsContainer.addView(row);
        }
    }

    private void updateGroupSpinner() {
        List<String> names = new ArrayList<>();
        for (Group group : groups) {
            names.add(group.name);
        }
        assignGroupSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void updateSubjectSpinner() {
        List<String> names = new ArrayList<>();
        for (Subject subject : subjects) {
            names.add(subject.name);
        }
        assignSubjectSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void updateTeacherSpinner() {
        List<String> names = new ArrayList<>();
        for (User teacher : teachers) {
            String name = teacher.lastName + " " + teacher.firstName;
            names.add(name.trim());
        }
        assignTeacherSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void createGroup() {
        String name = groupNameInput.getText().toString().trim();
        String courseText = groupCourseInput.getText().toString().trim();
        int course = courseText.isEmpty() ? 1 : Integer.parseInt(courseText);

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        apiService.createGroup(new GroupCreateRequest(name, course)).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(Call<Group> call, Response<Group> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    groupNameInput.setText("");
                    groupCourseInput.setText("");
                    loadData();
                } else {
                    Toast.makeText(SubjectsActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Group> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SubjectsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup(long id) {
        setLoading(true);
        apiService.deleteGroup(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    loadData();
                } else {
                    Toast.makeText(SubjectsActivity.this, "Failed to delete group", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SubjectsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createSubject() {
        String name = subjectNameInput.getText().toString().trim();
        String description = subjectDescriptionInput.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter subject name", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        apiService.createSubject(new SubjectRequest(name, description)).enqueue(new Callback<Subject>() {
            @Override
            public void onResponse(Call<Subject> call, Response<Subject> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    subjectNameInput.setText("");
                    subjectDescriptionInput.setText("");
                    loadData();
                } else {
                    Toast.makeText(SubjectsActivity.this, "Failed to create subject", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Subject> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SubjectsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditSubjectDialog(Subject subject) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText nameInput = new EditText(this);
        nameInput.setText(subject.name);
        EditText descInput = new EditText(this);
        descInput.setText(subject.description);

        layout.addView(nameInput);
        layout.addView(descInput);

        new AlertDialog.Builder(this)
                .setTitle("Edit subject")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> updateSubject(subject.id, nameInput.getText().toString().trim(), descInput.getText().toString().trim()))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateSubject(long id, String name, String description) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Subject name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        apiService.updateSubject(id, new SubjectRequest(name, description)).enqueue(new Callback<Subject>() {
            @Override
            public void onResponse(Call<Subject> call, Response<Subject> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    loadData();
                } else {
                    Toast.makeText(SubjectsActivity.this, "Failed to update subject", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Subject> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SubjectsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSubject(long id) {
        setLoading(true);
        apiService.deleteSubject(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    loadData();
                } else {
                    Toast.makeText(SubjectsActivity.this, "Failed to delete subject", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SubjectsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignTeacher() {
        if (subjects.isEmpty() || groups.isEmpty() || teachers.isEmpty()) {
            Toast.makeText(this, "Load data first", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject subject = subjects.get(assignSubjectSpinner.getSelectedItemPosition());
        Group group = groups.get(assignGroupSpinner.getSelectedItemPosition());
        User teacher = teachers.get(assignTeacherSpinner.getSelectedItemPosition());

        setLoading(true);
        apiService.assignTeacher(new SubjectAssignmentRequest(teacher.id, subject.id, group.id))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(SubjectsActivity.this, "Assigned", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SubjectsActivity.this, "Failed to assign", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(SubjectsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
