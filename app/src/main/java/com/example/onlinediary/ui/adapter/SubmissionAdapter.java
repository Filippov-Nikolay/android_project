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
import com.example.onlinediary.model.SubmissionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {
    public interface SubmissionActionListener {
        void onDownload(SubmissionItem item);
        void onGrade(SubmissionItem item);
    }

    private final List<SubmissionItem> items = new ArrayList<>();
    private final SubmissionActionListener listener;

    public SubmissionAdapter(SubmissionActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SubmissionItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_submission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubmissionItem item = items.get(position);
        String studentName = item.studentName == null ? "Student" : item.studentName;
        holder.studentText.setText(studentName);
        holder.avatarText.setText(initials(studentName));

        boolean submitted = item.submitted;
        boolean graded = item.grade != null;
        updateStatusBadge(holder.statusBadge, submitted, graded);

        holder.dateText.setText(item.submittedAt == null || item.submittedAt.isEmpty()
                ? "Not submitted"
                : "Submitted: " + dateOnly(item.submittedAt));

        boolean hasFile = item.fileName != null && !item.fileName.trim().isEmpty();
        holder.fileText.setText(hasFile ? "Download file" : "File missing");
        holder.fileText.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                hasFile ? R.color.schedule_accent : R.color.schedule_muted
        ));

        if (graded) {
            holder.gradeBadge.setVisibility(View.VISIBLE);
            holder.gradeBadge.setText(String.valueOf(item.grade));
            holder.gradeText.setVisibility(View.GONE);
        } else {
            holder.gradeBadge.setVisibility(View.GONE);
            holder.gradeText.setVisibility(View.VISIBLE);
            holder.gradeText.setText("Not graded");
        }

        holder.btnDownload.setVisibility(hasFile ? View.VISIBLE : View.GONE);
        holder.btnDownload.setOnClickListener(v -> listener.onDownload(item));
        holder.btnGrade.setEnabled(submitted);
        holder.btnGrade.setAlpha(submitted ? 1f : 0.4f);
        holder.btnGrade.setOnClickListener(v -> listener.onGrade(item));
        holder.itemView.setAlpha(submitted ? 1f : 0.7f);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView avatarText;
        private final TextView studentText;
        private final TextView statusBadge;
        private final TextView dateText;
        private final TextView fileText;
        private final TextView gradeBadge;
        private final TextView gradeText;
        private final ImageButton btnDownload;
        private final ImageButton btnGrade;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarText = itemView.findViewById(R.id.submissionAvatar);
            studentText = itemView.findViewById(R.id.submissionStudent);
            statusBadge = itemView.findViewById(R.id.submissionStatusBadge);
            dateText = itemView.findViewById(R.id.submissionDate);
            fileText = itemView.findViewById(R.id.submissionFile);
            gradeBadge = itemView.findViewById(R.id.submissionGradeBadge);
            gradeText = itemView.findViewById(R.id.submissionGradeText);
            btnDownload = itemView.findViewById(R.id.btnDownloadSubmission);
            btnGrade = itemView.findViewById(R.id.btnGradeSubmission);
        }
    }

    private void updateStatusBadge(TextView badge, boolean submitted, boolean graded) {
        if (badge == null) {
            return;
        }
        if (graded) {
            badge.setText("Reviewed");
            badge.setBackgroundResource(R.drawable.bg_submission_status_graded);
            badge.setTextColor(ContextCompat.getColor(badge.getContext(), R.color.schedule_accent));
            return;
        }
        if (submitted) {
            badge.setText("Submitted");
            badge.setBackgroundResource(R.drawable.bg_manage_status_active);
            badge.setTextColor(ContextCompat.getColor(badge.getContext(), R.color.manage_stat_green));
        } else {
            badge.setText("Missing");
            badge.setBackgroundResource(R.drawable.bg_submission_status_missing);
            badge.setTextColor(ContextCompat.getColor(badge.getContext(), R.color.schedule_muted));
        }
    }

    private String initials(String name) {
        if (name == null) {
            return "?";
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }
        String[] parts = trimmed.split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[1].substring(0, 1) : "";
        return (first + second).toUpperCase(Locale.US);
    }

    private String dateOnly(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "--";
        }
        int idx = value.indexOf('T');
        return idx > 0 ? value.substring(0, idx) : value;
    }
}
