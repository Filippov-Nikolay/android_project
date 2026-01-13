package com.example.onlinediary.util;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleTextWatcher implements TextWatcher {
    public interface OnTextChanged {
        void onTextChanged(String text);
    }

    private final OnTextChanged onTextChanged;

    public SimpleTextWatcher(OnTextChanged onTextChanged) {
        this.onTextChanged = onTextChanged;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        onTextChanged.onTextChanged(s.toString());
    }
}
