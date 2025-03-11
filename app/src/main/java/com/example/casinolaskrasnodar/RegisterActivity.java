package com.example.casinolaskrasnodar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.casinolaskrasnodar.AuthService;
import com.example.casinolaskrasnodar.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            AuthService.signUp(email, password, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(String userId) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this,
                                "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this,
                                    "Ошибка: " + message, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });
    }
}