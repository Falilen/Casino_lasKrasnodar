package com.example.casinolaskrasnodar;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "AuthService";
    private static final Logger log = LoggerFactory.getLogger(RegisterActivity.class);
    private SupabaseManager supabaseManager;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        supabaseManager = new SupabaseManager();
        setupUI();

    }

    private void setupUI() {
        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button registerButton = findViewById(R.id.register_button);
        TextView loginLink = findViewById(R.id.login_link);
        //Button loginButton = findViewById(R.id.login_button);


        // Переход на экран входа
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

//        loginButton.setOnClickListener(v -> {
//            String email = emailInput.getText().toString().trim();
//            String password = passwordInput.getText().toString().trim();
//
//            if (isValid(email, password)) {
//                processLogin(email, password);
//            } else {
//                showToast("Неверный email или пароль");
//            }
//        });


        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (isValid(email, password)) {
                checkAndRegister(email, password);
            } else {
                showToast("Пароль должен содержать минимум 8 символов и цифру");
            }
        });
    }

    private void checkAndRegister(String email, String password) {
        if (!isNetworkAvailable()) {
            showErrorDialog("Нет интернет-соединения");
            return;
        }

        supabaseManager.checkEmailExists(email, new SupabaseManager.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Check email response: " + response); // Добавьте лог
                    if ("exists".equals(response)) {
                        showErrorDialog("Этот email уже зарегистрирован");
                    } else {
                        processRegistration(email, password);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showErrorDialog("Ошибка проверки email: " + e.getMessage()));
            }
        });
    }


    private void processLogin(String email, String password) {
        supabaseManager.signInWithEmail(email, password, new SupabaseManager.SupabaseCallback() {
            @Override
            public void onSuccess(String accessToken) { // Теперь приходит токен
                try {
                    // Сохраняем токен
                    Common.AUTH_TOKEN = accessToken;

                    // Получаем auth_id из токена (альтернативный способ)
                    String authId = parseAuthIdFromToken(accessToken); // Новый метод

                    // Загружаем данные пользователя
                    supabaseManager.fetchUserProfile(authId, new SupabaseManager.SupabaseCallback() {
                        @Override
                        public void onSuccess(String userData) {
                            try {
                                // Supabase возвращает массив, даже для одного пользователя
                                JsonArray usersArray = gson.fromJson(userData, JsonArray.class);

                                if (usersArray.size() == 0) {
                                    showErrorDialog("Пользователь не найден");
                                    return;
                                }

                                JsonObject user = usersArray.get(0).getAsJsonObject();
                                Common.SCORE = user.get("balance").getAsInt();

                                // Переход на главный экран
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(intent);
                                Log.e("Trula", "Larila");
                                finish();

                            } catch (JsonSyntaxException e) {
                                runOnUiThread(() -> showErrorDialog("Ошибка данных: " + e.getMessage()));
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> showErrorDialog("Неверный email или пароль"));
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> showErrorDialog("Ошибка входа"));
                }
            }

            @Override
            public void onError(Exception e) {
                showErrorDialog("Неверный email или пароль");
            }
        });
    }

    private String parseAuthIdFromToken(String accessToken) {
        // Декодируем JWT (упрощенно)
        String[] parts = accessToken.split("\\.");
        String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
        JsonObject payloadJson = gson.fromJson(payload, JsonObject.class);
        return payloadJson.get("sub").getAsString(); // auth_id хранится в поле "sub"
    }


    private void processRegistration(String email, String password) {
        supabaseManager.signUpWithEmail(email, password, new SupabaseManager.SupabaseCallback() {
            @Override
            public void onSuccess(String authResponse) {
                runOnUiThread(() -> {
                    showToast("Регистрация успешна!");
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showErrorDialog(e.getMessage()));
            }
        });
    }





    private boolean isValid(String email, String password) {
        return email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") &&
                password.length() >= 8 &&
                password.matches(".*\\d.*");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorDialog(String message) {
        if (!isFinishing()) { // Проверка, что активити активна
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Ошибка")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
            });
        }
    }

    public void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideSystemUI();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }



}