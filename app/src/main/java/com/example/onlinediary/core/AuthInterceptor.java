package com.example.onlinediary.core;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final AuthStore authStore;

    public AuthInterceptor(AuthStore authStore) {
        this.authStore = authStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = authStore.getToken();

        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(original);
        }

        Request updated = original.newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(updated);
    }
}
