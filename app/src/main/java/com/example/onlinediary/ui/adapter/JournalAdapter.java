package com.example.onlinediary.ui.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinediary.R;
import com.example.onlinediary.model.JournalEntry;
import com.example.onlinediary.util.SimpleSpinnerListener;
import com.example.onlinediary.util.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {
    private static final List<String> ATTENDANCE = Arrays.asList("PRESENT", "LATE", "ABSENT");
    private static final List<String> WORK_TYPES = Arrays.asList("CLASSWORK", "INDEPENDENT", "TEST", "THEMATIC");

    private final List<JournalEntry> items = new ArrayList<>();

    public void setItems(List<JournalEntry> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<JournalEntry> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final Spinner attendanceSpinner;
        private final Spinner workTypeSpinner;
        private final EditText gradeInput;
        private TextWatcher gradeWatcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.journalStudentName);
            attendanceSpinner = itemView.findViewById(R.id.spinnerAttendance);
            workTypeSpinner = itemView.findViewById(R.id.spinnerWorkType);
            gradeInput = itemView.findViewById(R.id.inputGrade);

            attendanceSpinner.setAdapter(new ArrayAdapter<>(
                    itemView.getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    ATTENDANCE
            ));

            workTypeSpinner.setAdapter(new ArrayAdapter<>(
                    itemView.getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    WORK_TYPES
            ));

            attendanceSpinner.setOnItemSelectedListener(new SimpleSpinnerListener(position -> {
                int index = getBindingAdapterPosition();
                if (index >= 0 && index < items.size()) {
                    items.get(index).attendance = ATTENDANCE.get(position);
                }
            }));

            workTypeSpinner.setOnItemSelectedListener(new SimpleSpinnerListener(position -> {
                int index = getBindingAdapterPosition();
                if (index >= 0 && index < items.size()) {
                    items.get(index).workType = WORK_TYPES.get(position);
                }
            }));
        }

        void bind(JournalEntry entry) {
            nameText.setText(entry.studentFullName);
            attendanceSpinner.setSelection(indexOf(ATTENDANCE, entry.attendance));
            workTypeSpinner.setSelection(indexOf(WORK_TYPES, entry.workType));

            if (gradeWatcher != null) {
                gradeInput.removeTextChangedListener(gradeWatcher);
            }

            gradeInput.setText(entry.grade == null ? "" : entry.grade);
            gradeWatcher = new SimpleTextWatcher(text -> {
                int index = getBindingAdapterPosition();
                if (index >= 0 && index < items.size()) {
                    items.get(index).grade = text;
                }
            });
            gradeInput.addTextChangedListener(gradeWatcher);
        }

        private int indexOf(List<String> list, String value) {
            if (value == null) {
                return 0;
            }
            int index = list.indexOf(value.toUpperCase());
            return index >= 0 ? index : 0;
        }
    }
}
