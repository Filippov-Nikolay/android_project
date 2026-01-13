package com.example.onlinediary.network;

import android.content.Context;

import com.example.onlinediary.core.AuthInterceptor;
import com.example.onlinediary.core.AuthStore;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // EB backend host; keep trailing slash for Retrofit base URL
    private static final String BASE_URL = "http://appjava-env.eba-c47sb33n.eu-west-2.elasticbeanstalk.com/";
    private static ApiService service;

    public static ApiService getService(Context context) {
        if (service == null) {
            AuthStore authStore = new AuthStore(context.getApplicationContext());
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(authStore))
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(ApiService.class);
        }

        return service;
    }
}
