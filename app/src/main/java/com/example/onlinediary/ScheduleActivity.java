package com.example.onlinediary;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

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

        AuthStore authStore = new AuthStore(this);
        String role = authStore.getRole() == null ? "" : authStore.getRole();
        canSeeJournal = "TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        currentMonth = LocalDate.now().withDayOfMonth(1);
        selectedDate = LocalDate.now();

        calendarAdapter = new CalendarAdapter(this::onDaySelected);
        calendarGrid.setLayoutManager(new GridLayoutManager(this, 7));
        calendarGrid.setAdapter(calendarAdapter);
        calendarGrid.setNestedScrollingEnabled(false);

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> moveMonth(-1));
        findViewById(R.id.btnNextMonth).setOnClickListener(v -> moveMonth(1));
        findViewById(R.id.btnListMode).setOnClickListener(v -> setCalendarMode(false));
        findViewById(R.id.btnCalendarMode).setOnClickListener(v -> setCalendarMode(true));

        timelineView.setHourRange(8, 19);
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
        currentMonth = currentMonth.plusMonths(offset).withDayOfMonth(1);
        if (!selectedDate.getMonth().equals(currentMonth.getMonth())) {
            selectedDate = currentMonth.withDayOfMonth(1);
        }
        updateCalendar();
        updateTimeline();
    }

    private void setCalendarMode(boolean showMonth) {
        showMonthView = showMonth;
        updateCalendar();
    }

    private void onDaySelected(CalendarDay day) {
        selectedDate = day.date;
        currentMonth = selectedDate.withDayOfMonth(1);
        updateCalendar();
        updateTimeline();
    }

    private void updateCalendar() {
        LocalDate titleDate = showMonthView ? currentMonth : selectedDate.withDayOfMonth(1);
        monthTitle.setText(MONTH_FORMAT.format(titleDate));
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
            boolean hasEvents = eventsByDate.containsKey(date);
            boolean selected = date.equals(selectedDate);
            boolean today = date.equals(LocalDate.now());
            days.add(new CalendarDay(date, inMonth, hasEvents, selected, today));
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
            boolean hasEvents = eventsByDate.containsKey(date);
            boolean selected = date.equals(selectedDate);
            boolean today = date.equals(LocalDate.now());
            days.add(new CalendarDay(date, inMonth, hasEvents, selected, today));
        }
        return days;
    }

    private int toMondayIndex(DayOfWeek dayOfWeek) {
        return (dayOfWeek.getValue() + 6) % 7;
    }

    private void rebuildEventIndex() {
        eventsByDate.clear();
        for (ScheduleEvent event : allEvents) {
            LocalDate date = parseDate(event.date);
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
        copy.sort((a, b) -> parseEventStart(a).compareTo(parseEventStart(b)));
        return copy;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.length() < 10) {
            return null;
        }
        String text = value;
        int tIndex = value.indexOf('T');
        if (tIndex > 0) {
            text = value.substring(0, tIndex);
        } else if (value.length() > 10) {
            text = value.substring(0, 10);
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseEventStart(ScheduleEvent event) {
        LocalTime parsed = parseTime(event.startTime);
        if (parsed != null) {
            return parsed;
        }
        int lesson = Math.max(1, event.lessonNumber);
        int hour = 8 + (lesson - 1);
        return LocalTime.of(Math.min(23, hour), 0);
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        int tIndex = text.indexOf('T');
        if (tIndex >= 0) {
            text = text.substring(tIndex + 1);
        }
        int dotIndex = text.indexOf('.');
        if (dotIndex > 0) {
            text = text.substring(0, dotIndex);
        }
        if (text.length() > 5) {
            text = text.substring(0, 5);
        }
        try {
            return LocalTime.parse(text);
        } catch (Exception e) {
            return null;
        }
    }
}
