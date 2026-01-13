package com.example.onlinediary;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.util.MultipartUtils;
import com.example.onlinediary.util.SimpleItemSelectedListener;
import com.example.onlinediary.util.TopHeaderHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserCreateActivity extends AppCompatActivity {
    private final List<Group> groups = new ArrayList<>();
    private Spinner roleSpinner;
    private Spinner groupSpinner;
    private EditText loginInput;
    private EditText emailInput;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText passwordInput;
    private TextView avatarLabel;
    private View groupContainer;
    private ProgressBar progressBar;
    private Uri avatarUri;
    private ApiService apiService;

    private final ActivityResultLauncher<String> avatarPicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                avatarUri = uri;
                avatarLabel.setText(uri == null ? "No avatar selected" : "Avatar selected");
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_create);
        TopHeaderHelper.bind(this);

        roleSpinner = findViewById(R.id.spinnerUserRole);
        groupSpinner = findViewById(R.id.spinnerUserGroup);
        loginInput = findViewById(R.id.inputUserLogin);
        emailInput = findViewById(R.id.inputUserEmail);
        firstNameInput = findViewById(R.id.inputUserFirstName);
        lastNameInput = findViewById(R.id.inputUserLastName);
        passwordInput = findViewById(R.id.inputUserPassword);
        avatarLabel = findViewById(R.id.labelAvatar);
        groupContainer = findViewById(R.id.userGroupContainer);
        progressBar = findViewById(R.id.userCreateProgress);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_dark,
                new String[]{"STUDENT", "TEACHER", "ADMIN"}
        );
        roleAdapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
        roleSpinner.setAdapter(roleAdapter);
        roleSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            String role = roleSpinner.getSelectedItem().toString();
            groupContainer.setVisibility("STUDENT".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);
        }));

        View btnPickAvatar = findViewById(R.id.btnPickAvatar);
        View btnCreate = findViewById(R.id.btnCreateUserSubmit);
        View btnCancel = findViewById(R.id.btnCreateUserCancel);

        btnPickAvatar.setOnClickListener(v -> avatarPicker.launch("image/*"));
        btnCreate.setOnClickListener(v -> createUser());
        btnCancel.setOnClickListener(v -> finish());

        apiService = ApiClient.getService(this);
        loadGroups();
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
                    ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                            UserCreateActivity.this,
                            R.layout.item_spinner_dark,
                            names
                    );
                    groupAdapter.setDropDownViewResource(R.layout.item_spinner_dark_dropdown);
                    groupSpinner.setAdapter(groupAdapter);
                } else {
                    Toast.makeText(UserCreateActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Toast.makeText(UserCreateActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUser() {
        String login = loginInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (login.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("login", MultipartUtils.toTextBody(login));
        fields.put("email", MultipartUtils.toTextBody(email));
        fields.put("firstName", MultipartUtils.toTextBody(firstName));
        fields.put("lastName", MultipartUtils.toTextBody(lastName));
        fields.put("password", MultipartUtils.toTextBody(password));
        fields.put("role", MultipartUtils.toTextBody(role));

        if ("STUDENT".equalsIgnoreCase(role) && !groups.isEmpty()) {
            Group group = groups.get(groupSpinner.getSelectedItemPosition());
            fields.put("groupId", MultipartUtils.toTextBody(String.valueOf(group.id)));
        }

        List<MultipartBody.Part> parts = new ArrayList<>();
        if (avatarUri != null) {
            try {
                parts.add(MultipartUtils.createFilePart(this, "avatar", avatarUri, "avatar"));
            } catch (IOException e) {
                Toast.makeText(this, "Failed to read avatar", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        setLoading(true);
        apiService.registerUser(fields, parts).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(UserCreateActivity.this, "User created", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UserCreateActivity.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                Toast.makeText(UserCreateActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
