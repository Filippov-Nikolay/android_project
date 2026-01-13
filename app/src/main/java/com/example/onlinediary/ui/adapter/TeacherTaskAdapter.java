package com.example.onlinediary.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.onlinediary.R;
import com.example.onlinediary.core.AuthStore;
import com.example.onlinediary.model.TeacherTask;
import com.example.onlinediary.util.ApiUrls;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        holder.subjectText.setText(safeUpper(task.subjectName));
        holder.titleText.setText(task.title == null ? "Task" : task.title);
        holder.groupText.setText(task.groupName == null ? "Group" : task.groupName);

        int submitted = getSubmittedCount(task);
        int total = getTotalCount(task);
        int max = Math.max(total, 1);
        int percent = total == 0 ? 0 : Math.round((submitted * 100f) / total);
        holder.progressPercent.setText(percent + "%");
        holder.progressCount.setText(submitted + "/" + total);
        holder.progressBar.setMax(max);
        holder.progressBar.setProgress(Math.min(submitted, max));

        holder.createdText.setText("Created: " + dateOnly(task.createdAt));
        holder.deadlineText.setText("Due: " + dateOnly(task.deadline));

        if (task.isOverdue) {
            holder.statusBadge.setText("Closed");
            holder.statusBadge.setBackgroundResource(R.drawable.bg_manage_status_closed);
            holder.statusBadge.setTextColor(
                    holder.itemView.getContext().getColor(R.color.schedule_muted)
            );
            holder.deadlineText.setTextColor(
                    holder.itemView.getContext().getColor(R.color.manage_stat_red)
            );
        } else {
            holder.statusBadge.setText("Active");
            holder.statusBadge.setBackgroundResource(R.drawable.bg_manage_status_active);
            holder.statusBadge.setTextColor(
                    holder.itemView.getContext().getColor(R.color.manage_stat_green)
            );
            holder.deadlineText.setTextColor(
                    holder.itemView.getContext().getColor(R.color.schedule_muted)
            );
        }

        String iconUrl = buildIconUrl(task == null ? null : task.iconFileName);
        if (iconUrl != null) {
            holder.iconView.setColorFilter(null);
            holder.iconView.setImageTintList(null);
            Glide.with(holder.itemView)
                    .load(buildGlideModel(holder.itemView.getContext(), iconUrl))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.iconView);
        } else {
            holder.iconView.setImageResource(android.R.drawable.ic_menu_gallery);
            holder.iconView.setImageTintList(ContextCompat.getColorStateList(
                    holder.itemView.getContext(),
                    R.color.schedule_muted
            ));
        }

        holder.btnSubmissions.setOnClickListener(v -> listener.onSubmissions(task));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(task));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView subjectText;
        private final TextView titleText;
        private final TextView statusBadge;
        private final TextView groupText;
        private final TextView progressPercent;
        private final TextView progressCount;
        private final ProgressBar progressBar;
        private final TextView createdText;
        private final TextView deadlineText;
        private final ImageButton btnSubmissions;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.taskIcon);
            subjectText = itemView.findViewById(R.id.taskSubject);
            titleText = itemView.findViewById(R.id.taskTitle);
            statusBadge = itemView.findViewById(R.id.taskStatusBadge);
            groupText = itemView.findViewById(R.id.taskGroup);
            progressPercent = itemView.findViewById(R.id.taskProgressPercent);
            progressCount = itemView.findViewById(R.id.taskProgressCount);
            progressBar = itemView.findViewById(R.id.taskProgressBar);
            createdText = itemView.findViewById(R.id.taskCreatedDate);
            deadlineText = itemView.findViewById(R.id.taskDeadlineDate);
            btnSubmissions = itemView.findViewById(R.id.btnTaskSubmissions);
            btnDelete = itemView.findViewById(R.id.btnTaskDelete);
        }
    }

    private String dateOnly(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "--";
        }
        int idx = value.indexOf('T');
        return idx > 0 ? value.substring(0, idx) : value;
    }

    private String safeUpper(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "SUBJECT";
        }
        return value.trim().toUpperCase(Locale.US);
    }

    private String buildIconUrl(String iconFileName) {
        if (iconFileName == null) {
            return null;
        }
        String trimmed = iconFileName.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/api/") || trimmed.startsWith("api/")) {
            String path = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
            return ApiUrls.BASE_URL + path;
        }
        return ApiUrls.fileDownloadUrl(trimmed);
    }

    private Object buildGlideModel(Context context, String url) {
        String token = new AuthStore(context).getToken();
        if (token == null || token.trim().isEmpty()) {
            return url;
        }
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .build());
    }

    private int getSubmittedCount(TeacherTask task) {
        if (task == null) {
            return 0;
        }
        if (task.stats != null) {
            return task.stats.submitted;
        }
        return task.submissionCount == null ? 0 : task.submissionCount;
    }

    private int getTotalCount(TeacherTask task) {
        if (task == null) {
            return 0;
        }
        if (task.stats != null) {
            return task.stats.total;
        }
        return task.totalStudents == null ? 0 : task.totalStudents;
    }
}
