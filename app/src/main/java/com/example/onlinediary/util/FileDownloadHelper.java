package com.example.onlinediary.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class FileDownloadHelper {
    private FileDownloadHelper() {}

    public static void downloadFile(Context context, Call<ResponseBody> call, String fileName) {
        Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Запускаем сохранение в фоновом потоке, чтобы не вешать UI
                    new Thread(() -> {
                        boolean success = saveFile(context, response.body(), fileName);
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(context, "File saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                } else {
                    Toast.makeText(context, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Download", "Error", t);
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static boolean saveFile(Context context, ResponseBody body, String fileName) {
        try {
            InputStream is = body.byteStream();
            OutputStream os;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Для Android 10 и выше используем MediaStore (не нужны разрешения на запись)
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return false;
                os = context.getContentResolver().openOutputStream(uri);
            } else {
                // Для старых Android
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                os = new FileOutputStream(file);
            }

            if (os == null) return false;

            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }

            os.flush();
            os.close();
            is.close();
            return true;
        } catch (Exception e) {
            Log.e("Download", "Save error: " + e.getMessage());
            return false;
        }
    }
}