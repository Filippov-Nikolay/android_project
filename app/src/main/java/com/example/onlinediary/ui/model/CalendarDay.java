package com.example.onlinediary.ui.model;

import java.time.LocalDate;

public class CalendarDay {
    public final LocalDate date;
    public final boolean inMonth;
    public final boolean hasEvents;
    public final boolean selected;
    public final boolean today;

    public CalendarDay(LocalDate date, boolean inMonth, boolean hasEvents, boolean selected, boolean today) {
        this.date = date;
        this.inMonth = inMonth;
        this.hasEvents = hasEvents;
        this.selected = selected;
        this.today = today;
    }
}
