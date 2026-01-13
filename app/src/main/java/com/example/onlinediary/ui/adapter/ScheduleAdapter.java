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

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    public interface JournalClickListener {
        void onJournalClick(ScheduleEvent event);
    }

    private final List<ScheduleEvent> items = new ArrayList<>();
    private final boolean showJournal;
    private final JournalClickListener listener;

    public ScheduleAdapter(boolean showJournal, JournalClickListener listener) {
        this.showJournal = showJournal;
        this.listener = listener;
    }

    public void setItems(List<ScheduleEvent> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleEvent event = items.get(position);
        holder.subjectText.setText(event.subjectName);

        String details = event.date + " | Lesson " + event.lessonNumber + " | " + event.teacherFullName;
        if (event.room != null && !event.room.isEmpty()) {
            details = details + " | Room " + event.room;
        }
        if (event.groupName != null && !event.groupName.isEmpty()) {
            details = details + " | " + event.groupName;
        }
        holder.detailText.setText(details);

        if (showJournal) {
            holder.btnJournal.setVisibility(View.VISIBLE);
            holder.btnJournal.setOnClickListener(v -> listener.onJournalClick(event));
        } else {
            holder.btnJournal.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView subjectText;
        private final TextView detailText;
        private final Button btnJournal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectText = itemView.findViewById(R.id.scheduleSubject);
            detailText = itemView.findViewById(R.id.scheduleDetail);
            btnJournal = itemView.findViewById(R.id.btnJournal);
        }
    }
}
