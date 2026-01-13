package com.example.onlinediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        progressBar = findViewById(R.id.usersProgress);
        Button btnCreate = findViewById(R.id.btnCreateUser);
        RecyclerView recyclerView = findViewById(R.id.usersList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        recyclerView.setAdapter(adapter);

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
                } else {
                    Toast.makeText(UsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(UsersActivity.this, "Network error", Toast.LENGTH_SHORT).show();
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
}
