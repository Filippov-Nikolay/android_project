package com.example.onlinediary.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.onlinediary.R;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.util.ScheduleTimeUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleTimelineLayout extends FrameLayout {
    public interface EventClickListener {
        void onEventClick(ScheduleEvent event);
    }

    private static final int DEFAULT_START_HOUR = 8;
    private static final int DEFAULT_END_HOUR = 18;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nowDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<ScheduleEvent> events = new ArrayList<>();

    private int startHour = DEFAULT_START_HOUR;
    private int endHour = DEFAULT_END_HOUR;
    private int hourHeightPx;
    private int labelWidthPx;
    private int labelGapPx;
    private int eventSidePaddingPx;

    private LocalDate selectedDate = LocalDate.now();
    private EventClickListener eventClickListener;

    public ScheduleTimelineLayout(Context context) {
        super(context);
        init();
    }

    public ScheduleTimelineLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScheduleTimelineLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        hourHeightPx = dpToPx(80);
        labelWidthPx = dpToPx(64);
        labelGapPx = dpToPx(8);
        eventSidePaddingPx = dpToPx(8);

        int leftPadding = labelWidthPx + labelGapPx;
        setPadding(leftPadding, dpToPx(12), eventSidePaddingPx, dpToPx(16));

        linePaint.setColor(getColor(R.color.schedule_line));
        linePaint.setStrokeWidth(dpToPx(1));

        textPaint.setColor(getColor(R.color.schedule_muted));
        textPaint.setTextSize(spToPx(11));
        textPaint.setTextAlign(Paint.Align.RIGHT);

        nowPaint.setColor(getColor(R.color.schedule_now));
        nowPaint.setStrokeWidth(dpToPx(1.5f));

        nowDotPaint.setColor(getColor(R.color.schedule_now));
        nowDotPaint.setStyle(Paint.Style.FILL);
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate = date == null ? LocalDate.now() : date;
        invalidate();
    }

    public void setOnEventClickListener(EventClickListener listener) {
        eventClickListener = listener;
    }

    public void setHourRange(int startHour, int endHour) {
        this.startHour = Math.max(0, startHour);
        this.endHour = Math.min(23, Math.max(this.startHour + 1, endHour));
        requestLayout();
        invalidate();
    }

    public void setEvents(List<ScheduleEvent> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        updateHourRangeFromEvents();
        renderEventCards();
        requestLayout();
        invalidate();
    }

    private void updateHourRangeFromEvents() {
        int minHour = DEFAULT_START_HOUR;
        int maxHour = DEFAULT_END_HOUR;
        for (ScheduleEvent event : events) {
            LocalTime start = ScheduleTimeUtils.getStartTime(event);
            LocalTime end = ScheduleTimeUtils.getEndTime(event, start);
            if (start != null) {
                minHour = Math.min(minHour, start.getHour());
                maxHour = Math.max(maxHour, start.getHour());
            }
            if (end != null) {
                minHour = Math.min(minHour, end.getHour());
                int endHour = end.getMinute() > 0 ? end.getHour() + 1 : end.getHour();
                maxHour = Math.max(maxHour, endHour);
            }
        }
        startHour = Math.max(0, minHour);
        endHour = Math.min(23, Math.max(startHour + 1, maxHour));
    }

    private void renderEventCards() {
        removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (ScheduleEvent event : events) {
            LocalTime start = ScheduleTimeUtils.getStartTime(event);
            LocalTime end = ScheduleTimeUtils.getEndTime(event, start);
            if (start == null || end == null) {
                continue;
            }

            int top = timeToPx(start);
            int height = Math.max(dpToPx(64), timeToPx(end) - timeToPx(start));

            View card = inflater.inflate(R.layout.item_schedule_timeline_event, this, false);
            View typeBar = card.findViewById(R.id.scheduleEventTypeBar);
            TextView title = card.findViewById(R.id.scheduleEventTitle);
            TextView detail = card.findViewById(R.id.scheduleEventDetail);

            String titleText = event.title;
            if (titleText == null || titleText.trim().isEmpty()) {
                titleText = event.subjectName == null ? "Lesson" : event.subjectName;
            }
            title.setText(titleText);

            String timeRange = formatTime(start) + " - " + formatTime(end);
            detail.setText(timeRange);
            typeBar.setBackgroundColor(resolveTypeColor(event));

            if (eventClickListener != null) {
                card.setOnClickListener(v -> eventClickListener.onEventClick(event));
            }

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    height
            );
            params.topMargin = top + dpToPx(4);
            params.leftMargin = dpToPx(2);
            params.rightMargin = dpToPx(2);
            addView(card, params);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = ((endHour - startHour) + 1) * hourHeightPx + getPaddingTop() + getPaddingBottom();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
        int childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        measureChildren(childWidthSpec, childHeightSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int contentLeft = getPaddingLeft();
        int contentRight = getWidth() - getPaddingRight();
        int startY = getPaddingTop();

        for (int hour = startHour; hour <= endHour; hour++) {
            int offset = hour - startHour;
            int y = startY + (offset * hourHeightPx);
            canvas.drawLine(contentLeft, y, contentRight, y, linePaint);
            String label = String.format(Locale.US, "%02d:00", hour);
            float textX = contentLeft - dpToPx(6);
            float textY = y + dpToPx(4);
            canvas.drawText(label, textX, textY, textPaint);
        }

        drawNowIndicator(canvas, contentLeft, contentRight, startY);
    }

    private void drawNowIndicator(Canvas canvas, int contentLeft, int contentRight, int startY) {
        if (selectedDate == null || !selectedDate.equals(LocalDate.now())) {
            return;
        }

        LocalTime now = LocalTime.now();
        if (now.getHour() < startHour || now.getHour() > endHour) {
            return;
        }

        float minutesFromStart = (now.getHour() - startHour) * 60f + now.getMinute();
        float y = startY + (minutesFromStart / 60f) * hourHeightPx;
        canvas.drawLine(contentLeft, y, contentRight, y, nowPaint);

        float dotRadius = dpToPx(5);
        canvas.drawCircle(contentLeft - dotRadius, y, dotRadius, nowDotPaint);
    }

    private int timeToPx(LocalTime time) {
        int minutes = (time.getHour() - startHour) * 60 + time.getMinute();
        return Math.max(0, Math.round(minutes * (hourHeightPx / 60f)));
    }

    private String formatTime(LocalTime time) {
        return String.format(Locale.US, "%02d:%02d", time.getHour(), time.getMinute());
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private int spToPx(float sp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                getResources().getDisplayMetrics()
        );
    }

    private int getColor(int colorId) {
        return getResources().getColor(colorId);
    }

    private int resolveTypeColor(ScheduleEvent event) {
        if (event == null || event.type == null) {
            return getColor(R.color.schedule_accent);
        }
        String type = event.type.trim().toLowerCase(Locale.US);
        if ("lecture".equals(type)) {
            return getColor(R.color.schedule_type_lecture);
        }
        if ("practice".equals(type)) {
            return getColor(R.color.schedule_type_practice);
        }
        if ("exam".equals(type)) {
            return getColor(R.color.schedule_type_exam);
        }
        return getColor(R.color.schedule_accent);
    }
}
