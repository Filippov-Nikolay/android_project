package com.example.onlinediary.util;

public final class ApiUrls {
    public static final String BASE_URL = "http://10.0.2.2:8080";

    private ApiUrls() {}

    public static String fileDownloadUrl(String fileName) {
        return BASE_URL + "/api/files/download/" + fileName;
    }
}
