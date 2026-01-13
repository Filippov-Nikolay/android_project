package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.model.SubmissionItem;

import java.util.ArrayList;
import java.util.List;

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
        holder.studentText.setText(item.studentName);

        String status = item.submitted ? "Submitted" : "Missing";
        if (item.grade != null) {
            status = status + " | Grade: " + item.grade;
        }
        holder.statusText.setText(status);

        holder.btnDownload.setVisibility(item.fileName == null || item.fileName.isEmpty() ? View.GONE : View.VISIBLE);
        holder.btnDownload.setOnClickListener(v -> listener.onDownload(item));
        holder.btnGrade.setEnabled(item.submitted);
        holder.btnGrade.setOnClickListener(v -> listener.onGrade(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView studentText;
        private final TextView statusText;
        private final Button btnDownload;
        private final Button btnGrade;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentText = itemView.findViewById(R.id.submissionStudent);
            statusText = itemView.findViewById(R.id.submissionStatus);
            btnDownload = itemView.findViewById(R.id.btnDownloadSubmission);
            btnGrade = itemView.findViewById(R.id.btnGradeSubmission);
        }
    }
}
