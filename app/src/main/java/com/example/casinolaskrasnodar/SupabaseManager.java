// SupabaseClient.java
package com.example.casinolaskrasnodar;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import okhttp3.*;
import okhttp3.Callback; // Для OkHttp [[7]]




public class SupabaseManager {
    private static final String SUPABASE_URL = "https://uqfocjbhbmnganbaddwg.supabase.co/rest/v1/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVxZm9jamJoYm1uZ2FuYmFkZHdnIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0MTYzMTM4NSwiZXhwIjoyMDU3MjA3Mzg1fQ.E7iRKkVXfGVb8q155m8zhHz1ioicqQqqeRbFj-OW4eY";


    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final OkHttpClient client = new OkHttpClient();



    public void createUser(String email, long balance,String authId, SupabaseCallback callback) {
        String json = String.format(
                "{\"email\": \"%s\", \"balance\": %d, \"auth_id\": \"%s\"}",
                email, balance, authId
        );

        Log.e("CreateUserRequest", "Sending JSON: " + json); // Лог тела запроса

        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "users")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Prefer", "return=representation")
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


                    String responseData = responseBody != null ? responseBody.string() : "";
                    // В методе createUser():
                    Log.d("CreateUser", "Request JSON: " + json);
                    Log.d("CreateUser", "Response code: " + response.code());
                    Log.d("CreateUser", "Response body: " + responseData);


                    if (response.isSuccessful()) {
                        // Читаем тело ответа только один раз
                        callback.onSuccess(responseData);
                    } else {


                        // Обработка ошибки дубликата email
                        if (response.code() == 409) {
                            callback.onError(new Exception("Email уже используется"));
                        } else if (response.code() == 422) {
                            callback.onError(new Exception("Пользователь уже существует в Auth"));
                        } else {
                            callback.onError(new Exception("Ошибка сервера: " + responseData));
                        }





                    }
                }catch (Exception e) {
                    callback.onError(e);
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
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String responseData = responseBody != null ? responseBody.string() : "";
                    Log.d("CheckEmail", "Response data: " + responseData);

                    // Парсим ответ как JSON-массив
                    JsonArray usersArray = new Gson().fromJson(responseData, JsonArray.class);
                    boolean exists = usersArray.size() > 0;

                    callback.onSuccess(exists ? "exists" : "not_exists");
                } catch (JsonSyntaxException e) {
                    callback.onError(new Exception("Ошибка парсинга JSON: " + e.getMessage()));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }
        });
    }



    public void signUpWithEmail(String email, String password, SupabaseCallback callback) {
        String json = String.format(
                "{\"email\": \"%s\", \"password\": \"%s\"}",
                email,
                password
        );


        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://uqfocjbhbmnganbaddwg.supabase.co/auth/v1/signup")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();




        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    // Читаем тело ответа один раз
                    String responseData = responseBody != null ? responseBody.string() : "";
                    Log.d("SignUpResponse", "Code: " + response.code());
                    Log.d("SignUpResponse", "Body: " + responseData);

                    if (response.isSuccessful()) {
                        // Парсим JSON только если ответ успешный
                        JsonObject json = new Gson().fromJson(responseData, JsonObject.class);
                        if (json.has("access_token")) {
                            mainHandler.post(() -> callback.onSuccess(responseData));
                        } else {
                            mainHandler.post(() -> callback.onError(new Exception("Нет access_token")));
                        }
                    } else {
                        // Обработка ошибок сервера
                        String errorMsg = parseError(responseData);
                        mainHandler.post(() -> callback.onError(new Exception(errorMsg)));
                    }
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }


    public interface SupabaseCallback {
        void onSuccess(String response);
        void onError(Exception e);
    }



    void fetchUserProfile(String authId, SupabaseCallback supabaseCallback) {
        // Запрос к вашей таблице пользователей
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "users?auth_id=eq." + authId)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        String userData = responseBody.string();
                        // Обработка данных пользователя
                        mainHandler.post(() -> {
                            supabaseCallback.onSuccess(userData); // Выполняется в UI потоке
                        });

                    } else {
                        mainHandler.post(() -> {

                            supabaseCallback.onError(new Exception("Ошибка получения данных"));
                        });
                        Log.e("AuthResponse", "Response code: " + response.code());
                        Log.e("AuthResponse", "Body: " + responseBody.string());
                    }


                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> {
                    supabaseCallback.onError(e); // Ошибки тоже в UI потоке
                });

            }

        });

    }

    public void signInWithEmail(String email, String password, SupabaseCallback callback) {
        String json = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("https://uqfocjbhbmnganbaddwg.supabase.co/auth/v1/token?grant_type=password")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();


        Log.e("AuthRequest", "URL: " + request.url()); // Лог URL
        Log.e("AuthRequest", "Headers: " + request.headers()); // Лог заголовков
        Log.e("AuthRequest", "Body: " + json); // Лог тела запроса


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        callback.onError(new Exception("Пустой ответ сервера"));
                        return;
                    }

                    String responseBodyString = responseBody.string(); // Читаем один раз


                    if (response.isSuccessful()) {
                        String accessToken = parseAccessToken(responseBodyString); // Используем строку
                        mainHandler.post(() -> callback.onSuccess(responseBodyString));
                    } else {
                        mainHandler.post(() -> callback.onError(new Exception("Auth failed")));
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }
        });
    }

    private String parseAccessToken(String json) {
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);
        return obj.get("access_token").getAsString();
    }

    private String parseError(String errorBody) {
        try {
            JsonObject errorObj = new Gson().fromJson(errorBody, JsonObject.class);
            return errorObj.get("message").getAsString();
        } catch (Exception e) {
            return "Ошибка сервера: " + errorBody;
        }
    }

    private String parseAuthIdFromToken(String authToken) throws Exception {
        try {
            String[] parts = authToken.split("\\.");
            if (parts.length != 3) {
                throw new Exception("Invalid JWT format");
            }

            String payload = new String(
                    Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING),
                    StandardCharsets.UTF_8
            );

            JsonObject payloadJson = new Gson().fromJson(payload, JsonObject.class);
            if (!payloadJson.has("sub")) {
                throw new Exception("sub field missing in token");
            }

            return payloadJson.get("sub").getAsString();
        } catch (Exception e) {
            throw new Exception("Token parsing error: " + e.getMessage());
        }
    }


    public void updateUserBalance(String authToken, int newBalance, SupabaseCallback callback) {
        Log.d("SupabaseDebug", "Starting updateUserBalance...");

        try {
            Log.d("SupabaseDebug", "Parsing auth token...");

            String authId = parseAuthIdFromToken(authToken);

            Log.d("SupabaseDebug", "Parsed auth_id: " + authId);

            String url = SUPABASE_URL + "rpc/update_balance";

            Log.d("SupabaseDebug", "Final request URL: " + url);

            JsonObject json = new JsonObject();
            json.addProperty("user_auth_id", authId);
            json.addProperty("new_balance", newBalance);


            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json.toString(), MediaType.get("application/json")))
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY) // Используйте API_KEY для service_role
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "params=multiple-objects")
                    .build();

            Log.d("SupabaseDebug", "Executing request...");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("SupabaseDebug", "Request failed: ", e);
                    mainHandler.post(() -> callback.onError(e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("SupabaseDebug", "Response received");
                    try (ResponseBody responseBody = response.body()) {
                        String bodyContent = responseBody != null ? responseBody.string() : "empty";
                        Log.d("SupabaseDebug", "Response code: " + response.code());
                        Log.d("SupabaseDebug", "Response body: " + bodyContent);

                        if (response.isSuccessful()) {
                            mainHandler.post(() -> callback.onSuccess("Balance updated"));
                        } else {
                            Log.e("SupabaseDebug", "Update failed");
                            mainHandler.post(() -> callback.onError(new Exception("Error code: " + response.code())));
                        }

                    }
                }
            });

        } catch (Exception e) {
            mainHandler.post(() -> callback.onError(e));
        }
    }


    public void fetchUserBalance(String authToken, SupabaseCallback callback) {
        try {
            String authId = parseAuthIdFromToken(authToken);

            // Используйте service_role ключ для доступа к данным
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "users?auth_id=eq." + authId + "&select=balance")
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY) // ← Исправлено
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("ляяя",  "");
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try (ResponseBody body = response.body()) {
                        if (!response.isSuccessful()) {
                            throw new Exception("HTTP " + response.code());
                        }

                        JsonArray users = new Gson().fromJson(body.string(), JsonArray.class);
                        if (users.size() == 0) {
                            throw new Exception("Пользователь не найден");
                        }

                        int balance = users.get(0).getAsJsonObject().get("balance").getAsInt();
                        callback.onSuccess(String.valueOf(balance));
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e);
        }
    }

}