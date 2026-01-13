package com.example.onlinediary.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.ui.model.CalendarDay;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    public interface DayClickListener {
        void onDayClick(CalendarDay day);
    }

    private final List<CalendarDay> days = new ArrayList<>();
    private final DayClickListener listener;

    public CalendarAdapter(DayClickListener listener) {
        this.listener = listener;
    }

    public void setDays(List<CalendarDay> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        holder.dayText.setText(String.valueOf(day.date.getDayOfMonth()));

        if (day.selected) {
            holder.dayText.setBackgroundResource(R.drawable.bg_calendar_selected);
            holder.dayText.setTextColor(holder.itemView.getResources().getColor(R.color.schedule_text));
        } else {
            holder.dayText.setBackground(null);
            int color = day.inMonth
                    ? holder.itemView.getResources().getColor(R.color.schedule_text)
                    : holder.itemView.getResources().getColor(R.color.schedule_muted);
            holder.dayText.setTextColor(color);
        }

        boolean showDot = day.hasEvents && day.inMonth && !day.selected;
        holder.dotView.setVisibility(showDot ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(v -> listener.onDayClick(day));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView dayText;
        private final View dotView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.calendarDayText);
            dotView = itemView.findViewById(R.id.calendarDayDot);
        }
    }
}
