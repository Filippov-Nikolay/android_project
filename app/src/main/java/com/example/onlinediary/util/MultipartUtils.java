package com.example.onlinediary.util;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class MultipartUtils {
    private static final MediaType TEXT = MediaType.parse("text/plain");

    private MultipartUtils() {}

    public static RequestBody toTextBody(String value) {
        if (value == null) {
            value = "";
        }
        return RequestBody.create(value, TEXT);
    }

    public static MultipartBody.Part createFilePart(Context context, String partName, Uri uri, String prefix) throws IOException {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        byte[] fileBytes;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Could not open input stream");
            }
            fileBytes = readAllBytes(inputStream);
        }

        // ИСПРАВЛЕНО: Для byte[] сначала идут БАЙТЫ, потом МЕДИАТИП
        // В твоем коде было наоборот, из-за чего данные не читались
        RequestBody requestBody = RequestBody.create(fileBytes, MediaType.parse(mimeType));

        String fileName = FileUtils.getFileName(context, uri);
        if (fileName == null || fileName.isEmpty()) {
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            fileName = prefix + "_" + System.currentTimeMillis() + (extension != null ? "." + extension : "");
        }

        return MultipartBody.Part.createFormData(partName, fileName, requestBody);
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}