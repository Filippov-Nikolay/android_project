package com.example.onlinediary;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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

    private ChipGroup groupsChipGroup;
    private ChipGroup subjectsChipGroup;
    private TextView groupsEmpty;
    private TextView subjectsEmpty;
    private ProgressBar progressBar;

    private Spinner assignSubjectSpinner;
    private Spinner assignGroupSpinner;
    private Spinner assignTeacherSpinner;

    private EditText groupNameInput;
    private Spinner groupCourseSpinner;
    private EditText subjectNameInput;
    private EditText subjectDescriptionInput;
    private View editOverlay;
    private View editCard;
    private EditText editSubjectNameInput;
    private EditText editSubjectDescriptionInput;
    private Subject editingSubject;
    private int pendingCalls = 0;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);

        getWindow().setStatusBarColor(getColor(R.color.schedule_background));
        getWindow().setNavigationBarColor(getColor(R.color.schedule_background));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        groupsChipGroup = findViewById(R.id.groupsChipGroup);
        subjectsChipGroup = findViewById(R.id.subjectsChipGroup);
        groupsEmpty = findViewById(R.id.groupsEmpty);
        subjectsEmpty = findViewById(R.id.subjectsEmpty);
        progressBar = findViewById(R.id.subjectsProgress);

        assignSubjectSpinner = findViewById(R.id.spinnerAssignSubject);
        assignGroupSpinner = findViewById(R.id.spinnerAssignGroup);
        assignTeacherSpinner = findViewById(R.id.spinnerAssignTeacher);

        groupNameInput = findViewById(R.id.inputGroupName);
        groupCourseSpinner = findViewById(R.id.spinnerGroupCourse);
        subjectNameInput = findViewById(R.id.inputSubjectName);
        subjectDescriptionInput = findViewById(R.id.inputSubjectDescription);
        editOverlay = findViewById(R.id.subjectEditOverlay);
        editCard = findViewById(R.id.subjectEditCard);
        editSubjectNameInput = findViewById(R.id.editSubjectName);
        editSubjectDescriptionInput = findViewById(R.id.editSubjectDescription);

        View btnCreateGroup = findViewById(R.id.btnCreateGroup);
        View btnCreateSubject = findViewById(R.id.btnCreateSubject);
        View btnAssign = findViewById(R.id.btnAssignTeacher);
        View btnEditCancel = findViewById(R.id.btnEditSubjectCancel);
        View btnEditSave = findViewById(R.id.btnEditSubjectSave);

        btnCreateGroup.setOnClickListener(v -> createGroup());
        btnCreateSubject.setOnClickListener(v -> createSubject());
        btnAssign.setOnClickListener(v -> assignTeacher());
        btnEditCancel.setOnClickListener(v -> hideEditOverlay());
        btnEditSave.setOnClickListener(v -> updateSubject());
        editOverlay.setOnClickListener(v -> hideEditOverlay());
        editCard.setOnClickListener(v -> {
        });

        setupCourseSpinner();

        apiService = ApiClient.getService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        pendingCalls = 0;
        setLoading(true);
        beginCall();
        apiService.getGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                endCall();
                if (response.isSuccessful() && response.body() != null) {
                    groups.clear();
                    groups.addAll(response.body());
                    renderGroups();
                    updateGroupSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                endCall();
                Toast.makeText(SubjectsActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });

        beginCall();
        apiService.getSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                endCall();
                if (response.isSuccessful() && response.body() != null) {
                    subjects.clear();
                    subjects.addAll(response.body());
                    renderSubjects();
                    updateSubjectSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                endCall();
                Toast.makeText(SubjectsActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });

        beginCall();
        apiService.getTeachers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                endCall();
                if (response.isSuccessful() && response.body() != null) {
                    teachers.clear();
                    teachers.addAll(response.body());
                    updateTeacherSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                endCall();
                Toast.makeText(SubjectsActivity.this, "Failed to load teachers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderGroups() {
        groupsChipGroup.removeAllViews();
        for (Group group : groups) {
            String label = group.name + (group.course != null ? " (" + group.course + ")" : "");
            Chip chip = createChip(label, true);
            chip.setOnCloseIconClickListener(v -> confirmDeleteGroup(group));
            groupsChipGroup.addView(chip);
        }
        groupsEmpty.setVisibility(groups.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void renderSubjects() {
        subjectsChipGroup.removeAllViews();
        for (Subject subject : subjects) {
            String label = subject.name == null ? "Subject" : subject.name;
            Chip chip = createChip(label, true);
            chip.setOnClickListener(v -> showEditOverlay(subject));
            chip.setOnCloseIconClickListener(v -> confirmDeleteSubject(subject));
            subjectsChipGroup.addView(chip);
        }
        subjectsEmpty.setVisibility(subjects.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateGroupSpinner() {
        List<String> names = new ArrayList<>();
        for (Group group : groups) {
            names.add(group.name);
        }
        assignGroupSpinner.setAdapter(createSpinnerAdapter(names, "Select group"));
    }

    private void updateSubjectSpinner() {
        List<String> names = new ArrayList<>();
        for (Subject subject : subjects) {
            names.add(subject.name);
        }
        assignSubjectSpinner.setAdapter(createSpinnerAdapter(names, "Select subject"));
    }

    private void updateTeacherSpinner() {
        List<String> names = new ArrayList<>();
        for (User teacher : teachers) {
            String name = teacher.lastName + " " + teacher.firstName;
            names.add(name.trim());
        }
        assignTeacherSpinner.setAdapter(createSpinnerAdapter(names, "Select teacher"));
    }

    private void createGroup() {
        String name = groupNameInput.getText().toString().trim();
        int course = Integer.parseInt(groupCourseSpinner.getSelectedItem().toString());

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
                    groupCourseSpinner.setSelection(0);
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

    private void showEditOverlay(Subject subject) {
        editingSubject = subject;
        editSubjectNameInput.setText(subject.name == null ? "" : subject.name);
        editSubjectDescriptionInput.setText(subject.description == null ? "" : subject.description);
        editOverlay.setVisibility(View.VISIBLE);
    }

    private void hideEditOverlay() {
        editingSubject = null;
        editOverlay.setVisibility(View.GONE);
    }

    private void updateSubject() {
        if (editingSubject == null) {
            return;
        }

        long id = editingSubject.id;
        String name = editSubjectNameInput.getText().toString().trim();
        String description = editSubjectDescriptionInput.getText().toString().trim();
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
                    hideEditOverlay();
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

        int subjectIndex = assignSubjectSpinner.getSelectedItemPosition() - 1;
        int groupIndex = assignGroupSpinner.getSelectedItemPosition() - 1;
        int teacherIndex = assignTeacherSpinner.getSelectedItemPosition() - 1;

        if (subjectIndex < 0 || groupIndex < 0 || teacherIndex < 0) {
            Toast.makeText(this, "Select subject, group, and teacher", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject subject = subjects.get(subjectIndex);
        Group group = groups.get(groupIndex);
        User teacher = teachers.get(teacherIndex);

        setLoading(true);
        apiService.assignTeacher(new SubjectAssignmentRequest(teacher.id, subject.id, group.id))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(SubjectsActivity.this, "Assigned", Toast.LENGTH_SHORT).show();
                            assignSubjectSpinner.setSelection(0);
                            assignGroupSpinner.setSelection(0);
                            assignTeacherSpinner.setSelection(0);
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

    private void beginCall() {
        pendingCalls++;
        setLoading(true);
    }

    private void endCall() {
        pendingCalls = Math.max(0, pendingCalls - 1);
        setLoading(pendingCalls > 0);
    }

    private void setupCourseSpinner() {
        List<String> courses = new ArrayList<>();
        courses.add("1");
        courses.add("2");
        courses.add("3");
        courses.add("4");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, courses);
        adapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
        groupCourseSpinner.setAdapter(adapter);
    }

    private ArrayAdapter<String> createSpinnerAdapter(List<String> items, String placeholder) {
        List<String> values = new ArrayList<>();
        values.add(placeholder);
        values.addAll(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, values);
        adapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
        return adapter;
    }

    private Chip createChip(String label, boolean closable) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setTextColor(getColor(R.color.schedule_text));
        chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.schedule_surface_muted)));
        chip.setChipStrokeColor(ColorStateList.valueOf(getColor(R.color.schedule_line)));
        chip.setChipStrokeWidth(1f);
        chip.setCloseIconVisible(closable);
        chip.setCloseIconTint(ColorStateList.valueOf(getColor(R.color.schedule_muted)));
        chip.setEnsureMinTouchTargetSize(false);
        return chip;
    }

    private void confirmDeleteGroup(Group group) {
        new AlertDialog.Builder(this)
                .setTitle("Delete group")
                .setMessage("Are you sure you want to delete " + group.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteGroup(group.id))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmDeleteSubject(Subject subject) {
        new AlertDialog.Builder(this)
                .setTitle("Delete subject")
                .setMessage("Are you sure you want to delete " + subject.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSubject(subject.id))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
