package com.example.onlinediary.ui.model;

import java.time.LocalDate;

public class CalendarDay {
    public final LocalDate date;
    public final boolean inMonth;
    public final boolean hasLecture;
    public final boolean hasPractice;
    public final boolean hasExam;
    public final boolean selected;
    public final boolean today;

    public CalendarDay(LocalDate date, boolean inMonth, boolean hasLecture, boolean hasPractice, boolean hasExam,
                       boolean selected, boolean today) {
        this.date = date;
        this.inMonth = inMonth;
        this.hasLecture = hasLecture;
        this.hasPractice = hasPractice;
        this.hasExam = hasExam;
        this.selected = selected;
        this.today = today;
    }
}
