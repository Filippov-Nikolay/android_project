package com.example.onlinediary.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.onlinediary.R;
import com.google.android.material.button.MaterialButton;

public final class DialogHelper {
    private DialogHelper() {}

    public static void showConfirm(
            Activity activity,
            String title,
            String message,
            String confirmText,
            String cancelText,
            Runnable onConfirm
    ) {
        if (activity == null || activity.isFinishing() || isDestroyed(activity)) {
            return;
        }
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_confirm, null);

        TextView titleView = dialogView.findViewById(R.id.confirmTitle);
        TextView messageView = dialogView.findViewById(R.id.confirmMessage);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmYes);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnConfirmCancel);

        if (titleView != null) {
            titleView.setText(isEmpty(title) ? "Confirm" : title);
        }
        if (messageView != null) {
            messageView.setText(isEmpty(message) ? "Are you sure?" : message);
        }
        if (btnConfirm != null) {
            btnConfirm.setText(isEmpty(confirmText) ? "Yes" : confirmText);
        }
        if (btnCancel != null) {
            btnCancel.setText(isEmpty(cancelText) ? "Cancel" : cancelText);
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            });
        }

        dialog.show();
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isDestroyed(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed();
    }
}
