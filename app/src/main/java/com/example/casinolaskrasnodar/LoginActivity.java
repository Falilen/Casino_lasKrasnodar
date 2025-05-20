package com.example.casinolaskrasnodar;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.google.gson.JsonObject;

// LoginActivity.java
public class LoginActivity extends AppCompatActivity {
    private SupabaseManager supabaseManager;
    private EditText emailInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        hideSystemUI();
        setupUI();
        supabaseManager = new SupabaseManager();
    }

    private void setupUI() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.login_button);
        TextView registerLink = findViewById(R.id.register_link);

        // Переход на регистрацию
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Обработка входа
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!isEmailValid(email)) {
                showToast("Неверный формат email");
            } else if (!isPasswordValid(password)) {
                showToast("Пароль должен содержать минимум 8 символов и цифру");
            } else {
                processLogin(email, password);
            }
        });
    }


    private void processLogin(String email, String password) {
        supabaseManager.signInWithEmail(email, password, new SupabaseManager.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> { // Обернуть в UI поток
                    try {
                        JsonObject json = new Gson().fromJson(response, JsonObject.class);
                        String accessToken = json.get("access_token").getAsString();
                        Common.AUTH_TOKEN = accessToken;

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } catch (Exception e) {
                        showError("Ошибка обработки ответа: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showError("Ошибка входа: Неправильный пароль")); // UI поток
            }
        });
    }


    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Ошибка")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean isEmailValid(String email) {
        return email.matches("^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w-]+\\.)+[\\w-]+$");
    }


    private boolean isPasswordValid(String password) {
        return password.length() >= 8 && password.matches(".*\\d.*");
    }

    // LoginActivity.java
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

}