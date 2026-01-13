package com.example.onlinediary.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.onlinediary.AdminStatsActivity;
import com.example.onlinediary.DashboardActivity;
import com.example.onlinediary.HomeworkActivity;
import com.example.onlinediary.ManageHomeworkActivity;
import com.example.onlinediary.R;
import com.example.onlinediary.ScheduleActivity;
import com.example.onlinediary.ScheduleEditActivity;
import com.example.onlinediary.SubjectsActivity;
import com.example.onlinediary.UsersActivity;

public final class BottomNavHelper {
    private BottomNavHelper() {}

    public static void setupStudentNav(Activity activity, int activeId) {
        View nav = activity.findViewById(R.id.studentBottomNav);
        if (nav == null) {
            return;
        }
        View dashboard = activity.findViewById(R.id.navStudentDashboard);
        View homework = activity.findViewById(R.id.navStudentHomework);
        View schedule = activity.findViewById(R.id.navStudentSchedule);

        setNavItemState(activity, R.id.navStudentDashboardIndicator, R.id.navStudentDashboardIcon,
                R.id.navStudentDashboardLabel, activeId == R.id.navStudentDashboard);
        setNavItemState(activity, R.id.navStudentHomeworkIndicator, R.id.navStudentHomeworkIcon,
                R.id.navStudentHomeworkLabel, activeId == R.id.navStudentHomework);
        setNavItemState(activity, R.id.navStudentScheduleIndicator, R.id.navStudentScheduleIcon,
                R.id.navStudentScheduleLabel, activeId == R.id.navStudentSchedule);

        if (dashboard != null) {
            dashboard.setOnClickListener(v -> navigate(activity, DashboardActivity.class,
                    activeId == R.id.navStudentDashboard));
        }
        if (homework != null) {
            homework.setOnClickListener(v -> navigate(activity, HomeworkActivity.class,
                    activeId == R.id.navStudentHomework));
        }
        if (schedule != null) {
            schedule.setOnClickListener(v -> navigate(activity, ScheduleActivity.class,
                    activeId == R.id.navStudentSchedule));
        }
    }

    public static void setupTeacherNav(Activity activity, int activeId) {
        View nav = activity.findViewById(R.id.teacherBottomNav);
        if (nav == null) {
            return;
        }
        View homework = activity.findViewById(R.id.navTeacherHomework);
        View schedule = activity.findViewById(R.id.navTeacherSchedule);

        setNavItemState(activity, R.id.navTeacherHomeworkIndicator, R.id.navTeacherHomeworkIcon,
                R.id.navTeacherHomeworkLabel, activeId == R.id.navTeacherHomework);
        setNavItemState(activity, R.id.navTeacherScheduleIndicator, R.id.navTeacherScheduleIcon,
                R.id.navTeacherScheduleLabel, activeId == R.id.navTeacherSchedule);

        if (homework != null) {
            homework.setOnClickListener(v -> navigate(activity, ManageHomeworkActivity.class,
                    activeId == R.id.navTeacherHomework));
        }
        if (schedule != null) {
            schedule.setOnClickListener(v -> navigate(activity, ScheduleActivity.class,
                    activeId == R.id.navTeacherSchedule));
        }
    }

    public static void setupAdminNav(Activity activity, int activeId) {
        View nav = activity.findViewById(R.id.adminBottomNav);
        if (nav == null) {
            return;
        }
        View stats = activity.findViewById(R.id.navAdminStats);
        View users = activity.findViewById(R.id.navAdminUsers);
        View subjects = activity.findViewById(R.id.navAdminSubjects);
        View schedule = activity.findViewById(R.id.navAdminSchedule);

        setNavItemState(activity, R.id.navAdminStatsIndicator, R.id.navAdminStatsIcon,
                R.id.navAdminStatsLabel, activeId == R.id.navAdminStats);
        setNavItemState(activity, R.id.navAdminUsersIndicator, R.id.navAdminUsersIcon,
                R.id.navAdminUsersLabel, activeId == R.id.navAdminUsers);
        setNavItemState(activity, R.id.navAdminSubjectsIndicator, R.id.navAdminSubjectsIcon,
                R.id.navAdminSubjectsLabel, activeId == R.id.navAdminSubjects);
        setNavItemState(activity, R.id.navAdminScheduleIndicator, R.id.navAdminScheduleIcon,
                R.id.navAdminScheduleLabel, activeId == R.id.navAdminSchedule);

        if (stats != null) {
            stats.setOnClickListener(v -> navigate(activity, AdminStatsActivity.class,
                    activeId == R.id.navAdminStats));
        }
        if (users != null) {
            users.setOnClickListener(v -> navigate(activity, UsersActivity.class,
                    activeId == R.id.navAdminUsers));
        }
        if (subjects != null) {
            subjects.setOnClickListener(v -> navigate(activity, SubjectsActivity.class,
                    activeId == R.id.navAdminSubjects));
        }
        if (schedule != null) {
            schedule.setOnClickListener(v -> navigate(activity, ScheduleEditActivity.class,
                    activeId == R.id.navAdminSchedule));
        }
    }

    private static void setNavItemState(
            Activity activity,
            int indicatorId,
            int iconId,
            int labelId,
            boolean active
    ) {
        View indicator = activity.findViewById(indicatorId);
        ImageView icon = activity.findViewById(iconId);
        TextView label = activity.findViewById(labelId);

        int color = ContextCompat.getColor(
                activity,
                active ? R.color.schedule_accent : R.color.schedule_muted
        );

        if (indicator != null) {
            indicator.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
        }
        if (icon != null) {
            icon.setColorFilter(color);
        }
        if (label != null) {
            label.setTextColor(color);
        }
    }

    private static void navigate(Activity activity, Class<?> target, boolean active) {
        if (active) {
            return;
        }
        Intent intent = new Intent(activity, target);
        activity.startActivity(intent);
        activity.finish();
    }
}
