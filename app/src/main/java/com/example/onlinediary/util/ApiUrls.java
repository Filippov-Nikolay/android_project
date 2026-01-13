package com.example.onlinediary.util;

import android.net.Uri;

public final class ApiUrls {
    public static final String BASE_URL = "http://10.0.2.2:8080";

    private ApiUrls() {}

    public static String fileDownloadUrl(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return BASE_URL + "/api/files/download/";
        }
        return BASE_URL + "/api/files/download/" + Uri.encode(fileName);
    }
}
