package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardNewsAdapter extends RecyclerView.Adapter<DashboardNewsAdapter.ViewHolder> {
    public static class NewsItem {
        public final String title;
        public final String subtitle;
        public final String date;

        public NewsItem(String title, String subtitle, String date) {
            this.title = title;
            this.subtitle = subtitle;
            this.date = date;
        }
    }

    private final List<NewsItem> items = new ArrayList<>();

    public void setItems(List<NewsItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.title.setText(item.title);

        if (item.subtitle == null || item.subtitle.trim().isEmpty()) {
            holder.subtitle.setVisibility(View.GONE);
        } else {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(item.subtitle);
        }

        if (item.date == null || item.date.trim().isEmpty()) {
            holder.date.setVisibility(View.GONE);
        } else {
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setText("Posted: " + item.date);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView date;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dashboardNewsTitle);
            subtitle = itemView.findViewById(R.id.dashboardNewsSubtitle);
            date = itemView.findViewById(R.id.dashboardNewsDate);
        }
    }
}
