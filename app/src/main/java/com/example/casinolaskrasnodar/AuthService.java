package com.example.casinolaskrasnodar;

import com.example.casinolaskrasnodar.SupabaseClient;

import io.ktor.client.statement.*;
import org.json.JSONObject;
import io.ktor.http.HttpStatusCode;
import io.ktor.utils.io.*;
import io.ktor.utils.io.jvm.javaio.*;
import java.nio.charset.StandardCharsets;
import io.ktor.client.statement.HttpResponse;
import io.ktor.utils.io.ByteReadChannel;
import java.net.HttpURLConnection;


import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;

public class AuthService {
    public interface AuthCallback {
        void onSuccess(String userId);
        void onError(String message);
    }

    public static void signUp(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                // 1. Создаем JSON для регистрации
                JSONObject authData = new JSONObject();
                authData.put("email", email);
                authData.put("password", password);

                

                // 2. Отправляем запрос
                CompletableFuture<HttpResponse> future = SupabaseClient.post(
                        "/auth/v1/signup",
                        authData.toString()
                );

                HttpResponse response = future.get();

                String responseBody = ((ByteReadChannel) response.getBody())
                        .readRemaining()
                        .readText(StandardCharsets.UTF_8);


                if (response.getStatus().getValue() == HttpURLConnection.HTTP_OK) {
                    JSONObject user = new JSONObject(responseBody);
                    createUserProfile(user.getString("id"), email, callback);
                } else {
                    callback.onError("Ошибка регистрации: " + response.getStatus());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private static void createUserProfile(String userId, String email, AuthCallback callback) {
        try {
            JSONObject profileData = new JSONObject();
            profileData.put("auth_id", userId);
            profileData.put("email", email);
            profileData.put("balance", 1000);

            CompletableFuture<HttpResponse> future = SupabaseClient.post(
                    "/rest/v1/users",
                    profileData.toString()
            );

            HttpResponse response = future.get();

            if (response.getStatus().getValue() == HttpURLConnection.HTTP_CREATED)  {
                callback.onSuccess(userId);
            } else {
                callback.onError("Ошибка создания профиля: " + response.getBodyAsText());
            }
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}