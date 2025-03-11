package com.example.casinolaskrasnodar;

import static android.net.http.HttpResponseCache.install;

import io.ktor.client.*;
import io.ktor.client.engine.okhttp.*;
import io.ktor.client.request.*;
import io.ktor.http.*;
import io.ktor.client.statement.*;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

public class SupabaseClient {
    private static final String SUPABASE_URL = "ВАШ_PROJECT_URL";
    private static final String SUPABASE_KEY = "ВАШ_ANON_KEY";

    private static final HttpClient client = new HttpClient(OkHttp.INSTANCE) {
        {
            install(io.ktor.client.plugins.DefaultRequest.class, config -> {
                config.getHeaders().append("apikey", SUPABASE_KEY);
                config.getHeaders().append("Authorization", "Bearer " + SUPABASE_KEY);
            });
        }
    };

    public static CompletableFuture<HttpResponse> post(String endpoint, String jsonBody) {
        return client.execute(
                new HttpRequestBuilder()
                        .url(SUPABASE_URL + endpoint)
                        .method(HttpMethod.Post)
                        .header("Content-Type", "application/json")
                        .setBody(jsonBody)
        ).toCompletableFuture();
    }
}