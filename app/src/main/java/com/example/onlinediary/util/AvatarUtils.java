package com.example.onlinediary.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.ColorInt;

public final class AvatarUtils {

    private AvatarUtils() {}

    public static Bitmap decode(String base64) {
        if (base64 == null || base64.trim().isEmpty()) return null;
        String clean = base64;
        int comma = base64.indexOf(',');
        if (comma > 0) {
            clean = base64.substring(comma + 1);
        }
        try {
            byte[] data = Base64.decode(clean, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @return true if avatar bitmap was set, false if placeholder used.
     */
    public static boolean bind(ImageView view, String base64, @DrawableRes int placeholderRes, @ColorInt int placeholderTint) {
        Bitmap bmp = decode(base64);
        if (bmp != null) {
            view.setImageBitmap(bmp);
            view.setColorFilter(null);
            return true;
        } else {
            view.setImageResource(placeholderRes);
            view.setColorFilter(placeholderTint);
            return false;
        }
    }

    public static boolean bindInitials(ImageView view, Bitmap bitmap, @DrawableRes int placeholderRes, @ColorInt int placeholderTint) {
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            view.setColorFilter(null);
            return true;
        } else {
            view.setImageResource(placeholderRes);
            view.setColorFilter(placeholderTint);
            return false;
        }
    }
}
