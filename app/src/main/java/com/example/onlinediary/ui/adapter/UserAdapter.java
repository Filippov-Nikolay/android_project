package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    public interface UserActionListener {
        void onEdit(User user);
        void onDelete(User user);
    }

    private final List<User> items = new ArrayList<>();
    private final UserActionListener listener;

    public UserAdapter(UserActionListener listener) {
        this.listener = listener;
    }

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = items.get(position);
        holder.nameText.setText(buildName(user));
        holder.idText.setText("ID " + user.id);
        holder.emailText.setText(safe(user.email, "-"));
        holder.loginText.setText(buildLogin(user.login));
        holder.groupText.setText(buildGroup(user.groupName));

        String role = user.role == null ? "" : user.role.trim().toUpperCase(Locale.US);
        holder.roleText.setText(role.isEmpty() ? "USER" : role);
        applyRoleStyle(holder, role);

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

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarView;
        private final TextView avatarInitials;
        private final TextView nameText;
        private final TextView idText;
        private final TextView emailText;
        private final TextView loginText;
        private final TextView roleText;
        private final TextView groupText;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.userAvatar);
            avatarInitials = itemView.findViewById(R.id.userAvatarInitials);
            nameText = itemView.findViewById(R.id.userName);
            idText = itemView.findViewById(R.id.userId);
            emailText = itemView.findViewById(R.id.userEmail);
            loginText = itemView.findViewById(R.id.userLogin);
            roleText = itemView.findViewById(R.id.userRoleBadge);
            groupText = itemView.findViewById(R.id.userGroup);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }

    private String buildName(User user) {
        String last = user.lastName == null ? "" : user.lastName.trim();
        String first = user.firstName == null ? "" : user.firstName.trim();
        String name = (last + " " + first).trim();
        return name.isEmpty() ? "User" : name;
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String buildLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            return "-";
        }
        String value = login.trim();
        return value.startsWith("@") ? value : "@" + value;
    }

    private String buildGroup(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return "-";
        }
        return groupName.trim();
    }

    private void applyRoleStyle(ViewHolder holder, String role) {
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
    }
}
