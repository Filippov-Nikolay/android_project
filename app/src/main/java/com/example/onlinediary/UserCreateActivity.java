package com.example.onlinediary;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.util.FileUtils;
import com.example.onlinediary.util.MultipartUtils;
import com.example.onlinediary.util.SimpleItemSelectedListener;
import com.example.onlinediary.util.TopHeaderHelper;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
    private ShapeableImageView avatarPreview;
    private View groupContainer;
    private ProgressBar progressBar;
    private Uri avatarUri;
    private ApiService apiService;

    private final ActivityResultLauncher<String> avatarPicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                avatarUri = uri;
                if (uri != null) {
                    bindPreview(uri);
                } else {
                    resetAvatarPreview();
                    avatarLabel.setText("No avatar selected");
                }
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
        avatarPreview = findViewById(R.id.avatarPreview);
        groupContainer = findViewById(R.id.userGroupContainer);
        progressBar = findViewById(R.id.userCreateProgress);
        resetAvatarPreview();

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
        if ("STUDENT".equalsIgnoreCase(role) && (groups.isEmpty() || groupSpinner.getSelectedItemPosition() < 0)) {
            Toast.makeText(this, "Select group for student", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.Map<String, RequestBody> fields = new java.util.HashMap<>();
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

        MultipartBody.Part avatarPart = null;
        if (avatarUri != null) {
            try {
                avatarPart = MultipartUtils.createImagePart(this, "file", avatarUri, "avatar");
            } catch (IOException e) {
                Toast.makeText(this, "Failed to read avatar", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        setLoading(true);
        apiService.registerUser(fields, avatarPart).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(UserCreateActivity.this, "User created", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UserCreateActivity.this, "Failed to create user (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(UserCreateActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void bindPreview(Uri uri) {
        Glide.with(this).clear(avatarPreview);

        avatarPreview.setImageTintList(null);
        avatarPreview.clearColorFilter();
        avatarPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarPreview.setPadding(0, 0, 0, 0);
        avatarPreview.setBackground(null);

        Glide.with(this)
                .load(uri)
                .transform(new CenterCrop())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(UserCreateActivity.this, "Unable to load image preview", Toast.LENGTH_SHORT).show();
                        avatarUri = null;
                        resetAvatarPreview();
                        avatarLabel.setText("No avatar selected");
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        avatarLabel.setText(FileUtils.getFileName(UserCreateActivity.this, uri));
                        return false;
                    }
                })
                .into(avatarPreview);
    }

    private void resetAvatarPreview() {
        if (avatarPreview != null) {
            avatarPreview.setImageResource(android.R.drawable.ic_menu_help);
            avatarPreview.setColorFilter(getColor(R.color.schedule_muted));
            avatarPreview.setBackgroundResource(R.drawable.bg_avatar_placeholder);
            int pad = (int) (18 * getResources().getDisplayMetrics().density);
            avatarPreview.setPadding(pad, pad, pad, pad);
            avatarPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }
}
