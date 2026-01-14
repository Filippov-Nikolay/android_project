package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.model.User;
import com.example.onlinediary.util.AvatarUtils;
import com.example.onlinediary.util.TopHeaderHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRecentUserAdapter extends RecyclerView.Adapter<AdminRecentUserAdapter.ViewHolder> {
    private final List<User> items = new ArrayList<>();

    public void setItems(List<User> newItems) {
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
                .inflate(R.layout.item_admin_recent_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = items.get(position);
        String name = buildName(user);
        holder.nameText.setText(name);
        holder.loginText.setText(buildLogin(user.login));

        String role = user.role == null ? "" : user.role.trim().toUpperCase(Locale.US);
        holder.roleText.setText(role.isEmpty() ? "USER" : role);

        int textColor = holder.itemView.getContext().getColor(R.color.schedule_muted);
        int bgRes = R.drawable.bg_admin_role_default;
        if ("ADMIN".equals(role)) {
            textColor = holder.itemView.getContext().getColor(R.color.manage_stat_red);
            bgRes = R.drawable.bg_admin_role_admin;
        } else if ("TEACHER".equals(role)) {
            textColor = holder.itemView.getContext().getColor(R.color.manage_stat_green);
            bgRes = R.drawable.bg_admin_role_teacher;
        } else if ("STUDENT".equals(role)) {
            textColor = holder.itemView.getContext().getColor(R.color.manage_stat_blue);
            bgRes = R.drawable.bg_admin_role_student;
        }

        holder.roleText.setTextColor(textColor);
        holder.roleText.setBackgroundResource(bgRes);

        int placeholderTint = holder.itemView.getContext().getColor(R.color.schedule_muted);
        boolean hasAvatar = AvatarUtils.bind(holder.avatarView, user.avatar, android.R.drawable.ic_menu_camera, placeholderTint);
        String initials = TopHeaderHelper.buildInitials(user.firstName, user.lastName, user.login);
        holder.avatarInitials.setText(initials);
        holder.avatarView.setBackgroundResource(R.drawable.bg_dashboard_avatar);
        holder.avatarInitials.setBackgroundResource(R.drawable.bg_dashboard_avatar);

        if (hasAvatar) {
            holder.avatarInitials.setVisibility(View.GONE);
            holder.avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.avatarView.setColorFilter(null);
        } else {
            holder.avatarInitials.setVisibility(View.VISIBLE);
            holder.avatarView.setImageDrawable(null);
            holder.avatarView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.avatarView.setColorFilter(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView avatarView;
        final TextView avatarInitials;
        final TextView nameText;
        final TextView loginText;
        final TextView roleText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.adminRecentAvatar);
            avatarInitials = itemView.findViewById(R.id.adminRecentInitials);
            nameText = itemView.findViewById(R.id.adminRecentName);
            loginText = itemView.findViewById(R.id.adminRecentLogin);
            roleText = itemView.findViewById(R.id.adminRecentRole);
        }
    }

    private String buildName(User user) {
        String first = user.firstName == null ? "" : user.firstName.trim();
        String last = user.lastName == null ? "" : user.lastName.trim();
        String name = (last + " " + first).trim();
        return name.isEmpty() ? "User" : name;
    }

    private String buildLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            return "@-";
        }
        String value = login.trim();
        return value.startsWith("@") ? value : "@" + value;
    }
}
