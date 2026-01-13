package com.example.onlinediary;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.User;
import com.example.onlinediary.model.UserUpdateRequest;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.util.SimpleItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserEditActivity extends AppCompatActivity {
    private final List<Group> groups = new ArrayList<>();
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private Spinner roleSpinner;
    private Spinner groupSpinner;
    private ProgressBar progressBar;
    private ApiService apiService;
    private long userId;
    private Long userGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        firstNameInput = findViewById(R.id.inputEditFirstName);
        lastNameInput = findViewById(R.id.inputEditLastName);
        emailInput = findViewById(R.id.inputEditEmail);
        roleSpinner = findViewById(R.id.spinnerEditRole);
        groupSpinner = findViewById(R.id.spinnerEditGroup);
        progressBar = findViewById(R.id.userEditProgress);
        Button btnUpdate = findViewById(R.id.btnUpdateUser);

        userId = getIntent().getLongExtra("userId", -1);
        if (userId <= 0) {
            Toast.makeText(this, "Missing user id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roleSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"STUDENT", "TEACHER", "ADMIN"}
        ));
        roleSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            String role = roleSpinner.getSelectedItem().toString();
            groupSpinner.setVisibility("STUDENT".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);
        }));

        apiService = ApiClient.getService(this);

        btnUpdate.setOnClickListener(v -> updateUser());

        loadGroups();
        loadUser();
    }

    private void loadUser() {
        setLoading(true);
        apiService.getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    firstNameInput.setText(user.firstName);
                    lastNameInput.setText(user.lastName);
                    emailInput.setText(user.email);
                    userGroupId = user.groupId;

                    String role = user.role == null ? "STUDENT" : user.role;
                    int roleIndex = role.equalsIgnoreCase("TEACHER") ? 1 : role.equalsIgnoreCase("ADMIN") ? 2 : 0;
                    roleSpinner.setSelection(roleIndex);
                    updateGroupSelection();
                } else {
                    Toast.makeText(UserEditActivity.this, "Failed to load user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                Toast.makeText(UserEditActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroups() {
        apiService.getGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groups.clear();
                    groups.addAll(response.body());
                    List<String> names = new ArrayList<>();
                    for (Group group : groups) {
                        names.add(group.name);
                    }
                    groupSpinner.setAdapter(new ArrayAdapter<>(
                            UserEditActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            names
                    ));
                    updateGroupSelection();
                } else {
                    Toast.makeText(UserEditActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Toast.makeText(UserEditActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGroupSelection() {
        if (userGroupId == null || groups.isEmpty()) {
            return;
        }
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).id == userGroupId) {
                groupSpinner.setSelection(i);
                break;
            }
        }
    }

    private void updateUser() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        Long groupId = null;
        if ("STUDENT".equalsIgnoreCase(role) && !groups.isEmpty()) {
            groupId = groups.get(groupSpinner.getSelectedItemPosition()).id;
        }

        UserUpdateRequest request = new UserUpdateRequest(firstName, lastName, email, role, groupId);

        setLoading(true);
        apiService.updateUser(userId, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(UserEditActivity.this, "User updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UserEditActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                Toast.makeText(UserEditActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
