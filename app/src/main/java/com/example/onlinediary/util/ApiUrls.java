package com.example.onlinediary.util;

import android.net.Uri;

public final class ApiUrls {
    // EB backend host used for download links
    public static final String BASE_URL = "http://appjava-env.eba-c47sb33n.eu-west-2.elasticbeanstalk.com";

    private ApiUrls() {}

    public static String fileDownloadUrl(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return BASE_URL + "/api/files/download/";
        }
        return BASE_URL + "/api/files/download/" + Uri.encode(fileName);
    }
}
