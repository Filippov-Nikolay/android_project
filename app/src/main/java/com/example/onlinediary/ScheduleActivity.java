package com.example.onlinediary;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.CalendarAdapter;
import com.example.onlinediary.ui.model.CalendarDay;
import com.example.onlinediary.ui.view.ScheduleTimelineLayout;
import com.example.onlinediary.util.BottomNavHelper;
import com.example.onlinediary.util.ScheduleTimeUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView monthTitle;
    private ImageButton btnListMode;
    private ImageButton btnCalendarMode;
    private RecyclerView calendarGrid;
    private ScheduleTimelineLayout timelineView;
    private CalendarAdapter calendarAdapter;
    private ApiService apiService;

    private final List<ScheduleEvent> allEvents = new ArrayList<>();
    private final Map<LocalDate, List<ScheduleEvent>> eventsByDate = new HashMap<>();
    private LocalDate currentMonth;
    private LocalDate selectedDate;
    private boolean showMonthView = true;
    private boolean canSeeJournal;

    private static final Locale SCHEDULE_LOCALE = new Locale("uk");
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("LLLL yyyy", SCHEDULE_LOCALE);
    private static final DateTimeFormatter WEEK_FORMAT =
            DateTimeFormatter.ofPattern("d MMM", SCHEDULE_LOCALE);
    private static final int DOT_LECTURE = 1;
    private static final int DOT_PRACTICE = 1 << 1;
    private static final int DOT_EXAM = 1 << 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        getWindow().setStatusBarColor(getColor(R.color.schedule_background));
        getWindow().setNavigationBarColor(getColor(R.color.schedule_background));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        progressBar = findViewById(R.id.scheduleProgress);
        monthTitle = findViewById(R.id.scheduleMonthTitle);
        calendarGrid = findViewById(R.id.scheduleCalendarGrid);
        timelineView = findViewById(R.id.scheduleTimeline);
        btnListMode = findViewById(R.id.btnListMode);
        btnCalendarMode = findViewById(R.id.btnCalendarMode);

        AuthStore authStore = new AuthStore(this);
        String role = authStore.getRole() == null ? "" : authStore.getRole();
        boolean isTeacher = "TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
        canSeeJournal = isTeacher;

        View studentNav = findViewById(R.id.studentBottomNav);
        View teacherNav = findViewById(R.id.teacherBottomNav);
        if (isTeacher) {
            if (studentNav != null) {
                studentNav.setVisibility(View.GONE);
            }
            if (teacherNav != null) {
                teacherNav.setVisibility(View.VISIBLE);
            }
            BottomNavHelper.setupTeacherNav(this, R.id.navTeacherSchedule);
        } else {
            if (studentNav != null) {
                studentNav.setVisibility(View.VISIBLE);
            }
            if (teacherNav != null) {
                teacherNav.setVisibility(View.GONE);
            }
            BottomNavHelper.setupStudentNav(this, R.id.navStudentSchedule);
        }

        currentMonth = LocalDate.now().withDayOfMonth(1);
        selectedDate = LocalDate.now();

        calendarAdapter = new CalendarAdapter(this::onDaySelected);
        calendarGrid.setLayoutManager(new GridLayoutManager(this, 7));
        calendarGrid.setAdapter(calendarAdapter);
        calendarGrid.setNestedScrollingEnabled(false);

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> moveMonth(-1));
        findViewById(R.id.btnNextMonth).setOnClickListener(v -> moveMonth(1));
        btnListMode.setOnClickListener(v -> setCalendarMode(false));
        btnCalendarMode.setOnClickListener(v -> setCalendarMode(true));

        timelineView.setHourRange(8, 18);
        timelineView.setSelectedDate(selectedDate);
        timelineView.setOnEventClickListener(event -> {
            if (!canSeeJournal) {
                return;
            }
            Intent intent = new Intent(ScheduleActivity.this, JournalActivity.class);
            intent.putExtra("scheduleId", event.id);
            startActivity(intent);
        });

        apiService = ApiClient.getService(this);
        setupWeekdayLabels();
        updateViewToggle();
        updateCalendar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchedule();
    }

    private void loadSchedule() {
        setLoading(true);
        apiService.getSchedule(null, null).enqueue(new Callback<List<ScheduleEvent>>() {
            @Override
            public void onResponse(Call<List<ScheduleEvent>> call, Response<List<ScheduleEvent>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allEvents.clear();
                    allEvents.addAll(response.body());
                    rebuildEventIndex();
                    updateCalendar();
                    updateTimeline();
                } else {
                    Toast.makeText(ScheduleActivity.this, "Failed to load schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleEvent>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ScheduleActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void moveMonth(int offset) {
        if (showMonthView) {
            currentMonth = currentMonth.plusMonths(offset).withDayOfMonth(1);
            if (!selectedDate.getMonth().equals(currentMonth.getMonth())) {
                selectedDate = currentMonth.withDayOfMonth(1);
            }
        } else {
            selectedDate = selectedDate.plusWeeks(offset);
            currentMonth = selectedDate.withDayOfMonth(1);
        }
        updateCalendar();
        updateTimeline();
    }

    private void setCalendarMode(boolean showMonth) {
        showMonthView = showMonth;
        updateViewToggle();
        updateCalendar();
    }

    private void onDaySelected(CalendarDay day) {
        selectedDate = day.date;
        currentMonth = selectedDate.withDayOfMonth(1);
        updateCalendar();
        updateTimeline();
    }

    private void updateCalendar() {
        monthTitle.setText(formatTitle());
        List<CalendarDay> days = showMonthView ? buildMonthDays(currentMonth) : buildWeekDays(selectedDate);
        calendarAdapter.setDays(days);
    }

    private void updateTimeline() {
        timelineView.setSelectedDate(selectedDate);
        timelineView.setEvents(getEventsForDate(selectedDate));
    }

    private List<CalendarDay> buildMonthDays(LocalDate month) {
        List<CalendarDay> days = new ArrayList<>();
        LocalDate firstOfMonth = month.withDayOfMonth(1);
        int dayOfWeekIndex = toMondayIndex(firstOfMonth.getDayOfWeek());
        LocalDate start = firstOfMonth.minusDays(dayOfWeekIndex);

        for (int i = 0; i < 42; i++) {
            LocalDate date = start.plusDays(i);
            boolean inMonth = date.getMonth().equals(month.getMonth());
            int mask = getDotMask(date);
            boolean hasLecture = (mask & DOT_LECTURE) != 0;
            boolean hasPractice = (mask & DOT_PRACTICE) != 0;
            boolean hasExam = (mask & DOT_EXAM) != 0;
            boolean selected = date.equals(selectedDate);
            boolean today = date.equals(LocalDate.now());
            days.add(new CalendarDay(date, inMonth, hasLecture, hasPractice, hasExam, selected, today));
        }
        return days;
    }

    private List<CalendarDay> buildWeekDays(LocalDate anchor) {
        List<CalendarDay> days = new ArrayList<>();
        int dayOfWeekIndex = toMondayIndex(anchor.getDayOfWeek());
        LocalDate start = anchor.minusDays(dayOfWeekIndex);

        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            boolean inMonth = date.getMonth().equals(anchor.getMonth());
            int mask = getDotMask(date);
            boolean hasLecture = (mask & DOT_LECTURE) != 0;
            boolean hasPractice = (mask & DOT_PRACTICE) != 0;
            boolean hasExam = (mask & DOT_EXAM) != 0;
            boolean selected = date.equals(selectedDate);
            boolean today = date.equals(LocalDate.now());
            days.add(new CalendarDay(date, inMonth, hasLecture, hasPractice, hasExam, selected, today));
        }
        return days;
    }

    private int toMondayIndex(DayOfWeek dayOfWeek) {
        return (dayOfWeek.getValue() + 6) % 7;
    }

    private void rebuildEventIndex() {
        eventsByDate.clear();
        for (ScheduleEvent event : allEvents) {
            LocalDate date = ScheduleTimeUtils.parseDate(event.date);
            if (date == null) {
                continue;
            }
            List<ScheduleEvent> list = eventsByDate.computeIfAbsent(date, key -> new ArrayList<>());
            list.add(event);
        }
    }

    private List<ScheduleEvent> getEventsForDate(LocalDate date) {
        List<ScheduleEvent> list = eventsByDate.get(date);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScheduleEvent> copy = new ArrayList<>(list);
        copy.sort((a, b) -> {
            LocalTime timeA = ScheduleTimeUtils.getStartTime(a);
            LocalTime timeB = ScheduleTimeUtils.getStartTime(b);
            if (timeA == null && timeB == null) {
                return 0;
            }
            if (timeA == null) {
                return 1;
            }
            if (timeB == null) {
                return -1;
            }
            return timeA.compareTo(timeB);
        });
        return copy;
    }

    private int getDotMask(LocalDate date) {
        List<ScheduleEvent> list = eventsByDate.get(date);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int mask = 0;
        for (ScheduleEvent event : list) {
            mask |= typeToMask(event.type);
            if (mask == (DOT_LECTURE | DOT_PRACTICE | DOT_EXAM)) {
                break;
            }
        }
        return mask;
    }

    private int typeToMask(String value) {
        if (value == null || value.trim().isEmpty()) {
            return DOT_LECTURE;
        }
        String type = value.trim().toLowerCase(Locale.US);
        if ("lecture".equals(type)) {
            return DOT_LECTURE;
        }
        if ("practice".equals(type)) {
            return DOT_PRACTICE;
        }
        if ("exam".equals(type)) {
            return DOT_EXAM;
        }
        return DOT_LECTURE;
    }

    private void updateViewToggle() {
        boolean monthSelected = showMonthView;
        btnCalendarMode.setBackgroundResource(monthSelected ? R.drawable.bg_schedule_segmented_active : android.R.color.transparent);
        btnListMode.setBackgroundResource(monthSelected ? android.R.color.transparent : R.drawable.bg_schedule_segmented_active);

        int activeColor = getColor(R.color.schedule_accent);
        int inactiveColor = getColor(R.color.schedule_muted);
        btnCalendarMode.setColorFilter(monthSelected ? activeColor : inactiveColor);
        btnListMode.setColorFilter(monthSelected ? inactiveColor : activeColor);
    }

    private void setupWeekdayLabels() {
        TextView[] labels = new TextView[]{
                findViewById(R.id.scheduleWeekMon),
                findViewById(R.id.scheduleWeekTue),
                findViewById(R.id.scheduleWeekWed),
                findViewById(R.id.scheduleWeekThu),
                findViewById(R.id.scheduleWeekFri),
                findViewById(R.id.scheduleWeekSat),
                findViewById(R.id.scheduleWeekSun)
        };
        DayOfWeek[] days = new DayOfWeek[]{
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        };
        for (int i = 0; i < labels.length; i++) {
            String label = days[i].getDisplayName(TextStyle.SHORT_STANDALONE, SCHEDULE_LOCALE);
            labels[i].setText(label.toUpperCase(SCHEDULE_LOCALE));
        }
    }

    private String formatMonthTitle(LocalDate date) {
        return capitalizeLabel(MONTH_FORMAT.format(date));
    }

    private String formatTitle() {
        if (showMonthView) {
            return formatMonthTitle(currentMonth);
        }
        LocalDate weekStart = selectedDate.minusDays(toMondayIndex(selectedDate.getDayOfWeek()));
        LocalDate weekEnd = weekStart.plusDays(6);
        String startLabel = capitalizeLabel(WEEK_FORMAT.format(weekStart));
        String endLabel = capitalizeLabel(WEEK_FORMAT.format(weekEnd));
        return startLabel + " - " + endLabel;
    }

    private String capitalizeLabel(String label) {
        if (label == null || label.isEmpty()) {
            return label;
        }
        return label.substring(0, 1).toUpperCase(SCHEDULE_LOCALE) + label.substring(1);
    }
}
