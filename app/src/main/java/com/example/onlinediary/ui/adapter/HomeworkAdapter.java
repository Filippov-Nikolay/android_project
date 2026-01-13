package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlinediary.R;
import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.util.ApiUrls;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.ViewHolder> {
    public interface HomeworkClickListener {
        void onHomeworkClick(HomeworkItem item);
    }

    private final List<HomeworkItem> items = new ArrayList<>();
    private final HomeworkClickListener listener;

    public HomeworkAdapter(HomeworkClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<HomeworkItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public HomeworkItem getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_homework, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeworkItem item = items.get(position);
        holder.titleText.setText(item.title == null ? "Task" : item.title);
        holder.subjectText.setText(safeUpper(item.subjectName));
        holder.createdText.setText("Created: " + dateOnly(item.createdAt));
        holder.deadlineText.setText("Due: " + dateOnly(item.deadline));

        applyStatus(holder.statusText, item.status);
        bindIcon(holder.iconView, item.iconFileName);

        holder.itemView.setOnClickListener(v -> listener.onHomeworkClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView subjectText;
        private final TextView titleText;
        private final TextView statusText;
        private final TextView createdText;
        private final TextView deadlineText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.homeworkItemIcon);
            subjectText = itemView.findViewById(R.id.homeworkItemSubject);
            titleText = itemView.findViewById(R.id.homeworkItemTitle);
            statusText = itemView.findViewById(R.id.homeworkItemStatus);
            createdText = itemView.findViewById(R.id.homeworkItemCreated);
            deadlineText = itemView.findViewById(R.id.homeworkItemDeadline);
        }
    }

    private void applyStatus(TextView view, String status) {
        String normalized = status == null ? "todo" : status.trim().toLowerCase(Locale.US);
        if ("pending".equals(normalized)) {
            view.setText("PENDING");
            view.setBackgroundResource(R.drawable.bg_homework_status_pending);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.manage_stat_orange));
        } else if ("done".equals(normalized)) {
            view.setText("DONE");
            view.setBackgroundResource(R.drawable.bg_homework_status_done);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.manage_stat_green));
        } else {
            view.setText("TODO");
            view.setBackgroundResource(R.drawable.bg_homework_status_todo);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.schedule_text));
        }
    }

    private void bindIcon(ImageView imageView, String iconFileName) {
        if (imageView == null) {
            return;
        }
        String url = buildIconUrl(iconFileName);
        if (url == null) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), R.color.schedule_muted));
            return;
        }
        imageView.setColorFilter(null);
        Glide.with(imageView.getContext())
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imageView);
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
        return ApiUrls.fileDownloadUrl(trimmed);
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
}
