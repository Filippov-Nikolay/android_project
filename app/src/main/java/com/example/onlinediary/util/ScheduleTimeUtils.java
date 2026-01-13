package com.example.onlinediary.util;

import com.example.onlinediary.model.ScheduleEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public final class ScheduleTimeUtils {
    private static final int DEFAULT_DURATION_MINUTES = 80;
    private static final Map<Integer, LocalTime> START_TIMES = new HashMap<>();
    private static final Map<Integer, LocalTime> END_TIMES = new HashMap<>();

    static {
        START_TIMES.put(1, LocalTime.of(8, 50));
        END_TIMES.put(1, LocalTime.of(10, 10));
        START_TIMES.put(2, LocalTime.of(10, 20));
        END_TIMES.put(2, LocalTime.of(11, 40));
        START_TIMES.put(3, LocalTime.of(12, 0));
        END_TIMES.put(3, LocalTime.of(13, 20));
        START_TIMES.put(4, LocalTime.of(13, 30));
        END_TIMES.put(4, LocalTime.of(14, 50));
        START_TIMES.put(5, LocalTime.of(15, 0));
        END_TIMES.put(5, LocalTime.of(16, 20));
        START_TIMES.put(6, LocalTime.of(16, 30));
        END_TIMES.put(6, LocalTime.of(17, 50));
    }

    private ScheduleTimeUtils() {}

    public static LocalTime getStartTime(ScheduleEvent event) {
        if (event == null) {
            return null;
        }
        LocalTime parsed = parseTime(event.startTime);
        if (parsed != null) {
            return parsed;
        }
        LocalTime mapped = START_TIMES.get(event.lessonNumber);
        if (mapped != null) {
            return mapped;
        }
        return LocalTime.of(8, 0);
    }

    public static LocalTime getEndTime(ScheduleEvent event, LocalTime start) {
        if (event == null) {
            return start;
        }
        LocalTime parsed = parseTime(event.endTime);
        if (parsed != null) {
            return parsed;
        }
        LocalTime mapped = END_TIMES.get(event.lessonNumber);
        if (mapped != null) {
            return mapped;
        }
        return start == null ? null : start.plusMinutes(DEFAULT_DURATION_MINUTES);
    }

    public static LocalDate parseDate(String value) {
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

    public static LocalTime parseTime(String value) {
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
