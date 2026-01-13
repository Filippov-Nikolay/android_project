package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.model.TeacherTask;

import java.util.ArrayList;
import java.util.List;

public class TeacherTaskAdapter extends RecyclerView.Adapter<TeacherTaskAdapter.ViewHolder> {
    public interface TaskActionListener {
        void onSubmissions(TeacherTask task);
        void onDelete(TeacherTask task);
    }

    private final List<TeacherTask> items = new ArrayList<>();
    private final TaskActionListener listener;

    public TeacherTaskAdapter(TaskActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TeacherTask> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeacherTask task = items.get(position);
        holder.titleText.setText(task.title + " (" + task.subjectName + ")");

        String meta = "Group: " + task.groupName + " | Due: " + task.deadline;
        holder.metaText.setText(meta);

        String progress = "Submitted: " + (task.stats == null ? 0 : task.stats.submitted)
                + "/" + (task.stats == null ? 0 : task.stats.total);
        holder.progressText.setText(progress);

        holder.btnSubmissions.setOnClickListener(v -> listener.onSubmissions(task));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(task));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView metaText;
        private final TextView progressText;
        private final Button btnSubmissions;
        private final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.taskTitle);
            metaText = itemView.findViewById(R.id.taskMeta);
            progressText = itemView.findViewById(R.id.taskProgress);
            btnSubmissions = itemView.findViewById(R.id.btnTaskSubmissions);
            btnDelete = itemView.findViewById(R.id.btnTaskDelete);
        }
    }
}
