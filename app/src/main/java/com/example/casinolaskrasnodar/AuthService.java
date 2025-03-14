// AuthService.java

package com.example.casinolaskrasnodar;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;



import java.util.UUID;

public class AuthService extends AppCompatActivity {
    private SupabaseManager supabaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        hideSystemUI();

        supabaseClient = new SupabaseManager();
        setupUI();
    }

    private void setupUI() {
        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();


            supabaseClient.checkEmailExists(email, new SupabaseManager.SupabaseCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        if (response.equals("exists")) {
                            Toast.makeText(AuthService.this, "Email уже зарегистрирован", Toast.LENGTH_SHORT).show();
                        } else {
                            // Продолжить регистрацию
                            if (isValid(email, password)) {
                                registerUser(email);
                            }
                            else
                            {
                                Toast.makeText(AuthService.this, "Заполните все поля", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AuthService.this, "Ошибка проверки email", Toast.LENGTH_SHORT).show();
                    });
                }
            });


            // Валидация данных

        });
    }

    private boolean isValid(String email, String password) {
        // Реализуйте проверку данных
        return !email.isEmpty() && !password.isEmpty();
    }

    private void registerUser(String email) {
        String authId = UUID.randomUUID().toString();
        long initialBalance = 1000;
        supabaseClient.createUser(email, initialBalance, authId, new SupabaseManager.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // Обработка успешной регистрации

                    Toast.makeText(AuthService.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {

                    String errorMessage = "Ошибка регистрации";
                    // Обработка ошибки

                    new AlertDialog.Builder(AuthService.this)
                            .setTitle("Ошибка")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Для Android 11+ (API 30+)
            getWindow().setDecorFitsSystemWindows(false);
            decorView.getWindowInsetsController().hide(
                    WindowInsets.Type.systemBars()
                            | WindowInsets.Type.displayCutout()
            );
            decorView.getWindowInsetsController().setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } else {
            // Для старых версий Android
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }
}