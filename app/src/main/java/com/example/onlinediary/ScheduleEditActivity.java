package com.example.onlinediary;

import android.content.Intent;
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

import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.model.ScheduleRequest;
import com.example.onlinediary.model.Subject;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.util.BottomNavHelper;
import com.example.onlinediary.util.ScheduleTimeUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private Spinner lessonSpinner;
    private EditText dateInput;
    private EditText roomInput;
    private TextView titleText;
    private TextView subtitleText;
    private ProgressBar progressBar;
    private View listButton;

    private ApiService apiService;
    private long scheduleId;
    private ScheduleEvent currentItem;
    private final List<Integer> lessonNumbers = new ArrayList<>();

    private static final String[] TYPE_LABELS = new String[]{"Lecture", "Practice", "Exam"};
    private static final String[] TYPE_VALUES = new String[]{"lecture", "practice", "exam"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);

        getWindow().setStatusBarColor(getColor(R.color.schedule_background));
        getWindow().setNavigationBarColor(getColor(R.color.schedule_background));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        groupSpinner = findViewById(R.id.spinnerScheduleGroup);
        subjectSpinner = findViewById(R.id.spinnerScheduleSubject);
        teacherSpinner = findViewById(R.id.spinnerScheduleTeacher);
        typeSpinner = findViewById(R.id.spinnerScheduleType);
        lessonSpinner = findViewById(R.id.spinnerScheduleLesson);
        dateInput = findViewById(R.id.inputScheduleDate);
        roomInput = findViewById(R.id.inputScheduleRoom);
        progressBar = findViewById(R.id.scheduleEditProgress);
        View btnSave = findViewById(R.id.btnSaveSchedule);
        listButton = findViewById(R.id.btnScheduleList);
        titleText = findViewById(R.id.scheduleEditTitle);
        subtitleText = findViewById(R.id.scheduleEditSubtitle);

        scheduleId = getIntent().getLongExtra("scheduleId", -1);

        setupTypeSpinner();
        setupLessonSpinner();

        apiService = ApiClient.getService(this);
        BottomNavHelper.setupAdminNav(this, R.id.navAdminSchedule);

        btnSave.setOnClickListener(v -> saveSchedule());
        listButton.setOnClickListener(v -> startActivity(new Intent(this, ScheduleAdminActivity.class)));

        if (scheduleId > 0) {
            titleText.setText("Edit schedule");
            subtitleText.setText("Update schedule entry details");
        } else {
            dateInput.setText(LocalDate.now().toString());
        }

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
                    groupSpinner.setAdapter(createSpinnerAdapter(names, "Select group"));
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
                    subjectSpinner.setAdapter(createSpinnerAdapter(names, "Select subject"));
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
                    teacherSpinner.setAdapter(createSpinnerAdapter(names, "Select teacher"));
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
                    dateInput.setText(dateOnly(currentItem.date));
                    roomInput.setText(currentItem.room == null ? "" : currentItem.room);
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
                groupSpinner.setSelection(i + 1);
                break;
            }
        }
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).id == currentItem.subjectId) {
                subjectSpinner.setSelection(i + 1);
                break;
            }
        }
        for (int i = 0; i < teachers.size(); i++) {
            if (teachers.get(i).id == currentItem.teacherId) {
                teacherSpinner.setSelection(i + 1);
                break;
            }
        }

        if (!lessonNumbers.isEmpty()) {
            int lessonIndex = lessonNumbers.indexOf(currentItem.lessonNumber);
            if (lessonIndex >= 0) {
                lessonSpinner.setSelection(lessonIndex);
            }
        }

        if (currentItem.type != null) {
            String type = currentItem.type.trim().toLowerCase(Locale.US);
            for (int i = 0; i < TYPE_VALUES.length; i++) {
                if (TYPE_VALUES[i].equals(type)) {
                    typeSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveSchedule() {
        String date = dateInput.getText().toString().trim();
        String room = roomInput.getText().toString().trim();

        int groupIndex = groupSpinner.getSelectedItemPosition() - 1;
        int subjectIndex = subjectSpinner.getSelectedItemPosition() - 1;
        int teacherIndex = teacherSpinner.getSelectedItemPosition() - 1;

        if (date.isEmpty() || groupIndex < 0 || subjectIndex < 0 || teacherIndex < 0 || lessonNumbers.isEmpty()) {
            Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int lessonNumber = lessonNumbers.get(lessonSpinner.getSelectedItemPosition());
        Group group = groups.get(groupIndex);
        Subject subject = subjects.get(subjectIndex);
        User teacher = teachers.get(teacherIndex);
        String type = TYPE_VALUES[typeSpinner.getSelectedItemPosition()].toUpperCase(Locale.US);

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

    private void setupTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, TYPE_LABELS);
        adapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
        typeSpinner.setAdapter(adapter);
    }

    private void setupLessonSpinner() {
        lessonNumbers.clear();
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            lessonNumbers.add(i);
            String range = ScheduleTimeUtils.getTimeRangeLabel(i);
            String label = range.isEmpty() ? "Lesson " + i : "Lesson " + i + " (" + range + ")";
            labels.add(label);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
        lessonSpinner.setAdapter(adapter);
    }

    private ArrayAdapter<String> createSpinnerAdapter(List<String> items, String placeholder) {
        List<String> values = new ArrayList<>();
        values.add(placeholder);
        values.addAll(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, values);
        adapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
        return adapter;
    }

    private String dateOnly(String value) {
        if (value == null) {
            return "";
        }
        int idx = value.indexOf('T');
        return idx > 0 ? value.substring(0, idx) : value;
    }
}
