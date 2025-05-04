package com.example.inviguard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText idEditText = findViewById(R.id.login_id);
        EditText pwEditText = findViewById(R.id.login_pw);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputId = idEditText.getText().toString().trim();
                String inputPw = pwEditText.getText().toString();

                if (inputId.equals("inviguard") && inputPw.equals("ieum123!")) {
                    Intent intent = new Intent(LoginActivity.this, AnalyzingActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
