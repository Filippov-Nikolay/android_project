package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.model.ScheduleEvent;

import java.util.ArrayList;
import java.util.List;

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
        holder.titleText.setText(event.subjectName + " - " + event.teacherFullName);

        String meta = event.date + " | Lesson " + event.lessonNumber + " | " + event.groupName;
        if (event.room != null && !event.room.isEmpty()) {
            meta = meta + " | Room " + event.room;
        }
        holder.metaText.setText(meta);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(event));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView metaText;
        private final Button btnEdit;
        private final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.adminScheduleTitle);
            metaText = itemView.findViewById(R.id.adminScheduleMeta);
            btnEdit = itemView.findViewById(R.id.btnEditSchedule);
            btnDelete = itemView.findViewById(R.id.btnDeleteSchedule);
        }
    }
}
