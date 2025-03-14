// SupabaseClient.java
package com.example.casinolaskrasnodar;

import android.util.Log;

import java.io.IOException;

import okhttp3.*;





public class SupabaseManager {
    private static final String SUPABASE_URL = "https://uqfocjbhbmnganbaddwg.supabase.co/rest/v1/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVxZm9jamJoYm1uZ2FuYmFkZHdnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDE2MzEzODUsImV4cCI6MjA1NzIwNzM4NX0.qaBuNppILfSqhdN91H-q0sX9WOwLUJBNTLKVkMURO_g";

    private final OkHttpClient client = new OkHttpClient();

    public void createUser(String email, long balance,String authId, SupabaseCallback callback) {
        String json = String.format(
                "{\"email\": \"%s\", \"balance\": %d, \"auth_id\": \"%s\"}",
                email, balance, authId
        );
        Log.e("Bred", json);
        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "users")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        // Читаем тело ответа только один раз
                        String responseData = responseBody.string();
                        callback.onSuccess(responseData);
                    } else {
                        // Читаем тело ошибки
                        String errorBody = responseBody != null ? responseBody.string() : "Empty error body";
                        callback.onError(new Exception("HTTP error: " + response.code() + ", " + errorBody));
                    }
                }
            }



        });


    }


    public void checkEmailExists(String email, SupabaseCallback callback) {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "users?email=eq." + email)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String responseData = responseBody.string();
                    boolean emailExists = !responseData.equals("[]"); // Если ответ не пустой, email занят
                    callback.onSuccess(emailExists ? "exists" : "not_exists");
                }
            }
        });
    }

    public interface SupabaseCallback {
        void onSuccess(String response);
        void onError(Exception e);
    }
}