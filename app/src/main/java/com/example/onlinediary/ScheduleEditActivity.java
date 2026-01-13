package com.example.onlinediary;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.model.ScheduleRequest;
import com.example.onlinediary.model.Subject;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleEditActivity extends AppCompatActivity {
    private final List<Group> groups = new ArrayList<>();
    private final List<Subject> subjects = new ArrayList<>();
    private final List<User> teachers = new ArrayList<>();

    private Spinner groupSpinner;
    private Spinner subjectSpinner;
    private Spinner teacherSpinner;
    private Spinner typeSpinner;
    private EditText dateInput;
    private EditText lessonInput;
    private EditText roomInput;
    private ProgressBar progressBar;

    private ApiService apiService;
    private long scheduleId;
    private ScheduleEvent currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);

        groupSpinner = findViewById(R.id.spinnerScheduleGroup);
        subjectSpinner = findViewById(R.id.spinnerScheduleSubject);
        teacherSpinner = findViewById(R.id.spinnerScheduleTeacher);
        typeSpinner = findViewById(R.id.spinnerScheduleType);
        dateInput = findViewById(R.id.inputScheduleDate);
        lessonInput = findViewById(R.id.inputScheduleLesson);
        roomInput = findViewById(R.id.inputScheduleRoom);
        progressBar = findViewById(R.id.scheduleEditProgress);
        Button btnSave = findViewById(R.id.btnSaveSchedule);

        scheduleId = getIntent().getLongExtra("scheduleId", -1);

        typeSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"lecture", "practice", "exam"}
        ));

        apiService = ApiClient.getService(this);

        btnSave.setOnClickListener(v -> saveSchedule());

        loadGroups();
        loadSubjects();
        loadTeachers();
        if (scheduleId > 0) {
            loadScheduleItem();
        }
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
                    groupSpinner.setAdapter(new ArrayAdapter<>(
                            ScheduleEditActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            names
                    ));
                    applySelection();
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Toast.makeText(ScheduleEditActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSubjects() {
        apiService.getSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects.clear();
                    subjects.addAll(response.body());
                    List<String> names = new ArrayList<>();
                    for (Subject subject : subjects) {
                        names.add(subject.name);
                    }
                    subjectSpinner.setAdapter(new ArrayAdapter<>(
                            ScheduleEditActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            names
                    ));
                    applySelection();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(ScheduleEditActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeachers() {
        apiService.getTeachers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teachers.clear();
                    teachers.addAll(response.body());
                    List<String> names = new ArrayList<>();
                    for (User teacher : teachers) {
                        names.add((teacher.lastName + " " + teacher.firstName).trim());
                    }
                    teacherSpinner.setAdapter(new ArrayAdapter<>(
                            ScheduleEditActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            names
                    ));
                    applySelection();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ScheduleEditActivity.this, "Failed to load teachers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScheduleItem() {
        setLoading(true);
        apiService.getScheduleItem(scheduleId).enqueue(new Callback<ScheduleEvent>() {
            @Override
            public void onResponse(Call<ScheduleEvent> call, Response<ScheduleEvent> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentItem = response.body();
                    dateInput.setText(currentItem.date);
                    lessonInput.setText(String.valueOf(currentItem.lessonNumber));
                    roomInput.setText(currentItem.room == null ? "" : currentItem.room);
                    if (currentItem.type != null) {
                        String type = currentItem.type.toLowerCase();
                        int index = type.equals("practice") ? 1 : type.equals("exam") ? 2 : 0;
                        typeSpinner.setSelection(index);
                    }
                    applySelection();
                } else {
                    Toast.makeText(ScheduleEditActivity.this, "Failed to load schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ScheduleEvent> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ScheduleEditActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applySelection() {
        if (currentItem == null) {
            return;
        }
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).id == currentItem.groupId) {
                groupSpinner.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).id == currentItem.subjectId) {
                subjectSpinner.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < teachers.size(); i++) {
            if (teachers.get(i).id == currentItem.teacherId) {
                teacherSpinner.setSelection(i);
                break;
            }
        }
    }

    private void saveSchedule() {
        String date = dateInput.getText().toString().trim();
        String lessonText = lessonInput.getText().toString().trim();
        String room = roomInput.getText().toString().trim();

        if (date.isEmpty() || lessonText.isEmpty() || groups.isEmpty() || subjects.isEmpty() || teachers.isEmpty()) {
            Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int lessonNumber = Integer.parseInt(lessonText);
        Group group = groups.get(groupSpinner.getSelectedItemPosition());
        Subject subject = subjects.get(subjectSpinner.getSelectedItemPosition());
        User teacher = teachers.get(teacherSpinner.getSelectedItemPosition());
        String type = typeSpinner.getSelectedItem().toString().toUpperCase();

        ScheduleRequest request = new ScheduleRequest(
                group.id,
                subject.id,
                teacher.id,
                date,
                lessonNumber,
                room,
                type
        );

        setLoading(true);
        Call<ScheduleEvent> call = scheduleId > 0
                ? apiService.updateSchedule(scheduleId, request)
                : apiService.createSchedule(request);

        call.enqueue(new Callback<ScheduleEvent>() {
            @Override
            public void onResponse(Call<ScheduleEvent> call, Response<ScheduleEvent> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ScheduleEditActivity.this, "Schedule saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ScheduleEditActivity.this, "Failed to save", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ScheduleEvent> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ScheduleEditActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
