package com.example.onlinediary.util;

import android.view.View;
import android.widget.AdapterView;

public class SimpleSpinnerListener implements AdapterView.OnItemSelectedListener {
    public interface OnSelect {
        void onSelect(int position);
    }

    private final OnSelect onSelect;

    public SimpleSpinnerListener(OnSelect onSelect) {
        this.onSelect = onSelect;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onSelect.onSelect(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
