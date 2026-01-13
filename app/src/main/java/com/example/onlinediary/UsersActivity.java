package com.example.onlinediary;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.UserAdapter;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private UserAdapter adapter;
    private ApiService apiService;
    private TextView emptyText;
    private RecyclerView usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        getWindow().setStatusBarColor(getColor(R.color.schedule_background));
        getWindow().setNavigationBarColor(getColor(R.color.schedule_background));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        progressBar = findViewById(R.id.usersProgress);
        emptyText = findViewById(R.id.usersEmpty);
        View btnCreate = findViewById(R.id.btnCreateUser);
        usersList = findViewById(R.id.usersList);
        usersList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(new UserAdapter.UserActionListener() {
            @Override
            public void onEdit(User user) {
                Intent intent = new Intent(UsersActivity.this, UserEditActivity.class);
                intent.putExtra("userId", user.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(User user) {
                deleteUser(user.id);
            }
        });
        usersList.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> startActivity(new Intent(UsersActivity.this, UserCreateActivity.class)));

        apiService = ApiClient.getService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        setLoading(true);
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                    toggleEmpty(response.body().isEmpty());
                } else {
                    Toast.makeText(UsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    toggleEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(UsersActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                toggleEmpty(true);
            }
        });
    }

    private void deleteUser(long id) {
        setLoading(true);
        apiService.deleteUser(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    loadUsers();
                } else {
                    Toast.makeText(UsersActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(UsersActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toggleEmpty(boolean isEmpty) {
        emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        usersList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
