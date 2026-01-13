package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.model.User;

import java.util.ArrayList;
import java.util.List;

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
        String name = (user.lastName == null ? "" : user.lastName) + " " + (user.firstName == null ? "" : user.firstName);
        holder.nameText.setText(name.trim());
        holder.metaText.setText(user.email + " | " + user.login);
        String roleGroup = user.role;
        if (user.groupName != null && !user.groupName.isEmpty()) {
            roleGroup = roleGroup + " | " + user.groupName;
        }
        holder.roleGroupText.setText(roleGroup);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView metaText;
        private final TextView roleGroupText;
        private final Button btnEdit;
        private final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.userName);
            metaText = itemView.findViewById(R.id.userMeta);
            roleGroupText = itemView.findViewById(R.id.userRoleGroup);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
