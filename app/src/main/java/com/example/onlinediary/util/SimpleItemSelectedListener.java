package com.example.onlinediary.util;

import android.view.View;
import android.widget.AdapterView;

public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
    public interface OnSelected {
        void onSelected(int position);
    }

    private final OnSelected onSelected;

    public SimpleItemSelectedListener(OnSelected onSelected) {
        this.onSelected = onSelected;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onSelected.onSelected(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
