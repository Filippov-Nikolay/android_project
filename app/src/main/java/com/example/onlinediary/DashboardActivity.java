package com.example.onlinediary;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.model.Accrual;
import com.example.onlinediary.model.DashboardStatsResponse;
import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.model.SubjectStat;
import com.example.onlinediary.model.User;
import com.example.onlinediary.network.ApiClient;
import com.example.onlinediary.network.ApiService;
import com.example.onlinediary.ui.adapter.DashboardNewsAdapter;
import com.example.onlinediary.ui.adapter.DashboardRowAdapter;
import com.example.onlinediary.util.BottomNavHelper;
import com.example.onlinediary.util.TopHeaderHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {
    private TextView avatarLarge;
    private TextView greetingText;
    private TextView groupText;
    private TextView quickLessonsValue;
    private TextView quickTodoValue;
    private TextView quickOverdueValue;
    private TextView accrualsEmpty;
    private TextView recommendationsEmpty;
    private TextView statsEmpty;
    private LinearLayout statsContainer;
    private TextView periodMonth;
    private TextView periodSemester;
    private TextView periodYear;
    private ProgressBar progressBar;
    private DashboardRowAdapter accrualsAdapter;
    private DashboardRowAdapter recommendationsAdapter;
    private DashboardNewsAdapter newsAdapter;
    private ApiService apiService;
    private int pendingCalls = 0;
    private Integer lessonsToday;
    private Integer todoCount;
    private Integer overdueCount;
    private String statsPeriod = "year";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        TopHeaderHelper.bind(this);

        avatarLarge = findViewById(R.id.dashboardAvatarLarge);
        greetingText = findViewById(R.id.dashboardGreeting);
        groupText = findViewById(R.id.dashboardGroup);
        quickLessonsValue = findViewById(R.id.dashboardQuickLessonsValue);
        quickTodoValue = findViewById(R.id.dashboardQuickTodoValue);
        quickOverdueValue = findViewById(R.id.dashboardQuickOverdueValue);
        accrualsEmpty = findViewById(R.id.dashboardAccrualsEmpty);
        recommendationsEmpty = findViewById(R.id.dashboardRecommendationsEmpty);
        statsEmpty = findViewById(R.id.dashboardStatsEmpty);
        statsContainer = findViewById(R.id.dashboardStatsContainer);
        periodMonth = findViewById(R.id.dashboardPeriodMonth);
        periodSemester = findViewById(R.id.dashboardPeriodSemester);
        periodYear = findViewById(R.id.dashboardPeriodYear);
        progressBar = findViewById(R.id.dashboardProgress);

        RecyclerView accrualsList = findViewById(R.id.dashboardAccrualsList);
        RecyclerView recommendationsList = findViewById(R.id.dashboardRecommendationsList);
        RecyclerView newsList = findViewById(R.id.dashboardNewsList);

        accrualsAdapter = new DashboardRowAdapter();
        recommendationsAdapter = new DashboardRowAdapter();
        newsAdapter = new DashboardNewsAdapter();

        accrualsList.setLayoutManager(new LinearLayoutManager(this));
        accrualsList.setAdapter(accrualsAdapter);
        accrualsList.setNestedScrollingEnabled(false);

        recommendationsList.setLayoutManager(new LinearLayoutManager(this));
        recommendationsList.setAdapter(recommendationsAdapter);
        recommendationsList.setNestedScrollingEnabled(false);

        LinearLayoutManager newsLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        newsList.setLayoutManager(newsLayout);
        newsList.setAdapter(newsAdapter);
        newsList.setNestedScrollingEnabled(false);

        findViewById(R.id.dashboardQuickLessons)
                .setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));
        findViewById(R.id.dashboardQuickTodo)
                .setOnClickListener(v -> startActivity(new Intent(this, HomeworkActivity.class)));
        findViewById(R.id.dashboardQuickOverdue)
                .setOnClickListener(v -> startActivity(new Intent(this, HomeworkActivity.class)));
        findViewById(R.id.dashboardProfileMore)
                .setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        periodMonth.setOnClickListener(v -> loadDashboardStats("month"));
        periodSemester.setOnClickListener(v -> loadDashboardStats("semester"));
        periodYear.setOnClickListener(v -> loadDashboardStats("year"));
        applyPeriodSelection(statsPeriod);

        setupStaticNews();
        setupStaticRecommendations();
        BottomNavHelper.setupStudentNav(this, R.id.navStudentDashboard);

        apiService = ApiClient.getService(this);
        loadData();
    }

    private void setupStaticNews() {
        List<DashboardNewsAdapter.NewsItem> items = new ArrayList<>();
        items.add(new DashboardNewsAdapter.NewsItem(
                "Hackathon",
                "Intensive hackathon week with teams and mentors.",
                "22.05.2024"
        ));
        items.add(new DashboardNewsAdapter.NewsItem(
                "Design Sprint",
                "New sprint focused on mobile UX and delivery.",
                "05.06.2024"
        ));
        newsAdapter.setItems(items);
    }

    private void setupStaticRecommendations() {
        int accent = ContextCompat.getColor(this, R.color.schedule_accent);
        List<DashboardRowAdapter.RowItem> items = new ArrayList<>();
        items.add(new DashboardRowAdapter.RowItem(
                "Java Programming - Intro to OOP",
                "Java Programming | Video | 12 min | YouTube",
                android.R.drawable.ic_media_play,
                accent
        ));
        items.add(new DashboardRowAdapter.RowItem(
                "Web Development - Flexbox to Grid",
                "Web Development | Article | 15 min | MDN",
                android.R.drawable.ic_menu_edit,
                accent
        ));
        items.add(new DashboardRowAdapter.RowItem(
                "Databases - JOIN practice",
                "Databases | Practice | 25 min | Stepik",
                android.R.drawable.ic_menu_agenda,
                accent
        ));
        items.add(new DashboardRowAdapter.RowItem(
                "Algorithms - Quick quiz",
                "Algorithms | Quiz | 10 min | Google Forms",
                android.R.drawable.ic_menu_help,
                accent
        ));
        recommendationsAdapter.setItems(items);
        recommendationsEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadData() {
        pendingCalls = 0;
        lessonsToday = null;
        todoCount = null;
        overdueCount = null;
        setLoading(true);
        loadUser();
        loadAccruals();
        loadScheduleAndHomeworkStats();
        loadDashboardStats(statsPeriod);
    }

    private void loadUser() {
        pendingCalls++;
        apiService.getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String first = safe(user.firstName);
                    String last = safe(user.lastName);
                    String fullName = (first + " " + last).trim();
                    if (fullName.isEmpty()) {
                        fullName = "Student";
                    }
                    setGreeting(fullName);
                    groupText.setText(getGroupLabel(user.groupName));

                    String initials = buildInitials(first, last);
                    avatarLarge.setText(initials);
                    TopHeaderHelper.updateHeaderUser(DashboardActivity.this, user);
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                pendingCalls--;
                setLoading(pendingCalls > 0);
                Toast.makeText(DashboardActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAccruals() {
        pendingCalls++;
        apiService.getAccruals().enqueue(new Callback<List<Accrual>>() {
            @Override
            public void onResponse(Call<List<Accrual>> call, Response<List<Accrual>> response) {
                pendingCalls--;
                List<DashboardRowAdapter.RowItem> items = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    int limit = Math.min(5, response.body().size());
                    for (int i = 0; i < limit; i++) {
                        items.add(mapAccrual(response.body().get(i)));
                    }
                }
                accrualsAdapter.setItems(items);
                accrualsEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<Accrual>> call, Throwable t) {
                pendingCalls--;
                accrualsAdapter.setItems(new ArrayList<>());
                accrualsEmpty.setVisibility(View.VISIBLE);
                setLoading(pendingCalls > 0);
                Toast.makeText(DashboardActivity.this, "Failed to load accruals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScheduleAndHomeworkStats() {
        final String today = LocalDate.now().toString();

        pendingCalls++;
        apiService.getSchedule(today, today).enqueue(new Callback<List<ScheduleEvent>>() {
            @Override
            public void onResponse(Call<List<ScheduleEvent>> call, Response<List<ScheduleEvent>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    updateStats(response.body().size(), null, null);
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<ScheduleEvent>> call, Throwable t) {
                pendingCalls--;
                setLoading(pendingCalls > 0);
                Toast.makeText(DashboardActivity.this, "Failed to load schedule", Toast.LENGTH_SHORT).show();
            }
        });

        pendingCalls++;
        apiService.getHomeworks().enqueue(new Callback<List<HomeworkItem>>() {
            @Override
            public void onResponse(Call<List<HomeworkItem>> call, Response<List<HomeworkItem>> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null) {
                    int todo = 0;
                    int overdue = 0;
                    for (HomeworkItem item : response.body()) {
                        if ("todo".equalsIgnoreCase(item.status)) {
                            todo++;
                        }
                        if (item.isOverdue) {
                            overdue++;
                        }
                    }
                    updateStats(null, todo, overdue);
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<List<HomeworkItem>> call, Throwable t) {
                pendingCalls--;
                setLoading(pendingCalls > 0);
                Toast.makeText(DashboardActivity.this, "Failed to load homework", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDashboardStats(String period) {
        statsPeriod = period;
        applyPeriodSelection(period);
        statsContainer.removeAllViews();
        statsContainer.setVisibility(View.GONE);
        statsEmpty.setText("Loading stats...");
        statsEmpty.setVisibility(View.VISIBLE);

        pendingCalls++;
        setLoading(true);
        apiService.getDashboardStats(period).enqueue(new Callback<DashboardStatsResponse>() {
            @Override
            public void onResponse(Call<DashboardStatsResponse> call, Response<DashboardStatsResponse> response) {
                pendingCalls--;
                if (response.isSuccessful() && response.body() != null && response.body().stats != null) {
                    renderStats(response.body().stats);
                } else {
                    statsEmpty.setText("No stats yet.");
                    statsEmpty.setVisibility(View.VISIBLE);
                }
                setLoading(pendingCalls > 0);
            }

            @Override
            public void onFailure(Call<DashboardStatsResponse> call, Throwable t) {
                pendingCalls--;
                statsEmpty.setText("Failed to load stats.");
                statsEmpty.setVisibility(View.VISIBLE);
                setLoading(pendingCalls > 0);
            }
        });
    }

    private void renderStats(List<SubjectStat> stats) {
        statsContainer.removeAllViews();
        if (stats == null || stats.isEmpty()) {
            statsEmpty.setText("No stats yet.");
            statsEmpty.setVisibility(View.VISIBLE);
            statsContainer.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        int accent = ContextCompat.getColor(this, R.color.schedule_accent);
        int track = ContextCompat.getColor(this, R.color.schedule_surface_muted);

        for (SubjectStat stat : stats) {
            View view = inflater.inflate(R.layout.item_dashboard_stat, statsContainer, false);
            TextView label = view.findViewById(R.id.dashboardStatLabel);
            TextView value = view.findViewById(R.id.dashboardStatValue);
            ProgressBar bar = view.findViewById(R.id.dashboardStatProgress);

            String labelText = stat.label == null || stat.label.trim().isEmpty()
                    ? "Subject"
                    : stat.label;
            label.setText(labelText);

            int max = stat.maxScore > 0 ? stat.maxScore : 1;
            int score = Math.max(0, Math.min(stat.score, max));
            value.setText(score + "/" + max);
            bar.setMax(max);
            bar.setProgress(score);

            int tint = accent;
            if (stat.color != null && !stat.color.trim().isEmpty()) {
                try {
                    tint = Color.parseColor(stat.color);
                } catch (IllegalArgumentException ignored) {
                    tint = accent;
                }
            }
            bar.setProgressTintList(ColorStateList.valueOf(tint));
            bar.setProgressBackgroundTintList(ColorStateList.valueOf(track));

            statsContainer.addView(view);
        }

        statsEmpty.setVisibility(View.GONE);
        statsContainer.setVisibility(View.VISIBLE);
    }

    private void updateStats(Integer lessons, Integer todo, Integer overdue) {
        if (lessons != null) {
            lessonsToday = lessons;
        }
        if (todo != null) {
            todoCount = todo;
        }
        if (overdue != null) {
            overdueCount = overdue;
        }

        quickLessonsValue.setText(lessonsToday == null ? "-" : String.valueOf(lessonsToday));
        quickTodoValue.setText(todoCount == null ? "-" : String.valueOf(todoCount));
        quickOverdueValue.setText(overdueCount == null ? "-" : String.valueOf(overdueCount));
    }

    private void setGreeting(String name) {
        String greeting = "Hello, " + name;
        SpannableString spannable = new SpannableString(greeting);
        int start = "Hello, ".length();
        if (start < greeting.length()) {
            int accent = ContextCompat.getColor(this, R.color.schedule_accent);
            spannable.setSpan(
                    new ForegroundColorSpan(accent),
                    start,
                    greeting.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        greetingText.setText(spannable);
    }

    private DashboardRowAdapter.RowItem mapAccrual(Accrual accrual) {
        String kind = accrual.kind == null ? "" : accrual.kind.trim().toLowerCase(Locale.US);
        int iconRes = android.R.drawable.ic_menu_edit;
        int iconColor = ContextCompat.getColor(this, R.color.schedule_accent);
        String kindLabel = "Assessment";

        if ("attendance".equals(kind)) {
            iconRes = android.R.drawable.ic_menu_my_calendar;
            iconColor = ContextCompat.getColor(this, R.color.schedule_dot_lecture);
            kindLabel = "Attendance";
        } else if ("classwork".equals(kind)) {
            iconRes = android.R.drawable.ic_menu_agenda;
            iconColor = ContextCompat.getColor(this, R.color.schedule_dot_practice);
            kindLabel = "Classwork";
        } else if ("homework".equals(kind)) {
            iconRes = android.R.drawable.ic_menu_edit;
            iconColor = ContextCompat.getColor(this, R.color.dashboard_orange);
            kindLabel = "Homework";
        }

        String title = buildTitle(accrual.subject, accrual.title, "Accrual");
        String score = accrual.maxScore > 0 ? "Score: " + accrual.score + "/" + accrual.maxScore : null;
        String date = dateOnly(accrual.date);
        String meta = joinParts(kindLabel, score, date);
        return new DashboardRowAdapter.RowItem(title, meta, iconRes, iconColor);
    }

    private String buildTitle(String subject, String title, String fallback) {
        String left = safe(subject);
        String right = safe(title);
        if (!left.isEmpty() && !right.isEmpty()) {
            return left + " - " + right;
        }
        if (!left.isEmpty()) {
            return left;
        }
        if (!right.isEmpty()) {
            return right;
        }
        return fallback;
    }

    private String joinParts(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append(part.trim());
        }
        return builder.toString();
    }

    private String dateOnly(String value) {
        if (value == null) {
            return "";
        }
        int idx = value.indexOf('T');
        return idx > 0 ? value.substring(0, idx) : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String getGroupLabel(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return "Group not set";
        }
        return groupName.trim();
    }

    private String buildInitials(String first, String last) {
        StringBuilder builder = new StringBuilder();
        if (first != null && !first.isEmpty()) {
            builder.append(first.substring(0, 1));
        }
        if (last != null && !last.isEmpty()) {
            builder.append(last.substring(0, 1));
        }
        String initials = builder.toString().toUpperCase(Locale.US);
        return initials.isEmpty() ? "OD" : initials;
    }

    private void applyPeriodSelection(String period) {
        setChip(periodMonth, "month".equals(period));
        setChip(periodSemester, "semester".equals(period));
        setChip(periodYear, "year".equals(period));
    }

    private void setChip(TextView chip, boolean selected) {
        int bg = selected ? R.drawable.bg_dashboard_chip_active : R.drawable.bg_dashboard_chip;
        int color = ContextCompat.getColor(
                this,
                selected ? R.color.schedule_background : R.color.schedule_muted
        );
        chip.setBackgroundResource(bg);
        chip.setTextColor(color);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
