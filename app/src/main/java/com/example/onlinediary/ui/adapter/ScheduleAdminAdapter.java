package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.util.ScheduleTimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleAdminAdapter extends RecyclerView.Adapter<ScheduleAdminAdapter.ViewHolder> {
    public interface ScheduleActionListener {
        void onEdit(ScheduleEvent event);
        void onDelete(ScheduleEvent event);
    }

    private final List<ScheduleEvent> items = new ArrayList<>();
    private final ScheduleActionListener listener;

    public ScheduleAdminAdapter(ScheduleActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ScheduleEvent> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<ScheduleEvent> getItemsForExport() {
        return new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleEvent event = items.get(position);
        holder.dateText.setText(dateOnly(event.date));
        holder.lessonText.setText(formatLesson(event.lessonNumber));
        holder.subjectText.setText(safeValue(event.subjectName, "Subject"));
        holder.teacherText.setText(safeValue(event.teacherFullName, "Teacher"));
        holder.groupBadge.setText(safeValue(event.groupName, "Group"));

        String typeKey = normalizeType(event.type);
        holder.typeBadge.setText(formatTypeLabel(typeKey));
        applyTypeBadge(holder.typeBadge, typeKey);

        if (isBlank(event.room)) {
            holder.roomText.setVisibility(View.GONE);
        } else {
            holder.roomText.setVisibility(View.VISIBLE);
            holder.roomText.setText("Room " + event.room.trim());
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(event));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateText;
        private final TextView lessonText;
        private final TextView subjectText;
        private final TextView teacherText;
        private final TextView groupBadge;
        private final TextView typeBadge;
        private final TextView roomText;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.adminScheduleDate);
            lessonText = itemView.findViewById(R.id.adminScheduleLesson);
            subjectText = itemView.findViewById(R.id.adminScheduleSubject);
            teacherText = itemView.findViewById(R.id.adminScheduleTeacher);
            groupBadge = itemView.findViewById(R.id.adminScheduleGroupBadge);
            typeBadge = itemView.findViewById(R.id.adminScheduleTypeBadge);
            roomText = itemView.findViewById(R.id.adminScheduleRoom);
            btnEdit = itemView.findViewById(R.id.btnEditSchedule);
            btnDelete = itemView.findViewById(R.id.btnDeleteSchedule);
        }
    }

    private String dateOnly(String value) {
        if (value == null) {
            return "--";
        }
        int idx = value.indexOf('T');
        return idx > 0 ? value.substring(0, idx) : value;
    }

    private String formatLesson(int lessonNumber) {
        String range = ScheduleTimeUtils.getTimeRangeLabel(lessonNumber);
        if (range.isEmpty()) {
            return "Lesson " + lessonNumber;
        }
        return "Lesson " + lessonNumber + " \u00b7 " + range;
    }

    private String safeValue(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeType(String value) {
        if (isBlank(value)) {
            return "lecture";
        }
        return value.trim().toLowerCase(Locale.US);
    }

    private String formatTypeLabel(String typeKey) {
        switch (typeKey) {
            case "practice":
                return "PRACTICE";
            case "exam":
                return "EXAM";
            default:
                return "LECTURE";
        }
    }

    private void applyTypeBadge(TextView badge, String typeKey) {
        if (badge == null) {
            return;
        }
        int bgRes;
        int textColor;
        if ("practice".equals(typeKey)) {
            bgRes = R.drawable.bg_admin_schedule_type_practice;
            textColor = ContextCompat.getColor(badge.getContext(), R.color.admin_schedule_type_practice);
        } else if ("exam".equals(typeKey)) {
            bgRes = R.drawable.bg_admin_schedule_type_exam;
            textColor = ContextCompat.getColor(badge.getContext(), R.color.admin_schedule_type_exam);
        } else {
            bgRes = R.drawable.bg_admin_schedule_type_lecture;
            textColor = ContextCompat.getColor(badge.getContext(), R.color.admin_schedule_type_lecture);
        }
        badge.setBackgroundResource(bgRes);
        badge.setTextColor(textColor);
    }
}
