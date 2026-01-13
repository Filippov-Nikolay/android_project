package com.example.onlinediary.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardRowAdapter extends RecyclerView.Adapter<DashboardRowAdapter.ViewHolder> {
    public static class RowItem {
        public final String title;
        public final String meta;
        public final int iconResId;
        public final int iconColor;

        public RowItem(String title, String meta, int iconResId, int iconColor) {
            this.title = title;
            this.meta = meta;
            this.iconResId = iconResId;
            this.iconColor = iconColor;
        }
    }

    private final List<RowItem> items = new ArrayList<>();

    public void setItems(List<RowItem> newItems) {
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
                .inflate(R.layout.item_dashboard_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RowItem item = items.get(position);
        holder.title.setText(item.title);
        if (item.meta == null || item.meta.trim().isEmpty()) {
            holder.meta.setVisibility(View.GONE);
        } else {
            holder.meta.setVisibility(View.VISIBLE);
            holder.meta.setText(item.meta);
        }

        if (item.iconResId != 0) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageResource(item.iconResId);
            holder.iconWrapper.setBackgroundTintList(ColorStateList.valueOf(item.iconColor));
        } else {
            holder.icon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView meta;
        final ImageView icon;
        final FrameLayout iconWrapper;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dashboardRowTitle);
            meta = itemView.findViewById(R.id.dashboardRowMeta);
            icon = itemView.findViewById(R.id.dashboardRowIcon);
            iconWrapper = itemView.findViewById(R.id.dashboardRowIconWrapper);
        }
    }
}
