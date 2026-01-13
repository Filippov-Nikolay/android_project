package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.model.HomeworkItem;

import java.util.ArrayList;
import java.util.List;

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
        holder.titleText.setText(item.title);

        String meta = item.subjectName;
        if (item.deadline != null && !item.deadline.isEmpty()) {
            meta = meta + " | Due " + item.deadline;
        }
        holder.metaText.setText(meta);

        String status = item.status == null ? "" : item.status.toUpperCase();
        holder.statusText.setText(status);

        holder.itemView.setOnClickListener(v -> listener.onHomeworkClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView metaText;
        private final TextView statusText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.homeworkItemTitle);
            metaText = itemView.findViewById(R.id.homeworkItemMeta);
            statusText = itemView.findViewById(R.id.homeworkItemStatus);
        }
    }
}
