package com.example.onlinediary.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class MultipartUtils {
    private static final MediaType TEXT = MediaType.parse("text/plain");
    private static final MediaType OCTET = MediaType.parse("application/octet-stream");

    private MultipartUtils() {}

    public static RequestBody toTextBody(String value) {
        if (value == null) {
            value = "";
        }
        return RequestBody.create(value, TEXT);
    }

    public static MultipartBody.Part createFilePart(Context context, String partName, Uri uri, String prefix) throws IOException {
        File file = FileUtils.copyToCache(context, uri, prefix);
        RequestBody body = RequestBody.create(file, OCTET);
        String fileName = FileUtils.getFileName(context, uri);
        return MultipartBody.Part.createFormData(partName, fileName, body);
    }
}
